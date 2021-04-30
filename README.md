![](FIXorchestraLogo.png)

# FIX Orchestra Resources

This project contains resources and sample code for FIX Orchestra version 1.0. Technical specifications for FIX Orchestra are in project [fix-orchestra-spec](https://github.com/FIXTradingCommunity/fix-orchestra-spec).

FIX Orchestra is intended to provide a standard and some reference implementation for *machine readable rules of engagement* between counterparties. The goal is to reduce the time to get counterparties trading, and improve accuracy of implementations.

### Tutorials

See [Orchestra tutorials](https://github.com/FIXTradingCommunity/fix-orchestra/wiki) and FAQ.

## References

[Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec) specifications for Orchestra in GitHub.

[Orchestrations](https://github.com/FIXTradingCommunity/orchestrations) public Orchestra files for service offerings

[FIX Standards](https://www.fixtrading.org/standards/) normative standards documents  

## Standards Versions

### Version 1.0 Technical Standard

Orchestra version 1.0 Draft Standard was promoted to Technical Standard with minor enhancements and corrections on February 17, 2021. Version 1.6.10 of this project conforms to Orchestra version 1.0 Techncial Standard.

## Normative Modules
The following modules are **normative**.

### repository
This module contains an XML schema for Orchestra. It is used to convey message structures and their components, as well as FIX application behaviors. Users may express workflow as responses to messages under different scenarios, as well as external state information that may influence behaviors.

In addition to providing the XML schema as a resource, the module builds Java bindings for the schema. 

### interfaces

This module provides an XML schema for service offerings, protocols and session provisioning. In addition to providing the XML schema as a resource, the module builds Java bindings for the schema. Service elements may link to Orchestra files composed in the `repository` schema.

### dsl-antlr - Score Domain Specific Language (DSL)

An orchestra file may contain conditional expressions to describe conditionally required fields and tell when a certain response to a message applies. Also, the DSL may be used for assignment expressions to set message fields and external state variables.

The Score grammar is provided in the notation of ANTLR4, and the project builds a lexer/parser for the grammar. This project generates Java code, but ANTLR4 is capable of generating several other programming languages from the same grammar.

## Informational Modules and Utilities

### repository2010
Repository 2010 Edition was the version of the FIX Repository prior to FIX Orchestra. This module provides a parser for its XML schema. It may be used to process existing Repository files and to convert their message structures to Orchestra format.

### orchestra2doc
This utility generates documentation for an Orchestra file that can be view in any web browser. The output of the generator may be used locally or from a web server.

### repository-util

This module contains RepositoryValidator to validate that a file conforms to the `repository` schema. Also, RepositoryCompressor extracts slices of a repository file. An XSLT is provided to translate existing Repository 2010 Edition files to the Orchestra schema.

### interfaces-util

This module contains InterfacesValidator to validate that a file conforms to the `interfaces` schema.

## Experimental Modules

Experimental modules have been moved to GitHub repository [fix-orchestra-experimental](https://github.com/FIXTradingCommunity/fix-orchestra-experimental)

This following modules are **experimental**. Requirements are still being gathered and discussed by the FIX Orchestra working group. Participation in the working group is encouraged by FIX Trading Community members, and more broadly, feedback is welcome from interested GitHub users.

### state-machine
A demonstration of state machine code generation from an Orchestra file.

### testgen
This module is a demonstration of acceptance test generation from an Orchestra file using Behavior Driven Design (BDD) concepts.

### Code Generation for Encoding

#### orchestra2sbe

This utility has been moved to repository [FIXTradingCommunity/fix-sbe-utilities](https://github.com/FIXTradingCommunity/fix-sbe-utilities).
It creates a [Simple Binary Encoding](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) message schema from an Orchestra file.

#### fix-orchestra-protobuf
See repository [FIXTradingCommunity/fix-orchestra-protobuf](https://github.com/FIXTradingCommunity/fix-orchestra-protobuf) for utilities to generate
Google Protocol Buffers schemas from an Orchestra file.

### FIX Engine Provisioning

Modules specific to QuickFIX have been moved to repository [fix-orchestra-quickfix](https://github.com/FIXTradingCommunity/fix-orchestra-quickfix). 
Some models in this project are intended to be operational while others are proofs of concept.

### repository-quickfix

This module generates a QuickFIX data dictionary from an Orchestra file. The format can be consumed by the C++, Java and .NET versions. Additionally, the module generates message classes for QuickFIX/J directly from an Orchestra file. Although the QuickFIX data dictionary format is not as richly featured as Orchestra, it is hoped that this utility will help with Orchestra adoption. 

### model-quickfix
This module generates code that is conformant to the QuickFIX/J API for validating and populating messages. It is dependent on `repository-quickfix`.

### session-quickfix
A demonstration of session configuration for QuickFIX open-source FIX engine. It consumes an XML file in the `interfaces` schema.

## License
© Copyright 2016-2021 FIX Protocol Limited

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


### Data Files
Data files in this project under `test/resources` are strictly for testing and to serve as examples for format. They are non-normative for FIX standards and may not be up to date.

## Build
The project is built with Maven version 3.3 or later. 

The project requires Java 11 or later. Some modules use Java Platform Module System (JPMS).


