plugins {
    id("treeish.spring-boot-app")
}

dependencies {
    implementation(project(":treeish-protocol"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(testFixtures(project(":treeish-core")))
}
