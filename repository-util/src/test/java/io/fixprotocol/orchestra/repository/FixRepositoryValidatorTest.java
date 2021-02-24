package io.fixprotocol.orchestra.repository;

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

public class FixRepositoryValidatorTest {

  private FixRepositoryValidator validator;
  private io.fixprotocol.orchestra.event.EventListener eventLogger;

  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @BeforeEach
  public void setUp() throws Exception {
    final OutputStream jsonOutputStream =
        new FileOutputStream("target/test/repositoryvalidator.json");
    eventLogger = FixRepositoryValidator.createLogger(jsonOutputStream);
    validator = new FixRepositoryValidator(eventLogger);
  }

  @AfterEach
  public void cleanUp() throws Exception {
    eventLogger.close();
  }

  @Test
  public void testValidateWithErrors() throws FileNotFoundException {
    InputStream inputStream = new FileInputStream("src/test/resources/repositorywitherrors.xml");
    validator.validate(inputStream);
  }

  @Test
  public void testValidate() throws FileNotFoundException {
    InputStream inputStream = new FileInputStream("src/test/resources/OrchestraFIXLatest.xml");
    validator.validate(inputStream);
  }

}
