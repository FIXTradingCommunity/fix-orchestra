package io.fixprotocol.orchestra.quickfix;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

public class DataDictionaryGeneratorTest {

  private DataDictionaryGenerator generator;

  @Before
  public void setUp() throws Exception {
    generator = new DataDictionaryGenerator();
  }

  @Test
  public void testGenerate() throws IOException, JAXBException {
    generator.generate(
        Thread.currentThread().getContextClassLoader().getResource("mit_compressed.xml").openStream(),
        new File("target/spec"));
  }

}
