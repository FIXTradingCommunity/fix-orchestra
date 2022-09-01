package io.fixprotocol.orchestra.repository;

import java.io.File;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class FixRepositoryValidatorTest {


  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @Test
  public void testValidateWithErrors() {
    RepositoryValidator
        .main(new String[] {"-s", "FIX", "-e", "src/test/resources/repositorywitherrors.json",
            "src/test/resources/repositorywitherrors.xml"});
  }

  @Disabled
  @Test
  public void testValidate() {
    RepositoryValidator.main(new String[] {"-s", "FIX", "-e",
        "src/test/resources/OrchestraFIXLatest.json", "src/test/resources/OrchestraFIXLatest.xml"});
  }

}
