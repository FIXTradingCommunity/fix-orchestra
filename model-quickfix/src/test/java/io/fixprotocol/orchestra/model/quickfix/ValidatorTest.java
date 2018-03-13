package io.fixprotocol.orchestra.model.quickfix;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.model.TestException;
import quickfix.SessionID;
import quickfix.field.TradSesStatus;
import quickfix.field.TradingSessionID;
import quickfix.fix50sp2.TradingSessionStatus;

public class ValidatorTest {

  private Validator validator;
  private static Repository repository;
  private RepositoryAdapter repositoryAdapter;
  private SessionID sessionID;

  @BeforeClass
  public static void setupOnce() throws Exception {
    repository = unmarshal(Thread.currentThread().getContextClassLoader().getResourceAsStream("mit_2016.xml"));
  }

  @Before
  public void setUp() throws Exception {
    sessionID = new SessionID("FIXT.1.1", "sender", "target");
    repositoryAdapter = new RepositoryAdapter(repository);
    final SymbolResolver symbolResolver = new SymbolResolver();
    //symbolResolver.setTrace(true);
    validator = new Validator(repositoryAdapter, symbolResolver);
  }

  private static Repository unmarshal(InputStream inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void emptyMessage() {
    TradingSessionStatus message = new TradingSessionStatus();
    MessageType messageType = repositoryAdapter.getMessage("TradingSessionStatus", "base");
    try {
      validator.validate(message, messageType);
      fail("TestException expected");
    } catch (TestException e) {
      assertTrue(e.hasDetails());
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void badCode() {
    TradingSessionStatus message = new TradingSessionStatus();
    message.set(new TradingSessionID(TradingSessionID.Day));
    message.set(new TradSesStatus(82));
    MessageType messageType = repositoryAdapter.getMessage("TradingSessionStatus", "base");
    try {
      validator.validate(message, messageType);
      fail("TestException expected");
    } catch (TestException e) {
      assertTrue(e.hasDetails());
      System.out.println(e.getMessage());
    }
  }

  @Test
  public void validMessage() throws TestException {
    TradingSessionStatus message = new TradingSessionStatus();
    message.set(new TradingSessionID(TradingSessionID.Day));
    message.set(new TradSesStatus(TradSesStatus.Open));
    MessageType messageType = repositoryAdapter.getMessage("TradingSessionStatus", "base");
    validator.validate(message, messageType);
  }

  /**
   * Conditionally required field test
   * @throws TestException expected
   * 
    <pre>
    <fixr:fieldRef id="567" name="TradSesStatusRejReason" added="FIX.4.4" presence="conditional">
                    <fixr:rule name="TradSesStatusRejReason" presence="required">
                        <fixr:when>TradSesStatus=^RequestRejected</fixr:when>
                    </fixr:rule>
    </pre>
   */
  @Test(expected = TestException.class)
  public void ruleViolation() throws TestException {
    TradingSessionStatus message = new TradingSessionStatus();
    message.set(new TradingSessionID(TradingSessionID.Day));
    message.set(new TradSesStatus(TradSesStatus.RequestRejected));
    MessageType messageType = repositoryAdapter.getMessage("TradingSessionStatus", "base");
    validator.validate(message, messageType);
  }

}
