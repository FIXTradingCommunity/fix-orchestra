package io.fixprotocol.orchestra.model.quickfix;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.model.TestException;
import quickfix.field.TradSesStatus;
import quickfix.field.TradingSessionID;
import quickfix.fix50sp2.TradingSessionStatus;

public class ValidatorTest {

  private Validator validator;
  private static Repository repository;
  
  @BeforeClass
  public static void setupOnce() throws Exception  {
    repository = unmarshal(new File("FixRepository2016.xml"));
  }
  
  @Before
  public void setUp() throws Exception {
    validator = new Validator(repository);
  }

  private static Repository unmarshal(File inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }
  
  @After
  public void tearDown() throws Exception {}

  @Test
  public void emptyMessage() {
    TradingSessionStatus message = new TradingSessionStatus();
    MessageType messageType = validator.getMessage("TradingSessionStatus", "base");
    try {
      validator.validate(message, messageType);
      fail("TestException exptected");
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
    MessageType messageType = validator.getMessage("TradingSessionStatus", "base");
    try {
      validator.validate(message, messageType);
      fail("TestException exptected");
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
    MessageType messageType = validator.getMessage("TradingSessionStatus", "base");
    validator.validate(message, messageType);
  }

}
