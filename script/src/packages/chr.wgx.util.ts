import { ByteArrayT } from './java'

export interface ResourceUtilStatic extends JvmClass {
    readTextFile(path: string): string
    readBinaryFile(path: string): ByteArrayT
}

export const ResourceUtil = Java.type<ResourceUtilStatic>('chr.wgx.util.ResourceUtil')
