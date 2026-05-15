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

import static org.junit.jupiter.api.Assertions.*;

class WipeConfigurationTest {

    @Test
    void testDefaultValues() {
        final WipeConfiguration config = new WipeConfiguration.Builder().build();

        assertEquals(4, config.getThreadCount());
        assertEquals(10 * 1024 * 1024, config.getBufferSize());
        assertFalse(config.isSkipConfirmation());
    }

    @Test
    void testBuilderThreadCount() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .threadCount(8)
                .build();

        assertEquals(8, config.getThreadCount());
    }

    @Test
    void testBuilderBufferSize() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .bufferSize(20 * 1024 * 1024)
                .build();

        assertEquals(20 * 1024 * 1024, config.getBufferSize());
    }

    @Test
    void testBuilderSkipConfirmation() {
        final WipeConfiguration config = new WipeConfiguration.Builder()
                .skipConfirmation(true)
                .build();

        assertTrue(config.isSkipConfirmation());
    }

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

    @Test
    void testInvalidThreadCountZero() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .threadCount(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testInvalidThreadCountNegative() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .threadCount(-1);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testInvalidBufferSizeZero() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .bufferSize(0);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void testInvalidBufferSizeNegative() {
        final WipeConfiguration.Builder builder = new WipeConfiguration.Builder()
                .bufferSize(-1000);

        assertThrows(IllegalArgumentException.class, builder::build);
    }

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
