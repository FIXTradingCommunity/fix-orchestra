package io.fixprotocol.orchestra.interfaces;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;

class Input implements LSInput {

  private String baseUri;
  private boolean certifiedText = false;
  private String encoding;
  private InputStream inputStream;
  private String publicId;
  private String systemId;

  public Input(String publicId, String systemId, InputStream inputStream, String baseURI) {
    this.publicId = publicId;
    this.systemId = systemId;
    this.inputStream = new BufferedInputStream(inputStream);
    this.baseUri = baseURI;
  }

  @Override
  public String getBaseURI() {
    return this.baseUri;
  }

  @Override
  public InputStream getByteStream() {
    return inputStream;
  }

  @Override
  public boolean getCertifiedText() {
    return this.certifiedText;
  }

  @Override
  public Reader getCharacterStream() {
    return null;
  }

  @Override
  public String getEncoding() {
    return this.encoding;
  }

  @Override
  public String getPublicId() {
    return publicId;
  }

  @Override
  public String getStringData() {
    return null;
  }

  @Override
  public String getSystemId() {
    return systemId;
  }

  @Override
  public void setBaseURI(String baseURI) {
    this.baseUri = baseURI;
  }

  @Override
  public void setByteStream(InputStream byteStream) {
    this.inputStream = byteStream;
  }

  @Override
  public void setCertifiedText(boolean certifiedText) {
    this.certifiedText = certifiedText;
  }

  @Override
  public void setCharacterStream(Reader characterStream) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Override
  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  @Override
  public void setStringData(String stringData) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

}