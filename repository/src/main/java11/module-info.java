module orchestra.repository {
  requires transitive java.xml.bind;
  requires transitive jaxb2.basics.runtime;
  
  exports io.fixprotocol._2020.orchestra.repository;
  opens io.fixprotocol._2020.orchestra.repository;
  exports org.purl.dc.elements._1;
  opens org.purl.dc.elements._1;
  exports org.purl.dc.terms;
  opens org.purl.dc.terms;
}