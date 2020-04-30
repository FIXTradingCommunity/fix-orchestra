module orchestra.score {
  requires antlr4;
  requires org.antlr.antlr4.runtime;
  
  exports io.fixprotocol.orchestra.model;
  opens io.fixprotocol.orchestra.model;
}