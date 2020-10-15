module orchestra.interfaces.util {
  requires transitive orchestra.interfaces;
  requires transitive orchestra.common;
  requires org.apache.logging.log4j;
  requires java.xml;
  requires orchestra.score;

  exports io.fixprotocol.orchestra.interfaces;
}