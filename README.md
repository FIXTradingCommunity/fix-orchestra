# FIX Orchestra Version 1.0 Release Candidate 1

FIX Orchestra is intended to provide a standard and some reference implementation for *machine readable rules of engagement* between counterparties.
The goal is to reduce the time to get counterparties trading, and improve accuracy of implementations.

## Status
Release Candidate 1 is the first of several planned preliminary releases. It is being published to gather feedback from the community, and is subject to
future corrections and enhancements. When the project is complete and publicly reviewed, the last Release Candidate will be promoted to Draft Standard
version 1.0.

## Normative Module

#### Repository2016
The **normative** part of FIX Orchestra RC1 is an XML schema, also known as FIX Repository 2016 Edition. It is used to convey message structures and their
components, as well as FIX application behaviors. Users may express workflow as responses to messages under different scenarios, as well as external
state information that may influence behaviors.

## Prototype Module
The following module should be considered a **prototype**, and is expected to be finalized in Release Candidate 2.

### Domain Specific Language (DSL)
An orchestra file may contain conditional expressions to describe conditionally required fields and tell when a certain
response to a message applies. Also, the DSL may be used for assignment expressions to set external state variables.
Module dsl-antlr contains a grammar using the notation of ANTLR4 and builds a lexer/parser for the grammar.

## Experimental Modules
This following modules are **experimental**. Requirements are still being gathered and discussed by the FIX Orchestra working group. Participation in the working group is encouraged by FIX Trading Community members, and more broadly, feedback is welcome from interested GitHub users.

### Session Configuration
This module allows counterparties to discover configurations of sessions between them. Direct support is supplied for FIXT sessions (the traditional FIX session layer) as well as FIXP, the FIX performance session layer. The prototype also demonstrates how session definitions could be extended to cover non-FIX protocols.

#### Session Configuration for QuickFIX
A demonstration of session configuration for QuickFIX/J open-source FIX engine. A module like
this needs to be developed for each FIX engine that uses a proprietary configuration format.

### Messages
Exposes message structures to discovery via FIX Orchestra through the use of [semantic web](https://en.wikipedia.org/wiki/Semantic_Web) technologies.

#### Repository2010
Parses FIX Repository 2010 Edition to import its message structures.

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

## Prerequisites
This project requires Java 8. It should run on any platform for which the JVM is supported.

## Build
The project is built with Maven. 

