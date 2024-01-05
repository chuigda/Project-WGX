package tech.icey.r77.vk;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import tech.icey.util.ManualDispose;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK11.*;
import static tech.icey.util.RuntimeError.*;

public final class Device implements ManualDispose {
	public Device(PhysicalDevice physicalDevice) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			PointerBuffer requiredExtensionsBuf = stack.mallocPointer(1);
			requiredExtensionsBuf.put(stack.ASCII(KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
			requiredExtensionsBuf.rewind();
			
			VkPhysicalDeviceFeatures features = VkPhysicalDeviceFeatures.calloc(stack);
			
			List<VkQueueFamilyProperties> graphicsQueueFamilies = 
					physicalDevice.physicalDeviceProperties.graphicsQueueFamilies;
			VkDeviceQueueCreateInfo.Buffer queueCreationInfoBuffer =
					VkDeviceQueueCreateInfo.calloc(graphicsQueueFamilies.size(), stack);
			
			for (int i = 0; i < graphicsQueueFamilies.size(); i++) {
				VkQueueFamilyProperties queueFamilyProperties = graphicsQueueFamilies.get(i);
				FloatBuffer priorities = stack.callocFloat(queueFamilyProperties.queueCount());
				queueCreationInfoBuffer.get(i)
	                .sType(VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
	                .queueFamilyIndex(i)
	                .pQueuePriorities(priorities);
			}
			
			VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                    .sType(VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                    .ppEnabledExtensionNames(requiredExtensionsBuf)
                    .pEnabledFeatures(features)
                    .pQueueCreateInfos(queueCreationInfoBuffer);
			
			PointerBuffer vkDeviceBuf = stack.mallocPointer(1);
			int result = vkCreateDevice(physicalDevice.vkPhysicalDevice, deviceCreateInfo, null, vkDeviceBuf);
			if (result != VK_SUCCESS) {
				runtimeError("无法创建 Vulkan 设备: %d", result);
			}
			
			VkDevice vkDevice = new VkDevice(vkDeviceBuf.get(0), physicalDevice.vkPhysicalDevice, deviceCreateInfo);

			this.physicalDevice = physicalDevice;
			this.vkDevice = vkDevice;
		}
	}

	@Override
	public boolean isManuallyDisposed() {
		return isDisposed;
	}

	@Override
	public void dispose() {
		if (!isDisposed) {
			vkDestroyDevice(vkDevice, null);
			isDisposed = true;
		}
	}

	public final PhysicalDevice physicalDevice;
	public final VkDevice vkDevice;

	private volatile boolean isDisposed = false;
}
