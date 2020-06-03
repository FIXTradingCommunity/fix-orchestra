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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;

/**
 * @author Don Mendelson
 *
 */
public class ScoreTranslatorTest {
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
      "#2017-02-02T22:13:28-06:00#",
      "#P30D#",
      "#PT30S#",
      "out.ExpireDate=#2017-02-03#",
      "out.ExpireTime=#2017-02-02T22:15:00Z#",
      "exists in.StopPx",
      "$Market.SecMassStatGrp[SecurityID==in.SecurityID].SecurityTradingStatus != ^TradingHalt and $Market.Phase == \"Open\"",
      "$Market.Phase == \"Closed\"",
      "!exists $Market.SecMassStatGrp[SecurityID==in.SecurityID]",
      })
  public void testExampleFieldCondition(String expression) throws Exception {
    ScoreLexer l = new ScoreLexer(CharStreams.fromString(expression));
    ScoreParser p = new ScoreParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException(String.format(
            "Failed to parse at line %d position %d due to %s", line, charPositionInLine, msg), e);
      }
    });
    ScoreTranslator visitor = new ScoreTranslator();
    AnyExpressionContext ctx = p.anyExpression();
    String text = visitor.visitAnyExpression(ctx);
    //System.out.println(text);
  }

}
