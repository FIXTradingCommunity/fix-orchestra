package io.fixprotocol.orchestra.quickfix;

import java.io.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DataDictionaryGeneratorTest {

  private DataDictionaryGenerator generator;

  @BeforeEach
  public void setUp() throws Exception {
    generator = new DataDictionaryGenerator();
  }

  @Test
  public void testGenerate() throws Exception {
    generator.generate(
        Thread.currentThread().getContextClassLoader().getResource("mit_compressed.xml").openStream(),
        new File("target/spec"));
  }

}
