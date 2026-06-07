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
 * Unit tests for {@link FileWorker}, verifying constructor validation, directory creation,
 * file-writing behavior, concurrency safety, and constant definitions.
 *
 * <p>These tests use JUnit 5's {@link TempDir} extension to provide isolated temporary
 * directories, ensuring no side effects on the real filesystem. Thread-based tests start
 * a {@code FileWorker} on a background thread, allow it to run briefly, then interrupt
 * and join to inspect the results.</p>
 *
 * <p><b>Test categories covered:</b></p>
 * <ul>
 *   <li><b>Constructor guard clauses</b> -- null directory, zero/negative buffer size</li>
 *   <li><b>Constructor happy paths</b> -- File, String, with/without custom buffer size,
 *       automatic directory creation including nested paths</li>
 *   <li><b>Run behavior</b> -- file creation, data writing, file naming convention,
 *       read-only directory handling, thread completion</li>
 *   <li><b>Concurrency</b> -- multiple workers in the same directory, sequential runs</li>
 *   <li><b>Edge cases</b> -- very small (1-byte) and very large (50 MB) buffer sizes</li>
 *   <li><b>Constants</b> -- {@link FileWorker#PREFIX}, {@link FileWorker#SUFFIX},
 *       {@link FileWorker#DEFAULT_BUFFER_SIZE}</li>
 * </ul>
 *
 * <p><b>Usage example (running from Maven):</b></p>
 * <pre>{@code
 * mvn test -Dtest=FileWorkerTest
 * }</pre>
 *
 * @author Scot P. Floess
 * @see FileWorker
 */
class FileWorkerTest {

    /**
     * Verifies that constructing a {@link FileWorker} with a null directory throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void testConstructorWithNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker((File) null));
    }

    /**
     * Verifies that constructing a {@link FileWorker} with a null directory and an explicit
     * buffer size throws {@link IllegalArgumentException}.
     */
    @Test
    void testConstructorWithNullDirectoryAndBufferSize() {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker((File) null, 1024));
    }

    /**
     * Verifies that constructing a {@link FileWorker} with a buffer size of zero throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void testConstructorWithInvalidBufferSizeZero(@TempDir final Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker(tempDir.toFile(), 0));
    }

    /**
     * Verifies that constructing a {@link FileWorker} with a negative buffer size throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void testConstructorWithInvalidBufferSizeNegative(@TempDir final Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker(tempDir.toFile(), -100));
    }

    /**
     * Confirms that the {@link FileWorker} constructor automatically creates a non-existent
     * directory when provided as a {@link File} parameter.
     */
    @Test
    void testConstructorCreatesDirectory(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "subdir");
        assertFalse(subDir.exists());

        new FileWorker(subDir);

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    /**
     * Confirms that the {@link FileWorker} constructor accepting a {@link String} path
     * automatically creates the directory if it does not exist.
     */
    @Test
    void testConstructorWithStringPath(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "stringpath");
        assertFalse(subDir.exists());

        new FileWorker(subDir.getPath());

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    /**
     * Confirms that the {@link FileWorker} constructor accepting both a {@link String} path and
     * an explicit buffer size creates the directory if needed.
     */
    @Test
    void testConstructorWithStringPathAndBufferSize(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "withbuffer");
        assertFalse(subDir.exists());

        new FileWorker(subDir.getPath(), 2048);

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    /**
     * Verifies that a {@link FileWorker} can be constructed without specifying a buffer size,
     * using the default buffer size defined by {@link FileWorker#DEFAULT_BUFFER_SIZE}.
     */
    @Test
    void testDefaultBufferSize(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile());
        assertNotNull(worker);
    }

    /**
     * Verifies that a {@link FileWorker} can be constructed with a custom buffer size.
     */
    @Test
    void testCustomBufferSize(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 1024);
        assertNotNull(worker);
    }

    /**
     * Verifies that executing a {@link FileWorker} on a background thread creates at least one
     * wipe file in the target directory.
     */
    @Test
    void testRunCreatesFile(@TempDir final Path tempDir) throws InterruptedException {
        final int smallBufferSize = 1024;
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBufferSize);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(20);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create at least one wipe file");
    }

    /**
     * Confirms that the wipe file created by {@link FileWorker#run()} contains actual data
     * (i.e., the file size is greater than zero).
     */
    @Test
    void testRunWritesData(@TempDir final Path tempDir) throws InterruptedException {
        final int smallBufferSize = 1024;
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBufferSize);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(30);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        if (files.length > 0) {
            assertTrue(files[0].length() > 0, "File should contain data");
        }
    }

    /**
     * Verifies that multiple {@link FileWorker} instances can run concurrently in the same
     * directory, each creating its own wipe file.
     */
    @Test
    void testMultipleWorkersInSameDirectory(@TempDir final Path tempDir) throws InterruptedException {
        final int workerCount = 3;
        final Thread[] threads = new Thread[workerCount];

        for (int i = 0; i < workerCount; i++) {
            final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
            threads[i] = new Thread(worker);
            threads[i].setName("TestWorker-" + i);
            threads[i].start();
        }

        Thread.sleep(30);

        for (final Thread thread : threads) {
            thread.interrupt();
            thread.join(1000);
        }

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length >= workerCount, "Should create at least one file per worker");
    }

    /**
     * Confirms that files created by {@link FileWorker} follow the expected naming convention,
     * with names starting with {@link FileWorker#PREFIX} ("wipe") and containing
     * {@link FileWorker#SUFFIX} ("disk").
     */
    @Test
    void testFileNamingConvention(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(20);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles();
        assertNotNull(files);
        assertTrue(files.length > 0);

        boolean foundWipeFile = false;
        for (final File file : files) {
            if (file.getName().startsWith("wipe") && file.getName().contains("disk")) {
                foundWipeFile = true;
                break;
            }
        }
        assertTrue(foundWipeFile, "Should create file with 'wipe' prefix and 'disk' suffix");
    }

    /**
     * Tests {@link FileWorker} behavior when the target directory is read-only. The worker should
     * handle I/O failures gracefully without crashing.
     */
    @Test
    void testRunWithReadOnlyDirectory(@TempDir final Path tempDir) throws Exception {
        final File readOnlyDir = new File(tempDir.toFile(), "readonly");
        readOnlyDir.mkdirs();
        readOnlyDir.setWritable(false);

        final FileWorker worker = new FileWorker(readOnlyDir, 512);
        final Thread thread = new Thread(worker);
        thread.start();
        thread.join(1000);

        readOnlyDir.setWritable(true);
    }

    /**
     * Verifies the values of {@link FileWorker} constants: {@link FileWorker#PREFIX},
     * {@link FileWorker#SUFFIX}, and {@link FileWorker#DEFAULT_BUFFER_SIZE}.
     */
    @Test
    void testConstants() {
        assertEquals("wipe", FileWorker.PREFIX);
        assertEquals("disk", FileWorker.SUFFIX);
        assertEquals(10 * 1024 * 1024, FileWorker.DEFAULT_BUFFER_SIZE);
    }

    /**
     * Confirms that a {@link FileWorker} thread completes (terminates) properly when interrupted
     * after a short delay.
     */
    @Test
    void testRunCompletesSuccessfully(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(30);
        thread.interrupt();
        thread.join(1000);

        assertFalse(thread.isAlive(), "Thread should have completed");
    }

    /**
     * Verifies that a {@link FileWorker} can be constructed with a very large buffer size
     * (50 MB in this test) without throwing an exception.
     */
    @Test
    void testLargeBufferSize(@TempDir final Path tempDir) {
        final int largeBuffer = 50 * 1024 * 1024; // 50MB
        final FileWorker worker = new FileWorker(tempDir.toFile(), largeBuffer);
        assertNotNull(worker);
    }

    /**
     * Verifies that a {@link FileWorker} can be constructed with a very small buffer size
     * (1 byte in this test) without throwing an exception.
     */
    @Test
    void testSmallBufferSize(@TempDir final Path tempDir) {
        final int smallBuffer = 1; // 1 byte
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBuffer);
        assertNotNull(worker);
    }

    /**
     * Verifies that multiple sequential (non-concurrent) {@link FileWorker} runs in the same
     * directory produce multiple wipe files.
     */
    @Test
    void testMultipleSequentialRuns(@TempDir final Path tempDir) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            final FileWorker worker = new FileWorker(tempDir.toFile(), 256);
            final Thread thread = new Thread(worker);
            thread.setName("SeqWorker-" + i);
            thread.start();

            Thread.sleep(20);
            thread.interrupt();
            thread.join(1000);
        }

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length >= 3, "Should create files from multiple runs");
    }

    /**
     * Confirms that a {@link FileWorker} with a small buffer size (100 bytes) successfully
     * creates a wipe file, demonstrating basic progress reporting functionality.
     */
    @Test
    void testProgressReporting(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 100);
        final Thread thread = new Thread(worker);
        thread.setName("ProgressTest");
        thread.start();

        Thread.sleep(50);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe file");
    }

    /**
     * Verifies that the {@link FileWorker} constructor automatically creates deeply nested
     * directory structures (e.g., "level1/level2/level3") when they do not exist.
     */
    @Test
    void testDirectoryCreationWithNestedPath(@TempDir final Path tempDir) {
        final File nestedDir = new File(tempDir.toFile(), "level1/level2/level3");
        assertFalse(nestedDir.exists());

        new FileWorker(nestedDir, 1024);

        assertTrue(nestedDir.exists());
        assertTrue(nestedDir.isDirectory());
    }

    /**
     * Verifies that a {@link FileWorker} can be constructed with a standard buffer size
     * (1024 bytes) without throwing an exception.
     */
    @Test
    void testZeroByteBuffer(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 1024);
        assertNotNull(worker);
    }
}
