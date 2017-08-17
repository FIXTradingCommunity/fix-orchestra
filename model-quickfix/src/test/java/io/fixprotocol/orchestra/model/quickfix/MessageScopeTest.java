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
import java.math.BigDecimal;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.OrderID;
import quickfix.fix50sp2.MarketDataIncrementalRefresh;

public class MessageScopeTest {

  private static Repository repository;
  
  @BeforeClass
  public static void setupOnce() throws Exception  {
    repository = unmarshal(new File("mit_2016.xml"));
  }

  private MessageScope messageScope;
  private MarketDataIncrementalRefresh md = new MarketDataIncrementalRefresh();
  
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    final RepositoryAdapter repositoryAdapter = new RepositoryAdapter(repository);
    final MessageType messageType =
        repositoryAdapter.getMessage("MarketDataIncrementalRefresh", "base");
    final SymbolResolver symbolResolver = new SymbolResolver();
    //symbolResolver.setTrace(true);
    final Evaluator evaluator = new Evaluator(symbolResolver);
    messageScope = new MessageScope(md, messageType, repositoryAdapter, symbolResolver, evaluator);
  }

  @Test
  public void testResolveField() {
    String mdReqId = "REQ1234";
    md.set(new MDReqID(mdReqId));
    PathStep pathStep = new PathStep("MDReqID");
    FixValue<?> node = (FixValue<?>) messageScope.resolve(pathStep );
    assertEquals(mdReqId, node.getValue().toString());
  }
  
  @Test
  public void testFieldNotFound() {
    PathStep pathStep = new PathStep("MDReqID");
    FixValue<?> node = (FixValue<?>) messageScope.resolve(pathStep );
    assertNull(node.getValue());
  }
  
  @Test
  public void testResolveGroupIndex() {
    MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
    char mdEntryType = '0';
    group.set(new MDEntryType(mdEntryType));
    group.set(new MDEntryPx(12.32));
    group.set(new MDEntrySize(100));
    group.set(new OrderID("ORDERID"));
    md.addGroup(group);
    PathStep pathStep = new PathStep("MDIncGrp");
    pathStep.setIndex(1);
    Scope node = (Scope) messageScope.resolve(pathStep );
    PathStep pathStep2 = new PathStep("MDEntryType");
    FixValue<?> node2 = (FixValue<?>) node.resolve(pathStep2);
    assertEquals(mdEntryType, node2.getValue());
  }
  
  @Test
  public void testGroupNotFound() {
    MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
    char mdEntryType = '0';
    group.set(new MDEntryType(mdEntryType));
    group.set(new MDEntryPx(12.32));
    group.set(new MDEntrySize(100));
    group.set(new OrderID("ORDERID"));
    md.addGroup(group);
    PathStep pathStep = new PathStep("MDIncGrp");
    pathStep.setIndex(2);
    Scope node = (Scope) messageScope.resolve(pathStep );
    assertNull(node);
  }

  @Test
  public void testResolveGroupPredicate() {
    MarketDataIncrementalRefresh.NoMDEntries group = new MarketDataIncrementalRefresh.NoMDEntries();
    group.set(new MDEntryType(MDEntryType.Bid));
    group.set(new MDEntryPx(12.32));
    group.set(new MDEntrySize(100));
    md.addGroup(group);
    group.set(new MDEntryType(MDEntryType.Bid));
    group.set(new MDEntryPx(12.31));
    group.set(new MDEntrySize(200));
    md.addGroup(group);
    PathStep pathStep = new PathStep("MDIncGrp");
    pathStep.setPredicate("MDEntryPx==12.31");
    Scope node = (Scope) messageScope.resolve(pathStep );
    assertNotNull(node);
    PathStep pathStep2 = new PathStep("MDEntryPx");
    FixValue<?> node2 = (FixValue<?>) node.resolve(pathStep2);
    assertNotNull(node2);
    assertEquals(new BigDecimal("12.31"), node2.getValue());
  }
  
  private static Repository unmarshal(File inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }
}
