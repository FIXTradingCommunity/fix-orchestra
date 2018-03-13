# FIX Orchestra Resources

This project contains resources and sample code for FIX Orchestra and FIX Repository 2016 Edition. Technical specifications for FIX Orchestra are in project [fix-orchestra-spec](https://github.com/FIXTradingCommunity/fix-orchestra-spec).

FIX Orchestra is intended to provide a standard and some reference implementation for *machine readable rules of engagement* between counterparties. The goal is to reduce the time to get counterparties trading, and improve accuracy of implementations.

### Planned Lifecycle

The planned lifecycle of this project is to roll out new features in a series of release candidates. After each release candidate is approved, it will be exposed to public review.  When version 1 is considered complete, the last release candidate will be promoted to Draft Standard.

### Participation

Issues may be entered here in GitHub or in a discussion forum on the [FIX Trading Community site](http://www.fixtradingcommunity.org/). In GitHub, anyone may enter issues or pull requests for the next release candidate. 

### References
Specifications for Orchestra are available in GitHub.

[Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec)

Public Orchestra files for service offerings

[Orchestrations](https://github.com/FIXTradingCommunity/orchestrations)

## Versions

### Current approved version: Release Candidate 3
Release Candidate 2 was approved by the Global Technical Committee on March 8, 2018 for 90 day public review.
Themes:
* Semantic concepts
* Additional demonstration applications: documentation generator, test generator
* Refinement of previous deliverables

### Release Candidate 2
Release Candidate 2 was approved by the Global Technical Committee on May 18, 2017 for 90 day public review. The themes for Release Candidate 2 were:
* Completion of a DSL grammar for conditional expressions
* FIXatdl integration
* Session configuration

### Release Candidate 1
Version 1.0 RC1 standardized the XML schema for FIX Orchestra and FIX Repository 2016 Edition. Release Candidate 1 was approved by the Global Technical Committee on Dec. 15, 2016 for 90 day public review. 


## Normative Modules
The following modules are **normative** for Orchestra Release Candidate 2.

### repository2016
This module contains an XML schema for Orchestra, also known as FIX Repository 2016 Edition. It is used to convey message structures and their components, as well as FIX application behaviors. Users may express workflow as responses to messages under different scenarios, as well as external state information that may influence behaviors.

In addition to providing the XML schema as a resource, the module builds Java bindings for the schema. An XSLT is provided to translate existing Repository 2010 Edition files to the 2016 Edition schema

### interfaces2016

This module provides an XML schema for service offerings, protocols and session provisioning. In addition to providing the XML schema as a resource, the module builds Java bindings for the schema. Service elements may link to Orchestra files composed in the `repository2016` schema.


### dsl-antlr - Score Domain Specific Language (DSL)

An orchestra file may contain conditional expressions to describe conditionally required fields and tell when a certain response to a message applies. Also, the DSL may be used for assignment expressions to set message fields and external state variables.

The Score grammar is provided in the notation of ANTLR4, and the project builds a lexer/parser for the grammar.

## Informational Modules

### repository2010
Repository 2010 Edition was the version of the FIX Repository prior to FIX Orchestra. This module provides a parser for its XML schema. It may be used to process existing Repository files and to convert their message structures to Orchestra format.

### repositoryDiffMerge
These utilities extract incremental changes to XML files and selectively apply changes to a base file. The difference format is conformant to standard [IETF RFC 5261](https://tools.ietf.org/html/rfc5261). A benefit of these utilities, aside from editing Orchestra files, is that they can be used for HTTP PATCH operations with Orchestra XML payloads.

### docgen
This utility generates documentation for an Orchestra file that can be view in any web browser. The output of the generator may be used locally or from a web server.

### testgen
This module is a demonstration of acceptance test generation from an Orchestra file using Behavior Driven Design (BDD) concepts.

## Experimental Modules
This following modules are **experimental**. Requirements are still being gathered and discussed by the FIX Orchestra working group. Participation in the working group is encouraged by FIX Trading Community members, and more broadly, feedback is welcome from interested GitHub users.

### FIX Engine Provisioning

#### repository-quickfix

This module generates a QuickFIX data dictionary from a FIX Repository 2016 or Orchestra file. The format can be consumed by the C++, Java and .NET versions. Additionally, the module generates message classes for QuickFIX/J directly from an Orchestra file. Although the QuickFIX data dictionary format is not as richly featured as Orchestra, it is hoped that this utility will help with Orchestra adoption. In future, message validation will be able to take advantage of conditional expressions in Orchestra.

#### model-quickfix
This module generates code that is conformant to the QuickFIX/J API for validating and populating messages. It is dependent on `repository-quickfix`.

#### session-quickfix
A demonstration of session configuration for QuickFIX open-source FIX engine. It consumes an XML file in the `interfaces2016` schema.

A module like this needs to be developed to support each FIX engine that uses a proprietary configuration format. The demonstration provides an example to follow for that work.

### Data Files
Data files in this project under `test/resources` are strictly for testing and to serve as examples for format. They are non-normative for FIX standards and may not be up to date.

See [FIX Standards](https://www.fixtrading.org/standards/) for normative standards documents and [FIX Repository](https://www.fixtrading.org/standards/fix-repository/) for latest Repository extension packs.

## License
© Copyright 2016-2018 FIX Protocol Limited

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

