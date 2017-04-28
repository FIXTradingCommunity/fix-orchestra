# Module dsl-antlr
This module provides a grammar for the Score DSL in Orchestra. The DSL expresses conditions and rules about message elements and tells when responses are triggered in a workflow.

See documentation of the grammar in [Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec/tree/master/v1-0-RC2)

This module uses [ANTLR4 language framework](http://www.antlr.org/)

This module provides these features:
* The Score grammar expressed in ANTLR4 notation
* Generation of a lexer and parser that implement the grammar
* Implementation of a visitor that processes DSL expressions
* Interfaces and base implementations for symbol tables for variables and message elements

The module may be included as a dependency in a Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>dsl-antlr</artifactId>
  <version>1.0.0-RC2</version>
</dependency>
```
