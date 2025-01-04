import { CGType, ClearBehavior, FieldInfoInput, ObjectCreateInfo, RenderEngineT, RenderPassAttachmentInfo, RenderPassCreateInfo, RenderPipelineCreateInfo, ShaderProgram, VertexInputInfo } from '../packages/chr.wgx.render'
import { ResourceUtil } from '../packages/chr.wgx.util';
import { makeFloatSegment, makeIntSegment } from '../packages/java';
import { Option } from '../packages/tech.icey.xjbutil'

declare const engine: RenderEngineT

const vertexInputInfo = new VertexInputInfo([
    new FieldInfoInput('position', CGType.Vec2),
    new FieldInfoInput('color', CGType.Vec3)
])

const object = engine.createObject(new ObjectCreateInfo(
    vertexInputInfo,
    makeFloatSegment([
        // vec2 position, vec3 color
        -0.25, -0.25, 1.0, 0.0, 0.0,
        0.25, -0.25, 1.0, 1.0, 0.0,
        0.25, 0.25, 0.0, 1.0, 0.0,
        -0.25, 0.25, 0.0, 0.0, 1.0
    ]),
    makeIntSegment([
        0, 1, 2,
        2, 3, 0
    ])
))

const pipeline = engine.createPipeline(new RenderPipelineCreateInfo(
    vertexInputInfo,
    [],
    Option.none(),
    Option.some(new ShaderProgram.Vulkan(
        ResourceUtil.readBinaryFile('/resources/shader/wgc0310v1/vk/screen_content.vert.spv'),
        ResourceUtil.readBinaryFile('/resources/shader/wgc0310v1/vk/screen_content.frag.spv')
    )),
    Option.some(new ShaderProgram.GLES2(
        ResourceUtil.readTextFile('/resources/shader/wgc0310v1/gles2/screen_content.vert'),
        ResourceUtil.readTextFile('/resources/shader/wgc0310v1/gles2/screen_content.frag')
    )),
    1,
    false
))

const defaultColorAttachment = engine.getDefaultAttachments().first()

const renderPass = engine.createRenderPass(new RenderPassCreateInfo(
    "FINAL_drill_typeScriptCreatedPass",
    114514,
    new RenderPassAttachmentInfo(defaultColorAttachment, ClearBehavior.CLEAR_ONCE)
))
const pipelineBind = renderPass.createPipelineBind(0, pipeline)
const renderTaskGroup = pipelineBind.createRenderTaskGroup([])

renderTaskGroup.addRenderTask(object)
