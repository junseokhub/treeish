package io.github.junseokhub.treeish.testing;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 테스트에서 실제 git CLI를 실행하는 헬퍼.
 *
 * <p>treeish가 만든 저장소를 git으로 검증하거나(git fsck, git cat-file), git이 만든 저장소를 treeish로 읽는 교차 검증에 쓴다.
 * 실행 결과가 사용자 환경에 흔들리지 않도록 전역·시스템 설정을 무시하고, 작성자·시각을 고정한다.
 */
public final class GitCli {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    /** 커밋 해시가 매번 같도록 고정하는 시각 (2026-01-01T00:00:00+09:00). */
    public static final String FIXED_DATE = "1767193200 +0900";

    private final Path workDir;

    private GitCli(Path workDir) {
        this.workDir = workDir;
    }

    /** {@code workDir}을 작업 디렉터리로 git을 실행하는 인스턴스를 만든다. */
    public static GitCli in(Path workDir) {
        return new GitCli(workDir);
    }

    /** 이 환경에 git이 설치돼 있는지 확인한다. 테스트에서 {@code assumeTrue(GitCli.isAvailable())}로 쓴다. */
    public static boolean isAvailable() {
        try {
            Process process = new ProcessBuilder("git", "--version")
                    .redirectErrorStream(true)
                    .start();
            process.getInputStream().readAllBytes();
            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public Path workDir() {
        return workDir;
    }

    /** git을 실행하고 표준 출력을 UTF-8 문자열로 돌려준다. 종료 코드가 0이 아니면 예외를 던진다. */
    public String run(String... args) {
        return new String(runBytes(new byte[0], args), StandardCharsets.UTF_8);
    }

    /** 표준 입력을 넘겨 git을 실행한다. 예: {@code git hash-object -w --stdin}. */
    public String runWithInput(byte[] stdin, String... args) {
        return new String(runBytes(stdin, args), StandardCharsets.UTF_8);
    }

    /** git을 실행하고 표준 출력을 바이트 그대로 돌려준다. {@code git cat-file blob}처럼 바이너리를 다룰 때 쓴다. */
    public byte[] runBytes(byte[] stdin, String... args) {
        List<String> command = new ArrayList<>();
        command.add("git");
        // 결과가 사용자 설정에 흔들리지 않도록 고정하는 옵션
        command.addAll(List.of(
                "-c", "init.defaultBranch=main",
                "-c", "core.autocrlf=false",
                "-c", "gc.auto=0",
                "-c", "commit.gpgSign=false",
                "-c", "tag.gpgSign=false"));
        command.addAll(List.of(args));

        ProcessBuilder builder = new ProcessBuilder(command).directory(workDir.toFile());
        Map<String, String> env = builder.environment();
        env.put("GIT_CONFIG_NOSYSTEM", "1");
        env.put("GIT_CONFIG_GLOBAL", nullDevice());
        env.put("GIT_TERMINAL_PROMPT", "0");
        env.put("LC_ALL", "C");
        env.put("GIT_AUTHOR_NAME", "Treeish Test");
        env.put("GIT_AUTHOR_EMAIL", "test@treeish.invalid");
        env.put("GIT_AUTHOR_DATE", FIXED_DATE);
        env.put("GIT_COMMITTER_NAME", "Treeish Test");
        env.put("GIT_COMMITTER_EMAIL", "test@treeish.invalid");
        env.put("GIT_COMMITTER_DATE", FIXED_DATE);

        Path stderrFile = null;
        try {
            stderrFile = Files.createTempFile("treeish-git-stderr", ".log");
            builder.redirectError(stderrFile.toFile());
            Process process = builder.start();

            // stdin 쓰기와 stdout 읽기를 동시에 해야 파이프 버퍼가 차서 멈추는 일이 없다.
            Thread writer = Thread.ofVirtual().start(() -> writeAndClose(process.getOutputStream(), stdin));
            byte[] stdout;
            try (InputStream in = process.getInputStream()) {
                stdout = in.readAllBytes();
            }
            writer.join();

            if (!process.waitFor(TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new GitCommandException(command, -1, "timed out after " + TIMEOUT);
            }
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                String stderr = Files.readString(stderrFile, StandardCharsets.UTF_8);
                throw new GitCommandException(command, exitCode, stderr);
            }
            return stdout;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while running " + command, e);
        } finally {
            if (stderrFile != null) {
                try {
                    Files.deleteIfExists(stderrFile);
                } catch (IOException ignored) {
                    // 임시 파일 정리 실패는 테스트 결과에 영향을 주지 않는다.
                }
            }
        }
    }

    private static void writeAndClose(OutputStream out, byte[] data) {
        try (out) {
            out.write(data);
        } catch (IOException e) {
            // git이 stdin을 읽지 않고 끝나면 broken pipe가 날 수 있다. 실패 여부는 종료 코드로 판단한다.
        }
    }

    private static String nullDevice() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win") ? "NUL" : "/dev/null";
    }

    /** git 명령이 0이 아닌 종료 코드로 끝났을 때 던진다. */
    public static final class GitCommandException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final int exitCode;

        GitCommandException(List<String> command, int exitCode, String stderr) {
            super("git exited with " + exitCode + ": " + String.join(" ", command) + "\n" + stderr);
            this.exitCode = exitCode;
        }

        public int exitCode() {
            return exitCode;
        }
    }
}
