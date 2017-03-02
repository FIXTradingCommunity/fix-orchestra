/**
 * Copyright 2017 FIX Protocol Ltd
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

import static org.junit.Assert.*;

import java.io.File;
import java.util.function.Function;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.Group;
import quickfix.Message;
import quickfix.field.ClOrdID;
import quickfix.fix50sp2.ExecutionReport;
import quickfix.fix50sp2.NewOrderSingle;

/**
 * @author donme
 *
 */
public class PopulatorTest {

  private static Repository repository;

  @BeforeClass
  public static void setupOnce() throws Exception {
    repository = unmarshal(new File("FixRepository2016.xml"));
  }

  private RepositoryAdapter repositoryAdapter;
  private Populator populator;
  
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
    repositoryAdapter = new RepositoryAdapter(repository);
    final SymbolResolver symbolResolver = new SymbolResolver();
    //symbolResolver.setTrace(true);
    populator = new Populator(repositoryAdapter, symbolResolver, groupFactory );
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
    
    String clOrdId = "ABC123";
    NewOrderSingle inboundMessage = new NewOrderSingle();
    inboundMessage.set(new ClOrdID(clOrdId));
    
    ExecutionReport outboundMessage = new ExecutionReport();

    populator.populate(inboundMessage, inboundMessageType, outboundMessage, outboundMessageType);
     
    System.out.println(outboundMessage.toString());
    assertEquals(clOrdId, outboundMessage.getClOrdID().getValue());
   }

  private static Repository unmarshal(File inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }
}
