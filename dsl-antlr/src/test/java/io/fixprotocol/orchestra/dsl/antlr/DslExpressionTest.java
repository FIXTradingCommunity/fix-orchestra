package io.fixprotocol.orchestra.dsl.antlr;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;

public class DslExpressionTest {
  @ParameterizedTest
  @ValueSource(strings = {
      "$x = 55", 
      "$x = -55",
      "$y = \"MyName\"", 
      "$x = 50 + 5", 
      "$z = in.OrdType",
      "out.OrdQty = 55", 
      "in.OrdType == ^Stop", 
      "in.OrdType in {^Stop, ^StopLimit}",
      "in.OrdType == ^Stop or in.OrdType == ^StopLimit", 
      "in.OrdQty > 0",
      "in.OrdQty != 0", 
      "in.OrdQty > 0 and in.OrdQty <= 10", 
      "in.Price < 100.00",
      "in.Price between 50.00 and 100.00",
      "in.Parties[PartyRole==4].PartyID=\"690\"",
      "out.Parties[1].PartyRole=4",
      "out.Parties[1].PartyRole=^ClearingFirm",
      "#2017-02-02T22:13:28Z#",
      "#2017-02-02T22:13:28.678Z#",
      "#22:13:28Z#",
      "#2017-02-02#",
      "#2017-02-02T22:13:28-6:00#",
      "#P30D#",
      "#PT30S#",
      "out.ExpireDate=#2017-02-03#",
      "out.ExpireTime=#2017-02-02T22:15Z#",
      "exists in.StopPx",
      })
  public void testExampleFieldCondition(String condition) throws ScoreException  {
    Evaluator.validateSyntax(condition);
  }
  
  @Test
  public void badExpression() {
    Exception exception = assertThrows(ScoreException.class, () -> {Evaluator.validateSyntax("2 > ");});
  }
}
