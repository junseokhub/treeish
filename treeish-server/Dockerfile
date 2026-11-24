# syntax=docker/dockerfile:1
# core, protocol, build-logic을 함께 빌드하므로 컨텍스트는 저장소 루트다.
#   docker build -f treeish-server/Dockerfile -t treeish-server .
# 컨텍스트 필터는 루트 .dockerignore가 담당한다.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
# 테스트는 실제 git CLI가 필요해서 이미지 빌드에서는 돌리지 않는다. (CI에서 ./gradlew build로 검증)
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :treeish-server:bootJar --no-daemon \
 && find treeish-server/build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /app.jar \;

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 treeish \
 && mkdir /app && chown treeish /app
WORKDIR /app
COPY --from=build /app.jar app.jar
USER treeish
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
