package chr.wgx.render.vk.compiled;

public sealed interface CompiledRenderPassOp permits
        ImageBarrierOp,
        RenderingBeginOp,
        RenderingEndOp,
        RenderOp
{
    default void recordToCommandBuffer(int frameIndex) {
        // TODO
    }
}
