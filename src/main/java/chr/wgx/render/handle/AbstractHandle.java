package chr.wgx.render.handle;

public abstract sealed class AbstractHandle permits
        ColorAttachmentHandle,
        DepthAttachmentHandle,
        ObjectHandle,
        RenderPipelineHandle,
        RenderTaskHandle,
        SamplerHandle,
        UniformHandle
{
    private final long handle;

    public AbstractHandle(long handle) {
        this.handle = handle;
    }

    public long getId() {
        return handle;
    }

    @Override
    public String toString() {
        String className = this.getClass().getSimpleName();
        return String.format("%s(%d)", className, handle);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AbstractHandle that = (AbstractHandle) obj;
        return handle == that.handle;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(handle);
    }
}
