# Contributing to jsecurity

Thank you for your interest in contributing to jsecurity! This document provides guidelines and instructions for contributing to this project.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Setup](#development-setup)
4. [Coding Standards](#coding-standards)
5. [Testing Requirements](#testing-requirements)
6. [Submitting Changes](#submitting-changes)
7. [Pull Request Process](#pull-request-process)
8. [Reporting Bugs](#reporting-bugs)

## Code of Conduct

This project adheres to professional and respectful collaboration. Please:
- Be respectful and constructive in discussions
- Focus on the technical merits of contributions
- Help maintain a welcoming environment for all contributors

## Getting Started

### Prerequisites

Before contributing, ensure you have:
- **Java Development Kit (JDK) 11 or higher** - Required for compilation
- **Apache Maven 3.6+** - Required for building and testing
- **Git** - For version control
- A Java IDE (recommended: IntelliJ IDEA, Eclipse, or VS Code with Java extensions)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR-USERNAME/jsecurity.git
   cd jsecurity
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/FlossWare/jsecurity.git
   ```

## Development Setup

### Building the Project

Build the project to ensure your environment is set up correctly:

```bash
mvn clean package
```

This will:
- Compile all source code
- Run the full test suite
- Generate the executable JAR at `target/jsecurity-1.0.jar`

### Running Tests

Run the test suite:

```bash
mvn test
```

Run tests with coverage report:

```bash
mvn clean test jacoco:report
```

View coverage at `target/site/jacoco/index.html`

### Generating Documentation

Generate JavaDoc:

```bash
mvn javadoc:javadoc
```

View docs at `target/site/apidocs/index.html`

## Coding Standards

### Code Style

Follow these conventions consistently throughout the codebase:

#### General Guidelines
- **Indentation**: 4 spaces (no tabs)
- **Line Length**: Max 120 characters (prefer 100)
- **Encoding**: UTF-8
- **File Format**: Unix line endings (LF)

#### Java Conventions
- **Naming**:
  - Classes: `PascalCase` (e.g., `FileWorker`, `CleanDisk`)
  - Methods/Variables: `camelCase` (e.g., `formatBytes`, `bufferSize`)
  - Constants: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_BUFFER_SIZE`, `PREFIX`)
  - Package: lowercase (e.g., `org.flossware.jsecurity.disk`)

- **Visibility**:
  - Use package-private (no modifier) for internal classes
  - Use `private` for implementation details
  - Only expose `public` APIs when necessary
  - Document all public APIs with JavaDoc

- **Braces**:
  - Always use braces for `if`, `for`, `while`, even for single statements
  - Opening brace on same line as statement
  - Closing brace on new line

#### Example Code Style

```java
/**
 * Performs an important operation.
 *
 * @param input the input value
 * @return the result
 * @throws IllegalArgumentException if input is null
 */
public int doSomething(final String input) {
    if (input == null) {
        throw new IllegalArgumentException("Input cannot be null");
    }
    
    final int result = process(input);
    return result;
}
```

### JavaDoc Requirements

- **All public classes and methods** must have JavaDoc
- Include:
  - Summary sentence (first sentence is critical)
  - `@param` tags for all parameters
  - `@return` tag for non-void methods
  - `@throws` tags for declared exceptions
- Keep JavaDoc concise but complete
- Focus on *what* and *why*, not *how* (code shows the how)

### Copyright Headers

All Java files must include the GPL copyright header:

```java
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
```

## Testing Requirements

### Test Coverage

All contributions must include appropriate tests:

- **New features**: Require comprehensive test coverage (aim for 80%+ line coverage)
- **Bug fixes**: Add a test that reproduces the bug, then fix it
- **Refactoring**: Ensure existing tests pass; add new tests for changed behavior

### Test Structure

- Use **JUnit Jupiter (JUnit 5)** for all tests
- Place tests in `src/test/java` mirroring the source structure
- Test class naming: `[ClassUnderTest]Test.java`
- Test method naming: `test[MethodName][Condition]` (e.g., `testConstructorWithNullDirectory`)

### Test Guidelines

1. **Isolation**: Each test should be independent and not rely on other tests
2. **Cleanup**: Use `@TempDir` for file operations to ensure automatic cleanup
3. **Assertions**: Use clear assertion messages
4. **Edge Cases**: Test null inputs, empty values, boundary conditions, and invalid states
5. **Thread Safety**: Test concurrent operations where applicable

### Example Test

```java
@Test
void testConstructorWithNullDirectory() {
    assertThrows(IllegalArgumentException.class, 
        () -> new FileWorker(null, 1024),
        "Constructor should reject null directory");
}

@Test
void testFileCreation(@TempDir final Path tempDir) {
    final FileWorker worker = new FileWorker(tempDir.toFile(), 512);
    assertNotNull(worker, "Worker should be created successfully");
}
```

### Running Tests Locally

Before submitting a PR, ensure:

```bash
# All tests pass
mvn clean test

# No JavaDoc warnings
mvn javadoc:javadoc

# Project builds successfully
mvn clean package
```

## Submitting Changes

### Branch Strategy

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Use descriptive branch names:
   - `feature/add-multi-pass-wipe` - New features
   - `fix/thread-cleanup-issue` - Bug fixes
   - `docs/improve-usage-guide` - Documentation updates
   - `refactor/simplify-validation` - Code refactoring

### Commit Messages

Write clear, descriptive commit messages:

**Format:**
```
Short summary (50 chars or less)

Detailed explanation of what changed and why, if necessary.
Include relevant context, references to issues, etc.

Fixes #123
```

**Good commit messages:**
- `Add support for custom wipe patterns`
- `Fix thread interrupt handling in FileWorker`
- `Update README with Windows-specific instructions`

**Bad commit messages:**
- `fix bug`
- `changes`
- `wip`

### Before Submitting

Checklist before creating a PR:

- [ ] All tests pass (`mvn test`)
- [ ] Code follows project style guidelines
- [ ] New code has appropriate test coverage
- [ ] JavaDoc is complete for public APIs
- [ ] No compiler warnings
- [ ] CHANGELOG.md updated (if applicable)
- [ ] README.md updated (if user-facing changes)
- [ ] Commits are clean and well-organized

## Pull Request Process

1. **Update your fork** with the latest upstream changes:
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push your branch** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request** on GitHub with:
   - Clear title describing the change
   - Detailed description of what changed and why
   - Link to related issues (e.g., "Fixes #42")
   - Screenshots/examples if relevant

4. **Address review feedback** promptly:
   - Make requested changes in new commits
   - Push to your branch (PR will update automatically)
   - Respond to reviewer comments

5. **After approval**, maintainers will merge your PR

### PR Review Criteria

Your PR will be reviewed for:

- **Correctness**: Does the code work as intended?
- **Testing**: Are there adequate tests? Do they pass?
- **Code Quality**: Does it follow project conventions?
- **Documentation**: Is it properly documented?
- **Safety**: Are there potential security issues?
- **Performance**: Are there obvious performance problems?

## Reporting Bugs

### Before Reporting

1. Check if the issue already exists in GitHub Issues
2. Verify it's reproducible with the latest version
3. Gather relevant information (OS, Java version, command used)

### Bug Report Template

Create a new issue with:

**Title**: Short, descriptive summary

**Description**:
```
**Environment:**
- OS: [e.g., Ubuntu 22.04, Windows 11, macOS 13]
- Java Version: [e.g., OpenJDK 11.0.16]
- jsecurity Version: [e.g., 1.0]

**Steps to Reproduce:**
1. Run command: `java -jar jsecurity-1.0.jar -t 4 /tmp/test`
2. Observe behavior...

**Expected Behavior:**
Should...

**Actual Behavior:**
Instead...

**Error Messages/Logs:**
```
[paste relevant logs here]
```

**Additional Context:**
Any other relevant information...
```

## Questions?

If you have questions:
- Open an issue with the `question` label
- Check existing issues for similar questions
- Review the [README](README.md) and [USAGE](USAGE.md) documentation

## License

By contributing to jsecurity, you agree that your contributions will be licensed under the GNU General Public License v3.0 or later. See [LICENSE](LICENSE) for details.

---

Thank you for contributing to jsecurity! Your efforts help make secure disk wiping accessible to everyone.
