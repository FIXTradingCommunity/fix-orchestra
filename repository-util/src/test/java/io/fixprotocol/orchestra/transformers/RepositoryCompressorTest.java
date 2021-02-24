package io.fixprotocol.orchestra.transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import io.fixprotocol.orchestra.repository.FixRepositoryValidator;
import io.fixprotocol.orchestra.event.EventListener;

public class RepositoryCompressorTest {

  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @Test
  public void tradeCaptureCategory() throws Exception {
    final String outfile = "target/test/tradecapture.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/OrchestraFIXLatest.xml", "-o",
        outfile, "--category", "TradeCapture"});
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        FixRepositoryValidator.createLogger(new FileOutputStream("target/test/tradecapture.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    validator.validate(new FileInputStream(new File(outfile)));
  }

  @Test
  public void sectionExceptCategory() throws Exception {
    final String outfile = "target/test/tradeX.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/OrchestraFIXLatest.xml", "-o",
        outfile, "--section", "Trade", "--notcategory", "CrossOrders"});
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        FixRepositoryValidator.createLogger(new FileOutputStream("target/test/tradeX.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    validator.validate(new FileInputStream(new File(outfile)));
  }

  @Test
  public void pretrade() throws Exception {
    final String outfile = "target/test/pretrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/OrchestraFIXLatest.xml", "-o",
        outfile, "--section", "PreTrade"});
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        FixRepositoryValidator.createLogger(new FileOutputStream("target/test/pretrade.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    validator.validate(new FileInputStream(new File(outfile)));
  }

  @Test
  public void posttrade() throws Exception {
    final String outfile = "target/test/posttrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/OrchestraFIXLatest.xml", "-o",
        outfile, "--section", "PostTrade"});
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        FixRepositoryValidator.createLogger(new FileOutputStream("target/test/posttrade.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    validator.validate(new FileInputStream(new File(outfile)));
  }

  @Test
  public void session() throws Exception {
    final String outfile = "target/test/fixt.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String[] {"-i", "src/test/resources/OrchestraFIXLatest.xml", "-o",
        outfile, "--section", "Session"});
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        FixRepositoryValidator.createLogger(new FileOutputStream("target/test/fixt.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    validator.validate(new FileInputStream(new File(outfile)));
  }
}
