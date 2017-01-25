package io.fixprotocol.orchestra.dsl.antlr;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ConditionalAndExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ConditionalExpressionContext;
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
public class DslVisitorTest {

  private String fieldCondition;

  public DslVisitorTest(String fieldCondition) {
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

  private class TestScoreVisitor implements ScoreVisitor<Object> {

    @Override
    public Object visit(ParseTree tree) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Object visitChildren(RuleNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Object visitTerminal(TerminalNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public Object visitAnyExpression(AnyExpressionContext ctx) {
      AssignmentContext assignment = ctx.assignment();
      if (assignment != null) {
        visitAssignment(assignment);
        return assignment;
      } else {
        ConditionalOrExpressionContext orExpression = ctx.conditionalOrExpression();
        if (orExpression != null) {
          visitConditionalOrExpression(orExpression);
          return orExpression;
        }
      }
      return null;
    }

    @Override
    public Object visitAssignment(AssignmentContext ctx) {
      System.out.format("Assignment: ");
      visitVariable(ctx.variable());
      System.out.format(" op[%s] ", ctx.assignmentOp.getText());   
      visitSimpleExpression(ctx.simpleExpression());
      System.out.format("%n");
      return ctx;
    }

    @Override
    public Object visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
      System.out.format("Expression: ");
      Iterator<Token> opIter = ctx.orOp.iterator();
      List<ConditionalAndExpressionContext> andExpressions = ctx.conditionalAndExpression();
      for (ConditionalAndExpressionContext andExpression : andExpressions) {
        visitConditionalAndExpression(andExpression);
        if (opIter.hasNext()) {
          System.out.format(" op[%s] ", opIter.next().getText());
        }
      }
      System.out.format("%n");
      return ctx;
    }

    @Override
    public Object visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
      List<ConditionalExpressionContext> conditionalExpressions = ctx.conditionalExpression();
      Iterator<Token> opIter = ctx.andOp.iterator();
      for (ConditionalExpressionContext conditionalExpression : conditionalExpressions) {
        if (conditionalExpression instanceof RelationalExpressionContext) {
          visitRelationalExpression((RelationalExpressionContext) conditionalExpression);
        } else if (conditionalExpression instanceof SetExpressionContext) {
          visitSetExpression((SetExpressionContext) conditionalExpression);
        } else if (conditionalExpression instanceof RangeExpressionContext) {
          visitRangeExpression((RangeExpressionContext) conditionalExpression);
        }
        
        if (opIter.hasNext()) {
          System.out.format(" op[%s] ", opIter.next().getText());
        }
      }
      return ctx;
    }

    @Override
    public Object visitRelationalExpression(RelationalExpressionContext ctx) {
      visitSimpleExpression(ctx.left);
      System.out.format(" op[%s] ", ctx.relationalOp.getText());
      visitFactor(ctx.right);
      return ctx;
    }

    @Override
    public Object visitSimpleExpression(SimpleExpressionContext ctx) {
      List<TermContext> terms = ctx.term();
      Iterator<Token> opIter = ctx.termOp.iterator();
      for (TermContext term : terms) {
        visitTerm(term);
        if (opIter.hasNext()) {
          System.out.format(" op[%s] ",opIter.next().getText());
        }
      }
      return ctx;
    }

    @Override
    public Object visitTerm(TermContext ctx) {
      List<FactorContext> factors = ctx.factor();
      Iterator<Token> opIter = ctx.factorOp.iterator();
      for (FactorContext factor : factors) {
        visitFactor(factor);
        if (opIter.hasNext()) {
          System.out.format(" op[%s] ",opIter.next().getText());
        }
      }
      return ctx;
    }

    @Override
    public Object visitSetExpression(SetExpressionContext ctx) {
      visitSimpleExpression(ctx.left);
      List<FactorContext> factors = ctx.factor();
      System.out.format("set[");
      factors.forEach(f -> visitFactor(f));
      System.out.format("]", ctx.factor);
      return ctx;
    }

    @Override
    public Object visitRangeExpression(RangeExpressionContext ctx) {
      visitSimpleExpression(ctx.left);
      System.out.format("range[%s..%s]", ctx.min.getText(), ctx.max.getText());
      return ctx;
    }

    @Override
    public Object visitFactor(FactorContext ctx) {
      if (ctx.val != null) {
        visitValue(ctx.val);
      } else if (ctx.var != null) {
        visitVariable(ctx.var);
      } else {
        System.out.format("val[%s]", ctx.getText());
      }
      return ctx; 
    }

    @Override
    public Object visitValue(ValueContext ctx) {
      System.out.format("val[%s]", ctx.getText());
      return ctx;
    }

    @Override
    public Object visitVariable(VariableContext ctx) {
      System.out.format("var[%s]", ctx.getText());
      return ctx;
    }

    @Override
    public Object visitQualifiedId(QualifiedIdContext ctx) {
      List<TerminalNode> nodes = ctx.Identifier();
      return ctx;
    }

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
    TestScoreVisitor visitor = new TestScoreVisitor();
    AnyExpressionContext ctx = p.anyExpression();
    Object expression = visitor.visitAnyExpression(ctx);
    //System.out.println(expression.getClass().getSimpleName());
  }
}
