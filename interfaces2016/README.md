# Module interfaces2016
This module provides an XML schema for service offerings, protocols and session provisioning. Service elements may link to Orchestra files composed in the `repository2016` schema.

See documentation of the schema in [Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec/tree/master/v1-0-RC2)

The XML namespace for the schema is `http://fixprotocol.io/2016/fixinterfaces`.

## Build

In addition to providing the XML schema as a resource, this module builds Java bindings for the schema. The module may be included as a dependency in a Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>interfaces2016</artifactId>
  <version>1.2.0-RC3</version>
</dependency>
```
