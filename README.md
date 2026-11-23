# treeish

GitHub 같은 Git 호스팅 서버를 라이브러리(JGit 등) 없이 직접 구현하는 학습 프로젝트입니다.
git 객체 구조, 전송 프로토콜(Smart HTTP, SSH), diff 알고리즘, 코드 변경 후에도 리뷰 코멘트 위치를 추적하는 기능을 다룹니다.

## 검증 원칙

- 클라이언트는 수정하지 않은 실제 `git` CLI입니다. `git clone`, `git push`, `git fsck`가 통과해야 그 단계가 끝난 것으로 봅니다.
- 저장 포맷은 git의 디스크 포맷과 같습니다. treeish가 쓴 저장소를 git으로 바로 검증할 수 있습니다.
- JGit은 메인 코드에서 쓰지 않습니다. 테스트에서 결과를 비교하는 오라클과 참고 구현으로만 씁니다.

## 로드맵

| 단계 | 내용 | 상태 |
|---|---|---|
| 1 | 객체 저장소: loose object, ref, 해시 추상화(SHA-1/SHA-256) | 진행 중 |
| 2 | 팩파일 읽기: `.pack`/`.idx` v2, delta 해석 | |
| 3 | Smart HTTP clone/fetch: protocol v2, upload-pack | |
| 4 | push: receive-pack, quarantine, ref CAS | |
| 5 | diff 엔진: Myers, histogram, rename 감지, merge-base | |
| 6 | PR과 리뷰 도메인: Revision 스냅샷, 메모리 3-way merge | |
| 7 | 리뷰 코멘트 위치 추적: rebase·force-push 대응 | |
| 8 | SSH 전송 | |
| 9 | 성능과 운영: delta 생성, bitmap, 저장 계층 분리 | |

단계별 범위와 완료 기준은 [docs/roadmap.md](docs/roadmap.md)에 있습니다.

## 모듈

```
treeish-core      순수 Java: 객체, 저장소, ref, diff, merge
treeish-protocol  pkt-line, upload-pack, receive-pack (전송 계층과 무관)
treeish-server    Spring Boot: Smart HTTP / SSH 어댑터, 인증·인가
build-logic       공통 빌드 규칙 (툴체인, Error Prone + NullAway, Spotless)
```

## 요구 사항

- JDK 17 이상 (Gradle 실행용). 컴파일과 테스트에 쓰는 JDK 25는 없으면 Gradle이 자동으로 내려받습니다.
- git 2.40 이상 (교차 검증 테스트용)

## 빌드

```bash
./gradlew build            # 컴파일, 정적 분석, 포맷 검사, 테스트
./gradlew spotlessApply    # 코드 포맷 자동 정리
./gradlew :treeish-server:bootRun
```

커밋 전 비밀 정보 검사를 켜려면 한 번 실행합니다.

```bash
brew install gitleaks
git config core.hooksPath .githooks
```

## 설계 기록

주요 결정은 [docs/adr](docs/adr)에 남깁니다.

## 라이선스

[Apache License 2.0](LICENSE)
