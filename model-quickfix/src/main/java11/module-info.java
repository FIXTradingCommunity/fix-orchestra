module orchesta.model.qf {
  requires transitive orchesta.repository.qf ;
  requires quickfixj.core;
  requires java.xml.bind;
  requires java.xml;
  
  exports io.fixprotocol.orchestra.model.quickfix;
}