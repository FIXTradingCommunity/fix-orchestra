# Module repository

This module provides an XML schema for message structures and workflow. Users may express workflow as responses to messages under different scenarios, as well as external state information that may influence behaviors.

See documentation of the schema in [Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec/tree/master/v1-0-DRAFT)

The XML namespace for the schema is `http://fixprotocol.io/2022/orchestra/repository`.

## Project Features

In addition to providing the XML schema as a resource, this module builds Java bindings for the schema. In other words, an application can access or manipulate an Orchestra repository using plain old Java objects (POJO) without having to be concerned with XML node details.

## Build

This Maven module builds a multi-release jar (MRJAR) so it will run with either Java 8, prior to the introduction of the Java Package Module System (JPMS), or with modules in Java 11 or later. Building the MRJAR requires the presence of two Java Development Kits (JDK), but usage of it only requires a Java runtime version of choice.

### Java module

For use with Java 11 or later, the name of the produced Java module is `orchestra.repository`. See module-info.java for the full specification of dependencies and exposed packages.

### Maven dependency

This Maven module may be included as a dependency in a project as follows (substitute current version as needed):

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>repository</artifactId>
  <version>1.8.0</version>
</dependency>
```
