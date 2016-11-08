package io.fixprotocol.orchestra.owl.repository2016;

import static org.junit.Assert.*;

import org.junit.Test;

public class RepositoryToolTest {

  @Test
  public void testMain() {
    String[] args = {"FixRepository2016.xml", "FixRepository2016Owl.ttl", "http://io.fixprotocol/FixRepository2016Owl#"};
    RepositoryTool.main(args);
  }

}
