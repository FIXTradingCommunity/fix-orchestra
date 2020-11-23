module orchestra.repository.util {
  requires transitive orchestra.repository;
  requires transitive orchestra.common;
  requires commons.cli;
  requires org.apache.logging.log4j;
  requires java.xml;
  requires orchestra.score;
  requires Saxon.HE;

  exports io.fixprotocol.orchestra.repository;
  exports io.fixprotocol.orchestra.transformers;
}