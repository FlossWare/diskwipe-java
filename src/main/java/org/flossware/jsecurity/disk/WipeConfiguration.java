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

/**
 * Configuration for disk wiping operations.
 * Provides settings for thread count, buffer size, and operational flags.
 *
 * <p>Use the builder pattern to create instances:</p>
 * <pre>
 * WipeConfiguration config = new WipeConfiguration.Builder()
 *     .threadCount(8)
 *     .bufferSize(20 * 1024 * 1024)
 *     .skipConfirmation(true)
 *     .build();
 * </pre>
 *
 * @author Scot P. Floess
 */
class WipeConfiguration {
    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final int DEFAULT_BUFFER_SIZE = 10 * 1024 * 1024; // 10MB

    private final int threadCount;
    private final int bufferSize;
    private final boolean skipConfirmation;

    /**
     * Creates a configuration with specified settings.
     *
     * @param threadCount the number of worker threads
     * @param bufferSize the buffer size in bytes
     * @param skipConfirmation whether to skip the confirmation prompt
     * @throws IllegalArgumentException if threadCount or bufferSize is not positive
     */
    private WipeConfiguration(final int threadCount, final int bufferSize, final boolean skipConfirmation) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be positive, got: " + threadCount);
        }
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("Buffer size must be positive, got: " + bufferSize);
        }

        this.threadCount = threadCount;
        this.bufferSize = bufferSize;
        this.skipConfirmation = skipConfirmation;
    }

    /**
     * @return the number of worker threads to use
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @return the buffer size in bytes
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @return true if confirmation prompt should be skipped
     */
    public boolean isSkipConfirmation() {
        return skipConfirmation;
    }

    @Override
    public String toString() {
        return String.format("WipeConfiguration{threads=%d, bufferSize=%d bytes, skipConfirmation=%s}",
                threadCount, bufferSize, skipConfirmation);
    }

    /**
     * Builder for creating WipeConfiguration instances.
     */
    static class Builder {
        private int threadCount = DEFAULT_THREAD_COUNT;
        private int bufferSize = DEFAULT_BUFFER_SIZE;
        private boolean skipConfirmation = false;

        /**
         * Sets the number of worker threads.
         *
         * @param threadCount the number of threads (must be positive)
         * @return this builder
         */
        public Builder threadCount(final int threadCount) {
            this.threadCount = threadCount;
            return this;
        }

        /**
         * Sets the buffer size.
         *
         * @param bufferSize the buffer size in bytes (must be positive)
         * @return this builder
         */
        public Builder bufferSize(final int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        /**
         * Sets whether to skip the confirmation prompt.
         *
         * @param skipConfirmation true to skip confirmation
         * @return this builder
         */
        public Builder skipConfirmation(final boolean skipConfirmation) {
            this.skipConfirmation = skipConfirmation;
            return this;
        }

        /**
         * Builds the configuration.
         *
         * @return a new WipeConfiguration instance
         * @throws IllegalArgumentException if any values are invalid
         */
        public WipeConfiguration build() {
            return new WipeConfiguration(threadCount, bufferSize, skipConfirmation);
        }
    }
}
