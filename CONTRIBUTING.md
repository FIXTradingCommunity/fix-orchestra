# Contributors

Contributions to the fix-orchestra project are welcome. They must attributed to an issue and be consistent with the project roadmap. They should be submitted as pull request.  

They must conform to code style standards. All contributed implementation code must be accompanied by unit tests that demonstrate the proposed enhancement or defect resolution.

## Code Style
The style standard for this project is [Google Java Style](http://google.github.io/styleguide/javaguide.html)

Code formatters for popular IDEs are available in project [google/styleguide](https://github.com/google/styleguide)

The **Checkstyle** tool has a profile for Google style. 

### Fluent Interface

Classes with many attributes should follow the [Fluent Interface](http://www.martinfowler.com/bliki/FluentInterface.html) style.

### Dependency Injection

No dependency injection framework is used. Rather, when many attributes must be set to form a viable object, use a dedicated **Builder** with fluent interface and inject the Builder into the object's constructor.

### Generated Code

Code should be generated in the `generate-sources` phase of the Maven build into folder `target/generated-sources`.
