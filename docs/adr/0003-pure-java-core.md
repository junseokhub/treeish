# 0003. 코어를 Spring 없는 순수 Java 모듈로 분리한다

- 상태: 채택
- 날짜: 2026-11-23

## 맥락

객체 저장소, diff, merge, 프로토콜 처리는 웹 프레임워크와 무관한 로직이다. 이후 SSH 전송(8단계)과 저장 서비스 분리(9단계)에서도 같은 코드를 재사용해야 한다.

## 결정

- `treeish-core`: 객체, 저장소, ref, diff, merge. Spring 의존성 금지.
- `treeish-protocol`: pkt-line, upload-pack, receive-pack. `InputStream`/`OutputStream`에만 의존하고 HTTP·SSH를 모른다.
- `treeish-server`: Spring Boot. HTTP·SSH 어댑터가 protocol 모듈을 호출한다.
- 의존 방향은 server → protocol → core 한 방향이다.

## 고려한 대안

- 단일 Spring Boot 모듈: 시작은 빠르지만 프로토콜 코드가 Servlet API에 묶여 SSH 어댑터에서 재사용하기 어렵다.

## 결과

- 코어는 Spring 컨텍스트 없이 빠르게 단위 테스트할 수 있다.
- HTTP와 SSH가 같은 protocol 코드를 쓰는지가 계층 분리의 검증 기준이 된다.
