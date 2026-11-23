# Architecture Decision Records

설계 결정을 하나씩 기록한다. 새 결정은 `NNNN-제목.md`로 추가하고, 기존 결정을 뒤집을 때는 원래 문서를 고치지 않고 새 ADR에서 "대체함"으로 연결한다.

각 문서는 상태, 맥락, 결정, 고려한 대안, 결과를 담는다.

| 번호 | 제목 | 상태 |
|---|---|---|
| 0001 | [저장 포맷을 git 디스크 포맷과 같게 한다](0001-git-on-disk-format.md) | 채택 |
| 0002 | [JGit은 테스트 오라클로만 쓴다](0002-jgit-as-test-oracle-only.md) | 채택 |
| 0003 | [코어를 Spring 없는 순수 Java 모듈로 분리한다](0003-pure-java-core.md) | 채택 |
| 0004 | [해시 알고리즘을 처음부터 추상화한다](0004-hash-algorithm-abstraction.md) | 채택 |
