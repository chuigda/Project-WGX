import fs from 'fs'
import * as esbuild from 'esbuild'
import { makeBuildOptions } from './config.js'

fs.rmSync('build', { recursive: true, force: true })
fs.mkdirSync('build')

await esbuild.build(makeBuildOptions(
    'src/entry/generator.ts',
    'build/generator.js'
));
