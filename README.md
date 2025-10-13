# dbadvisor

A database advisor tool that provides recommendations for optimizing database and application queries.

## Modules
- `dbadvisor-core`: Core functionality for analyzing database queries and generating recommendations.
- `dbadvisor-h2`: H2 implementation.
- `dbadvisor-postgresql`: PostgreSQL implementation.
- `dbadvisor-hibernate`: Hibernate integration.

## Usage
To use the dbadvisor tool, include the desired modules in your project dependencies. 
Hibernate integration has the following configurations available that you can add to your configuration file:

```properties
# Enable or disable integration
dbadvisor.enabled=true

# Set advisors to be used (comma-separated)
dbadvisor.advisors=io.github.ivannavas.dbadvisor.core.advisors.IndexAdvisor,io.github.ivannavas.dbadvisor.core.advisors.SecurityAdvisor
```

## Current development
The project is currently under active development. Until the 1.0.0 release, the API may change without prior notice or deprecation warnings.
The objective at 1.0.0 is to have a useful set of advisors and a stable API.

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.