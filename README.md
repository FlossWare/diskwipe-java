# jSecurity

Java-based security utilities for secure disk operations.

## ⚠️ WARNING - READ BEFORE USE ⚠️

**This utility performs DESTRUCTIVE operations that CANNOT be reversed.**

- Data overwritten by this tool is **PERMANENTLY LOST**
- There is **NO UNDO** functionality
- **Verify your target directories multiple times** before proceeding
- Designed for secure disposal of storage devices, not regular file deletion
- Always maintain backups of important data elsewhere

**Recommended use cases:**
- Wiping free space before selling/disposing of hardware
- Securely erasing decommissioned drives
- Preparing media for secure destruction

**NOT recommended for:**
- Regular file deletion (use `rm` or OS file manager instead)
- Active filesystems with important data
- System directories (tool blocks these, but be careful)

## Description

jSecurity is a disk wiping utility that securely overwrites free disk space with zero-filled files. It uses multiple worker threads to efficiently fill the target directory until the disk is full, making previously deleted data unrecoverable.

## Features

- **Multi-threaded operation** - Configurable worker threads for optimal performance
- **Configurable buffer sizes** - Adjust memory usage and write performance
- **Safety guards** - Prevents wiping of critical system directories
- **Confirmation prompts** - Requires explicit confirmation before destructive operations
- **Progress reporting** - Real-time feedback on operation progress
- **Flexible CLI** - Command-line options for automation and customization

## Prerequisites

- **Java 11 or higher** - Required to run the application
- **Maven 3.6+** - Required to build from source
- **Sufficient disk permissions** - Write access to target directories

## Building

Build the project using Maven:

```bash
mvn clean package
```

This creates `target/jSecurity-1.0.0.jar` with all dependencies.

## Usage

### Basic Usage

```bash
java -jar target/jSecurity-1.0.0.jar <directory>
```

The tool will:
1. Display a warning about the destructive operation
2. Show the target directory and configuration
3. Prompt for confirmation (type `yes` to proceed)
4. Start worker threads to fill the disk
5. Report completion when disk is full

### Command-Line Options

```
Options:
  -t, --threads <count>      Number of worker threads (default: 4)
  -b, --buffer-size <bytes>  Buffer size in bytes (default: 10485760)
  -y, --yes                  Skip confirmation prompt (for automation)
  -h, --help                 Show help message
```

### Examples

**Wipe a single directory with defaults:**
```bash
java -jar target/jSecurity-1.0.0.jar /tmp/secure-wipe
```

**Use 8 threads with 20MB buffer:**
```bash
java -jar target/jSecurity-1.0.0.jar -t 8 -b 20971520 /tmp/secure-wipe
```

**Wipe multiple directories:**
```bash
java -jar target/jSecurity-1.0.0.jar /tmp/wipe1 /tmp/wipe2 /tmp/wipe3
```

**Skip confirmation (for scripts):**
```bash
java -jar target/jSecurity-1.0.0.jar -y /tmp/secure-wipe
```

## Safety Features

### Protected Directories

The tool automatically blocks operations on critical system directories:
- `/`, `/bin`, `/boot`, `/dev`, `/etc`, `/lib`, `/lib64`
- `/proc`, `/root`, `/sbin`, `/sys`, `/usr`, `/var`
- `/home`, `/Users`
- `C:\`, `C:\Windows`, `C:\Program Files` (Windows)

Attempting to wipe these directories will result in an error.

### Confirmation Prompt

Unless the `-y` flag is used, the tool displays:
- Target directories (absolute paths)
- Configuration (threads, buffer size)
- Warning about data loss
- Confirmation prompt requiring `yes` response

## How It Works

1. **Validation** - Checks target directories are safe and writable
2. **Configuration** - Parses command-line options and builds configuration
3. **Confirmation** - Prompts user to confirm (unless `-y` flag used)
4. **Thread Spawning** - Creates N worker threads (default: 4)
5. **File Creation** - Each thread creates temp files with pattern `wipe*.disk`
6. **Writing** - Each thread writes zero-filled buffers until disk is full
7. **Completion** - Reports when all threads have finished

The tool writes until it receives an `IOException` indicating the disk is full, then attempts a final write to fill any remaining space.

## Performance Tuning

### Thread Count (`-t`)

- **More threads** = faster fill on multi-core systems
- **Fewer threads** = lower CPU usage
- **Recommended**: Start with 4, increase if CPU is underutilized
- **Maximum**: Depends on system capabilities (8-16 is usually sufficient)

### Buffer Size (`-b`)

- **Larger buffers** = fewer system calls, potentially faster writes
- **Smaller buffers** = lower memory usage, more frequent progress updates
- **Default**: 10MB (10485760 bytes) is a good balance
- **Recommended range**: 1MB - 100MB depending on available RAM

## Testing

Run the test suite:

```bash
mvn test
```

Generate coverage report:

```bash
mvn jacoco:report
```

View coverage at `target/site/jacoco/index.html`

## Documentation

Generate JavaDoc documentation:

```bash
mvn javadoc:javadoc
```

View documentation at `target/site/apidocs/index.html`

For detailed usage scenarios and troubleshooting, see [USAGE.md](USAGE.md).

## License

This project is licensed under the GNU General Public License v3.0 or later.

See [LICENSE](LICENSE) for the full license text.

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass (`mvn test`)
5. Submit a pull request

## Security Considerations

- **This tool does NOT implement secure multi-pass wiping** (e.g., DoD 5220.22-M)
- It performs a **single-pass zero fill** of free space
- For highly sensitive data, consider:
  - Multiple passes with different patterns
  - Hardware-level secure erase (ATA Secure Erase, NVMe Sanitize)
  - Physical destruction of media

## Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Review existing issues for similar problems
- Include system details (OS, Java version) in bug reports

## Acknowledgments

Copyright (C) 2017 Scot P. Floess

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it under certain conditions. See LICENSE for details.
