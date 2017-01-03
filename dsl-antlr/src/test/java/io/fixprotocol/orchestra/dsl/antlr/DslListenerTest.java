package io.fixprotocol.orchestra.dsl.antlr;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ConditionalAndExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ConditionalOrExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.FactorContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.QualifiedIdContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.SetExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.SimpleExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.TermContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ValueContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VariableContext;

@RunWith(Parameterized.class)
public class DslListenerTest {

  private String fieldCondition;

  public DslListenerTest(String fieldCondition) {
    this.fieldCondition = fieldCondition;
  }

  @Parameterized.Parameters
  public static Collection<String[]> testFieldConditions() {
    return Arrays.asList(new String[][] {
      {"$x = 55"}, {"$y = \"MyName\""}, {"$x = 50 + 5"}, {"$z = this.OrdType"},
      {"this.OrdType == \"Stop\""}, {"this.OrdType in {\"Stop\", \"StopLimit\"}"},
      {"this.OrdType == \"Stop\" or this.OrdType == \"StopLimit\""}, {"this.OrdQty > 0"},
      {"this.OrdQty != 0"}, {"this.OrdQty > 0 and this.OrdQty <= 10"}, {"this.Price < 100.00"},
      {"this.Price between 50.00 and 100.00"},});
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
    p.addParseListener(new ScoreListener(){

      @Override
      public void visitTerminal(TerminalNode node) {
        // System.out.println("Visit terminal " + node.getText());    
      }

      @Override
      public void visitErrorNode(ErrorNode node) {
        System.out.println("Visit error " + node.getText());
        
      }

      @Override
      public void enterEveryRule(ParserRuleContext ctx) {
        // System.out.println("Enter rule " + ctx.getRuleContext().getRuleIndex());       
      }

      @Override
      public void exitEveryRule(ParserRuleContext ctx) {
        // System.out.println("Exit rule " + ctx.getRuleContext().getRuleIndex());    
      }

      @Override
      public void enterAnyExpression(AnyExpressionContext ctx) {
        // System.out.println("Enter AnyExpression " + ctx.getText());  
      }

      @Override
      public void exitAnyExpression(AnyExpressionContext ctx) {
        System.out.println("Exit AnyExpression " + ctx.getText());
        
      }

      @Override
      public void enterAssignment(AssignmentContext ctx) {
        // System.out.println("Enter Assignment " + ctx.getText());      
      }

      @Override
      public void exitAssignment(AssignmentContext ctx) {
        System.out.println("Exit Assignment " + ctx.getText()); 
      }

      @Override
      public void enterConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        // System.out.println("Enter ConditionalOrExpression " + ctx.getText());    
      }

      @Override
      public void exitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
        System.out.println("Exit ConditionalOrExpression " + ctx.getText());
        
      }

      @Override
      public void enterConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        // System.out.println("Enter ConditionalAndExpression " + ctx.getText());       
      }

      @Override
      public void exitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
        System.out.println("Exit ConditionalAndExpression " + ctx.getText());      
      }

      @Override
      public void enterRelationalExpression(RelationalExpressionContext ctx) {
        // System.out.println("Enter RelationalExpression " + ctx.getText()); 
      }

      @Override
      public void exitRelationalExpression(RelationalExpressionContext ctx) {
        System.out.format("Exit RelationalExpression left=%s op=%s right=%s%n", 
            ctx.left.getText(), ctx.relationalOp.getText(), ctx.right.getText());       
      }

      @Override
      public void enterSimpleExpression(SimpleExpressionContext ctx) {
        // System.out.println("Enter SimpleExpression " + ctx.getText());      
      }

      @Override
      public void exitSimpleExpression(SimpleExpressionContext ctx) {
        System.out.println("Exit SimpleExpression " + ctx.getText());     
      }

      @Override
      public void enterTerm(TermContext ctx) {
        // System.out.println("Enter Term " + ctx.getText());
      }

      @Override
      public void exitTerm(TermContext ctx) {
        System.out.println("Exit Term " + ctx.getText());
      }

      @Override
      public void enterSetExpression(SetExpressionContext ctx) {
        // System.out.println("Enter SetExpression " + ctx.getText());       
      }

      @Override
      public void exitSetExpression(SetExpressionContext ctx) {
        System.out.println("Exit SetExpression " + ctx.factor().stream().map(FactorContext::getText).collect(Collectors.toList()));      
      }

      @Override
      public void enterRangeExpression(RangeExpressionContext ctx) {
        // System.out.println("Enter RangeExpression " + ctx.getText());      
      }

      @Override
      public void exitRangeExpression(RangeExpressionContext ctx) {
        System.out.format("Exit RangeExpression; min=%s max=%s%n", ctx.min.getText(), ctx.max.getText());        
      }

      @Override
      public void enterFactor(FactorContext ctx) {
        // System.out.println("Enter Factor " + ctx.getText());     
      }

      @Override
      public void exitFactor(FactorContext ctx) {
        System.out.println("Exit Factor " + ctx.getText());       
      }

      @Override
      public void enterValue(ValueContext ctx) {
        // System.out.println("Enter Value " + ctx.getText());      
      }

      @Override
      public void exitValue(ValueContext ctx) {
        System.out.println("Exit Value " + ctx.getText());
        
      }

      @Override
      public void enterVariable(VariableContext ctx) {
        // System.out.println("Enter Variable " + ctx.getText());   
      }

      @Override
      public void exitVariable(VariableContext ctx) {
        System.out.println("Exit Variable " + ctx.getText());
        
      }

      @Override
      public void enterQualifiedId(QualifiedIdContext ctx) {
        // System.out.println("Enter QualifiedId " + ctx.getText());       
      }

      @Override
      public void exitQualifiedId(QualifiedIdContext ctx) {
        System.out.println("Exit QualifiedId " + ctx.Identifier());      
      }
      
    });
    p.anyExpression();
  }
}
