# jsecurity - Detailed Usage Guide

## Table of Contents

1. [Quick Start](#quick-start)
2. [Command-Line Reference](#command-line-reference)
3. [Usage Scenarios](#usage-scenarios)
4. [Safety Checklist](#safety-checklist)
5. [Performance Tuning](#performance-tuning)
6. [Troubleshooting](#troubleshooting)
7. [Advanced Topics](#advanced-topics)

## Quick Start

### Minimal Example

```bash
# Build the project
mvn clean package

# Wipe a directory (interactive mode with confirmation)
java -jar target/jsecurity-1.0.jar /tmp/secure-wipe
```

The tool will prompt for confirmation before proceeding.

### Non-Interactive Example

```bash
# Skip confirmation prompt (for scripts)
java -jar target/jsecurity-1.0.jar -y /tmp/secure-wipe
```

## Command-Line Reference

### Synopsis

```
java -jar jsecurity-1.0.jar [OPTIONS] DIRECTORY [DIRECTORY2 ...]
```

### Options

| Option | Long Form | Argument | Default | Description |
|--------|-----------|----------|---------|-------------|
| `-t` | `--threads` | `<count>` | 4 | Number of worker threads |
| `-b` | `--buffer-size` | `<bytes>` | 10485760 | Buffer size in bytes (10MB) |
| `-y` | `--yes` | - | false | Skip confirmation prompt |
| `-h` | `--help` | - | - | Display help and exit |

### Arguments

- **DIRECTORY**: One or more directory paths to wipe
  - Can be relative or absolute paths
  - Directories will be created if they don't exist
  - Multiple directories can be specified

### Exit Codes

| Code | Meaning |
|------|---------|
| 0 | Success (operation completed or help displayed) |
| 1 | Error (invalid arguments, unsafe directory, or runtime failure) |

## Usage Scenarios

### Scenario 1: Preparing a USB Drive for Sale

Before selling or giving away a USB drive, wipe its free space:

```bash
# Mount the drive (example: /media/usb)
# Delete any files you want to remove
# Then wipe free space

java -jar jsecurity-1.0.jar /media/usb/wipe-temp
```

**Steps:**
1. Mount the drive
2. Delete sensitive files normally
3. Create a temporary directory on the drive
4. Run jsecurity on that directory
5. After completion, delete the wipe directory
6. Unmount and remove the drive

### Scenario 2: Decommissioning a Server Disk

Securely wipe free space on a disk before decommissioning:

```bash
# Create a wipe directory
mkdir -p /mnt/old-disk/secure-wipe

# Run with more threads for faster completion
java -jar jsecurity-1.0.jar -t 16 -b 52428800 /mnt/old-disk/secure-wipe

# After completion, the disk can be safely removed
```

**Notes:**
- Use more threads (`-t 16`) for server-class hardware
- Increase buffer size (`-b 52428800` = 50MB) for better performance
- Monitor system resources during operation

### Scenario 3: Automated Cleanup Script

Integrate jsecurity into a cleanup script:

```bash
#!/bin/bash
set -e

# Configuration
WIPE_DIR="/tmp/secure-wipe-$$"
THREADS=8
BUFFER_SIZE=20971520  # 20MB

# Create temporary wipe directory
mkdir -p "$WIPE_DIR"

# Run jsecurity with auto-confirm
java -jar /path/to/jsecurity-1.0.jar \
    -y \
    -t "$THREADS" \
    -b "$BUFFER_SIZE" \
    "$WIPE_DIR"

# Cleanup
rm -rf "$WIPE_DIR"

echo "Secure wipe completed successfully"
```

### Scenario 4: Wiping Multiple Partitions

Wipe free space on multiple partitions simultaneously:

```bash
java -jar jsecurity-1.0.jar \
    /mnt/partition1/wipe \
    /mnt/partition2/wipe \
    /mnt/partition3/wipe
```

**Benefits:**
- Process multiple directories in sequence
- Single confirmation for all operations
- Consistent configuration across all targets

### Scenario 5: Low-Resource Systems

For systems with limited memory or CPU:

```bash
# Use fewer threads and smaller buffer
java -jar jsecurity-1.0.jar -t 1 -b 1048576 /tmp/wipe
```

**Configuration:**
- Single thread (`-t 1`) for minimal CPU usage
- 1MB buffer (`-b 1048576`) for low memory usage
- Slower but safe for resource-constrained systems

## Safety Checklist

Before running jsecurity, verify:

- [ ] **Backup critical data** - Ensure all important data is backed up elsewhere
- [ ] **Verify target directory** - Double-check you're targeting the correct path
- [ ] **Check available space** - Ensure the disk has free space to fill
- [ ] **System resources** - Ensure sufficient CPU and memory for operation
- [ ] **No active processes** - Verify no critical processes are using the disk
- [ ] **Sufficient time** - Operation may take hours for large disks
- [ ] **Monitoring capability** - Ensure you can monitor progress if needed

### Pre-Flight Verification

```bash
# 1. Check target directory
ls -la /target/directory

# 2. Check available space
df -h /target/directory

# 3. Check system resources
free -h
top

# 4. Verify directory is writable
touch /target/directory/test && rm /target/directory/test
```

### Post-Operation Verification

```bash
# 1. Check disk is actually full
df -h /target/directory

# 2. Verify wipe files were created
ls -lh /target/directory/wipe*

# 3. Cleanup wipe files
rm /target/directory/wipe*

# 4. Verify free space is restored
df -h /target/directory
```

## Performance Tuning

### Determining Optimal Thread Count

Your optimal thread count depends on several factors:

**CPU cores:**
```bash
# Check CPU core count
nproc
```

**Recommendations:**
- **Single disk (HDD)**: 2-4 threads (disk I/O is bottleneck)
- **Single disk (SSD)**: 4-8 threads (faster I/O allows more threads)
- **RAID array**: 8-16 threads (multiple disks benefit from parallelism)
- **Network storage**: 2-4 threads (network latency is bottleneck)

**Testing:**
```bash
# Try different thread counts and time the operation
time java -jar jsecurity-1.0.jar -y -t 4 /tmp/test-wipe-4
time java -jar jsecurity-1.0.jar -y -t 8 /tmp/test-wipe-8
```

### Determining Optimal Buffer Size

**Memory considerations:**
- Total buffer memory = `threads × buffer_size`
- Example: 8 threads × 20MB = 160MB RAM

**I/O considerations:**
- **HDD**: 10-50MB buffers reduce seek overhead
- **SSD**: 1-20MB buffers sufficient (minimal seek time)
- **Network**: Larger buffers (50-100MB) reduce round trips

**Recommendations:**
- **Default**: 10MB (`10485760`) - good for most cases
- **Low memory**: 1MB (`1048576`) - minimal RAM usage
- **High performance**: 50MB (`52428800`) - fewer system calls
- **Very low memory**: 512KB (`524288`) - absolute minimum

### Monitoring Performance

During operation, monitor:

```bash
# Monitor disk I/O
iostat -x 2

# Monitor CPU usage
top

# Monitor memory usage
free -h

# Monitor disk space
watch -n 5 'df -h /target/directory'
```

## Troubleshooting

### Problem: "SAFETY VIOLATION: Cannot wipe system directory"

**Cause:** Attempting to wipe a protected system directory.

**Solution:** Verify your target path. The tool blocks:
- `/`, `/bin`, `/etc`, `/usr`, `/var`, `/home`
- Create a subdirectory in a safe location instead

```bash
# Wrong: java -jar jsecurity.jar /home
# Right: java -jar jsecurity.jar /home/user/temp-wipe
```

### Problem: "Parent directory is not writable"

**Cause:** Insufficient permissions to create the target directory.

**Solutions:**
1. Use a directory you have write access to
2. Run with appropriate permissions (NOT recommended for root directories)
3. Change directory ownership

```bash
# Check permissions
ls -ld /parent/directory

# Fix permissions (if appropriate)
chmod u+w /parent/directory
```

### Problem: Operation is very slow

**Possible causes:**
1. Disk is nearly full (normal - takes time to fill remaining space)
2. Too many threads for disk type
3. Buffer size too small
4. Disk is failing/slow

**Solutions:**
```bash
# Check disk health
smartctl -a /dev/sdX

# Reduce threads for HDD
java -jar jsecurity.jar -t 2 /target

# Increase buffer size
java -jar jsecurity.jar -b 52428800 /target
```

### Problem: Out of memory error

**Cause:** Too many threads or too large buffer size.

**Solution:** Reduce total memory usage:

```bash
# Calculate: threads × buffer_size = total memory
# Example: 4 × 10MB = 40MB (safe)
# Example: 16 × 100MB = 1.6GB (may be too much)

# Reduce threads
java -jar jsecurity.jar -t 2 /target

# Or reduce buffer size
java -jar jsecurity.jar -b 5242880 /target  # 5MB
```

### Problem: Process interrupted/killed

**Causes:**
1. User interrupted with Ctrl+C
2. System killed process (OOM killer)
3. System shutdown/reboot

**Impact:**
- Partial wipe files remain on disk
- Free space is partially filled
- Operation is safe to restart

**Cleanup:**
```bash
# Remove partial wipe files
rm /target/directory/wipe*

# Restart operation
java -jar jsecurity.jar /target/directory
```

### Problem: Disk not completely full after operation

**Causes:**
1. Reserved space (filesystem reserves 5% typically)
2. Small remaining fragments
3. Metadata overhead

**Verification:**
```bash
# Check actual free space
df -h /target

# If significant space remains, run again
java -jar jsecurity.jar -y /target/wipe2
```

## Advanced Topics

### Integration with Secure Erase

For maximum security, combine jsecurity with hardware-level secure erase:

```bash
# 1. Wipe free space with jsecurity
java -jar jsecurity.jar -y /mnt/disk/wipe

# 2. Then use ATA Secure Erase (if supported)
hdparm --user-master u --security-set-pass password /dev/sdX
hdparm --user-master u --security-erase password /dev/sdX
```

### Multi-Pass Wiping

For paranoid security, run multiple passes:

```bash
#!/bin/bash
# 3-pass wipe script

for pass in 1 2 3; do
    echo "Pass $pass of 3..."
    java -jar jsecurity.jar -y /tmp/wipe-pass-$pass
    rm -rf /tmp/wipe-pass-$pass
done

echo "Multi-pass wipe complete"
```

### Verifying Wipe Effectiveness

Use forensic tools to verify data is unrecoverable:

```bash
# Use photorec to scan for recoverable files
photorec /dev/sdX

# Use foremost for file carving
foremost -i /dev/sdX -o recovery-test
```

### Customizing the JAR

Build a custom JAR with different defaults:

```bash
# Edit WipeConfiguration.java defaults
# Then rebuild
mvn clean package

# Or use environment variables/system properties (future enhancement)
```

### Logging Output

Capture operation log:

```bash
# Log to file
java -jar jsecurity.jar /target 2>&1 | tee wipe-operation.log

# Log with timestamps
java -jar jsecurity.jar /target 2>&1 | while read line; do
    echo "$(date -Iseconds) $line"
done | tee wipe-operation.log
```

## Best Practices

1. **Always backup first** - Verify backups before wiping
2. **Test on small volumes** - Understand behavior before large operations
3. **Monitor first run** - Watch resource usage and adjust accordingly
4. **Use appropriate threads** - More isn't always better
5. **Clean up after** - Remove wipe files after operation
6. **Verify completion** - Check disk is actually full
7. **Document configuration** - Record settings that work for your environment
8. **Schedule appropriately** - Run during off-hours for servers
9. **Plan for time** - Large disks take hours or days
10. **Have fallback** - Know how to stop and restart if needed

## Getting Help

- **Read error messages carefully** - They contain specific guidance
- **Check this document** - Most issues are covered here
- **Review README** - Basic usage and safety information
- **Check GitHub issues** - Someone may have had the same problem
- **Open an issue** - Provide system details and error messages

---

Copyright (C) 2017 Scot P. Floess - Licensed under GPL-3.0
