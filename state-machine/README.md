# Module state-machine

This module is a code generator for state machines as defined by an Orchestra file.

## Features

For each state machine defined in an Orchestra file:

* Generates an enum of states
* Generates an enum of transitions for each state with transitions (non-final states)
* Generates a state machine class to invoke transition events on an object

Not yet implemented:

* Parameters to an activity, e.g. order fills for a filled or partially filled event
* Implementation of triggered activities such as sending a message as result of a state transtion. However, generic event consumer interfaces
are provided to hook in such actions.


## Build

The module may be included as a dependency in a Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>state-machine</artifactId>
  <version>1.4.0-RC5</version>
</dependency>
```