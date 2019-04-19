package io.fixprotocol.orchestra.states;

import static io.fixprotocol.orchestra.model.SymbolResolver.CODE_SET_ROOT;
import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.SemanticErrorListener;
import io.fixprotocol.orchestra.model.FixNode;
import io.fixprotocol.orchestra.model.FixType;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.LocalScope;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.model.TreeSymbolTable;

class Order implements HasOrderState {

  private OrderState orderState = OrderState.getInitialState();
  private char TimeInForce;
  private String clOrdId;

  public void setOrderState(OrderState orderState) {
    this.orderState = orderState;
  }

  public Order(String clOrdId, char timeInForce) {
    this.clOrdId = clOrdId;
    TimeInForce = timeInForce;
  }

  public String getClOrdId() {
    return clOrdId;
  }

  @Override
  public OrderState getOrderState() {
    return this.orderState;
  }
  
  public char getTimeInForce() {
    return TimeInForce;
  }

}

class CodeSet {

  private String name;
  private Scope parent = null;
  private Map<String,  FixValue<?>> codes = new HashMap<>();

  public FixValue<?> assign(String name, FixValue<?> value) throws ModelException {
    codes.put(name, value);
    return value;
  }

  public FixNode resolve(String name) {
    return codes.get(name);
  }

  public String getName() {
    return name;
  }
  
  public Set<Map.Entry<String,  FixValue<?>>> codes() {
    return codes.entrySet();
  }
  
}


public class StateGeneratorTest {

  static final PathStep IN_ROOT = new PathStep("in.");

  private Evaluator evaluator;
  
  private BiConsumer<OrderState, Order> onEntry = new BiConsumer<OrderState, Order>() {

    @Override
    public void accept(OrderState orderState, Order order) {
      System.out.format("Entering state %s%n", orderState.name());
    }

  };
  private BiConsumer<OrderState, Order> onExit = new BiConsumer<OrderState, Order>() {

    @Override
    public void accept(OrderState orderState, Order order) {
      System.out.format("Exiting state %s%n", orderState.name());
    }

  };
  
  private OrderStateStateMachine<Order> stateMachine;

  private SemanticErrorListener semanticErrorListener = new SemanticErrorListener() {

    @Override
    public void onError(String msg) {
      fail(msg);
    }
    
  };

  private Scope inScope;
  private Scope codeScope;
  private static CodeSet timeInForceCodeSet = new CodeSet();

  @BeforeClass
  public static void setUpOnce() throws ModelException {
    timeInForceCodeSet.assign("Day", new FixValue<Character>(FixType.charType, '0'));
    timeInForceCodeSet.assign("GoodTilCancel", new FixValue<Character>(FixType.charType, '1'));
    timeInForceCodeSet.assign("GoodTilDate", new FixValue<Character>(FixType.charType, '6'));
  }

  @Before
  public void setUp() throws Exception {
    SymbolResolver symbolResolver = new SymbolResolver();
    inScope = symbolResolver.nest(IN_ROOT, new TreeSymbolTable("Objects"));
    codeScope = symbolResolver.nest(CODE_SET_ROOT, new TreeSymbolTable("Codes"));
    evaluator = new Evaluator(symbolResolver, semanticErrorListener);
    stateMachine = new OrderStateStateMachine<>();
  }

  @Test
  public void transition() throws Exception {
    Order order = new Order("CL00001", '0');
    assignObject(order);
    assignCodeSet(timeInForceCodeSet);
    order.setOrderState(stateMachine.tryTransition("Accept", order, onEntry, onExit, evaluator));
    assertEquals(OrderState.New, order.getOrderState());
  }
  
  @Test(expected = StateMachineException.class)
  public void badTransition() throws Exception {
    Order order = new Order("CL00001", '0');
    assignObject(order);
    assignCodeSet(timeInForceCodeSet);
    order.setOrderState(stateMachine.tryTransition("DoneForDay", order, onEntry, onExit, evaluator));
  }
  
  private void assignObject(Order order) throws ModelException {
    inScope.assign(new PathStep("TimeInForce"), new FixValue<Character>(FixType.charType, order.getTimeInForce()));
  }
  
  private void assignCodeSet(CodeSet codeSet) throws ModelException {
    for (Map.Entry<String,  FixValue<?>> code : codeSet.codes()) {
      codeScope.assign(new PathStep(code.getKey()), code.getValue());
    }
  }
  
  

}
