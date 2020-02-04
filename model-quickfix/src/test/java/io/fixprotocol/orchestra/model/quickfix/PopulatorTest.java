/**
 * Copyright 2017-2020 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package io.fixprotocol.orchestra.model.quickfix;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol._2020.orchestra.repository.MessageType;
import io.fixprotocol._2020.orchestra.repository.Repository;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.repository.RepositoryAccessor;
import quickfix.Group;
import quickfix.field.Account;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;

/**
 * @author Don Mendelson
 *
 */
public class PopulatorTest {

  private static Repository repository;

  @BeforeClass
  public static void setupOnce() throws Exception {
    repository = unmarshal(Thread.currentThread().getContextClassLoader().getResourceAsStream("mit_2016.xml"));
  }

  private RepositoryAccessor repositoryAdapter;
  private QuickfixPopulator populator;
  
  /**
   * QuickFIXJ can only instantiate repeating groups with their generated classes.
   * This suggests that this factory method should be generated as well, like existing
   * MessageFactory.
   */
  private Function<Integer, Group> groupFactory = new Function<Integer, Group>(){

    @Override
    public Group apply(Integer numInGroupFieldId) {
      switch (numInGroupFieldId) {
        case 453:
          return new ExecutionReport.NoPartyIDs();
        default:
        return null;
      }
    }
    
  };

  @Before
  public void setUp() throws Exception {
    repositoryAdapter = new RepositoryAccessor(repository);
    final SymbolResolver symbolResolver = new SymbolResolver();
    //symbolResolver.setTrace(true);
    populator = new QuickfixPopulator(repositoryAdapter, symbolResolver, groupFactory );
  }

  /**
   * 
   * <pre>
   * <fixr:fieldRef id="11" name="ClOrdID" added="FIX.2.7" updated="FIX.5.0SP2" updatedEP="188">
       <fixr:assign>in.ClOrdID</fixr:assign>
     </fixr:fieldRef>
     
      <fixr:groupRef id="1012" name="Parties" added="FIX.4.3" updated="FIX.5.0SP2" updatedEP="188">
      <fixr:blockAssignment>
          <fixr:fieldRef id="448" name="PartyID">
              <fixr:assign>"ABC"</fixr:assign>
          </fixr:fieldRef>
          <fixr:fieldRef id="447" name="PartyIDSource">
              <fixr:assign>^GeneralIdentifier</fixr:assign>
          </fixr:fieldRef>
          <fixr:fieldRef id="452" name="PartyRole">
              <fixr:assign>^ExecutingFirm</fixr:assign>
          </fixr:fieldRef>
      </fixr:blockAssignment>
      <fixr:blockAssignment>
          <fixr:fieldRef id="448" name="PartyID">
              <fixr:assign>"DEF"</fixr:assign>
          </fixr:fieldRef>
          <fixr:fieldRef id="447" name="PartyIDSource">
              <fixr:assign>^GeneralIdentifier</fixr:assign>
          </fixr:fieldRef>
          <fixr:fieldRef id="452" name="PartyRole">
              <fixr:assign>^ClearingFirm</fixr:assign>
          </fixr:fieldRef>
      </fixr:blockAssignment>
      </fixr:groupRef>
     </pre>
   */
  @Test
  public void testPopulate() throws ModelException, quickfix.FieldNotFound {
    MessageType inboundMessageType = repositoryAdapter.getMessage("NewOrderSingle", "base");
    MessageType outboundMessageType = repositoryAdapter.getMessage("ExecutionReport", "traded");
    
    NewOrderSingle inboundMessage = new NewOrderSingle();
    String clOrdId = "ABC123";
    // required fields
    inboundMessage.set(new ClOrdID(clOrdId));
    inboundMessage.set(new Account("ABC"));
    inboundMessage.set(new Side('2'));
    inboundMessage.set(new OrdType('2'));
    inboundMessage.set(new TimeInForce('2'));
    
    ExecutionReport outboundMessage = new ExecutionReport();

    populator.populate(inboundMessage, inboundMessageType, outboundMessage, outboundMessageType);
     
    System.out.println(outboundMessage.toString());
    assertEquals(clOrdId, outboundMessage.getClOrdID().getValue());
   }

  private static Repository unmarshal(InputStream inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }
}
