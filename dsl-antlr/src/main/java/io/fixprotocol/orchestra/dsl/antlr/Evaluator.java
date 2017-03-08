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

import java.io.IOException;
import java.io.StringReader;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.SymbolResolver;

/**
 * Evaluates a Score expression
 * 
 * @author Don Mendelson
 *
 */
public class Evaluator {

  private final BaseErrorListener errorListener = new BaseErrorListener() {
    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
        int charPositionInLine, String msg, RecognitionException e) {
      throw new IllegalStateException(String.format(
          "Failed to parse at line %d position %d due to %s", line, charPositionInLine, msg), e);
    }
  };

  private class DefaultSemanticErrorListener implements SemanticErrorListener {

    @Override
    public void onError(String msg) {
      throw new IllegalStateException(msg);
    }

  }

  private final ScoreVisitorImpl visitor;


  /**
   * Constructor with default SemanticErrorListener
   * 
   * @param symbolResolver resolves symbols
   */
  public Evaluator(SymbolResolver symbolResolver) {
    visitor = new ScoreVisitorImpl(symbolResolver, new DefaultSemanticErrorListener());
  }

  /**
   * Constructor
   *
   * @param symbolResolver resolves symbols
   * @param semanticErrorListener reports semantic errors
   */
  public Evaluator(SymbolResolver symbolResolver, SemanticErrorListener semanticErrorListener) {
    visitor = new ScoreVisitorImpl(symbolResolver, semanticErrorListener);
  }


  /**
   * Parses and evaluates a Score expression
   * 
   * @param expression a Boolean predicate in the Score grammar
   * @return the value of the expression
   * @throws ScoreException if the expression is invalid syntactically or semantically
   */
  public FixValue<?> evaluate(String expression) throws ScoreException {
    try {
      final ScoreLexer lexer = new ScoreLexer(new ANTLRInputStream(new StringReader(expression)));
      final ScoreParser parser = new ScoreParser(new CommonTokenStream(lexer));
      parser.addErrorListener(errorListener);
      final AnyExpressionContext ctx = parser.anyExpression();
      return visitor.visitAnyExpression(ctx);
    } catch (IOException e) {
      throw new RuntimeException("Internal reading from String", e);
    } catch (IllegalStateException e) {
      throw new ScoreException("Syntactical or semantic error; " + e.getMessage());
    }
  }

}
