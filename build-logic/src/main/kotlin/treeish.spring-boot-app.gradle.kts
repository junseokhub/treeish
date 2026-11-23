// Spring Boot 실행 모듈 (server, 이후 review)
import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("treeish.java-conventions")
    id("org.springframework.boot")
}

dependencies {
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
}
