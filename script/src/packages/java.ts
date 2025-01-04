/// ------ Array types ------ ///

export declare class JvmListBase<T> extends JvmObject {
    [index: number]: T
    length: number

    protected constructor()
}

export declare class ByteArrayT extends JvmListBase<number> {
    private constructor()
}

export declare class IntArrayT extends JvmListBase<number> {
    private constructor()
}

export declare class FloatArrayT extends JvmListBase<number> {
    private constructor()
}

export interface ByteArrayStatic extends JvmClass {
    new(length: number): ByteArrayT
}

export interface IntArrayStatic extends JvmClass {
    new(length: number): IntArrayT
}

export interface FloatArrayStatic extends JvmClass {
    new(length: number): FloatArrayT
}

export const ByteArray: ByteArrayStatic = Java.type('byte[]')
export const IntArray: IntArrayStatic = Java.type('int[]')
export const FloatArray: FloatArrayStatic = Java.type('float[]')

/// ------ package java.lang.foreign ------ ///

export declare class MemorySegmentT extends JvmObject { private constructor() }

export declare class MemorySegmentStatic extends JvmClass {
    ofArray(array: IntArrayT): MemorySegmentT
    ofArray(array: FloatArrayT): MemorySegmentT
}

export function makeIntSegment(array: number[]): MemorySegmentT {
    const intArray = Java.to<IntArrayT>(array, IntArray)
    return MemorySegment.ofArray(intArray)
}

export function makeFloatSegment(array: number[]): MemorySegmentT {
    const floatArray = Java.to<FloatArrayT>(array, FloatArray)
    return MemorySegment.ofArray(floatArray)
}

/// ------ package java.util ------ ///

export declare class ArrayListT<T> extends JvmListBase<T> { private constructor() }

export interface ArrayListStatic extends JvmClass {
    new<T>(): ArrayListT<T>
}

/// ------ actual loading of java.* packages ------ ///

declare global {
    export interface jpackage_java {
        lang: jpackage_java_lang
        util: jpackage_java_util
    }

    export interface jpackage_java_lang {
        foreign: jpackage_java_lang_foreign
    }

    export interface jpackage_java_lang_foreign {
        MemorySegment: MemorySegmentStatic
    }

    export interface jpackage_java_util {
        ArrayList: ArrayListStatic
    }

    export const java: jpackage_java
}

export const MemorySegment = java.lang.foreign.MemorySegment
export const ArrayList = java.util.ArrayList
