package io.fixprotocol.orchestra.transformers;

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.repository.FixRepositoryValidator;
import io.fixprotocol.orchestra.repository.RepositoryValidator;
import io.fixprotocol.orchestra.transformers.RepositoryCompressor.HasCategory;
import io.fixprotocol.orchestra.transformers.RepositoryCompressor.HasSection;
import io.fixprotocol.orchestra.transformers.RepositoryCompressor.NotCategory;

public class RepositoryCompressorTest {

  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @Test
  public void tradeCaptureCategory() throws Exception {
    final String outfile = "target/test/tradecapture.xml";
    // Include every category in the "TradeCapture"
    RepositoryCompressor compressor =
        RepositoryCompressor.builder().inputFile("src/test/resources/OrchestraFIXLatest.xml")
            .outputFile(outfile).messagePredicate(new HasCategory("TradeCapture")).build();
    compressor.compress();
    assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        RepositoryValidator.createLogger(new FileOutputStream("target/test/tradecapture.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    assertTrue(validator.validate(new FileInputStream(new File(outfile))));
  }

  @Test
  public void sectionExceptCategory() throws Exception {
    final String outfile = "target/test/tradeX.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor compressor =
        RepositoryCompressor.builder().inputFile("src/test/resources/OrchestraFIXLatest.xml")
            .outputFile(outfile)
            .messagePredicate(new HasSection("Trade", RepositoryCompressor.isCategoryInSection)
                .and(new NotCategory("CrossOrders")))
            .build();
    compressor.compress();
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        RepositoryValidator.createLogger(new FileOutputStream("target/test/tradeX.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    assertTrue(validator.validate(new FileInputStream(new File(outfile))));
  }

  @Test
  public void pretrade() throws Exception {
    final String outfile = "target/test/pretrade.xml";
    // Include every category in the "PreTrade" section
    RepositoryCompressor compressor =
        RepositoryCompressor.builder().inputFile("src/test/resources/OrchestraFIXLatest.xml")
            .outputFile(outfile).messagePredicate(new HasSection("PreTrade", RepositoryCompressor.isCategoryInSection)).build();
    compressor.compress();
    assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        RepositoryValidator.createLogger(new FileOutputStream("target/test/pretrade.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    assertTrue(validator.validate(new FileInputStream(new File(outfile))));
  }

  @Test
  public void posttrade() throws Exception {
    final String outfile = "target/test/posttrade.xml";
    // Include every category in the "PostTrade" section
    RepositoryCompressor compressor =
        RepositoryCompressor.builder().inputFile("src/test/resources/OrchestraFIXLatest.xml")
            .outputFile(outfile).messagePredicate(new HasSection("PostTrade", RepositoryCompressor.isCategoryInSection)).build();
    compressor.compress();
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        RepositoryValidator.createLogger(new FileOutputStream("target/test/posttrade.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    assertTrue(validator.validate(new FileInputStream(new File(outfile))));
  }

  @Test
  public void session() throws Exception {
    final String outfile = "target/test/fixt.xml";
    // Include every category in the "Session" section
    RepositoryCompressor compressor =
        RepositoryCompressor.builder().inputFile("src/test/resources/OrchestraFIXLatest.xml")
            .outputFile(outfile).messagePredicate(new HasSection("Session", RepositoryCompressor.isCategoryInSection)).build();
    compressor.compress();
    Assertions.assertTrue(new File(outfile).exists());
    final EventListener eventLogger =
        RepositoryValidator.createLogger(new FileOutputStream("target/test/fixt.json"));
    final FixRepositoryValidator validator = new FixRepositoryValidator(eventLogger);
    assertTrue(validator.validate(new FileInputStream(new File(outfile))));
  }
}
