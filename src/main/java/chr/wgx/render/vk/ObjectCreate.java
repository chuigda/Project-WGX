package chr.wgx.render.vk;

import chr.wgx.render.RenderException;
import chr.wgx.render.handle.ObjectHandle;
import chr.wgx.render.info.ObjectCreateInfo;
import tech.icey.panama.annotation.enumtype;
import tech.icey.panama.buffer.PointerBuffer;
import tech.icey.vk4j.bitmask.VkAccessFlags;
import tech.icey.vk4j.bitmask.VkBufferUsageFlags;
import tech.icey.vk4j.bitmask.VkPipelineStageFlags;
import tech.icey.vk4j.datatype.VkBufferCopy;
import tech.icey.vk4j.datatype.VkBufferMemoryBarrier;
import tech.icey.vk4j.enumtype.VkResult;
import tech.icey.vma.bitmask.VmaAllocationCreateFlags;
import tech.icey.xjbutil.container.Pair;
import tech.icey.xjbutil.container.Ref;
import tech.icey.xjbutil.sync.Oneshot;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public final class ObjectCreate {
    public ObjectCreate(VulkanRenderEngine engine) {
        this.engine = engine;
    }

    public ObjectHandle createObjectImpl(ObjectCreateInfo info) throws RenderException {
        VulkanRenderEngineContext cx = engine.cx;

        long bufferSize = info.pData.segment().byteSize();
        assert bufferSize % info.vertexInputInfo.stride == 0;
        long vertexCount = bufferSize / info.vertexInputInfo.stride;

        try (Arena arena = Arena.ofConfined()) {
            Resource.Buffer stagingBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VmaAllocationCreateFlags.VMA_ALLOCATION_CREATE_HOST_ACCESS_RANDOM_BIT,
                    null
            );

            PointerBuffer ppData = PointerBuffer.allocate(arena);
            @enumtype(VkResult.class) int result = cx.vma.vmaMapMemory(
                    cx.vmaAllocator,
                    stagingBuffer.allocation,
                    ppData
            );
            if (result != VkResult.VK_SUCCESS) {
                stagingBuffer.dispose(cx);
                throw new RenderException("无法映射缓冲区内存, 错误代码: " + VkResult.explain(result));
            }
            MemorySegment pData = ppData.read().reinterpret(bufferSize);
            pData.copyFrom(info.pData.segment());
            cx.vma.vmaUnmapMemory(cx.vmaAllocator, stagingBuffer.allocation);

            Resource.Buffer vertexBuffer = Resource.Buffer.create(
                    cx,
                    bufferSize,
                    VkBufferUsageFlags.VK_BUFFER_USAGE_TRANSFER_DST_BIT
                            | VkBufferUsageFlags.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    0,
                    null
            );

            if (cx.dedicatedTransferQueue.isSome()) {
                cx.executeTransferCommand(cmd -> {
                    VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                    copyRegion.size(bufferSize);
                    cx.dCmd.vkCmdCopyBuffer(cmd, stagingBuffer.buffer, vertexBuffer.buffer, 1, copyRegion);

                    VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                    barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                    barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                    barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                    barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                    barrier.buffer(vertexBuffer.buffer);
                    barrier.offset(0);
                    barrier.size(bufferSize);
                    cx.dCmd.vkCmdPipelineBarrier(
                            cmd,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                            0,
                            0, null,
                            1, barrier,
                            0, null
                    );
                });

                Pair<Oneshot.Sender<Boolean>, Oneshot.Receiver<Boolean>> channel = Oneshot.create();
                synchronized (unAcquiredObjects) {
                    unAcquiredObjects.value.add(new UnacquiredObject(
                            vertexBuffer,
                            bufferSize,
                            channel.first()
                    ));
                }

                if (!channel.second().recv()) {
                    throw new RenderException("缓冲区传输失败");
                }
                stagingBuffer.dispose(cx);

                long handle = engine.nextHandle();
                synchronized (engine.objects) {
                    engine.objects.put(handle, new Resource.Object(vertexBuffer, info.vertexInputInfo, vertexCount));
                }
                return new ObjectHandle(handle);
            }
            else {
                Pair<Oneshot.Sender<Boolean>, Oneshot.Receiver<Boolean>> channel = Oneshot.create();
                synchronized (unUploadedObjects) {
                    unUploadedObjects.value.add(new UnUploadedObject(
                            stagingBuffer,
                            vertexBuffer,
                            bufferSize,
                            channel.first()
                    ));
                }

                if (!channel.second().recv()) {
                    throw new RenderException("缓冲区传输失败");
                }
                stagingBuffer.dispose(cx);

                long handle = engine.nextHandle();
                synchronized (engine.objects) {
                    engine.objects.put(handle, new Resource.Object(vertexBuffer, info.vertexInputInfo, vertexCount));
                }
                return new ObjectHandle(handle);
            }
        }
    }

    public void handleObjectUploading() {
        if (engine.cx.dedicatedTransferQueue.isSome()) {
            queueTransferAcquireObjects();
        }
        else {
            uploadPendingObjects();
        }
    }

    private void queueTransferAcquireObjects() {
        VulkanRenderEngineContext cx = engine.cx;
        if (hasAcquireOrUploadJob.getAndSet(true)) {
            return;
        }

        List<UnacquiredObject> objectsToAcquire;
        synchronized (unAcquiredObjects) {
            objectsToAcquire = unAcquiredObjects.value;
            if (objectsToAcquire.isEmpty()) {
                hasAcquireOrUploadJob.set(false);
                return;
            }
            unAcquiredObjects.value = new ArrayList<>();
        }

        new Thread(() -> {
            try (Arena arena = Arena.ofConfined()) {
                cx.executeGraphicsCommand(cmd -> {
                    for (UnacquiredObject object : objectsToAcquire) {
                        VkBufferMemoryBarrier barrier = VkBufferMemoryBarrier.allocate(arena);
                        barrier.srcAccessMask(VkAccessFlags.VK_ACCESS_TRANSFER_WRITE_BIT);
                        barrier.dstAccessMask(VkAccessFlags.VK_ACCESS_VERTEX_ATTRIBUTE_READ_BIT);
                        barrier.srcQueueFamilyIndex(cx.dedicatedTransferQueueFamilyIndex.get());
                        barrier.dstQueueFamilyIndex(cx.graphicsQueueFamilyIndex);
                        barrier.buffer(object.buffer.buffer);
                        barrier.offset(0);
                        barrier.size(object.bufferSize);
                        cx.dCmd.vkCmdPipelineBarrier(
                                cmd,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                VkPipelineStageFlags.VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                                0,
                                0, null,
                                1, barrier,
                                0, null
                        );
                    }
                });

                for (UnacquiredObject object : objectsToAcquire) {
                    object.onTransferComplete.send(true);
                }
            } catch (RenderException e) {
                logger.severe("无法执行缓冲区传输任务: " + e.getMessage());
                for (UnacquiredObject object : objectsToAcquire) {
                    object.onTransferComplete.send(false);
                    object.buffer.dispose(cx);
                }
            } finally {
                hasAcquireOrUploadJob.set(false);
            }
        }).start();
    }

    private void uploadPendingObjects() {
        VulkanRenderEngineContext cx = engine.cx;
        if (hasAcquireOrUploadJob.getAndSet(true)) {
            return;
        }

        List<UnUploadedObject> objectsToUpload;
        synchronized (unUploadedObjects) {
            objectsToUpload = unUploadedObjects.value;
            if (objectsToUpload.isEmpty()) {
                hasAcquireOrUploadJob.set(false);
                return;
            }
            unUploadedObjects.value = new ArrayList<>();
        }

        new Thread(() -> {
            try (Arena arena = Arena.ofConfined()) {
                cx.executeGraphicsCommand(cmd -> {
                    for (UnUploadedObject object : objectsToUpload) {
                        VkBufferCopy copyRegion = VkBufferCopy.allocate(arena);
                        copyRegion.size(object.bufferSize);
                        cx.dCmd.vkCmdCopyBuffer(cmd, object.stagingBuffer.buffer, object.vertexBuffer.buffer, 1, copyRegion);
                    }
                });

                for (UnUploadedObject object : objectsToUpload) {
                    object.onUploadComplete.send(true);
                }
            } catch (RenderException e) {
                logger.severe("无法执行缓冲区传输任务: " + e.getMessage());
                for (UnUploadedObject object : objectsToUpload) {
                    object.onUploadComplete.send(false);
                    // staging buffer 在另一边 dispose
                    object.vertexBuffer.dispose(cx);
                }
            } finally {
                hasAcquireOrUploadJob.set(false);
            }
        }).start();
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class UnacquiredObject {
        public final Resource.Buffer buffer;
        public final long bufferSize;
        public final Oneshot.Sender<Boolean> onTransferComplete;

        UnacquiredObject(
                Resource.Buffer buffer,
                long bufferSize,
                Oneshot.Sender<Boolean> onTransferComplete
        ) {
            this.buffer = buffer;
            this.bufferSize = bufferSize;
            this.onTransferComplete = onTransferComplete;
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static final class UnUploadedObject {
        public final Resource.Buffer stagingBuffer;
        public final Resource.Buffer vertexBuffer;
        public final long bufferSize;
        public final Oneshot.Sender<Boolean> onUploadComplete;

        UnUploadedObject(
                Resource.Buffer stagingBuffer,
                Resource.Buffer vertexBuffer,
                long bufferSize,
                Oneshot.Sender<Boolean> onUploadComplete
        ) {
            this.stagingBuffer = stagingBuffer;
            this.vertexBuffer = vertexBuffer;
            this.bufferSize = bufferSize;
            this.onUploadComplete = onUploadComplete;
        }
    }

    private final VulkanRenderEngine engine;
    private final Ref<List<UnacquiredObject>> unAcquiredObjects = new Ref<>(new ArrayList<>());
    private final Ref<List<UnUploadedObject>> unUploadedObjects = new Ref<>(new ArrayList<>());
    private final AtomicBoolean hasAcquireOrUploadJob = new AtomicBoolean(false);

    private static final Logger logger = Logger.getLogger(ObjectCreate.class.getName());
}
