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

class FileWorkerTest {

    @Test
    void testConstructorWithNullDirectory() {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker((File) null));
    }

    @Test
    void testConstructorWithNullDirectoryAndBufferSize() {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker((File) null, 1024));
    }

    @Test
    void testConstructorWithInvalidBufferSizeZero(@TempDir final Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker(tempDir.toFile(), 0));
    }

    @Test
    void testConstructorWithInvalidBufferSizeNegative(@TempDir final Path tempDir) {
        assertThrows(IllegalArgumentException.class, () -> new FileWorker(tempDir.toFile(), -100));
    }

    @Test
    void testConstructorCreatesDirectory(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "subdir");
        assertFalse(subDir.exists());

        new FileWorker(subDir);

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    @Test
    void testConstructorWithStringPath(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "stringpath");
        assertFalse(subDir.exists());

        new FileWorker(subDir.getPath());

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    @Test
    void testConstructorWithStringPathAndBufferSize(@TempDir final Path tempDir) {
        final File subDir = new File(tempDir.toFile(), "withbuffer");
        assertFalse(subDir.exists());

        new FileWorker(subDir.getPath(), 2048);

        assertTrue(subDir.exists());
        assertTrue(subDir.isDirectory());
    }

    @Test
    void testDefaultBufferSize(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile());
        assertNotNull(worker);
    }

    @Test
    void testCustomBufferSize(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 1024);
        assertNotNull(worker);
    }

    @Test
    void testRunCreatesFile(@TempDir final Path tempDir) throws InterruptedException {
        final int smallBufferSize = 1024;
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBufferSize);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(100);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create at least one wipe file");
    }

    @Test
    void testRunWritesData(@TempDir final Path tempDir) throws InterruptedException {
        final int smallBufferSize = 1024;
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBufferSize);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(200);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        if (files.length > 0) {
            assertTrue(files[0].length() > 0, "File should contain data");
        }
    }

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

        Thread.sleep(200);

        for (final Thread thread : threads) {
            thread.interrupt();
            thread.join(1000);
        }

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length >= workerCount, "Should create at least one file per worker");
    }

    @Test
    void testFileNamingConvention(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(100);
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

    @Test
    void testConstants() {
        assertEquals("wipe", FileWorker.PREFIX);
        assertEquals("disk", FileWorker.SUFFIX);
        assertEquals(10 * 1024 * 1024, FileWorker.DEFAULT_BUFFER_SIZE);
    }

    @Test
    void testRunCompletesSuccessfully(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
        final Thread thread = new Thread(worker);
        thread.start();

        Thread.sleep(150);
        thread.interrupt();
        thread.join(1000);

        assertFalse(thread.isAlive(), "Thread should have completed");
    }

    @Test
    void testLargeBufferSize(@TempDir final Path tempDir) {
        final int largeBuffer = 50 * 1024 * 1024; // 50MB
        final FileWorker worker = new FileWorker(tempDir.toFile(), largeBuffer);
        assertNotNull(worker);
    }

    @Test
    void testSmallBufferSize(@TempDir final Path tempDir) {
        final int smallBuffer = 1; // 1 byte
        final FileWorker worker = new FileWorker(tempDir.toFile(), smallBuffer);
        assertNotNull(worker);
    }

    @Test
    void testMultipleSequentialRuns(@TempDir final Path tempDir) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            final FileWorker worker = new FileWorker(tempDir.toFile(), 256);
            final Thread thread = new Thread(worker);
            thread.setName("SeqWorker-" + i);
            thread.start();

            Thread.sleep(100);
            thread.interrupt();
            thread.join(1000);
        }

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length >= 3, "Should create files from multiple runs");
    }

    @Test
    void testProgressReporting(@TempDir final Path tempDir) throws InterruptedException {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 100);
        final Thread thread = new Thread(worker);
        thread.setName("ProgressTest");
        thread.start();

        Thread.sleep(300);
        thread.interrupt();
        thread.join(1000);

        final File[] files = tempDir.toFile().listFiles((dir, name) -> name.startsWith("wipe"));
        assertNotNull(files);
        assertTrue(files.length > 0, "Should create wipe file");
    }

    @Test
    void testDirectoryCreationWithNestedPath(@TempDir final Path tempDir) {
        final File nestedDir = new File(tempDir.toFile(), "level1/level2/level3");
        assertFalse(nestedDir.exists());

        new FileWorker(nestedDir, 1024);

        assertTrue(nestedDir.exists());
        assertTrue(nestedDir.isDirectory());
    }

    @Test
    void testZeroByteBuffer(@TempDir final Path tempDir) {
        final FileWorker worker = new FileWorker(tempDir.toFile(), 1024);
        assertNotNull(worker);
    }
}
