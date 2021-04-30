module orchestra.common {
  exports io.fixprotocol.orchestra.event;
  
  opens io.fixprotocol.orchestra.event;
  requires com.fasterxml.jackson.core;
  requires org.apache.logging.log4j;
}