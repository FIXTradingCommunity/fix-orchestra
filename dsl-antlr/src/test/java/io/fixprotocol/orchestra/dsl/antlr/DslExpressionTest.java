package io.fixprotocol.orchestra.dsl.antlr;

import java.util.Arrays;
import java.util.Collection;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
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
      {"$x = 55"}, 
      {"$x = -55"},
      {"$y = \"MyName\""}, 
      {"$x = 50 + 5"}, 
      {"$z = in.OrdType"},
      {"out.OrdQty = 55"}, 
      {"in.OrdType == ^Stop"}, 
      {"in.OrdType in {^Stop, ^StopLimit}"},
      {"in.OrdType == ^Stop or in.OrdType == ^StopLimit"}, 
      {"in.OrdQty > 0"},
      {"in.OrdQty != 0"}, 
      {"in.OrdQty > 0 and in.OrdQty <= 10"}, 
      {"in.Price < 100.00"},
      {"in.Price between 50.00 and 100.00"},
      {"in.Parties[PartyRole==4].PartyID=\"690\""},
      {"out.Parties[1].PartyRole=4"},
      {"out.Parties[1].PartyRole=^ClearingFirm"},
      {"#2017-02-02T22:13:28Z#"},
      {"#2017-02-02T22:13:28.678Z#"},
      {"#22:13:28Z#"},
      {"#2017-02-02#"},
      {"#2017-02-02T22:13:28-6:00#"},
      {"#P30D#"},
      {"#PT30S#"},
      {"out.ExpireDate=#2017-02-03#"},
      {"out.ExpireTime=#2017-02-02T22:15Z#"},
      {"exists in.StopPx"},
      });
  }

  @Test
  public void testExampleFieldCondition() throws Exception {
    ScoreLexer l = new ScoreLexer(CharStreams.fromString(fieldCondition));
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
