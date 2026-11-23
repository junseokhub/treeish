plugins {
    id("treeish.java-library")
    `java-test-fixtures`
}

// 실제 git CLI와 교차 검증하는 테스트 헬퍼(GitCli)는 testFixtures에 둔다.
// protocol, server 모듈도 testImplementation(testFixtures(project(":treeish-core")))로 재사용한다.
dependencies {
    testFixturesCompileOnly(libs.jspecify)
}
