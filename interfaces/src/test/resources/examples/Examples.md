# FIX Orchestra Examples

## SampleInterfaces.xml

This example shows service offering definitions and session configurations.

Some features to note:

* Three interfaces named Private, Public, and OrderRouting are configured.
* The Private interface references three orchestrations, which would be Orchestra files in the `repository2016` schema. Orchestration files can either be local files or accessible through a web interface.
* Private interface defines a protocol stack with "orderRouting" application layer, a FIXatdl user interface for algorithm controls, and session layer, and a transport.
* One session is configured for the Private interface. The configuration includes identifiers, and two transport addresses.
* Session "XYZ-ABC" has an activation time. This supports sending a new or changed interfaces file in advance of the session being authorized.
* The Public interface shows how a multicast is specified.
* Session "OR1" shows an example of security keys, including public certificates and a private key. They are encoded in RFC 7468 format.
