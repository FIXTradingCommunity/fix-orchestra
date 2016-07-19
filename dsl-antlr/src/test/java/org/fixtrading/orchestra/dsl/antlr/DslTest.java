package org.fixtrading.orchestra.dsl.antlr;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.BeforeClass;
import org.junit.Test;

public class DslTest {

  private static List<String> fieldConditions = new ArrayList<String>();

  @BeforeClass
  public static void init() {
    fieldConditions.add("this.OrdType");
    fieldConditions.add("\"Stop\"");
    fieldConditions.add("this.OrdType == \"Stop\"");
    fieldConditions.add("this.OrdType in {\"Stop\", \"StopLimit\"}");
  }

  @Test
  public void testExampleFieldCondition() throws Exception {
    for (String fieldCondition : fieldConditions) {
      ScoreLexer l = new ScoreLexer(new ANTLRInputStream(new StringReader(fieldCondition)));
      ScoreParser p = new ScoreParser(new CommonTokenStream(l));
      p.addErrorListener(new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
            int charPositionInLine, String msg, RecognitionException e) {
          throw new IllegalStateException(
              String.format("Failed to parse at line %d position %d due to %s", line,
                  charPositionInLine, msg),
              e);
        }
      });
      p.anyExpression();
    }
  }
}
