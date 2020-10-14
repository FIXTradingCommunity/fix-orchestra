package io.fixprotocol.orchestra.repository;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

class ResourceResolver implements LSResourceResolver {

  private URL baseUrl;

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
      String baseURI) {
    InputStream resourceAsStream = null;

    final int filePos = systemId.lastIndexOf('/') + 1;
    final String filePath = baseUrl.getPath() + "/" + systemId.substring(filePos);
    try {
      final URL fileUrl = new URL(baseUrl.getProtocol(), baseUrl.getHost(), filePath);
      resourceAsStream = fileUrl.openStream();
    } catch (final IOException e) {
      // Not a fatal error for schema validation
    }
    return new Input(publicId, systemId, resourceAsStream, baseURI);
  }


  public void setBaseUrl(URL baseUrl) {
    this.baseUrl = baseUrl;
  }

}
