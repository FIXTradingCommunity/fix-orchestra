# FIX Orchestra Resources

This project contains resources and sample code for FIX Orchestra and FIX Repository 2016 Edition. Technical specifications for FIX Orchestra are in project [fix-orchestra-spec](https://github.com/FIXTradingCommunity/fix-orchestra-spec).

FIX Orchestra is intended to provide a standard and some reference implementation for *machine readable rules of engagement* between counterparties. The goal is to reduce the time to get counterparties trading, and improve accuracy of implementations.

## Status

### Planned Lifecycle

The planned lifecycle of this project is to roll out new features in a series of release candidates. After each release candidate is approved, it will be exposed to public review. Issues may be entered here in GitHub or in a discussion forum on the [FIX Trading Community site](http://www.fixtradingcommunity.org/). When version 1.0 is considered complete, the last release candidate will be promoted to Draft Standard.

### Approved version: 1.0 Release Candidate 1
Version 1.0 RC1 standardizes the XML schema for FIX Orchestra and FIX Repository 2016 Edition. Release Candidate 1 was approved by the Global Technical Committee on Dec. 15, 2016 for 90 day public review. The status of each module is listed below for the current release candidate.

### Working version: 1.0 Release Candidate 2
The FIX Orchestra working group will consider issues and pull requests for the next release candidate. The planned themes for Release Candidate 2 are:
* Completion of a DSL grammar for conditional expressions
* FIXatdl integration
* Session configuration


## Normative Module

### Repository2016
The **normative** part of FIX Orchestra RC1 is an XML schema, also known as FIX Repository 2016 Edition. It is used to convey message structures and their
components, as well as FIX application behaviors. Users may express workflow as responses to messages under different scenarios, as well as external
state information that may influence behaviors.

In addition to providing the XML schema as a resource, the module builds Java bindings for the schema. An XSLT is provided to translate existing Repository 2010 Edition files to the 2016 Edition schema

The module may be included as a dependency in an application Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>repository2016</artifactId>
  <version>1.0.0-RC1</version>
</dependency>
```

### Repository2010
Repository 2010 Edition was the version of the FIX Repository prior to FIX Orchestra. This module provides a parser for its XML schema. It may be used to process existing Repository files and to convert their message structures to Orchestra format.

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>repository2010</artifactId>
  <version>1.0.0-RC1</version>
</dependency>
```

## Prototype Modules
The following modules should be considered **prototypes**, and are expected to be finalized in Release Candidate 2.

### Domain Specific Language (DSL)
An orchestra file may contain conditional expressions to describe conditionally required fields and tell when a certain
response to a message applies. Also, the DSL may be used for assignment expressions to set external state variables.
Module `dsl-antlr` contains a grammar using the notation of ANTLR4 and builds a lexer/parser for the grammar.

### Conversion to Semantic Web
The `repositoryToOWL` module translates a FIX Repository or Orchestra XML file to Web Ontology Language (OWL), a semantic notation. As compared to XML, OWL is more expressive, can support web endpoints for programmatic queries and user interfaces, and can be used to express equivalencies between FIX and other protocols.

## Experimental Modules
This following modules are **experimental**. Requirements are still being gathered and discussed by the FIX Orchestra working group. Participation in the working group is encouraged by FIX Trading Community members, and more broadly, feedback is welcome from interested GitHub users.

### FIX Engine Provisioning

#### QuickFIX Data Dictionary and Code Generator

This module generates a QuickFIX data dictionary from a FIX Repository 2016 or Orchestra file. The format can be consumed by the C++, Java and .NET versions. Additionally, the module generates message classes for QuickFIX/J directly from an Orchestra file. Although the QuickFIX data dictionary format is not as richly featured as Orchestra, it is hoped that this utility will help with Orchestra adoption. In future, message validation will be able to take advantage of conditional expressions in Orchestra.

#### Session Configuration
This module allows counterparties to discover configurations of sessions between them. Direct support is supplied for FIXT sessions (the traditional FIX session layer) as well as FIXP, the FIX performance session layer. The prototype also demonstrates how session definitions could be extended to cover non-FIX protocols.

#### Session Configuration for QuickFIX
A demonstration of session configuration for QuickFIX open-source FIX engine. A module like
this needs to be developed for each FIX engine that uses a proprietary configuration format.


## License
© Copyright 2016 FIX Protocol Limited

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

### Technical Specifications License

Note that the related Technical Specifications project has a different license than these resources. See [fix-orchestra-spec](https://github.com/FIXTradingCommunity/fix-orchestra-spec/blob/master/LICENSE)

## Prerequisites
This project requires Java 8 or later. It should run on any platform for which the JVM is supported.

## Build
The project is built with Maven version 3.0 or later. 

