/// ------ package chr.wgx.render.common ------ ///

import { MemorySegmentT } from "./java"
import { OptionT } from "./tech.icey.xjbutil"

export declare class BlendModeT extends JvmObject { private constructor() }
export declare class CGTypeT extends JvmObject { private constructor() }
export declare class ClearBehaviorT extends JvmObject { private constructor() }
export declare class PixelFormatT extends JvmObject { private constructor() }
export declare class ShaderStageT extends JvmObject { private constructor() }
export declare class UniformUpdateFrequencyT extends JvmObject { private constructor() }

export interface BlendModeStatic extends JvmClass {
    NONE: BlendModeT
    COMMON_TRANSPARENT_BLENDING: BlendModeT
}

export interface CGTypeStatic extends JvmClass {
    Float: CGTypeT
    Int: CGTypeT

    Vec2: CGTypeT
    Vec3: CGTypeT
    Vec4: CGTypeT

    Mat2: CGTypeT
    Mat3: CGTypeT
    Mat4: CGTypeT
}

export interface ClearBehaviorStatic extends JvmClass {
    CLEAR_ONCE: ClearBehaviorT
    CLEAR_ALWAYS: ClearBehaviorT
}

export interface PixelFormatStatic extends JvmClass {
    RGBA_OPTIMAL: PixelFormatT
    RGBA_SWAPCHAIN: PixelFormatT
    R32_FLOAT: PixelFormatT
    R32_UINT: PixelFormatT
    DEPTH_BUFFER_OPTIMAL: PixelFormatT
}

export interface ShaderStageStatic extends JvmClass {
    VERTEX: ShaderStageT
    FRAGMENT: ShaderStageT
    VERTEX_AND_FRAGMENT: ShaderStageT
}

export interface UniformUpdateFrequencyStatic extends JvmClass {
    PER_FRAME: UniformUpdateFrequencyT
    MANUAL: UniformUpdateFrequencyT
}

export const BlendMode = Java.type<BlendModeStatic>('chr.wgx.render.common.BlendMode')
export const CGType = Java.type<CGTypeStatic>('chr.wgx.render.common.CGType')
export const ClearBehavior = Java.type<ClearBehaviorStatic>('chr.wgx.render.common.ClearBehavior')
export const PixelFormat = Java.type<PixelFormatStatic>('chr.wgx.render.common.PixelFormat')
export const ShaderStage = Java.type<ShaderStageStatic>('chr.wgx.render.common.ShaderStage')
export const UniformUpdateFrequency = Java.type<UniformUpdateFrequencyStatic>('chr.wgx.render.common.UniformUpdateFrequency')

export declare class ColorT extends JvmObject {
    public r: number
    public g: number
    public b: number
    public a: number

    private constructor()
}

export interface ColorStatic extends JvmClass {
    new(r: number, g: number, b: number, a: number): ColorT
    new(r: number, g: number, b: number): ColorT
}

export const Color = Java.type<ColorStatic>('chr.wgx.render.common.Color')

/// ------ package chr.wgx.render.info ------ ///

export declare class DescriptorTypeT extends JvmObject { private constructor() }

export interface DescriptorTypeStatic extends JvmClass {
    COMBINED_IMAGE_SAMPLER: DescriptorTypeT
    UNIFORM_BUFFER: DescriptorTypeT
}

export const DescriptorType = Java.type<DescriptorTypeStatic>('chr.wgx.render.info.DescriptorType')

export declare class AttachmentCreateInfoT extends JvmObject {
    pixelFormat: PixelFormatT
    width: number
    height: number

    private constructor()
}

export declare class FieldInfoT extends JvmObject {
    name: string
    type: CGTypeT
    location: number
    byteOffset: number

    private constructor()
}

export declare class FieldInfoInputT extends JvmObject {
    name: string
    type: CGTypeT

    private constructor()
}

export declare class DescriptorLayoutBindingInfoT extends JvmObject {
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

export declare class DescriptorSetCreateInfoT extends JvmObject {
    layout: DescriptorSetLayoutT
    descriptors: DescriptorT[]

    private constructor()
}

export declare class DescriptorT extends JvmObject {}

export declare class DescriptorSetLayoutT extends JvmObject {
    info: DescriptorSetLayoutCreateInfoT

    private constructor()
}

export declare class DescriptorSetLayoutCreateInfoT extends JvmObject {
    bindings: DescriptorLayoutBindingInfoT[]

    private constructor()
}

export declare class ObjectCreateInfoT extends JvmObject {
    vertexInputInfo: VertexInputInfoT
    pVertices: MemorySegmentT
    pIndices: MemorySegmentT

    private constructor()
}

export declare class VertexInputInfoT extends JvmObject {
    attributes: FieldInfoT[]
    attributeMap: Map<string, FieldInfoT>
    stride: number

    private constructor()
}

export declare class PushConstantInfoT extends JvmObject {
    pushConstantRanges: PushConstantRangeT[]
    cpuLayout: MemoryLayout
    bufferSize: number

    private constructor()
}

export declare class PushConstantRangeT extends JvmObject {
    name: string
    shaderStage: ShaderStageT
    type: CGTypeT
    offset: number

    private constructor()
}

export declare class RenderPassAttachmentInfoT extends JvmObject {
    attachment: AttachmentT
    clearBehavior: ClearBehaviorT
    clearColor: ColorT

    private constructor()
}

export declare class RenderPassCreateInfoT extends JvmObject {
    name: string
    priority: number

    colorAttachmentInfos: RenderPassAttachmentInfoT[]
    depthAttachmentInfo: OptionT<RenderPassAttachmentInfoT>

    private constructor()
}

export declare class RenderPipelineCreateInfoT extends JvmObject {
    vertexInputInfo: VertexInputInfoT
    descriptorSetLayouts: DescriptorSetLayoutT[]
    pushConstantInfo: OptionT<PushConstantInfoT>
    vulkanShaderProgram: OptionT<ShaderProgram$VulkanT>
    gles2ShaderProgram: OptionT<ShaderProgram$GLES2T>
    colorAttachmentCount: number
    depthTest: boolean

    private constructor()
}

export declare class ShaderProgramT extends JvmObject { protected constructor() }

export declare class ShaderProgram$GLES2T extends ShaderProgramT {
    vertexShader: string
    fragmentShader: string

    private constructor()
}

export declare class ShaderProgram$VulkanT extends ShaderProgramT {
    vertexShader: number[];
    fragmentShader: number[];

    private constructor()
}

export declare class TextureBindingInfoT extends DescriptorLayoutBindingInfoT {
    private constructor()
}

export declare class TextureCreateInfoT extends JvmObject {
    image: BufferedImage
    mipmap: boolean

    private constructor()
}

export declare class UniformBufferCreateInfoT extends JvmObject {
    updateFrequency: UniformUpdateFrequencyT
    bindingInfo: UniformBufferBindingInfoT
    init: OptionT<MemorySegmentT>

    private constructor()
}

export interface AttachmentCreateInfoStatic extends JvmClass {
    new(pixelFormat: PixelFormatT, width: number, height: number): AttachmentCreateInfoT
    new(pixelFormat: PixelFormatT): AttachmentCreateInfoT
}

export interface FieldInputStatic extends JvmClass {
    new(name: string, type: CGTypeT): FieldInfoInputT
}

export interface UniformBufferBindingInfoStatic extends JvmClass {
    new(bindingName: string, stage: ShaderStageT, fieldInfoInputs: FieldInfoInputT[]): UniformBufferBindingInfoT
}

export interface DescriptorSetCreateInfoStatic extends JvmClass {
    new(descriptorType: DescriptorTypeT, bindingName: string, stage: ShaderStageT): DescriptorSetCreateInfoT
}

export interface DescriptorSetLayoutStatic extends JvmClass {
    new(info: DescriptorSetLayoutCreateInfoT): DescriptorSetLayoutT
}

export interface DescriptorSetLayoutCreateInfoStatic extends JvmClass {
    new(bindings: DescriptorLayoutBindingInfoT[]): DescriptorSetLayoutCreateInfoT
}

export interface ObjectCreateInfoStatic extends JvmClass {
    new(vertexInputInfo: VertexInputInfoT, pVertices: MemorySegmentT, pIndices: MemorySegmentT): ObjectCreateInfoT
}

export interface VertexInputInfoStatic extends JvmClass {
    new(attributes: FieldInfoInputT[]): VertexInputInfoT
}

export interface PushConstantInfoStatic extends JvmClass {
    new(pushConstantRanges: PushConstantRangeT[]): PushConstantInfoT
}

export interface PushConstantRangeStatic extends JvmClass {
    new(name: string, shaderStage: ShaderStageT, type: CGTypeT, offset: number): PushConstantRangeT
}

export interface RenderPassAttachmentInfoStatic extends JvmClass {
    new(attachment: AttachmentT, clearBehavior: ClearBehaviorT): RenderPassAttachmentInfoT
    new(attachment: AttachmentT, clearBehavior: ClearBehaviorT, clearColor: ColorT): RenderPassAttachmentInfoT
}

export interface RenderPassCreateInfoStatic extends JvmClass {
    new(
        name: string,
        priority: number,
        colorAttachmentInfos: RenderPassAttachmentInfoT,
        depthAttachmentInfo: OptionT<RenderPassAttachmentInfoT>
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
        depthAttachmentInfo: OptionT<RenderPassAttachmentInfoT>
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


export interface RenderPipelineCreateInfoStatic extends JvmClass {
    new(
        vertexInputInfo: VertexInputInfoT,
        descriptorSetLayouts: DescriptorSetLayoutT[],
        pushConstantInfo: OptionT<PushConstantInfoT>,
        vulkanShaderProgram: OptionT<ShaderProgram$VulkanT>,
        gles2ShaderProgram: OptionT<ShaderProgram$GLES2T>,
        colorAttachmentCount: number,
        depthTest: boolean
    ): RenderPipelineCreateInfoT
}

export interface ShaderProgram$GLES2Static extends JvmClass {
    new(vertexShader: string, fragmentShader: string): ShaderProgram$GLES2T
}

export interface ShaderProgram$VulkanStatic extends JvmClass {
    new(vertexShader: number[], fragmentShader: number[]): ShaderProgram$VulkanStatic
}

export interface ShaderProgramStatic extends JvmClass {
    GLES2: ShaderProgram$GLES2Static
    Vulkan: ShaderProgram$VulkanStatic
}

export interface TextureBindingInfoStatic extends JvmClass {
    new(bindingName: string, stage: ShaderStageT): TextureBindingInfoT
}

export interface TextureCreateInfoStatic extends JvmClass {
    new(image: BufferedImage, mipmap: boolean): TextureCreateInfoT
}

export interface UniformBufferCreateInfoStatic extends JvmClass {
    new(
        updateFrequency: UniformUpdateFrequencyT,
        bindingInfo: UniformBufferBindingInfoT
    ): UniformBufferCreateInfoT
    new(
        updateFrequency: UniformUpdateFrequencyT,
        bindingInfo: UniformBufferBindingInfoT,
        init: MemorySegmentT
    ): UniformBufferCreateInfoT
}

export const AttachmentCreateInfo = Java.type<AttachmentCreateInfoStatic>('chr.wgx.render.info.AttachmentCreateInfo')
export const FieldInfoInput = Java.type<FieldInputStatic>('chr.wgx.render.info.FieldInfoInput')
export const UniformBufferBindingInfo = Java.type<UniformBufferBindingInfoStatic>('chr.wgx.render.info.UniformBufferBindingInfo')
export const DescriptorSetCreateInfo = Java.type<DescriptorSetCreateInfoStatic>('chr.wgx.render.info.DescriptorSetCreateInfo')
export const DescriptorSetLayout = Java.type<DescriptorSetLayoutStatic>('chr.wgx.render.data.DescriptorSetLayout')
export const DescriptorSetLayoutCreateInfo = Java.type<DescriptorSetLayoutCreateInfoStatic>('chr.wgx.render.info.DescriptorSetLayoutCreateInfo')
export const ObjectCreateInfo = Java.type<ObjectCreateInfoStatic>('chr.wgx.render.info.ObjectCreateInfo')
export const VertexInputInfo = Java.type<VertexInputInfoStatic>('chr.wgx.render.info.VertexInputInfo')
export const PushConstantInfo = Java.type<PushConstantInfoStatic>('chr.wgx.render.info.PushConstantInfo')
export const PushConstantRange = Java.type<PushConstantRangeStatic>('chr.wgx.render.info.PushConstantRange')
export const RenderPassAttachmentInfo = Java.type<RenderPassAttachmentInfoStatic>('chr.wgx.render.info.RenderPassAttachmentInfo')
export const RenderPassCreateInfo = Java.type<RenderPassCreateInfoStatic>('chr.wgx.render.info.RenderPassCreateInfo')
export const RenderPipelineCreateInfo = Java.type<RenderPipelineCreateInfoStatic>('chr.wgx.render.info.RenderPipelineCreateInfo')
export const ShaderProgram = Java.type<ShaderProgramStatic>('chr.wgx.render.info.ShaderProgram')
export const TextureBindingInfo = Java.type<TextureBindingInfoStatic>('chr.wgx.render.info.TextureBindingInfo')
export const TextureCreateInfo = Java.type<TextureCreateInfoStatic>('chr.wgx.render.info.TextureCreateInfo')
export const UniformBufferCreateInfo = Java.type<UniformBufferCreateInfoStatic>('chr.wgx.render.info.UniformBufferCreateInfo')

/// ------ package chr.wgx.render.data ------ ///

export declare class AttachmentT extends JvmObject {
    createInfo: AttachmentCreateInfoT

    private constructor()
}
