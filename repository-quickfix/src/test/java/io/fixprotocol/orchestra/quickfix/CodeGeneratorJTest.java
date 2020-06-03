package io.fixprotocol.orchestra.quickfix;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CodeGeneratorJTest {

  private CodeGeneratorJ generator;

  @BeforeEach
  public void setUp() throws Exception {
    generator = new CodeGeneratorJ();
  }

  @Test
  public void testGenerate() throws IOException {
    generator.generate(
        Thread.currentThread().getContextClassLoader().getResource("mit_compressed.xml").openStream(),
        new File("target/spec/generated-sources"));
  }

}
