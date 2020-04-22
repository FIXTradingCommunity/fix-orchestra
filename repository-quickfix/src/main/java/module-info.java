module orchesta.repository.config {
  requires orchestra.repository;
  requires quickfixj.core;
  requires java.xml.bind;
  requires java.xml;
  
  exports io.fixprotocol.orchestra.quickfix;
}