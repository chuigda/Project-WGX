package tech.icey.wgx.core

import tech.icey.wgx.babel.BabelPlugin

class ExampleKotlinPlugin : BabelPlugin {
    override fun getName(): String = "Example Kotlin Plugin"

    override fun getDescription(): String = "演示 Kotlin 的部分功能"

    override fun getComponents(): List<Any> = listOf()
}
