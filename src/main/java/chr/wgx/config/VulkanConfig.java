package chr.wgx.config;

import java.util.logging.Logger;

/// Vulkan 渲染器配置
public final class VulkanConfig {
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

    /// 帧率上限
    public int maxFPS = 0;

    /// 最多允许同时渲染的帧数
    public int maxFramesInFlight = 2;

    /// 是否启用各向异性过滤
    public boolean enableAnisotropy = false;
    /// 各向异性过滤层级
    public float anisotropyLevel = 1.0f;

    /// 是否总是使用图形队列进行传输操作（即使专门的传输队列可用）
    public boolean alwaysUploadWithGraphicsQueue = false;

    public void detectJVMArgumentsOverride() {
        String physicalDeviceID = System.getProperty("wgx.vulkan.physicalDeviceID");
        if (physicalDeviceID != null) {
            try {
                this.physicalDeviceID = Integer.parseInt(physicalDeviceID);
            } catch (NumberFormatException e) {
                logger.warning("vulkan.physicalDeviceID 参数无效: " + physicalDeviceID);
            }
        }

        String validationLayers = System.getProperty("wgx.vulkan.validationLayers");
        if (validationLayers != null) {
            this.validationLayers = !validationLayers.isBlank();
        }

        String alwaysUploadWithGraphicsQueue = System.getProperty("wgx.vulkan.noTransferQueue");
        if (alwaysUploadWithGraphicsQueue != null) {
            this.alwaysUploadWithGraphicsQueue = !alwaysUploadWithGraphicsQueue.isBlank();
        }
    }

    public static final VulkanConfig DEFAULT = new VulkanConfig();
    private static final Logger logger = Logger.getLogger(VulkanConfig.class.getName());
}
