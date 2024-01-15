# Module dsl-antlr
This module provides a grammar for the Score Domain Specific Language (DSL) in Orchestra. The DSL expresses conditions and rules about message elements and tells when responses are triggered in a workflow. Also, expressions can be written to assign values to field in outgoing messages or state variables.

See documentation of the grammar in [Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec/tree/master/v1-0-STANDARD)

This module uses the [ANTLR4 language framework](http://www.antlr.org/)

This module provides these features:
* The Score grammar expressed in ANTLR4 notation
* Generation of a lexer and parser that implement the grammar
* Implementation of a visitor that processes DSL expressions
* Interfaces and base implementations for symbol tables for variables and message elements

## Build

The module may be included as a dependency in a Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>dsl-antlr</artifactId>
  <version>1.8.0</version>
</dependency>
```
