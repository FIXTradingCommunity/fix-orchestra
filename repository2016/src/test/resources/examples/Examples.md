# FIX Orchestra Examples

## mit_2016.xml

This example shows an order message an several possible responses to it.
Some features to note:
* Use of `maxImplLength` attribute to set the maximum length of a field.
* A `rule` element to tell when a field is required.
* Use of `context` attribute to distinguish multiple uses of the same message type.

### Status
 
As of Release Candidate 1, this example is believed to be accurate for XML schema conformance. However, the conditional expressions are only suggestive. The DSL grammar
is expected to be completed in Release Candidate 2.

