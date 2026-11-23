# 로드맵

각 단계는 "실제 git CLI로 검증 가능한 완료 기준"을 가진다.

## 0단계. 사전 학습

- Pro Git 10장 (Git Internals)
- git 공식 문서: `gitformat-pack`, `gitprotocol-pack`, `gitprotocol-v2`, `gitprotocol-http`, `gitprotocol-capabilities`
- `GIT_TRACE_PACKET=1`, `GIT_TRACE_CURL=1`로 실제 clone/push 패킷 덤프 읽기

## 1단계. 객체 저장소

- 범위
  - blob, tree, commit, tag 파싱과 직렬화 (원본 바이트 그대로 재직렬화되어 해시가 보존돼야 함)
  - zlib, 해시 계산, `ObjectId`의 해시 알고리즘 추상화 (SHA-1, SHA-256)
  - loose object 읽기·쓰기 (임시 파일에 쓰고 원자적 rename)
  - refs: loose 파일, `packed-refs`, symbolic `HEAD`, ref 이름 검증 (`check-ref-format` 규칙)
  - `.lock` 파일 기반 원자적 ref 갱신과 CAS (기대한 old 값이 다르면 실패)
- 완료 기준
  - treeish가 쓴 저장소가 `git fsck --strict`를 통과한다.
  - git이 만든 저장소의 모든 loose 객체를 treeish가 읽고, 재직렬화한 해시가 원래 해시와 같다.
  - SHA-256 저장소(`git init --object-format=sha256`)에서도 위 두 가지가 성립한다.
  - 같은 ref를 여러 스레드가 같은 old 값으로 갱신하면 정확히 하나만 성공한다.

## 2단계. 팩파일 읽기

- 범위: `.pack`/`.idx` v2 파싱, OFS_DELTA/REF_DELTA 해석, delta base 캐시, mmap 랜덤 액세스
- 완료 기준: `git gc`를 마친 대형 저장소의 전체 객체를 읽은 결과가 `git rev-list --all --objects`, `git cat-file`과 일치

## 3단계. Smart HTTP clone/fetch (upload-pack, protocol v2)

- 범위: pkt-line, capability advertisement, `ls-refs`, `fetch`, want/have 협상, delta 없는 팩 생성, `side-band-64k`
- 완료 기준: `git clone http://localhost:8080/...` 성공, 증분 fetch에서 새 객체만 전송

## 4단계. push (receive-pack, v0/v1)

- 범위: command list, index-pack(해시 계산·delta 해석·thin pack 보완), connectivity check, quarantine, ref CAS, `atomic`, report-status, pre-receive 정책
- 완료 기준: push/force-push/브랜치 삭제 동작, 동시 push 시 하나만 성공, 손상된 팩이 저장소를 오염시키지 않음

## 5단계. diff 엔진

- 범위: Myers(linear-space), histogram, tree diff, rename 감지, unified diff, merge-base
- 완료 기준: 실제 저장소 커밋 N개에서 `git diff --diff-algorithm=myers|histogram` 출력과 일치, 속성 테스트로 패치 적용 결과 검증

## 6단계. PR과 리뷰 도메인

- 범위: Revision 불변 스냅샷 `(base, head, merge_base)`, three-dot diff, 메모리 3-way merge, merge/squash/rebase, push 이벤트(outbox → Kafka)
- 완료 기준: 머지 결과 tree가 `git merge-tree --write-tree`와 일치

## 7단계. 리뷰 코멘트 위치 추적

- 범위: 불변 원본 앵커 + revision별 계산 위치, head tree 간 diff로 줄 매핑, hunk 안이면 fuzzy 재배치 후 outdated, rename·삭제·LEFT 쪽·범위 코멘트, range-diff 기반 "마지막 리뷰 이후 변경분"
- 완료 기준: 속성 테스트(매핑된 줄 내용 = 원래 줄 내용), 실제 rebase 히스토리 데이터셋으로 재배치 정확도 측정

## 8단계. SSH 전송

- 범위: Apache MINA SSHD 임베드, 공개키 인증, HTTP와 같은 protocol 코어 재사용

## 9단계. 성능과 운영

- 범위: delta 압축 팩 생성, repack/GC, reachability bitmap, commit-graph, multi-pack-index, gRPC 저장 서비스 분리, 복제
- 완료 기준: 최적화 전후 clone 시간·메모리 벤치마크
