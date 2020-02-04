package io.fixprotocol.orchestra.transformers;

import org.jvnet.jaxb2_commons.lang.JAXBCopyStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;

class AttributeCopyStrategy extends JAXBCopyStrategy {

  public static final AttributeCopyStrategy INSTANCE = new AttributeCopyStrategy();

  @Override
  protected Object copyInternal(ObjectLocator locator, Object object) {
    if (object instanceof String || object instanceof Number || object instanceof Boolean) {
      return object;
    } else {
      return null;
    }
  }

}

