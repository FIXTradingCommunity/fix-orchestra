module orchesta.interfaces.config {
  requires orchestra.interfaces;
  requires quickfixj.core;
  requires java.xml.bind;
  requires java.xml;
  
  exports io.fixprotocol.orchestra.session.quickfix;
}