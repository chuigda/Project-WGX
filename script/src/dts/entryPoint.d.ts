export {}

declare global {
    class jclass_EntryPoint {
        register(entryPoint: () => Generator): void;
    }

    const EntryPoint: jclass_EntryPoint;
}
