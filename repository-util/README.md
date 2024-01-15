# Module repository-util

This module provides utilities to help with the Orchestra Repository schema.

## Project Features

This module provides these applications:
* RepositoryValidator validates that an XML file conforns to the Orchestra Repository schema.
* RepositoryCompressor creates extracts from an Orchestra file by section or category.
* Unified2OrchestraTransformer (unified2orchestra.xslt) populates an Orchestra file from an existing Unified Repository 2010 Edition file.
* Transform enrich_datatypes.xslt adds datatype mappings to an Orchestra file.

### Running RepositoryValidator

RepositoryValidator supports two validation styles:
* Basic validation (the default) merely ensures that an Orchestra file conforms to the Repository XML schema.
* FIX style validation additionally checks contents for conformance to FIX style rules. This is invoked with command line argument `-s FIX`.

Other validations may be added in the future.

RepositoryValidator requires one command line argument, the name of an Orchestra Repository file to validate. Output goes to the console by default
but may be directed to a JSON event file suitable for rendering.

Command line arguments:
```
usage: RepositoryValidator [options] <input-file>
 -?,--help             display usage
 -e,--eventlog <arg>   path of JSON event file
 -s,--style <arg>      validation style
 ```

### Running RepositoryCompressor

RepositoryCompressor extracts a subset of an existing Orchestra file.

Command line arguments:
```
usage: RepositoryCompressor
-?,--help display usage
-c,--category <arg> select messages by category
-f,--flow <arg> select messages by flow
-i,--input <arg> path of input file
-n,--notcategory <arg> select messages except category
-o,--output <arg> path of output file
-s,--section <arg> select messages by section
-x,--notsection <arg> select messages except section
```

## Build

This Maven module builds a multi-release jar (MRJAR) so it will run with either Java 8, prior to the introduction of the Java Package Module System (JPMS), or with modules in Java 11 or later. Building the MRJAR requires the presence of two Java Development Kits (JDK), but usage of it only requires a Java runtime version of choice.

### Java module

For use with Java 11 or later, the name of the produced Java module is `orchestra.repository.util`. See module-info.java for the full specification of dependencies and exposed packages.

### Maven dependency

This Maven module may be included as a dependency in a project as follows (substitute current version as needed):

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>repository-util</artifactId>
  <version>1.8.0</version>
</dependency>
```
