package io.fixprotocol.orchestra.owl.repository2010;

import static org.junit.Assert.*;

import org.junit.Test;

public class RepositoryToolTest {

  @Test
  public void testMain() {
    String[] args = {"FixRepository.xml", "FixRepositoryOwl.ttl", "http://test2010#"};
    RepositoryTool.main(args);
  }

}
