package io.fixprotocol.orchestra.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

class CustomNamespaceContext implements NamespaceContext {
  private final Map<String, String> namespaces = new HashMap<>();

  public CustomNamespaceContext() {
    namespaces.put("xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    namespaces.put("xml", XMLConstants.XML_NS_URI);
    namespaces.put("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
  }
  
  public void populate(Document doc) {
    final Element baselineRoot = doc.getDocumentElement();
    final NamedNodeMap rootAttributes = baselineRoot.getAttributes();

    for (int i = 0; i < rootAttributes.getLength(); i++) {
      final Attr attr = (Attr) rootAttributes.item(i);
      final String prefix = attr.getPrefix();
      if ("xmlns".equals(prefix)) {
        register(attr.getLocalName(), attr.getValue());
      } else if ("xmlns".equals(attr.getLocalName())) {
        // default namespace
        register(XMLConstants.DEFAULT_NS_PREFIX, attr.getValue());
      }
    }
  }

  public String getNamespaceURI(String prefix) {
    if (prefix == null) {
      throw new NullPointerException("Null prefix");
    }
    String uri = namespaces.get(prefix);
    if (uri != null) {
      return uri;
    } else {
      return XMLConstants.NULL_NS_URI;
    }
  }

  // This method isn't necessary for XPath processing.
  public String getPrefix(String uri) {
    throw new UnsupportedOperationException();
  }

  // This method isn't necessary for XPath processing either.
  public Iterator<?> getPrefixes(String uri) {
    throw new UnsupportedOperationException();
  }

  public void register(String prefix, String uri) {
    namespaces.put(prefix, uri);
  }

}