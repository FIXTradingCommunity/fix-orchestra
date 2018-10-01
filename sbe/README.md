# SBE Schema  Generator

This utility generates a Simple Binary Encoding (SBE) message schema from an Orchestra file. See the [Simple Binary Encoding](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) standard and XML schema.

Two XSL Transforms are provided:

## SBE_datatypes.xslt

This transform inserts SBE datatype mappings into an existing Orchestra file. The mappings provided in the script are examples. You may wish to edit them before running the script.

For each FIX datatype, the script contains an element similar to the following snippet. The `mappedDatatype` element may contain any definition of an SBE type, consistent with its XML schema, including composite types.

```xml
<fixr:mappedDatatype standard="SBE">
  <sbe:type name="int" primitiveType="int32"/>
</fixr:mappedDatatype>
```

The script only updates the `<datatypes>` section. All other elements are simply passed through to the result.


## OrchestraToSBE.xslt

This transform converts an Orchestra file to an SBE message schema. Embedded datatype mappings are translated into a `<types>` element in the SBE schema.
The script converts each message definition in the input file to a template in the message schema. Multiple scenarios of the same message type are supported. In the resulting template, the template name is formed as the original name concatenated with the scenario name. 
## File Preparation

The input file must conform to the Orchestra XML Schema. Be sure to populate the metadata section with important identification.