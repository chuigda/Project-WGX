import { es5Plugin } from 'esbuild-plugin-es5'
import esbuildPluginTsc from 'esbuild-plugin-tsc'

export const makeBuildOptions = (entryPoint, outfile) => ({
    entryPoints: [entryPoint],
    outfile: outfile,
    // sourcemap: true,
    bundle: true,
    plugins: [esbuildPluginTsc({ force: true }), es5Plugin()],
    target: ['es5'],
    platform: 'node',
    format: 'iife',
    charset: 'utf8'
})
