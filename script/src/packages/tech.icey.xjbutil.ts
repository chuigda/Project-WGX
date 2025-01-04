export declare class PairT<T1, T2> extends JvmObject {
    first(): T1
    second(): T2
    setFirst(first: T1): void
    setSecond(second: T2): void

    private constructor()
}

export declare class OptionT<T> extends JvmObject {
    get(): any
    nullable(): any | null
    isSome(): boolean
    isNone(): boolean

    private constructor()
}

export interface PairStatic extends JvmClass {
    new<T1, T2>(first: T1, second: T2): PairT<T1, T2>
}

export interface OptionStatic extends JvmClass {
    some<T>(value: T): OptionT<T>
    none<T>(): OptionT<T>
    fromNullable<T>(value: T | null): OptionT<T>
}

export const Pair: PairStatic = Java.type('tech.icey.xjbutil.container.Pair')
export const Option: OptionStatic = Java.type('tech.icey.xjbutil.container.Option')
