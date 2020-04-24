module message.model {
  requires orchestra.repository;
  requires orchestra.score;
  
  exports io.fixprotocol.orchestra.message;
  opens io.fixprotocol.orchestra.message;
}