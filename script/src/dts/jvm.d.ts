export {}

declare global {
    export interface jpackge_java {
        lang: jpackage_java_lang
    }

    export interface jpackage_java_lang {
        System: jclass_java_lang_System
    }

    export class jclass_java_lang_System {
        out: jclass_java_io_PrintStream
        err: jclass_java_io_PrintStream
    }

    export class jclass_java_io_PrintStream {
        public print(x: boolean): void
        public print(x: string): void
        public print(x: number): void
        public print(x: object): void

        public println(): void
        public println(x: string): void
        public println(x: boolean): void
        public println(x: number): void
        public println(x: object): void

        public printf(format: string, ...args: any[]): jclass_java_io_PrintStream
    }

    const java: jpackge_java

    export interface JavaClass {
        type(name: String): any
    }

    const Java: JavaClass;

    /// 用来防止用户错误地用一个字面量赋值给一个 JVM 类型的变量
    export class JvmClass {
        private __SECRET_INTERNALS_DO_NOT_USE_OR_YOU_WILL_BE_FIRED: number
    }

    export class MemorySegment extends JvmClass { private constructor() }
    export class MemoryLayout extends JvmClass { private constructor() }
    export class BufferedImage extends JvmClass { private constructor() }
}
