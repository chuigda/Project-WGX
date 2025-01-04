export {}

declare global {
    /// 防止用户错误地用一个字面量赋值给一个 JVM 类型的变量
    export class JvmObject {
        private __SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED: number
    }

    /// 阻止用户手贱自己弄一个 interface 当成 JVM class 用
    export class JvmClass {
        private __SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED: number
    }

    export interface JavaClass {
        type<T extends JvmClass>(name: String): T
        to<T extends JvmObject>(value: any, clazz: JvmClass): T
    }

    const Java: JavaClass;

    export class MemoryLayout extends JvmObject { private constructor() }
    export class BufferedImage extends JvmObject { private constructor() }

    export function print(...value: any[]): void
}
