export declare class PairT extends JvmClass {
    first(): any
    second(): any
    setFirst(first: any): void
    setSecond(second: any): void

    private constructor()
}

export declare class OptionT<T> extends JvmClass {
    get(): any
    nullable(): any | null
    isSome(): boolean
    isNone(): boolean

    private constructor()
}

export interface PairStatic {
    new(first: any, second: any): PairT
}

export interface OptionStatic {
    some<T>(value: T): OptionT<T>
    none<T>(): OptionT<T>
    fromNullable<T>(value: T | null): OptionT<T>
}

export const Pair: PairStatic = Java.type('tech.icey.xjbutil.container.Pair')
export const Option: OptionStatic = Java.type('tech.icey.xjbutil.container.Option')
