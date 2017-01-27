package io.fixprotocol.orchestra.dsl.antlr;

import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AddSubContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.BooleanNotContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.CharacterContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ContainsContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DecimalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.EqualityContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IndexContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IntegerContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalAndContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalOrContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.MulDivContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ParensContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.PredContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.StringContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VarContext;
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
      {"this.Price between 50.00 and 100.00"},
      {"this.Parties[PartyRole=4].PartyID=\"690\""},{"this.Parties[1].PartyRole=4"}});
  }

  private class TestScoreVisitor implements ScoreVisitor<Object> {

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeVisitor#visit(org.antlr.v4.runtime.tree.ParseTree)
     */
    @Override
    public Object visit(ParseTree tree) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeVisitor#visitChildren(org.antlr.v4.runtime.tree.RuleNode)
     */
    @Override
    public Object visitChildren(RuleNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeVisitor#visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
     */
    @Override
    public Object visitTerminal(TerminalNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeVisitor#visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
     */
    @Override
    public Object visitErrorNode(ErrorNode node) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAnyExpression(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext)
     */
    @Override
    public Object visitAnyExpression(AnyExpressionContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAssignment(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext)
     */
    @Override
    public Object visitAssignment(AssignmentContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitParens(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ParensContext)
     */
    @Override
    public Object visitParens(ParensContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitString(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.StringContext)
     */
    @Override
    public Object visitString(StringContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitLogicalAnd(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalAndContext)
     */
    @Override
    public Object visitLogicalAnd(LogicalAndContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAddSub(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AddSubContext)
     */
    @Override
    public Object visitAddSub(AddSubContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitInteger(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IntegerContext)
     */
    @Override
    public Object visitInteger(IntegerContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitMulDiv(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.MulDivContext)
     */
    @Override
    public Object visitMulDiv(MulDivContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitCharacter(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.CharacterContext)
     */
    @Override
    public Object visitCharacter(CharacterContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitContains(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ContainsContext)
     */
    @Override
    public Object visitContains(ContainsContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitVariable(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VariableContext)
     */
    @Override
    public Object visitVariable(VariableContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitRelational(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalContext)
     */
    @Override
    public Object visitRelational(RelationalContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitBooleanNot(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.BooleanNotContext)
     */
    @Override
    public Object visitBooleanNot(BooleanNotContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitDecimal(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DecimalContext)
     */
    @Override
    public Object visitDecimal(DecimalContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitLogicalOr(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalOrContext)
     */
    @Override
    public Object visitLogicalOr(LogicalOrContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitEquality(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.EqualityContext)
     */
    @Override
    public Object visitEquality(EqualityContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitRange(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeContext)
     */
    @Override
    public Object visitRange(RangeContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitVar(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VarContext)
     */
    @Override
    public Object visitVar(VarContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitIndex(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IndexContext)
     */
    @Override
    public Object visitIndex(IndexContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitPred(io.fixprotocol.orchestra.dsl.antlr.ScoreParser.PredContext)
     */
    @Override
    public Object visitPred(PredContext ctx) {
      // TODO Auto-generated method stub
      return null;
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
