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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Worker thread that creates and fills temporary files with zeros until disk is full.
 * This class is designed to securely wipe free disk space by overwriting it with zeros.
 *
 * <p>Thread-safe: Each instance operates on its own file in the target directory.</p>
 *
 * @author Scot P. Floess
 */
class FileWorker implements Runnable {
    static final String PREFIX = "wipe";
    static final String SUFFIX = "disk";
    static final int DEFAULT_BUFFER_SIZE = 10 * 1024 * 1024; // 10MB default

    private final File dir;
    private final int bufferSize;
    private final byte[] buffer;

    /**
     * Creates a FileWorker with the specified directory and buffer size.
     *
     * @param dir the directory to create wipe files in
     * @param bufferSize the size of the buffer to use for writing (in bytes)
     * @throws IllegalArgumentException if dir is null or bufferSize is not positive
     */
    FileWorker(final File dir, final int bufferSize) {
        if (dir == null) {
            throw new IllegalArgumentException("Directory cannot be null");
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive, got: " + bufferSize);
        }

        this.dir = dir;
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];
        dir.mkdirs();
    }

    /**
     * Creates a FileWorker with the specified directory and default buffer size.
     *
     * @param dir the directory to create wipe files in
     */
    FileWorker(final File dir) {
        this(dir, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Creates a FileWorker with the specified directory path and buffer size.
     *
     * @param dir the directory path to create wipe files in
     * @param bufferSize the size of the buffer to use for writing (in bytes)
     */
    FileWorker(final String dir, final int bufferSize) {
        this(new File(dir), bufferSize);
    }

    /**
     * Creates a FileWorker with the specified directory path and default buffer size.
     *
     * @param dir the directory path to create wipe files in
     */
    FileWorker(final String dir) {
        this(new File(dir), DEFAULT_BUFFER_SIZE);
    }

    /**
     * Executes the disk wiping operation.
     * Creates a temporary file and fills it with zeros until the disk is full.
     * Uses try-with-resources to ensure proper cleanup of file handles.
     */
    @Override
    public void run() {
        final String threadName = Thread.currentThread().getName();
        File tempFile = null;

        try {
            tempFile = File.createTempFile(PREFIX, SUFFIX, dir);
            System.out.println(threadName + ": Created " + tempFile.getName());

            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rws")) {
                long totalBytesWritten = 0;
                int writeCount = 0;

                while (!Thread.currentThread().isInterrupted()) {
                    raf.write(buffer);
                    totalBytesWritten += bufferSize;
                    writeCount++;

                    if (writeCount % 100 == 0) {
                        System.out.println(threadName + ": " + formatBytes(totalBytesWritten) + " written");
                    }
                }

            } catch (final IOException ioException) {
                System.out.println(threadName + ": Disk full. Free space: " + formatBytes(dir.getFreeSpace()));
                attemptFinalWrite(tempFile);
            }

        } catch (final IOException ioException) {
            System.err.println(threadName + ": Failed to create temp file: " + ioException.getMessage());
        } catch (final Exception e) {
            System.err.println(threadName + ": Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Attempts to write remaining free space to fill the disk completely.
     * This is a best-effort attempt after the main write loop fills the disk.
     *
     * @param file the file to write to
     */
    private void attemptFinalWrite(final File file) {
        final long freeSpace = dir.getFreeSpace();
        if (freeSpace <= 0 || freeSpace > Integer.MAX_VALUE) {
            return;
        }

        try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
            raf.seek(raf.length());
            final byte[] finalBuffer = new byte[(int) freeSpace];
            raf.write(finalBuffer);
            System.out.println(Thread.currentThread().getName() + ": Filled remaining " + formatBytes(freeSpace));
        } catch (final IOException ioException) {
            // Expected - disk is completely full
        }
    }

    /**
     * Formats bytes into a human-readable string.
     *
     * @param bytes the number of bytes
     * @return formatted string (e.g., "1.5 GB")
     */
    private String formatBytes(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }
}
