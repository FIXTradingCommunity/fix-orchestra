Orchestra Test Generator
-------------------------
This utility generates an acceptance test from an Orchestra file. The generator produces artifacts for the [Cucumber](https://cucumber.io/), a Behavior Driven Development (BDD) framework. Generated code drives QuickFIX/J to send and receive messages to implement test plans.

This project is intended to be a demonstration. Currently, the generated tests are rudimentary, but they can serve as a starting point for firms to adapt it their FIX engines and extend its capabilities. 

## Usage

To run the documentation generator, enter this command line:

```
java -jar testgen-1.2.0-RC3-SNAPSHOT-jar-with-dependencies <filename> [resources-dir] [source-dir]
```

### Command line arguments
1. Name of Orchestra input file in Repository 2016 Edition format
2. Name of resources output directory--will be created if it does not exist, defaults to `src/test/resources`
3. Name of source output directory--will be created if it does not exist, defaults to `src/test/java`

## Generated artifacts

### Resources
* A QuickFIX data dictionary based on message structures in the Orchestra file
* A Cucumber feature file for each actor in the Orchestra file. A feature file is a text file that describes system behavior in language that is readable by non-technical users.

### Generated code
The project delivers code that drives steps of a test and validates received messages according to conditions in the feature file.

