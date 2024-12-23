package chr.wgx.render.vk.compiled;

public sealed interface CompiledRenderPassOp permits
        ImageBarrierOp,
        RenderPassBeginOp,
        RenderPassEndOp,
        RenderOp
{
    default void recordToCommandBuffer(int frameIndex) {
        // TODO
    }
}
