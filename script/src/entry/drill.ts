import { System } from '../jvm'

import { Option, Pair } from '../packages/tech.icey.xjbutil'
import { AttachmentCreateInfo, PixelFormat, UniformUpdateFrequency } from '../packages/chr.wgx.render'

const p = new Pair("asd", "def")

System.err.println(p.first())
System.err.println(p.second())

const opt1 = Option.some([114, 514, 1919, 810])
System.err.println(opt1)
System.err.println(opt1.isSome())
System.err.println("opt1.get() = " + opt1.get())

const opt2 = Option.none()
System.err.println(opt2)
System.err.println(opt2.isNone())
System.err.println("opt2.nullable() = " + opt2.nullable())

System.err.println(UniformUpdateFrequency.PER_FRAME)

System.err.println(new AttachmentCreateInfo(PixelFormat.RGBA_OPTIMAL, 1920, 1080))
