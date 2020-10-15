package io.fixprotocol.orchestra.interfaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.fixprotocol.orchestra.event.EventListener;

public class InterfacesValidatorTest  {
  
  private InterfacesValidator validator;
  private EventListener eventLogger;
  
  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @BeforeEach
  public void setUp() throws Exception {
    eventLogger = InterfacesValidator.createLogger(new FileOutputStream("target/test/interfacesvalidator.json"));
    validator = new InterfacesValidator(eventLogger);
  }
  
  @AfterEach
  public void cleanUp() throws Exception {
    eventLogger.close();
  }

  @Test
  public void testValidate() throws FileNotFoundException {
    InputStream inputStream = new FileInputStream("src/test/resources/interfaceswitherrors.xml");
    validator.validate(inputStream);
  }

}
