package io.fixprotocol.orchestra.testgen;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.model.quickfix.MessageScope;
import io.fixprotocol.orchestra.model.quickfix.RepositoryAdapter;
import quickfix.DataDictionary;
import quickfix.InvalidMessage;
import quickfix.Message;

import static org.junit.Assert.*;

public class OrchestraStepDefinitions {

  private final Evaluator evaluator;
  private final DataDictionary dd;
  private Message receivedMessage;
  private Message sentMessage;
  private final SymbolResolver symbolResolver;
  private final RepositoryAdapter repositoryAdapter;
  private MessageType receivedMessageType;
  private MessageType sentMessageType;
  private MessageScope messageScope;
  
  public OrchestraStepDefinitions(DataDictionary dd, Repository repository) {
    this.dd = dd;
    symbolResolver = new SymbolResolver();
    repositoryAdapter = new RepositoryAdapter(repository);
    evaluator = new Evaluator(symbolResolver);
  }

  @Then ("[^\\s]+) receives message [^\\s]+)")
  public void actor_receives_message(String actor, String messageData) throws InvalidMessage {
    receivedMessage.fromString(messageData, dd, false);
    final MessageScope receivedMessageScope =
        new MessageScope(receivedMessage, receivedMessageType, repositoryAdapter, symbolResolver, evaluator);
    messageScope = receivedMessageScope;
    final Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT);
    local.nest(new PathStep(sentMessageType.getName()), receivedMessageScope);

  }
  
  @Given("([^\\s]+) sends message (.*)$")
  public void actor_sends_message(String actor, String messageData) throws InvalidMessage {
    sentMessage.fromString(messageData, dd, false);
    final MessageScope sentMessageScope =
        new MessageScope(receivedMessage, sentMessageType, repositoryAdapter, symbolResolver, evaluator);
    messageScope = sentMessageScope;
    final Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT);
    local.nest(new PathStep(sentMessageType.getName()), sentMessageScope);
    symbolResolver.nest(new PathStep("in."), sentMessageScope);
  }
  
  @When ("(.*)")
  public void condition(String expression) throws ScoreException {
    FixValue<?> result = evaluator.evaluate(expression);
    assertTrue(result.getValue() == Boolean.TRUE);
  }

  @And ("field ([^\\s]+) is present$")
  public void field_is_present(String fieldName) {
    PathStep pathStep = new PathStep(fieldName);
    FixValue<?> node = (FixValue<?>) messageScope.resolve(pathStep );
    assertNotNull(node);
  }
  
  @And ("field ([^\\s]+) is equal to (.*)$") 
  public void field_is_value(String fieldName, String value) {
    PathStep pathStep = new PathStep(fieldName);
    FixValue<?> node = (FixValue<?>) messageScope.resolve(pathStep );
    assertEquals(value, node.getValue().toString());
  }

}
