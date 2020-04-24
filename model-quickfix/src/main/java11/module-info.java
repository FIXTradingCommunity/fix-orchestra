module orchesta.model.qf {
  requires orchesta.repository.qf ;
  requires quickfixj.core;
  requires java.xml.bind;
  requires java.xml;
  
  exports io.fixprotocol.orchestra.model.quickfix;
}