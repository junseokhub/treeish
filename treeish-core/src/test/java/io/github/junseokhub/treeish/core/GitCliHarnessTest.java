package io.github.junseokhub.treeish.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.junseokhub.treeish.testing.GitCli;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * 교차 검증용 git CLI 헬퍼가 제대로 동작하는지 확인한다.
 *
 * <p>1단계를 구현하면 이 클래스 옆에 "treeish가 쓴 객체를 git이 읽는다", "git이 쓴 객체를 treeish가 읽는다" 테스트를 추가한다.
 */
class GitCliHarnessTest {

    @BeforeAll
    static void requireGit() {
        assumeTrue(GitCli.isAvailable(), "git CLI가 PATH에 없어서 교차 검증 테스트를 건너뛴다");
    }

    @Test
    void 베어_저장소를_만들면_기본_브랜치가_main이다(@TempDir Path dir) throws Exception {
        GitCli git = GitCli.in(dir);

        git.run("init", "--bare", "repo.git");

        String head = Files.readString(dir.resolve("repo.git/HEAD"), StandardCharsets.UTF_8);
        assertThat(head).isEqualTo("ref: refs/heads/main\n");
    }

    @Test
    void blob_해시는_헤더를_포함한_SHA1이다(@TempDir Path dir) {
        GitCli git = GitCli.in(dir);

        // SHA-1("blob 6\0hello\n")
        String oid = git.runWithInput("hello\n".getBytes(StandardCharsets.UTF_8), "hash-object", "--stdin");

        assertThat(oid.strip()).isEqualTo("ce013625030ba8dba906f756967f9e9ca394464a");
    }

    @Test
    void 커밋_시각이_고정돼_있어서_해시가_재현된다(@TempDir Path dir) {
        GitCli git = GitCli.in(dir);
        git.run("init", "a");
        git.run("init", "b");

        git.run("-C", "a", "commit", "--allow-empty", "-m", "init");
        git.run("-C", "b", "commit", "--allow-empty", "-m", "init");

        assertThat(git.run("-C", "a", "rev-parse", "HEAD")).isEqualTo(git.run("-C", "b", "rev-parse", "HEAD"));
    }

    @Test
    void 실패한_명령은_stderr를_담아_예외를_던진다(@TempDir Path dir) {
        GitCli git = GitCli.in(dir);

        assertThatThrownBy(() -> git.run("cat-file", "-p", "deadbeef"))
                .isInstanceOf(GitCli.GitCommandException.class)
                .hasMessageContaining("cat-file");
    }
}
