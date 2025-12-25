# 1.4.0

- build.gradle reorganize and make more consistent
- Rename some classes to be more general
- Update changelog script
- Add version.sh
- Logging standardization and track event counts in messages

# 1.3.0

- Refactor to a more modular project and class structure and include tests
- Ensure gradle dependencies are updated to the latest
- EventScraper implements AutoCloseable
- Move hardcoded strings to constants
- Fail fast by requireNonNull for arguments expected to be non-null
- Don't use instanceof
- Clean up jib configuration and remove old RaleighEventsRSSGenerator

# 1.2.0

- Add checks for cyclomatic complexity and npathcomplexity; address findings
- Log feed statistics (count, last date)
- RSS commits include number of events
- Refine logging to include existing, new events at INFO level
- More flexible end page number parsing
- Default to harvesting 30 days into the future, instead of one week, which is configurable using the env DAYS_INTO_FUTURE
- Automatically filter events older than DROP_EVENTS_OLDER_THAN_DAYS (default 30 days) from the RSS feed

# 1.1.0

- Move to package visitraleigh.events
- Migrate to using a SLF4J logger
- Address 'XML parsers should not be vulnerable to XXE attacks' and trivial SonarQube findings
- Reduce cognitive complexity of scrapeEvents(), parseEventFromLink()
- Add checkstyle and spotbugs to project
- Address checkstyle findings
- Update run.sh for logging

# 1.0.1

- Fix description formatting
- Always pull image for version
- Fix additional whitespace in output RSS
- Generate change log
