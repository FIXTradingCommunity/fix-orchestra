module orchestra.repository2010 {
  requires transitive java.xml.bind;
  requires transitive jaxb2.basics.runtime;
  requires Saxon.HE;
  
  exports io.fixprotocol._2010.orchestra.repository;
  opens io.fixprotocol._2010.orchestra.repository;
}