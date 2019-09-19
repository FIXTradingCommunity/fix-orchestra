# Module repository2016
This module provides an XML schema for message structures and workflow. The message structure feature is also known as FIX Repository 2016 Edition. Users may express workflow as responses to messages under different scenarios, as well as external state information that may influence behaviors.

See documentation of the schema in [Orchestra specifications](https://github.com/FIXTradingCommunity/fix-orchestra-spec/tree/master/v1-0-RC2)

The XML namespace for the schema is `http://fixprotocol.io/2016/fixrepository`.

## Project Features

In addition to providing the XML schema as a resource, this module provides these features:
* Builds Java bindings for the schema. 
* Transform Repository2010to2016.xsl populates a Repository 2016 Edition file from an existing Repository 2010 Edition file.
* Transform enrich_datatypes.xslt adds datatype mappings to an Orchestra file.

## Build

The module may be included as a dependency in a Maven project as follows:

```xml
<dependency>
  <groupId>io.fixprotocol.orchestra</groupId>
  <artifactId>repository2016</artifactId>
  <version>1.4.0-RC5</version>
</dependency>
```
