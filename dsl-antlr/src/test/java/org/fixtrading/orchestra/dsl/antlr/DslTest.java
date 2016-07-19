package org.fixtrading.orchestra.dsl.antlr;

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

@RunWith(Parameterized.class)
public class DslTest {

  private String fieldCondition;

  public DslTest(String fieldCondition) {
    this.fieldCondition = fieldCondition;
  }

  @Parameterized.Parameters
  public static Collection<String[]> testFieldConditions() {
    return Arrays.asList(new String[][] {
      {"this.OrdType"}, 
      {"\"Stop\""},
      {"this.OrdType == \"Stop\""},
      {"this.OrdType in {\"Stop\", \"StopLimit\"}"},
      {"this.OrdType == \"Stop\" or this.OrdType == \"StopLimit\""},
      {"this.OrdQty > 0"},
      {"this.OrdQty != 0"},
      {"this.OrdQty > 0 and this.OrdQty <= 10"},
      {"this.Price < 100.00"},
     });
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
    p.anyExpression();
  }
}
