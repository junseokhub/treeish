plugins {
    id("treeish.java-library")
}

dependencies {
    api(project(":treeish-core"))

    testImplementation(testFixtures(project(":treeish-core")))
}
