package io.fixprotocol.orchestra.transformers;

import java.io.File;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class RepositoryCompressorTest {
  
  @BeforeClass
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @Test
  public void sectionExceptCategory() throws Exception {
    final String outfile = "target/test/tradeX.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String [] {"-i", "src/test/resources/FixRepository50SP2EP247.xml", "-o", outfile, 
        "--section", "Trade", "--notcategory", "CrossOrders"});
    Assert.assertTrue(new File(outfile).exists());
  }
  
  @Test
  public void pretrade() throws Exception {
    final String outfile = "target/test/pretrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String [] {"-i", "src/test/resources/FixRepository50SP2EP247.xml", "-o", outfile, 
        "--section", "PreTrade"});
    Assert.assertTrue(new File(outfile).exists());
  }

  @Test
  public void posttrade() throws Exception {
    final String outfile = "target/test/posttrade.xml";
    // Include every category in the "Trade" section except "CrossOrders"
    RepositoryCompressor.main(new String [] {"-i", "src/test/resources/FixRepository50SP2EP247.xml", "-o", outfile, 
        "--section", "PostTrade"});
    Assert.assertTrue(new File(outfile).exists());
  }
}
