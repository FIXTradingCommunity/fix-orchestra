# XML Diff/Merge Utilities

## Overview

Official Orchestra files will be issued by the FIX Trading Community to represent standards and best practices. However, not all Orchestra files will come from the same source, and changes will be made on different lifecycles. Orchestra files will also be issued by firms to communicate their service offerings to their counterparties. Therefore, users will have a problem to track incremental changes and also to selectively apply changes to a base file. For example, a firm may wish to apply an incremental change issued as an Extension Pack to its own Orchestra file. The diff/merge utilities support those needs.

The utilities work on any XML files; they are not XML-schema aware.

### XML Patch Operations
These utilities make use of a difference format conformant to standard "An Extensible Markup Language (XML) Patch Operations Framework Utilizing XML Path Language (XPath) Selectors", [IETF RFC 5261](https://tools.ietf.org/html/rfc5261). Another benefit of these utilities, aside from editing Orchestra files, is that they can be used for HTTP PATCH operations with XML payloads.

## Difference

The XmlDiff utility compares two XML files and generates a third file that represents their differences in RFC 5261 format. Differences are encoded as additions, replacements, or removals of XML elements and attributes.

To run the difference utility, run this command line:

```
java io.fixprotocol.orchestra.xml.XmlDiff <in-file1> <in-file2> [output-file]
```
If the output file is not provided, then results go to the console.

## Merge

The XmlMerge utility takes a base XML file and a difference file and merges the two to produce an new XML file.

To run the merge utility, run this command line:

```
java io.fixprotocol.orchestra.xml.XmlMerge <base-xml-file> <diff-file> <output-xml-file>
```