# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5] - 2026-05-16

### Changed
- Updated all documentation to reference `main` branch instead of `master`
- Updated README.md CI/CD section with correct branch name
- Updated CHANGELOG.md with complete version history

## [1.4] - 2026-05-16

### Changed
- Migrated from `master` to `main` as default branch
- Updated GitHub Actions workflow to trigger on `main` branch
- Deleted `master` branch (both local and remote)

## [1.2] - 2026-05-15

### Added
- GitHub Actions CD-CI workflow for automated builds
- Automatic version bumping on push to main branch
- Deployment pipeline to packagecloud.io (flossware/java/maven2)
- Automatic git tagging for releases
- Maven SCM integration for version control operations

### Changed
- Artifact naming from jSecurity to jsecurity
- Version format from X.Y.Z to X.Y (matches jcollections project)
- All documentation references updated from "jSecurity" to "jsecurity"
- Enhanced .gitignore for Maven versioning artifacts

## [1.0] - 2026-05-15

### Added
- Comprehensive test suite with 76 unit tests covering all core functionality
- Enhanced test coverage for CleanDisk argument parsing and validation
- Additional FileWorker tests for concurrent operations and edge cases
- WipeConfiguration tests for builder pattern validation
- Thread interrupt handling for graceful shutdown of worker threads
- Progress reporting every 100 write operations per thread
- Support for multiple directory wipe operations in single invocation

### Changed
- Upgraded JUnit from 4.x to JUnit Jupiter 5.10.2
- Updated Maven compiler plugin to 3.11.0
- Updated Maven Surefire plugin to 3.2.5
- Updated Maven JavaDoc plugin to 3.6.3
- Updated Maven JAR plugin to 3.3.0
- Updated JaCoCo plugin to 0.8.11 for code coverage reporting
- Improved FileWorker thread safety and interrupt responsiveness
- Enhanced error handling and validation throughout codebase

### Fixed
- Critical bug fixes in file worker operations
- Thread completion and cleanup issues
- Argument parsing edge cases
- Buffer size validation
- Directory creation for nested paths

### Security
- Added comprehensive safety checks for system directories
- Validated write permissions before destructive operations
- Enhanced confirmation prompts with detailed warnings

## [0.1.0] - 2017-02-26

### Added
- Initial release of jsecurity disk wiping utility
- Multi-threaded disk space wiping with configurable worker threads
- Configurable buffer sizes for performance tuning
- Safety guards preventing wipe of critical system directories
- Command-line interface with options for threads, buffer size, and confirmation
- Confirmation prompts before destructive operations
- Support for Linux and Windows protected paths
- Progress reporting during wipe operations
- Zero-fill implementation for free space overwriting
- Maven build system with JAR packaging
- GPLv3 licensing

### Protection
- Blocked dangerous system directories: `/`, `/bin`, `/boot`, `/dev`, `/etc`, `/lib`, `/lib64`, `/proc`, `/root`, `/sbin`, `/sys`, `/usr`, `/var`, `/home`, `/Users`
- Windows protected paths: `C:\`, `C:\Windows`, `C:\Program Files`

[1.5]: https://github.com/FlossWare/jsecurity/compare/v1.4...v1.5
[1.4]: https://github.com/FlossWare/jsecurity/compare/v1.2...v1.4
[1.2]: https://github.com/FlossWare/jsecurity/compare/v1.0...v1.2
[1.0]: https://github.com/FlossWare/jsecurity/compare/v0.1...v1.0
[0.1.0]: https://github.com/FlossWare/jsecurity/releases/tag/v0.1.0
