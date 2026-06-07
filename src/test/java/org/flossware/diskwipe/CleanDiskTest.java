/*
 * Copyright (C) 2017-2026 Scot P. Floess
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.flossware.diskwipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CleanDisk} application class. This test suite verifies the behavior
 * of directory validation, command-line argument parsing, byte formatting, and disk wiping functionality.
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *   <li><strong>Directory Validation:</strong> Ensures dangerous system directories (e.g., /root, /etc, /bin)
 *       are rejected while safe directories are accepted.</li>
 *   <li><strong>Command-line Parsing:</strong> Validates handling of short/long options, missing values,
 *       invalid arguments, and edge cases.</li>
 *   <li><strong>Byte Formatting:</strong> Tests human-readable byte size conversion (B, KB, MB, GB, TB)
 *       with various inputs including edge cases.</li>
 *   <li><strong>Disk Wiping:</strong> Verifies that the wiping process creates temporary files and
 *       respects thread configuration.</li>
 * </ul>
 *
 * <h2>Usage Notes</h2>
 * <ul>
 *   <li>Tests use {@link org.junit.jupiter.api.io.TempDir @TempDir} for isolated, temporary directories.</li>
 *   <li>Wiping operations are interrupted after a short delay to verify thread creation without
 *       fully completing the wipe process.</li>
 *   <li>All dangerous directory checks include subdirectories (e.g., /etc/subdir is also rejected).</li>
 * </ul>
 *
 * <h2>Exit Code Convention</h2>
 * <ul>
 *   <li><code>0</code>: Success or valid help request</li>
 *   <li><code>1</code>: Errors including validation failures, invalid arguments, or missing required options</li>
 * </ul>
 *
 * @see CleanDisk
 * @see WipeConfiguration
 * @since 1.0
 */
class CleanDiskTest {

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects the root directory
     * ("/") as unsafe, throwing {@link IllegalArgumentException}.
     */
    @Test
    void testValidateSafeDirectoryWithRoot() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/bin" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithBin() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/bin"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/etc" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithEtc() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/etc"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/usr" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithUsr() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/usr"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/home" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithHome() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/home"));
    }

    /**
     * Verifies that subdirectories of dangerous system directories (e.g., "/etc/subdir") are also
     * rejected as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithSubdirOfDangerous() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/etc/subdir"));
    }

    /**
     * Confirms that a safe temporary directory passes validation without throwing an exception.
     */
    @Test
    void testValidateSafeDirectoryWithTempDir(@TempDir final Path tempDir) {
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(tempDir.toString()));
    }

    /**
     * Confirms that a non-existent but safe nested path (outside dangerous system directories)
     * passes validation.
     */
    @Test
    void testValidateSafeDirectoryWithNonExistentSafeDir(@TempDir final Path tempDir) {
        final String safePath = new File(tempDir.toFile(), "safe/nested/path").getPath();
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(safePath));
    }

    /**
     * Verifies that a file (as opposed to a directory) is rejected by
     * {@link CleanDisk#validateSafeDirectory(String)}.
     */
    @Test
    void testValidateSafeDirectoryWithFile(@TempDir final Path tempDir) throws Exception {
        final File file = new File(tempDir.toFile(), "testfile");
        file.createNewFile();

        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory(file.getPath()));
    }

    /**
     * Confirms that {@link CleanDisk#run(String[])} returns exit code 1 when invoked with no
     * command-line arguments.
     */
    @Test
    void testRunWithNoArgs() {
        final int exitCode = CleanDisk.run(new String[]{});
        assertEquals(1, exitCode, "Should return error code when no arguments provided");
    }

    /**
     * Confirms that the long-form help flag ("--help") causes {@link CleanDisk#run(String[])}
     * to return exit code 0.
     */
    @Test
    void testRunWithHelpFlag() {
        final int exitCode = CleanDisk.run(new String[]{"--help"});
        assertEquals(0, exitCode, "Should return success code for help");
    }

    /**
     * Confirms that the short-form help flag ("-h") causes {@link CleanDisk#run(String[])}
     * to return exit code 0.
     */
    @Test
    void testRunWithHelpShortFlag() {
        final int exitCode = CleanDisk.run(new String[]{"-h"});
        assertEquals(0, exitCode, "Should return success code for help");
    }

    /**
     * Verifies that an unrecognized command-line option causes {@link CleanDisk#run(String[])}
     * to return exit code 1.
     */
    @Test
    void testRunWithUnknownOption() {
        final int exitCode = CleanDisk.run(new String[]{"--unknown"});
        assertEquals(1, exitCode, "Should return error code for unknown option");
    }

    /**
     * Verifies that a non-numeric thread count value causes {@link CleanDisk#run(String[])}
     * to return exit code 1.
     */
    @Test
    void testRunWithInvalidThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "invalid", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for invalid thread count");
    }

    /**
     * Confirms that a missing value for the "-t" (thread count) option results in exit code 1.
     */
    @Test
    void testRunWithMissingThreadCountValue(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t"});
        assertEquals(1, exitCode, "Should return error code when thread count value is missing");
    }

    /**
     * Verifies that a non-numeric buffer size value causes {@link CleanDisk#run(String[])}
     * to return exit code 1.
     */
    @Test
    void testRunWithInvalidBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "invalid", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for invalid buffer size");
    }

    /**
     * Confirms that a missing value for the "-b" (buffer size) option results in exit code 1.
     */
    @Test
    void testRunWithMissingBufferSizeValue(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b"});
        assertEquals(1, exitCode, "Should return error code when buffer size value is missing");
    }

    /**
     * Verifies that a negative thread count is rejected with exit code 1.
     */
    @Test
    void testRunWithNegativeThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "-1", "-y", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for negative thread count");
    }

    /**
     * Verifies that a buffer size of zero is rejected with exit code 1.
     */
    @Test
    void testRunWithZeroBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "0", "-y", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for zero buffer size");
    }

    /**
     * Confirms that a dangerous system directory (e.g., "/etc") is rejected with exit code 1.
     */
    @Test
    void testRunWithDangerousDirectory() {
        final int exitCode = CleanDisk.run(new String[]{"-y", "/etc"});
        assertEquals(1, exitCode, "Should return error code for dangerous directory");
    }

    /**
     * Tests {@link CleanDisk#formatBytes(long)} with various byte counts, verifying correct
     * conversion to human-readable format (B, KB, MB, GB).
     */
    @Test
    void testFormatBytes() {
        assertEquals("0 B", CleanDisk.formatBytes(0));
        assertEquals("512 B", CleanDisk.formatBytes(512));
        assertEquals("1.0 KB", CleanDisk.formatBytes(1024));
        assertEquals("1.5 KB", CleanDisk.formatBytes(1536));
        assertEquals("1.0 MB", CleanDisk.formatBytes(1024 * 1024));
        assertEquals("10.0 MB", CleanDisk.formatBytes(10 * 1024 * 1024));
        assertEquals("1.0 GB", CleanDisk.formatBytes(1024L * 1024 * 1024));
    }

    /**
     * Verifies that {@link CleanDisk#wipeDir(String, WipeConfiguration)} creates worker threads
     * and wipe files according to the specified configuration.
     */
    @Test
    void testWipeDirCreatesThreads(@TempDir final Path tempDir) throws Exception {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(2)
                .bufferSize(512)
                .build();

        final Thread wipeThread = new Thread(() -> {
            try {
                CleanDisk.wipeDir(tempDir.toString(), config);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        wipeThread.start();
        Thread.sleep(50);
        wipeThread.interrupt();
        wipeThread.join(2000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files");
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/var" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithVar() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/var"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/root" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithRootHome() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/root"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/sbin" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithSbin() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/sbin"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/boot" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithBoot() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/boot"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/dev" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithDev() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/dev"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/proc" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithProc() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/proc"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/sys" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithSys() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/sys"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/lib" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithLib() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/lib"));
    }

    /**
     * Verifies that {@link CleanDisk#validateSafeDirectory(String)} rejects "/lib64" as unsafe.
     */
    @Test
    void testValidateSafeDirectoryWithLib64() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/lib64"));
    }

    /**
     * Confirms that {@link CleanDisk#run(String[])} succeeds (exit code 0) with a valid thread
     * count and a safe directory.
     */
    @Test
    void testRunWithValidThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with valid thread count");
    }

    /**
     * Confirms that {@link CleanDisk#run(String[])} succeeds with a valid buffer size and a safe
     * directory.
     */
    @Test
    void testRunWithValidBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "1024", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with valid buffer size");
    }

    /**
     * Verifies that {@link CleanDisk#run(String[])} succeeds when multiple options (thread count
     * and buffer size) are provided together.
     */
    @Test
    void testRunWithMultipleOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-b", "2048", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with multiple options");
    }

    /**
     * Confirms that long-form command-line options (e.g., "--threads", "--buffer-size", "--yes")
     * work correctly.
     */
    @Test
    void testRunWithLongFormOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"--threads", "2", "--buffer-size", "1024", "--yes", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with long form options");
    }

    /**
     * Tests {@link CleanDisk#formatBytes(long)} with very large byte counts (terabytes).
     */
    @Test
    void testFormatBytesWithLargeValues() {
        assertEquals("1.0 TB", CleanDisk.formatBytes(1024L * 1024 * 1024 * 1024));
        assertEquals("1.5 TB", CleanDisk.formatBytes((long)(1.5 * 1024 * 1024 * 1024 * 1024)));
    }

    /**
     * Tests {@link CleanDisk#formatBytes(long)} with edge-case values around unit boundaries.
     */
    @Test
    void testFormatBytesWithEdgeCases() {
        assertEquals("1023 B", CleanDisk.formatBytes(1023));
        assertEquals("1.0 KB", CleanDisk.formatBytes(1024));
        assertEquals("1023.0 KB", CleanDisk.formatBytes(1024 * 1023));
    }

    /**
     * Verifies that {@link CleanDisk#run(String[])} returns exit code 1 when options are provided
     * but no target directories are specified.
     */
    @Test
    void testRunWithNoDirectories() {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-b", "1024"});
        assertEquals(1, exitCode, "Should fail when no directories specified");
    }

    /**
     * Confirms that {@link CleanDisk#run(String[])} succeeds when short-form and long-form options
     * are mixed in the same command line.
     */
    @Test
    void testRunWithMixedOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "4", "--buffer-size", "512", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with mixed short/long options");
    }

    /**
     * Verifies that {@link CleanDisk#wipeDir(String, WipeConfiguration)} functions correctly
     * with a single worker thread.
     */
    @Test
    void testWipeDirWithSingleThread(@TempDir final Path tempDir) throws Exception {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(1)
                .bufferSize(256)
                .build();

        final Thread wipeThread = new Thread(() -> {
            try {
                CleanDisk.wipeDir(tempDir.toString(), config);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        wipeThread.start();
        Thread.sleep(30);
        wipeThread.interrupt();
        wipeThread.join(2000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files with single thread");
    }

    /**
     * Verifies that {@link CleanDisk#wipeDir(String, WipeConfiguration)} functions correctly
     * with multiple worker threads (8 in this test).
     */
    @Test
    void testWipeDirWithManyThreads(@TempDir final Path tempDir) throws Exception {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(8)
                .bufferSize(128)
                .build();

        final Thread wipeThread = new Thread(() -> {
            try {
                CleanDisk.wipeDir(tempDir.toString(), config);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        wipeThread.start();
        Thread.sleep(50);
        wipeThread.interrupt();
        wipeThread.join(3000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files with many threads");
    }

    /**
     * Confirms that an existing safe directory (created by {@link TempDir}) passes validation.
     */
    @Test
    void testValidateSafeDirectoryWithExistingDirectory(@TempDir final Path tempDir) {
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(tempDir.toString()));
    }

    /**
     * Verifies that {@link CleanDisk#printUsage()} executes without throwing an exception.
     */
    @Test
    void testPrintUsageDoesNotThrow() {
        assertDoesNotThrow(() -> CleanDisk.printUsage());
    }

    /**
     * Confirms that {@link CleanDisk#formatBytes(long)} produces consistent output containing
     * appropriate unit labels.
     */
    @Test
    void testFormatBytesConsistency() {
        final long bytes = 1536;
        final String formatted = CleanDisk.formatBytes(bytes);
        assertTrue(formatted.contains("KB") || formatted.contains("B"));
    }
}
