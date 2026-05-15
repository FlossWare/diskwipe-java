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
package org.flossware.jsecurity.disk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CleanDiskTest {

    @Test
    void testValidateSafeDirectoryWithRoot() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/"));
    }

    @Test
    void testValidateSafeDirectoryWithBin() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/bin"));
    }

    @Test
    void testValidateSafeDirectoryWithEtc() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/etc"));
    }

    @Test
    void testValidateSafeDirectoryWithUsr() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/usr"));
    }

    @Test
    void testValidateSafeDirectoryWithHome() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/home"));
    }

    @Test
    void testValidateSafeDirectoryWithSubdirOfDangerous() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/etc/subdir"));
    }

    @Test
    void testValidateSafeDirectoryWithTempDir(@TempDir final Path tempDir) {
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(tempDir.toString()));
    }

    @Test
    void testValidateSafeDirectoryWithNonExistentSafeDir(@TempDir final Path tempDir) {
        final String safePath = new File(tempDir.toFile(), "safe/nested/path").getPath();
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(safePath));
    }

    @Test
    void testValidateSafeDirectoryWithFile(@TempDir final Path tempDir) throws Exception {
        final File file = new File(tempDir.toFile(), "testfile");
        file.createNewFile();

        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory(file.getPath()));
    }

    @Test
    void testRunWithNoArgs() {
        final int exitCode = CleanDisk.run(new String[]{});
        assertEquals(1, exitCode, "Should return error code when no arguments provided");
    }

    @Test
    void testRunWithHelpFlag() {
        final int exitCode = CleanDisk.run(new String[]{"--help"});
        assertEquals(0, exitCode, "Should return success code for help");
    }

    @Test
    void testRunWithHelpShortFlag() {
        final int exitCode = CleanDisk.run(new String[]{"-h"});
        assertEquals(0, exitCode, "Should return success code for help");
    }

    @Test
    void testRunWithUnknownOption() {
        final int exitCode = CleanDisk.run(new String[]{"--unknown"});
        assertEquals(1, exitCode, "Should return error code for unknown option");
    }

    @Test
    void testRunWithInvalidThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "invalid", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for invalid thread count");
    }

    @Test
    void testRunWithMissingThreadCountValue(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t"});
        assertEquals(1, exitCode, "Should return error code when thread count value is missing");
    }

    @Test
    void testRunWithInvalidBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "invalid", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for invalid buffer size");
    }

    @Test
    void testRunWithMissingBufferSizeValue(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b"});
        assertEquals(1, exitCode, "Should return error code when buffer size value is missing");
    }

    @Test
    void testRunWithNegativeThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "-1", "-y", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for negative thread count");
    }

    @Test
    void testRunWithZeroBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "0", "-y", tempDir.toString()});
        assertEquals(1, exitCode, "Should return error code for zero buffer size");
    }

    @Test
    void testRunWithDangerousDirectory() {
        final int exitCode = CleanDisk.run(new String[]{"-y", "/etc"});
        assertEquals(1, exitCode, "Should return error code for dangerous directory");
    }

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
        Thread.sleep(200);
        wipeThread.interrupt();
        wipeThread.join(2000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files");
    }

    @Test
    void testValidateSafeDirectoryWithVar() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/var"));
    }

    @Test
    void testValidateSafeDirectoryWithRootHome() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/root"));
    }

    @Test
    void testValidateSafeDirectoryWithSbin() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/sbin"));
    }

    @Test
    void testValidateSafeDirectoryWithBoot() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/boot"));
    }

    @Test
    void testValidateSafeDirectoryWithDev() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/dev"));
    }

    @Test
    void testValidateSafeDirectoryWithProc() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/proc"));
    }

    @Test
    void testValidateSafeDirectoryWithSys() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/sys"));
    }

    @Test
    void testValidateSafeDirectoryWithLib() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/lib"));
    }

    @Test
    void testValidateSafeDirectoryWithLib64() {
        assertThrows(IllegalArgumentException.class, () -> CleanDisk.validateSafeDirectory("/lib64"));
    }

    @Test
    void testRunWithValidThreadCount(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with valid thread count");
    }

    @Test
    void testRunWithValidBufferSize(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-b", "1024", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with valid buffer size");
    }

    @Test
    void testRunWithMultipleOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-b", "2048", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with multiple options");
    }

    @Test
    void testRunWithLongFormOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"--threads", "2", "--buffer-size", "1024", "--yes", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with long form options");
    }

    @Test
    void testFormatBytesWithLargeValues() {
        assertEquals("1.0 TB", CleanDisk.formatBytes(1024L * 1024 * 1024 * 1024));
        assertEquals("1.5 TB", CleanDisk.formatBytes((long)(1.5 * 1024 * 1024 * 1024 * 1024)));
    }

    @Test
    void testFormatBytesWithEdgeCases() {
        assertEquals("1023 B", CleanDisk.formatBytes(1023));
        assertEquals("1.0 KB", CleanDisk.formatBytes(1024));
        assertEquals("1023.0 KB", CleanDisk.formatBytes(1024 * 1023));
    }

    @Test
    void testRunWithNoDirectories() {
        final int exitCode = CleanDisk.run(new String[]{"-t", "2", "-b", "1024"});
        assertEquals(1, exitCode, "Should fail when no directories specified");
    }

    @Test
    void testRunWithMixedOptions(@TempDir final Path tempDir) {
        final int exitCode = CleanDisk.run(new String[]{"-t", "4", "--buffer-size", "512", "-y", tempDir.toString()});
        assertEquals(0, exitCode, "Should succeed with mixed short/long options");
    }

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
        Thread.sleep(150);
        wipeThread.interrupt();
        wipeThread.join(2000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files with single thread");
    }

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
        Thread.sleep(300);
        wipeThread.interrupt();
        wipeThread.join(3000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe files with many threads");
    }

    @Test
    void testValidateSafeDirectoryWithExistingDirectory(@TempDir final Path tempDir) {
        assertDoesNotThrow(() -> CleanDisk.validateSafeDirectory(tempDir.toString()));
    }

    @Test
    void testPrintUsageDoesNotThrow() {
        assertDoesNotThrow(() -> CleanDisk.printUsage());
    }

    @Test
    void testFormatBytesConsistency() {
        final long bytes = 1536;
        final String formatted = CleanDisk.formatBytes(bytes);
        assertTrue(formatted.contains("KB") || formatted.contains("B"));
    }
}
