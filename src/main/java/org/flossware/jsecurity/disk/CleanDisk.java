/*
 * Copyright (C) 2017 Scot P. Floess
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Disk wiping utility that fills free disk space with zero-filled files.
 *
 * <p><strong>WARNING:</strong> This utility performs destructive operations.
 * Data overwritten by this tool cannot be recovered. Use with extreme caution.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * java -jar jSecurity.jar [options] &lt;directory&gt; [directory2 ...]
 *
 * Options:
 *   -t, --threads &lt;count&gt;      Number of worker threads (default: 4)
 *   -b, --buffer-size &lt;bytes&gt;  Buffer size in bytes (default: 10485760)
 *   -y, --yes                  Skip confirmation prompt
 *   -h, --help                 Show this help message
 * </pre>
 *
 * @author Scot P. Floess
 */
public class CleanDisk {
    private static final Set<String> DANGEROUS_PATHS = new HashSet<>(Arrays.asList(
            "/", "/bin", "/boot", "/dev", "/etc", "/lib", "/lib64",
            "/proc", "/root", "/sbin", "/sys", "/usr", "/var",
            "/home", "/Users", "C:\\", "C:\\Windows", "C:\\Program Files"
    ));

    /**
     * Validates that a directory is safe to wipe.
     *
     * @param dirPath the directory path to validate
     * @throws IllegalArgumentException if the directory is not safe to wipe
     */
    static void validateSafeDirectory(final String dirPath) {
        final File dir = new File(dirPath);
        final File absDir = dir.getAbsoluteFile();
        final String absPath = absDir.getPath();

        for (final String dangerousPath : DANGEROUS_PATHS) {
            if (absPath.equals(dangerousPath) || absPath.startsWith(dangerousPath + File.separator)) {
                throw new IllegalArgumentException(
                        "SAFETY VIOLATION: Cannot wipe system directory: " + absPath);
            }
        }

        final File parent = absDir.getParentFile();
        if (parent != null && parent.exists() && !parent.canWrite()) {
            throw new IllegalArgumentException(
                    "Parent directory is not writable: " + parent.getPath());
        }

        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Path exists but is not a directory: " + absPath);
        }
    }

    /**
     * Prompts the user for confirmation before wiping.
     *
     * @param directories the directories that will be wiped
     * @param config the wipe configuration
     * @return true if user confirms
     * @throws IOException if input cannot be read
     */
    static boolean confirmWipe(final List<String> directories, final WipeConfiguration config) throws IOException {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("WARNING: DESTRUCTIVE OPERATION");
        System.out.println("=".repeat(70));
        System.out.println("\nThis will fill the following directories with zero-filled files:");
        for (final String dir : directories) {
            System.out.println("  - " + new File(dir).getAbsolutePath());
        }
        System.out.println("\nConfiguration:");
        System.out.println("  Threads: " + config.getThreadCount());
        System.out.println("  Buffer size: " + formatBytes(config.getBufferSize()));
        System.out.println("\nThis operation will continue until the disk is full.");
        System.out.println("Data cannot be recovered after being overwritten.");
        System.out.println("\n" + "=".repeat(70));
        System.out.print("\nType 'yes' to confirm: ");
        System.out.flush();

        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        final String response = reader.readLine();
        return "yes".equalsIgnoreCase(response);
    }

    /**
     * Wipes a directory by filling it with zero-filled files.
     *
     * @param dir the directory to wipe
     * @param config the wipe configuration
     * @throws InterruptedException if thread execution is interrupted
     */
    static void wipeDir(final String dir, final WipeConfiguration config) throws InterruptedException {
        final int threadCount = config.getThreadCount();
        final List<Thread> threads = new ArrayList<>(threadCount);

        System.out.println("\nStarting wipe operation on: " + new File(dir).getAbsolutePath());
        System.out.println("Spawning " + threadCount + " worker threads...\n");

        for (int index = 0; index < threadCount; index++) {
            final Thread thread = new Thread(new FileWorker(dir, config.getBufferSize()));
            thread.setName("WipeThread-" + index);
            thread.start();
            threads.add(thread);
        }

        System.out.println("All threads started. Waiting for completion...\n");

        for (final Thread thread : threads) {
            if (thread != null) {
                thread.join();
                System.out.println(thread.getName() + " completed");
            }
        }

        System.out.println("\nWipe operation completed for: " + dir);
    }

    /**
     * Parses command-line arguments and executes the wipe operation.
     *
     * @param args command-line arguments
     * @return exit code (0 for success, non-zero for error)
     */
    static int run(final String[] args) {
        if (args.length == 0) {
            printUsage();
            return 1;
        }

        final WipeConfiguration.Builder configBuilder = new WipeConfiguration.Builder();
        final List<String> directories = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];

            switch (arg) {
                case "-h":
                case "--help":
                    printUsage();
                    return 0;

                case "-t":
                case "--threads":
                    if (i + 1 >= args.length) {
                        System.err.println("Error: " + arg + " requires a value");
                        return 1;
                    }
                    try {
                        configBuilder.threadCount(Integer.parseInt(args[++i]));
                    } catch (final NumberFormatException e) {
                        System.err.println("Error: Invalid thread count: " + args[i]);
                        return 1;
                    }
                    break;

                case "-b":
                case "--buffer-size":
                    if (i + 1 >= args.length) {
                        System.err.println("Error: " + arg + " requires a value");
                        return 1;
                    }
                    try {
                        configBuilder.bufferSize(Integer.parseInt(args[++i]));
                    } catch (final NumberFormatException e) {
                        System.err.println("Error: Invalid buffer size: " + args[i]);
                        return 1;
                    }
                    break;

                case "-y":
                case "--yes":
                    configBuilder.skipConfirmation(true);
                    break;

                default:
                    if (arg.startsWith("-")) {
                        System.err.println("Error: Unknown option: " + arg);
                        printUsage();
                        return 1;
                    }
                    directories.add(arg);
                    break;
            }
        }

        if (directories.isEmpty()) {
            System.err.println("Error: No directories specified");
            printUsage();
            return 1;
        }

        final WipeConfiguration config;
        try {
            config = configBuilder.build();
        } catch (final IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        }

        for (final String dir : directories) {
            try {
                validateSafeDirectory(dir);
            } catch (final IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
                return 1;
            }
        }

        if (!config.isSkipConfirmation()) {
            try {
                if (!confirmWipe(directories, config)) {
                    System.out.println("\nOperation cancelled by user.");
                    return 0;
                }
            } catch (final IOException e) {
                System.err.println("Error reading confirmation: " + e.getMessage());
                return 1;
            }
        }

        final long startTime = System.currentTimeMillis();
        System.out.println("\n" + "=".repeat(70));
        System.out.println("Starting disk wipe operation");
        System.out.println("=".repeat(70));

        try {
            for (final String dir : directories) {
                wipeDir(dir, config);
            }
        } catch (final InterruptedException e) {
            System.err.println("\nOperation interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            return 1;
        } catch (final Exception e) {
            System.err.println("\nUnexpected error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }

        final long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
        System.out.println("\n" + "=".repeat(70));
        System.out.println("All operations completed successfully");
        System.out.println("Total time: " + elapsedSeconds + " seconds");
        System.out.println("=".repeat(70));

        return 0;
    }

    /**
     * Prints usage information.
     */
    static void printUsage() {
        System.out.println("jSecurity Disk Wiper - Secure Free Space Overwrite Utility");
        System.out.println("\nUsage: java -jar jSecurity.jar [options] <directory> [directory2 ...]");
        System.out.println("\nOptions:");
        System.out.println("  -t, --threads <count>      Number of worker threads (default: 4)");
        System.out.println("  -b, --buffer-size <bytes>  Buffer size in bytes (default: 10485760)");
        System.out.println("  -y, --yes                  Skip confirmation prompt");
        System.out.println("  -h, --help                 Show this help message");
        System.out.println("\nExamples:");
        System.out.println("  java -jar jSecurity.jar /tmp/wipe");
        System.out.println("  java -jar jSecurity.jar -t 8 -b 20971520 /tmp/wipe");
        System.out.println("  java -jar jSecurity.jar -y /tmp/wipe1 /tmp/wipe2");
        System.out.println("\nWARNING: This tool overwrites free disk space. Use with caution!");
    }

    /**
     * Formats bytes into a human-readable string.
     *
     * @param bytes the number of bytes
     * @return formatted string (e.g., "10.0 MB")
     */
    static String formatBytes(final long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), unit);
    }

    /**
     * Main entry point.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        System.exit(run(args));
    }
}
