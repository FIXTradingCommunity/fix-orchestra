package io.fixprotocol.orchestra.quickfix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class CodeGeneratorJTest {

  private CodeGeneratorJ generator;

  @Before
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
