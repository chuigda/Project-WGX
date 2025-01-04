import { CGType, FieldInfoInput, ObjectCreateInfo, VertexInputInfo } from '../packages/chr.wgx.render'
import { makeFloatSegment, makeIntSegment } from '../packages/java'

const objCreateInfo = new ObjectCreateInfo(
    new VertexInputInfo([
        new FieldInfoInput('position', CGType.Vec2),
        new FieldInfoInput('color', CGType.Vec3)
    ]),
    makeFloatSegment([
        // vec2 pos, vec3 color
        -0.5, -0.5,  1.0, 0.0, 0.0,
        0.5, -0.5,   0.0, 1.0, 0.0,
        0.5, 0.5,    0.0, 0.0, 1.0,
        -0.5, 0.5,   1.0, 1.0, 1.0
    ]),
    makeIntSegment([
        0, 1, 2,
        2, 3, 0
    ])
)

print(objCreateInfo)
