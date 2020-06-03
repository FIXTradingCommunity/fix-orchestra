package io.fixprotocol.orchestra.transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.spi.LoggerContextFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import io.fixprotocol.orchestra.repository.RepositoryValidator;

class CustomLogFactory implements LoggerContextFactory {
  private final org.apache.logging.log4j.spi.LoggerContext ctx;

  CustomLogFactory() {
    final ConfigurationBuilder<BuiltConfiguration> builder =
        ConfigurationBuilderFactory.newConfigurationBuilder();
    builder.setStatusLevel(Level.WARN);
    final AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
        .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
        .add(builder.newLayout("PatternLayout").addAttribute("pattern",
            "%date %-5level: %msg%n%throwable"));
    builder.add(appenderBuilder);
    builder.add(builder.newRootLogger(Level.INFO).add(builder.newAppenderRef("Stdout")));
    ctx = Configurator.initialize(builder.build());
  }

  @Override
  public org.apache.logging.log4j.spi.LoggerContext getContext(String fqcn, ClassLoader loader,
      Object externalContext, boolean currentContext) {
    return ctx;
  }

  @Override
  public org.apache.logging.log4j.spi.LoggerContext getContext(String fqcn, ClassLoader loader,
      Object externalContext, boolean currentContext, URI configLocation, String name) {
    return ctx;
  }

  @Override
  public void removeContext(org.apache.logging.log4j.spi.LoggerContext context) {

  }
}

public class RepositoryCompressorTest {

  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
    LogManager.setFactory(new CustomLogFactory());
  }

  @Test
  public void validate()
      throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("mit_2016.xml")).build();
    validator.validate();
  }

  @Test
  public void tradeCaptureCategory() throws Exception {
    final String outfile = "target/test/tradecapture.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/FixRepository50SP2EP247.xml",
        "-o", outfile, "--category", "TradeCapture"});
    Assertions.assertTrue(new File(outfile).exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(new File(outfile))).build();
    validator.validate();
  }

  @Test
  public void sectionExceptCategory() throws Exception {
    final String outfile = "target/test/tradeX.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/FixRepository50SP2EP247.xml",
        "-o", outfile, "--section", "Trade", "--notcategory", "CrossOrders"});
    Assertions.assertTrue(new File(outfile).exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(new File(outfile))).build();
    validator.validate();
  }

  @Test
  public void pretrade() throws Exception {
    final String outfile = "target/test/pretrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/FixRepository50SP2EP247.xml",
        "-o", outfile, "--section", "PreTrade"});
    Assertions.assertTrue(new File(outfile).exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(new File(outfile))).build();
    validator.validate();
  }

  @Test
  public void posttrade() throws Exception {
    final String outfile = "target/test/posttrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/FixRepository50SP2EP247.xml",
        "-o", outfile, "--section", "PostTrade"});
    Assertions.assertTrue(new File(outfile).exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(new File(outfile))).build();
    validator.validate();
  }

  @Test
  public void session() throws Exception {
    final String outfile = "target/test/fixt.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/FixRepository50SP2EP247.xml",
        "-o", outfile, "--section", "Session"});
    Assertions.assertTrue(new File(outfile).exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(new File(outfile))).build();
    validator.validate();
  }
}
