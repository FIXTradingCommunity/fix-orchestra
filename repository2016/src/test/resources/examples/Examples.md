# FIX Orchestra Examples

## mit_2016.xml

This example shows an order message an several possible responses to it.
Some features to note:
* Use of `implMaxLength` attribute to set the maximum length of a field. See field "ClOrdLinkID" in message "NewOrderSingle", for example.
* A `rule` element to tell when a field is required. See rule "StopOrderRequiresStopPx" that tells when field "StopPx" is required.
* Use of `scenario` attribute to distinguish multiple uses of the same message type. For example, context="rejected" indicates the scenario for an ExecutionReport as rejection, as opposed to an accepted order. The message structure of  a "rejected" ExecutionReport extends its "base" scenario.
* Restricting the number of entries in a repeating group with `implMinOccurs` and `implMaxOccurs`. See the Parties group reference in the order message.
* Using the `supported` attribute to tell when standard codes are supported or forbidden by an implementation. See "TimeInForce" in the order message.

### Status

March 29, 2017

The XML file was updated to the XML schema as of Release Candidate 2, and the conditional expressions conform to the DSL grammar.

