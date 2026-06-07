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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link WipeConfiguration} and its nested {@link WipeConfiguration.Builder},
 * verifying default values, builder fluency, input validation, and string representation.
 *
 * <p>This test class exercises the following aspects of {@code WipeConfiguration}:</p>
 * <ul>
 *   <li><strong>Default values</strong> -- ensures a freshly built configuration carries the
 *       expected defaults (4 threads, 10 MB buffer, confirmation not skipped).</li>
 *   <li><strong>Individual builder setters</strong> -- confirms that each setter
 *       ({@code threadCount}, {@code bufferSize}, {@code skipConfirmation}) correctly
 *       overrides its corresponding default.</li>
 *   <li><strong>Builder chaining</strong> -- validates that all setters can be chained in a
 *       single fluent expression to produce a fully customized configuration.</li>
 *   <li><strong>Input validation</strong> -- asserts that zero or negative values for
 *       {@code threadCount} and {@code bufferSize} cause {@link IllegalArgumentException}
 *       to be thrown at build time.</li>
 *   <li><strong>{@code toString()} contract</strong> -- checks that the string representation
 *       contains the thread count, buffer size, and skip-confirmation flag.</li>
 * </ul>
 *
 * <p><strong>Usage example (Maven):</strong></p>
 * <pre>{@code
 * // Run all tests in this class:
 * mvn test -Dtest=WipeConfigurationTest
 *
 * // Run a single test method:
 * mvn test -Dtest=WipeConfigurationTest#testBuilderChaining
 * }</pre>
 *
 * @author Scot P. Floess
 * @see WipeConfiguration
 * @see WipeConfiguration.Builder
 */
class WipeConfigurationTest {

    /**
     * Verifies that a {@link WipeConfiguration} created via the builder with no explicit
     * settings carries the documented defaults: 4 threads, a 10 MB (10,485,760 byte) buffer,
     * and confirmation not skipped.
     */
    @Test
    void testDefaultValues() {
        final WipeConfiguration config = new WipeConfiguration.Builder().build();

        assertEquals(4, config.getThreadCount());
        assertEquals(10 * 1024 * 1024, config.getBufferSize());
        assertFalse(config.isSkipConfirmation());
    }

    /**
     * Confirms that {@link WipeConfiguration.Builder#threadCount(int)} correctly overrides the
     * default thread count when a positive value is supplied.
     */
    @Test
    void testBuilderThreadCount() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(8)
                .build();

        assertEquals(8, config.getThreadCount());
    }

    /**
     * Confirms that {@link WipeConfiguration.Builder#bufferSize(int)} correctly overrides the
     * default buffer size when a positive value is supplied.
     */
    @Test
    void testBuilderBufferSize() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .bufferSize(20 * 1024 * 1024)
                .build();

        assertEquals(20 * 1024 * 1024, config.getBufferSize());
    }

    /**
     * Confirms that {@link WipeConfiguration.Builder#skipConfirmation(boolean)} correctly
     * overrides the default skip-confirmation flag (which defaults to {@code false}).
     */
    @Test
    void testBuilderSkipConfirmation() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .skipConfirmation(true)
                .build();

        assertTrue(config.isSkipConfirmation());
    }

    /**
     * Validates that all builder setters can be chained in a single fluent expression and that
     * the resulting {@link WipeConfiguration} reflects every customized value.
     */
    @Test
    void testBuilderChaining() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(16)
                .bufferSize(5 * 1024 * 1024)
                .skipConfirmation(true)
                .build();

        assertEquals(16, config.getThreadCount());
        assertEquals(5 * 1024 * 1024, config.getBufferSize());
        assertTrue(config.isSkipConfirmation());
    }

    /**
     * Asserts that building a configuration with a thread count of zero throws
     * {@link IllegalArgumentException}, since at least one thread is required.
     */
    @Test
    void testInvalidThreadCountZero() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .threadCount(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     * Asserts that building a configuration with a negative thread count throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void testInvalidThreadCountNegative() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .threadCount(-1);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     * Asserts that building a configuration with a buffer size of zero throws
     * {@link IllegalArgumentException}, since a non-empty buffer is required for I/O operations.
     */
    @Test
    void testInvalidBufferSizeZero() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .bufferSize(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     * Asserts that building a configuration with a negative buffer size throws
     * {@link IllegalArgumentException}.
     */
    @Test
    void testInvalidBufferSizeNegative() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .bufferSize(-1000);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    /**
     * Verifies that {@link WipeConfiguration#toString()} produces a string containing the
     * thread count, buffer size, and skip-confirmation flag, ensuring diagnostic output
     * is informative.
     */
    @Test
    void testToString() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(4)
                .bufferSize(10485760)
                .skipConfirmation(false)
                .build();

        final String str = config.toString();
        assertTrue(str.contains("threads=4"));
        assertTrue(str.contains("bufferSize=10485760"));
        assertTrue(str.contains("skipConfirmation=false"));
    }
}
