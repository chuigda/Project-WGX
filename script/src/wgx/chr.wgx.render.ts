/// ------ package chr.wgx.render.common ------ ///

export declare class BlendModeT extends JvmClass { private constructor() }
export declare class CGTypeT extends JvmClass { private constructor() }
export declare class ClearBehaviorT extends JvmClass { private constructor() }
export declare class PixelFormatT extends JvmClass { private constructor() }
export declare class ShaderStageT extends JvmClass { private constructor() }
export declare class UniformUpdateFrequencyT extends JvmClass { private constructor() }

export interface BlendModeStatic {
    NONE: BlendModeT
    COMMON_TRANSPARENT_BLENDING: BlendModeT
}

export interface CGTypeStatic {
    Float: CGTypeT
    Int: CGTypeT

    Vec2: CGTypeT
    Vec3: CGTypeT
    Vec4: CGTypeT

    Mat2: CGTypeT
    Mat3: CGTypeT
    Mat4: CGTypeT
}

export interface ClearBehaviorStatic {
    CLEAR_ONCE: ClearBehaviorT
    CLEAR_ALWAYS: ClearBehaviorT
}

export interface PixelFormatStatic {
    RGBA_OPTIMAL: PixelFormatT
    RGBA_SWAPCHAIN: PixelFormatT
    R32_FLOAT: PixelFormatT
    R32_UINT: PixelFormatT
    DEPTH_BUFFER_OPTIMAL: PixelFormatT
}

export interface ShaderStageStatic {
    VERTEX: ShaderStageT
    FRAGMENT: ShaderStageT
    VERTEX_AND_FRAGMENT: ShaderStageT
}

export interface UniformUpdateFrequencyStatic {
    PER_FRAME: UniformUpdateFrequencyT
    MANUAL: UniformUpdateFrequencyT
}

export const BlendMode: BlendModeStatic = Java.type('chr.wgx.render.common.BlendMode')
export const CGType: CGTypeStatic = Java.type('chr.wgx.render.common.CGType')
export const ClearBehavior: ClearBehaviorStatic = Java.type('chr.wgx.render.common.ClearBehavior')
export const PixelFormat: PixelFormatStatic = Java.type('chr.wgx.render.common.PixelFormat')
export const ShaderStage: ShaderStageStatic = Java.type('chr.wgx.render.common.ShaderStage')
export const UniformUpdateFrequency: UniformUpdateFrequencyStatic = Java.type('chr.wgx.render.common.UniformUpdateFrequency')

export declare class ColorT extends JvmClass {
    public r: number
    public g: number
    public b: number
    public a: number

    private constructor()
}

export interface ColorStatic {
    new(r: number, g: number, b: number, a: number): ColorT
    new(r: number, g: number, b: number): ColorT
}

export const Color: ColorStatic = Java.type('chr.wgx.render.common.Color')

/// ------ package chr.wgx.render.info ------ ///

export declare class DescriptorTypeT extends JvmClass { private constructor() }

export interface DescriptorTypeStatic {
    COMBINED_IMAGE_SAMPLER: DescriptorTypeT
    UNIFORM_BUFFER: DescriptorTypeT
}

export const DescriptorType: DescriptorTypeStatic = Java.type('chr.wgx.render.info.DescriptorType')

export declare class AttachmentCreateInfoT extends JvmClass {
    pixelFormat: PixelFormatT
    width: number
    height: number

    private constructor()
}

export declare class DescriptorLayoutBindingInfoT extends JvmClass {
    descriptorType: DescriptorTypeT
    bindingName: string
    stage: ShaderStageT

    protected constructor()
}

export interface AttachmentCreateInfoStatic {
    new(pixelFormat: PixelFormatT, width: number, height: number): AttachmentCreateInfoT
    new(pixelFormat: PixelFormatT): AttachmentCreateInfoT
}

export const AttachmentCreateInfo: AttachmentCreateInfoStatic = Java.type('chr.wgx.render.info.AttachmentCreateInfo')
