package io.fixprotocol.orchestra.dsl.antlr;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;

@RunWith(Parameterized.class)
public class DslExpressionTest {

  private String fieldCondition;

  public DslExpressionTest(String fieldCondition) {
    this.fieldCondition = fieldCondition;
  }

  @Parameterized.Parameters
  public static Collection<String[]> testFieldConditions() {
    return Arrays.asList(new String[][] {
      {"$x = 55"}, {"$y = \"MyName\""}, {"$x = 50 + 5"}, {"$z = this.OrdType"},
      {"this.OrdType == \"Stop\""}, {"this.OrdType in {\"Stop\", \"StopLimit\"}"},
      {"this.OrdType == \"Stop\" or this.OrdType == \"StopLimit\""}, {"this.OrdQty > 0"},
      {"this.OrdQty != 0"}, {"this.OrdQty > 0 and this.OrdQty <= 10"}, {"this.Price < 100.00"},
      {"this.Price between 50.00 and 100.00"},
      {"this.Parties[PartyRole=4].PartyID=\"690\""},{"this.Parties[1].PartyRole=4"}});
  }

  @Test
  public void testExampleFieldCondition() throws Exception {
    ScoreLexer l = new ScoreLexer(new ANTLRInputStream(new StringReader(fieldCondition)));
    ScoreParser p = new ScoreParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException(String.format(
            "Failed to parse at line %d position %d due to %s", line, charPositionInLine, msg), e);
      }
    });
    ScoreBaseVisitor<Object> visitor = new ScoreBaseVisitor<>();
    AnyExpressionContext ctx = p.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    //System.out.println(expression.getClass().getSimpleName());
  }
}
