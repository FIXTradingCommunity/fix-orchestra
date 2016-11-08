package io.fixprotocol.orchestra.owl.repository2010;

import static org.junit.Assert.*;

import org.junit.Test;

public class RepositoryToolTest {

  @Test
  public void testMain() {
    String[] args = {"FixRepository.xml", "FixRepositoryOwl2010.ttl", "http://io.fixprotocol/FixRepositoryOwl2010#"};
    RepositoryTool.main(args);
  }

}
