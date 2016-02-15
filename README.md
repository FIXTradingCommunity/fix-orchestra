# FIX Orchestra

FIX Orchestra is intended to provide a standard and some reference implementation for machine readable rules of engagement between counterparties. FIX Orchestra makes use of [semantic web](https://en.wikipedia.org/wiki/Semantic_Web) technologies.

## Status
This project is a **prototype**. Requirements are still being gathered and discussed by the FIX Orchestra working group. Participation in the working group is encouraged by FIX Trading Community members, and more broadly, feedback is welcome from interested GitHub users.

## Modules

### Session Configuration
This module allows counterparties to discover configurations of sessions between them. Direct support is supplied for FIXT sessions (the traditional FIX session layer) as well as FIXP, the FIX performance session layer. The prototype also demonstrates how session definitions could be extended to cover non-FIX protocols, such as NASDAQ OUCH.

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

