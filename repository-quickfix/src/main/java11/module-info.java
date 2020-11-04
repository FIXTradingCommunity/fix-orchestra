module orchesta.repository.qf {
  requires transitive orchestra.repository;
  requires quickfixj.core;
  requires java.xml.bind;
  requires java.xml;
  
  exports io.fixprotocol.orchestra.quickfix;
}