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
package io.fixprotocol.orchestra.dsl.antlr;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;

/**
 * @author Don Mendelson
 *
 */
public class ScoreVisitorImplTest {

  private class TestData {

    private final Object expected;

    private final String expression;

    public TestData(String expression, Object expected) {
      this.expression = expression;
      this.expected = expected;
    }

    public Object getExpected() {
      return expected;
    }

    public String getExpression() {
      return expression;
    }
  }

  private ScoreVisitorImpl visitor;
  private SymbolResolver symbolResolver;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    // Only resolve variables for now
    symbolResolver = new SymbolResolver();
    visitor = new ScoreVisitorImpl(symbolResolver);
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitAddSub(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AddSubContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitAddSub() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("33 + 44", new Integer(77)),
        new TestData("44 - 33", new Integer(11)),
        new TestData("7.12 + 2.34", new BigDecimal("9.46")),
        new TestData("65.55 - 2.34", new BigDecimal("63.21")),
        new TestData("7.12 + 2.0", new BigDecimal("9.12")),
        // TODO test conversion int + float
        };

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitAnyExpression(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitAnyExpression() throws IOException {
    TestData[] data =
        new TestData[] {new TestData("!(33 == 4 and 5 < 6) and 12 >= 11", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitAssignment(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitAssignment() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("$x = 33", 33), 
        new TestData("$y = 33.5", new BigDecimal("33.5")),
        new TestData("$a = 'a'", 'a'), 
        new TestData("$b = \"abc\"", "abc"),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
     }
  }
  
  @Test
  public void testReAssignment() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("$x = 33", 33), 
        new TestData("$x = 44", 44), 
        };

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
      assertEquals("x", fixValue.getName());
     }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitBooleanNot(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.BooleanNotContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitBooleanNot() throws IOException {
    TestData[] data = new TestData[] {new TestData("!(33 > 4)", Boolean.FALSE),
        new TestData("!(33 < 4)", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitCharacter(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.CharacterContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitCharacter() throws IOException {
    final String value = "\'g\'";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.charType, fixValue.getType());
    assertEquals(value.charAt(1), fixValue.getValue());
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitContains(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ContainsContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitContains() throws IOException {
    TestData[] data = new TestData[] {new TestData("33 in {4, 7, 9}", Boolean.FALSE),
        new TestData("33 in {4, 7, 9, 33}", Boolean.TRUE),
        new TestData("30 + 3 in {4, 7, 9, 33}", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitDecimal(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DecimalContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitDecimal() throws IOException {
    final String value = "456.789";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.floatType, fixValue.getType());
    assertEquals(new BigDecimal(value), fixValue.getValue());
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitEquality(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.EqualityContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitEquality() throws IOException {
    TestData[] data = new TestData[] {new TestData("33 == 4", Boolean.FALSE),
        new TestData("33 != 4", Boolean.TRUE), new TestData("33.5 == 4.0", Boolean.FALSE),
        new TestData("33.5 != 4.0", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitInteger(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IntegerContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitInteger() throws IOException {
    final String value = "456";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.intType, fixValue.getType());
    assertEquals(Integer.valueOf(value), fixValue.getValue());
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitLogicalAnd(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalAndContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitLogicalAnd() throws IOException {
    TestData[] data = new TestData[] {new TestData("33 == 4 and 5 < 6", Boolean.FALSE),
        new TestData("33 == 33 and 5 < 6", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitLogicalOr(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalOrContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitLogicalOr() throws IOException {
    TestData[] data = new TestData[] {new TestData("33 == 4 or 5 < 4", Boolean.FALSE),
        new TestData("33 == 4 or 5 > 4", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitMulDiv(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.MulDivContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitMulDiv() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("33 * 4", new Integer(132)),
        new TestData("44 / 3", new Integer(14)), 
        new TestData("44 % 3", new Integer(2)),
        new TestData("7.12 * 2.3", new BigDecimal("16.376")),
        new TestData("65.55 / 2.3", new BigDecimal("28.5")),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }
  
  @Test
  public void testVisitMulDivError() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("44 / 0", new Integer(2)),
        };

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      assertNull(expression);
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitParens(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ParensContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitParens() throws IOException {
    TestData[] data = new TestData[] {new TestData("2 + 3 * 5", new Integer(17)),
        new TestData("(2 + 3) * 5", new Integer(25))};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }

  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitRange(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitRange() throws IOException {
    TestData[] data = new TestData[] {
        new TestData("33 between 4 and 7", Boolean.FALSE),
        new TestData("33 between 4 and 37", Boolean.TRUE),
        };

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitRelational(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitRelational() throws IOException {
    TestData[] data = new TestData[] {new TestData("33 < 4", Boolean.FALSE),
        new TestData("33 <= 4", Boolean.FALSE), new TestData("33 <= 33", Boolean.TRUE),
        new TestData("33 > 4", Boolean.TRUE), new TestData("33 >= 4", Boolean.TRUE),
        new TestData("33 >= 33", Boolean.TRUE),};

    for (int i = 0; i < data.length; i++) {
      ScoreParser parser = parse(data[i].getExpression());
      AnyExpressionContext ctx = parser.anyExpression();
      Object expression = visitor.visitAnyExpression(ctx);
      FixValue<?> fixValue = (FixValue<?>) expression;
      assertEquals(data[i].getExpected(), fixValue.getValue());
    }
  }

  /**
   * Test method for
   * {@link io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl#visitString(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.StringContext)}.
   * 
   * @throws IOException
   */
  @Test
  public void testVisitString() throws IOException {
    final String value = "\"abcde\"";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.StringType, fixValue.getType());
    assertEquals(value.replace("\"", ""), fixValue.getValue());
  }

  @Test
  public void testVisitVariable() throws IOException, FixException {
    final String varName = "$orderCount";

    Scope scope = (Scope) symbolResolver.resolve(new PathStep("$"));
    scope.assign(new PathStep("orderCount"), new FixValue<Integer>(FixType.intType, 7));

    ScoreParser parser = parse(varName);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.intType, fixValue.getType());
    assertEquals(7, fixValue.getValue());
  }

  private ScoreParser parse(String expression) throws IOException {
    ScoreLexer l = new ScoreLexer(new ANTLRInputStream(new StringReader(expression)));
    ScoreParser p = new ScoreParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException(String.format(
            "Failed to parse at line %d position %d due to %s", line, charPositionInLine, msg), e);
      }
    });
    return p;
  }

  @Test
  public void testVisitDate() throws IOException {
    final String value = "#2017-02-03#";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.UTCDateOnly, fixValue.getType());
    assertEquals(LocalDate.parse(value.substring(1, value.lastIndexOf('#'))), fixValue.getValue());
  }
  
  @Test
  public void testVisitTime() throws IOException {
    final String value = "#2017-02-03T11:12:13.123456789Z#";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.UTCTimestamp, fixValue.getType());
    assertTrue(fixValue.getValue() instanceof Instant);
  }
  
  @Test
  public void testVisitTimestamp() throws IOException {
    final String value = "#T11:12:13.123456789Z#";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.UTCTimeOnly, fixValue.getType());
    assertTrue(fixValue.getValue() instanceof LocalTime);
  }
  
  @Test
  public void testVisitDuration() throws IOException {
    final String value = "#PT30S#";
    ScoreParser parser = parse(value);
    AnyExpressionContext ctx = parser.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    assertTrue(expression instanceof FixValue<?>);
    FixValue<?> fixValue = (FixValue<?>) expression;
    assertEquals(FixType.UTCTimeOnly, fixValue.getType());
    assertTrue(fixValue.getValue() instanceof Duration);
  }
}
