# VisitRaleigh Events Harvester

A Java-based web scraper that generates an RSS feed of events from https://www.visitraleigh.com/events

Built with Claude using SOLID principles and a modular, extensible architecture.

Feed exported to [https://github.com/mckelvym/visitraleigh.events.rss](https://github.com/mckelvym/visitraleigh.events.rss)

## Features

- **Incremental RSS Feed** - Appends new events to existing feed
- **JavaScript Rendering** - Uses Selenium WebDriver for JavaScript-populated sites
- **SOLID Architecture** - Clean, maintainable, extensible code
- **Configurable** - Environment variables for date ranges, e.g. `DROP_EVENTS_OLDER_THAN_DAYS`, `DAYS_INTO_FUTURE`)

---

## Quick Start

### Prerequisites

```bash
# Install ChromeDriver
brew install chromedriver

# Java 17+ required
java -version
```

### Build

```bash
./gradlew build
```

### Run

```bash
# Generate RSS feed (default: events.xml)
./gradlew run

# Custom output file
./gradlew run -Pargs='my-events.xml'
```

### Test

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

### Code Quality

```bash
# Check code style (Google Java Style)
./gradlew checkstyleMain

# Run static analysis
./gradlew spotbugsMain

# Run all checks
./gradlew check
```

---

## Architecture

### Overview

The application follows SOLID principles with clear separation of concerns:

visitraleigh.events/
- domain/              # Business entities (EventItem)
- config/              # Configuration (site-specific settings)
- webdriver/           # WebDriver lifecycle management
- scraper/             # Event scraping logic
- parser/              # HTML parsing & extraction
- feed/                # RSS feed generation
- RaleighEventsApplication.java  # Main entry point

### Key Design Patterns

- **Dependency Injection** - Manual constructor injection for testability
- **Strategy Pattern** - Multiple extraction strategies (e.g. 5 for titles)
- **Template Method** - Scraping workflow with customizable steps
- **Factory Pattern** - Secure XML factory creation

### SOLID Principles

- **Single Responsibility** - Each class has one clear purpose
- **Open/Closed** - Extend via interfaces, not modification
- **Liskov Substitution** - All implementations are interchangeable
- **Interface Segregation** - Small, focused interfaces (1-2 methods)
- **Dependency Inversion** - Depend on abstractions, not concretions

---

## Workflow

The application follows a three-phase workflow:

### Phase 1: Load Existing Feed

RssFeedManager.loadExistingGuids(filePath)
- Parse existing RSS XML (with XXE protection)
- Extract all GUIDs into Set
- Return Set of existing event IDs

### Phase 2: Scrape Events

EventScraper.scrapeEvents(existingGuids)
- Initialize WebDriver (Chrome headless)
- Load page 1 with Selenium, with target end date URL param (`DAYS_INTO_FUTURE`)
- Determine total pages from pagination
- For each page:
  - Load page with Selenium
  - Parse HTML with JSoup
  - Discover event links (EventLinkDiscoverer)
  - For each link:
    - Find event card container (EventCardFinder)
    - Extract title (TitleExtractor)
    - Extract date (DateExtractor)
    - Extract description (DescriptionExtractor)
    - Extract image URL (ImageExtractor)
    - Build EventItem
    - Add to list if not duplicate

### Phase 3: Generate RSS Feed

RssFeedManager.generateFeed(...)
- Create new RSS 2.0 XML document
- Add channel metadata
- Add new events (sorted by ID, descending)
- Import existing events from old feed
- Filter events by age (`DROP_EVENTS_OLDER_THAN_DAYS`)
- Format XML with indentation
- Write to file

---

## Configuration

### Environment Variables

```bash
# Number of days into the future to scrape (default: 30)
export DAYS_INTO_FUTURE=30

# Drop events older than this many days (default: 30)
export DROP_EVENTS_OLDER_THAN_DAYS=30
```

---

## Testing

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "HtmlParsingTest"

# With detailed output
./gradlew test --info

# Generate coverage report
./gradlew test jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html
```

### Test Coverage

Current: ~40% (foundation tests)
Target: 80%+

---

## Docker

### Build Image

```bash
./gradlew jib -Djib.to.image=docker-registry-image-name:tag
```

### Run Container

```bash
./scripts/run.sh
```

Update `version` as necessary.

The Dockerfile uses `seleniarm/standalone-chromium` as the base image for multi-platform support (amd64/arm64).

---

## Dependencies

### Runtime
- **JSoup** - HTML parsing
- **Selenium** - Browser automation for JavaScript rendering
- **SLF4J + Logback** - Logging
- **Guava** - Utilities

### Testing
- **JUnit** - Test framework
- **Mockito** - Mocking
- **AssertJ** - Fluent assertions

### Code Quality
- **Checkstyle** - Code style enforcement
- **SpotBugs** - Static analysis

---

## Build Outputs

### Targets

```bash
# Standard JAR
./gradlew jar
# Output: build/libs/visitraleigh.events-1.0.0.jar

# Fat JAR (all dependencies included)
./gradlew fatJar
# Output: build/libs/raleigh-events-rss-generator-all.jar

# Run fat JAR
java -jar build/libs/raleigh-events-rss-generator-all.jar events.xml
```

---

## Troubleshooting

### ChromeDriver Issues

```bash
# macOS: Allow chromedriver
xattr -d com.apple.quarantine $(which chromedriver)

# Verify installation
chromedriver --version
```

### Memory Issues

```bash
# Increase heap size
export GRADLE_OPTS="-Xmx2g"
./gradlew run
```

---

## Contributing

### Code Style

- **Google Java Style Guide** enforced via Checkstyle
- Max line length: 100 characters
- Indentation: 4 spaces (no tabs)
- Max method length: 150 lines
- Javadoc required for all public classes/methods

### Before Committing

```bash
# Run all checks
./gradlew clean check

# Ensure tests pass
./gradlew test

# Verify build succeeds
./gradlew build
```
