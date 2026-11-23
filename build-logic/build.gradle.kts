plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(plugin(libs.plugins.errorprone))
    implementation(plugin(libs.plugins.spotless))
    implementation(libs.spring.boot.gradle.plugin)
}

// 플러그인 id를 build-logic의 의존성 좌표로 바꾼다.
fun plugin(plugin: Provider<PluginDependency>): Provider<String> =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
