package chr.wgx.render.vk;

/// Vulkan 渲染器配置
public final class VkConfig {
    /// 选定的物理设备 ID，为 0 则自动选择第一个可用物理设备
    public int physicalDeviceID = 0;

    /// 是否启用校验层
    public boolean validationLayers = false;

    /// 垂直同步选项
    ///
    /// - `0`: 强制禁用垂直同步
    /// - `1`: 若 `VK_PRESENT_MODE_MAILBOX_KHR` 模式可用，则不启用垂直同步
    /// - `2`: 强制启用垂直同步
    public int vsync = 1;

    /// 是否启用多重采样抗锯齿
    public boolean enableMSAA = false;
    /// 多重采样抗锯齿层级
    public int msaaSampleCount = 4;

    /// 是否启用各向异性过滤
    public boolean enableAnisotropy = false;
    /// 各向异性过滤层级
    public float anisotropyLevel = 1.0f;

    /// 是否强制为所有图像和纹理使用 UNORM 格式而非 SRGB 格式
    ///
    /// 启用这一选项可以解决 AMD 驱动引起的一个 bug，该 bug 导致在多重采样启用时整个场景偏暗
    public boolean forceUNORM = false;
}
