package io.fixprotocol.orchestra.interfaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class InterfacesValidatorTest  {
  
  InterfacesValidator validator;
  
  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @BeforeEach
  public void setUp() throws Exception {
    validator = new InterfacesValidator();
  }

  @Test
  public void testValidate() throws FileNotFoundException {
    InputStream inputStream = new FileInputStream("src/test/resources/interfaceswitherrors.xml");
    OutputStream jsonOutputStream = new FileOutputStream("target/test/interfaceswitherrors.json");
    validator.validate(inputStream, jsonOutputStream, false);
  }

}
