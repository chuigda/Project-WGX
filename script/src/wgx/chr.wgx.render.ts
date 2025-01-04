/// ------ package chr.wgx.render.common ------ ///

import { BufferedImage, MemoryLayout, MemorySegment } from "../jvm"

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

export declare class FieldInfoT extends JvmClass {
    name: string
    type: CGTypeT
    location: number
    byteOffset: number

    private constructor()
}

export declare class FieldInfoInputT extends JvmClass {
    name: string
    type: CGTypeT

    private constructor()
}

export declare class DescriptorLayoutBindingInfoT extends JvmClass {
    descriptorType: DescriptorTypeT
    bindingName: string
    stage: ShaderStageT

    protected constructor()
}

export declare class UniformBufferBindingInfoT extends DescriptorLayoutBindingInfoT {
    fields: FieldInfoT[]
    bufferSize: number

    private constructor()
}

export declare class DescriptorSetCreateInfoT extends JvmClass {
    layout: DescriptorSetLayoutT
    descriptors: DescriptorT[]

    private constructor()
}

export declare class DescriptorT extends JvmClass { }

export declare class DescriptorSetLayoutT extends JvmClass {
    info: DescriptorSetLayoutCreateInfoT

    private constructor()
}

export declare class DescriptorSetLayoutCreateInfoT extends JvmClass {
    bindings: DescriptorLayoutBindingInfoT[]

    private constructor()
}

export declare class ObjectCreateInfoT extends JvmClass {
    vertexInputInfo: VertexInputInfoT;
    pVertices: MemorySegment;
    pIndices: MemorySegment;

    private constructor()
}

export declare class VertexInputInfoT extends JvmClass {
    attributes: FieldInfoT[]
    attributeMap: Map<string, FieldInfoT>
    stride: number

    private constructor()
}

export declare class PushConstantInfoT extends JvmClass {
    pushConstantRanges: PushConstantRangeT[]
    cpuLayout: MemoryLayout
    bufferSize: number

    private constructor()
}

export declare class PushConstantRangeT extends JvmClass {
    name: string
    shaderStage: ShaderStageT
    type: CGTypeT
    offset: number

    private constructor()
}


export declare class RenderPassAttachmentInfoT extends JvmClass {
    attachment: AttachmentT
    clearBehavior: ClearBehaviorT
    clearColor: ColorT

    private constructor()
}


export declare class AttachmentT extends JvmClass {
    createInfo: AttachmentCreateInfoT

    private constructor()
}

export declare class RenderPassCreateInfoT extends JvmClass {
    name: string
    priority: number

    colorAttachmentInfos: RenderPassAttachmentInfoT[]
    depthAttachmentInfo: RenderPassAttachmentInfoT | null

    private constructor()
}


export declare class RenderPipelineCreateInfoT extends JvmClass {
    vertexInputInfo: VertexInputInfoT
    descriptorSetLayouts: DescriptorSetLayoutT[]
    pushConstantInfo: PushConstantInfoT | null
    vulkanShaderProgram: ShaderProgram$VulkanT | null
    gles2ShaderProgram: ShaderProgram$GLES2T | null
    colorAttachmentCount: number
    depthTest: boolean

    private constructor()
}

export declare class ShaderProgram extends JvmClass { }

export declare class ShaderProgram$GLES2T extends ShaderProgram {
    vertexShader: string
    fragmentShader: string

    private constructor()
}

export declare class ShaderProgram$VulkanT extends ShaderProgram {
    vertexShader: number[];
    fragmentShader: number[];

    private constructor()
}

export declare class TextureBindingInfoT extends DescriptorLayoutBindingInfoT {
    private constructor()
}

export declare class TextureCreateInfoT extends JvmClass {
    image: BufferedImage
    mipmap: boolean

    private constructor()
}

export declare class UniformBufferCreateInfoT extends JvmClass {
    updateFrequency: UniformUpdateFrequencyT
    bindingInfo: UniformBufferBindingInfoT
    init: MemorySegment | null

    private constructor()
}

export interface AttachmentCreateInfoStatic {
    new(pixelFormat: PixelFormatT, width: number, height: number): AttachmentCreateInfoT
    new(pixelFormat: PixelFormatT): AttachmentCreateInfoT
}

export interface FieldInputStatic {
    new(name: string, type: CGTypeT): FieldInfoInputT
}

export interface UniformBufferBindingInfoStatic {
    new(bindingName: string, stage: ShaderStageT, fieldInfoInputs: FieldInfoInputT[]): UniformBufferBindingInfoT
}


export interface DescriptorSetCreateInfoStatic {
    new(descriptorType: DescriptorTypeT, bindingName: string, stage: ShaderStageT): DescriptorSetCreateInfoT
}

export interface DescriptorStatic {
}

export interface DescriptorSetLayoutStatic {
    new(info: DescriptorSetLayoutCreateInfoT): DescriptorSetLayoutT
}

export interface DescriptorSetLayoutCreateInfoStatic {
    new(bindings: DescriptorLayoutBindingInfoT[]): DescriptorSetLayoutCreateInfoT
}

export interface ObjectCreateInfoStatic {
    new(vertexInputInfo: VertexInputInfoT, pVertices: MemorySegment, pIndices: MemorySegment): ObjectCreateInfoT
}

export interface VertexInputInfoStatic {
    new(attributes: FieldInfoInputT[]): VertexInputInfoT
}

export interface PushConstantInfoStatic {
    new(pushConstantRanges: PushConstantRangeT[]): PushConstantInfoT
}

export interface PushConstantRangeStatic {
    new(name: string, shaderStage: ShaderStageT, type: CGTypeT, offset: number): PushConstantRangeT
}

export interface RenderPassAttachmentInfoStatic {
    new(attachment: AttachmentT, clearBehavior: ClearBehaviorT): RenderPassAttachmentInfoT
    new(attachment: AttachmentT, clearBehavior: ClearBehaviorT, clearColor: ColorT): RenderPassAttachmentInfoT
}

export interface AttachmentStatic {
    new(createInfo: AttachmentCreateInfoT): AttachmentT
}

export interface RenderPassCreateInfoStatic {
    new(
        name: string,
        priority: number,
        colorAttachmentInfos: RenderPassAttachmentInfoT,
        depthAttachmentInfo: RenderPassAttachmentInfoT | null
    ): RenderPassCreateInfoT
    new(
        name: string,
        priority: number,
        colorAttachmentInfos: RenderPassAttachmentInfoT[],
        depthAttachmentInfo: RenderPassAttachmentInfoT
    ): RenderPassCreateInfoT
    new(
        name: string,
        priority: number,
        colorAttachmentInfos: RenderPassAttachmentInfoT[]
    ): RenderPassCreateInfoT
    new(
        name: string,
        priority: number,
        colorAttachment: RenderPassAttachmentInfoT,
        depthAttachmentInfo: RenderPassAttachmentInfoT | null
    ): RenderPassCreateInfoT
    new(
        name: String,
        priority: number,
        colorAttachment: RenderPassAttachmentInfoT,
        depthAttachmentInfo: RenderPassAttachmentInfoT
    ): RenderPassCreateInfoT
    new(
        name: string,
        priority: number,
        colorAttachment: RenderPassAttachmentInfoT
    ): RenderPassCreateInfoT
}


export interface RenderPipelineCreateInfoStatic {
    new(
        vertexInputInfo: VertexInputInfoT,
        descriptorSetLayouts: DescriptorSetLayoutT[],
        pushConstantInfo: PushConstantInfoT | null,
        vulkanShaderProgram: ShaderProgram$VulkanT | null,
        gles2ShaderProgram: ShaderProgram$GLES2T | null,
        colorAttachmentCount: number,
        depthTest: boolean
    ): RenderPipelineCreateInfoT
}

export interface ShaderProgram$GLES2Static {
    new(vertexShader: string, fragmentShader: string): ShaderProgram$GLES2T
}

export interface ShaderProgram$VulkanStatic {
    new(vertexShader: number[], fragmentShader: number[]): ShaderProgram$VulkanStatic

}

export interface TextureBindingInfoStatic {
    new(bindingName: string, stage: ShaderStageT): TextureBindingInfoT
}

export interface TextureCreateInfoStatic {
    new(image: BufferedImage, mipmap: boolean): TextureCreateInfoT
}

export interface UniformBufferCreateInfoStatic {
    new(
        updateFrequency: UniformUpdateFrequencyT,
        bindingInfo: UniformBufferBindingInfoT
    ): UniformBufferCreateInfoT
    new(
        updateFrequency: UniformUpdateFrequencyT,
        bindingInfo: UniformBufferBindingInfoT,
        init: MemorySegment
    ): UniformBufferCreateInfoT
}

export const AttachmentCreateInfo: AttachmentCreateInfoStatic = Java.type('chr.wgx.render.info.AttachmentCreateInfo')
export const FieldInfoInput: FieldInputStatic = Java.type('chr.wgx.render.info.FieldInfoInput')
export const UniformBufferBindingInfo: UniformBufferBindingInfoStatic = Java.type('chr.wgx.render.info.UniformBufferBindingInfo')

export const DescriptorSetCreateInfo: DescriptorSetCreateInfoStatic = Java.type('chr.wgx.render.info.DescriptorSetCreateInfo')
export const Descriptor: DescriptorStatic = Java.type('chr.wgx.render.data.Descriptor')
export const DescriptorSetLayout: DescriptorSetLayoutStatic = Java.type('chr.wgx.render.data.DescriptorSetLayout')
export const DescriptorSetLayoutCreateInfo: DescriptorSetLayoutCreateInfoStatic = Java.type('chr.wgx.render.info.DescriptorSetLayoutCreateInfo')
export const ObjectCreateInfo: ObjectCreateInfoStatic = Java.type('chr.wgx.render.info.ObjectCreateInfo')
export const VertexInputInfo: VertexInputInfoStatic = Java.type('chr.wgx.render.info.VertexInputInfo')
export const PushConstantInfo: PushConstantInfoStatic = Java.type('chr.wgx.render.info.PushConstantInfo')
export const PushConstantRange: PushConstantRangeStatic = Java.type('chr.wgx.render.info.PushConstantRange')
export const RenderPassAttachmentInfo: RenderPassAttachmentInfoStatic = Java.type('chr.wgx.render.info.RenderPassAttachmentInfo')
export const Attachment: AttachmentStatic = Java.type('chr.wgx.render.data.Attachment')
export const RenderPassCreateInfo: RenderPassCreateInfoStatic = Java.type('chr.wgx.render.info.RenderPassCreateInfo')
export const RenderPipelineCreateInfo: RenderPipelineCreateInfoStatic = Java.type('chr.wgx.render.info.RenderPipelineCreateInfo')
export const ShaderProgram$GLES2: ShaderProgram$GLES2Static = Java.type('chr.wgx.render.info.ShaderProgram') // FIXME
export const ShaderProgram$Vulkan: ShaderProgram$VulkanStatic = Java.type('chr.wgx.render.info.ShaderProgram') // FIXME
export const TextureBindingInfo: TextureBindingInfoStatic = Java.type('chr.wgx.render.info.TextureBindingInfo')
export const TextureCreateInfo: TextureCreateInfoStatic = Java.type('chr.wgx.render.info.TextureCreateInfo')
export const UniformBufferCreateInfo: UniformBufferCreateInfoStatic = Java.type('chr.wgx.render.info.UniformBufferCreateInfo')