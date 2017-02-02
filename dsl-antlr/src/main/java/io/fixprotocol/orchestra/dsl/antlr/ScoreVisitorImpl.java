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

import java.math.BigDecimal;
import java.util.List;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AddSubContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.BooleanNotContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.CharacterContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ContainsContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DecimalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.EqualityContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ExprContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IndexContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IntegerContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalAndContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalOrContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.MulDivContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ParensContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.PredContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.QualContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.StringContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VarContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VariableContext;

/**
 * Evaluates Score DSL expressions
 * 
 * @author Don Mendelson
 *
 */
public class ScoreVisitorImpl extends AbstractParseTreeVisitor<FixValue<?>>
    implements ScoreVisitor<FixValue<?>> {

  private final SemanticErrorListener errorListener;
  private final SymbolResolver symbolResolver;
  private PathStep pathStep;
  private Scope currentScope;

  /**
   * Constructor with default SemanticErrorListener
   * 
   * @param symbolResolver resolves symbols in variable and message spaces
   */
  public ScoreVisitorImpl(SymbolResolver symbolResolver) {
    this(symbolResolver, new BaseSemanticErrorListener());
  }

  /**
   * Constructor
   * 
   * @param symbolResolver resolves symbols in variable and message spaces
   * @param errorListener listens for semantic errors
   */
  public ScoreVisitorImpl(SymbolResolver symbolResolver, SemanticErrorListener errorListener) {
    this.symbolResolver = symbolResolver;
    this.errorListener = errorListener;
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAddSub(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.AddSubContext)
   */
  @Override
  public FixValue<?> visitAddSub(AddSubContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));

    try {
      switch (ctx.op.getText()) {
        case "+":
          return operand0.add(operand1);
        case "-":
          return operand0.subtract(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAnyExpression(io.fixprotocol.orchestra.dsl
   * .antlr.ScoreParser.AnyExpressionContext)
   */
  @Override
  public FixValue<?> visitAnyExpression(AnyExpressionContext ctx) {
    return visitChildren(ctx);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitAssignment(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.AssignmentContext)
   */
  @Override
  public FixValue<?> visitAssignment(AssignmentContext ctx) {
    FixValue<?> val = visit(ctx.expr());
    FixValue<?> var = visitVar(ctx.var());
    try {
      if (var != null) {
        var.assign(val);
        return var;
      } else {
        return currentScope.assign(pathStep, FixValue.copy(pathStep.getName(), val));
      }
    } catch (FixException e) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", e.getMessage(), ctx.getText()));
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitBooleanNot(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.BooleanNotContext)
   */
  @Override
  public FixValue<?> visitBooleanNot(BooleanNotContext ctx) {
    FixValue<?> operand = visit(ctx.expr());
    try {
      return operand.not();
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitCharacter(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.CharacterContext)
   */
  @Override
  public FixValue<?> visitCharacter(CharacterContext ctx) {
    return new FixValue<Character>(FixType.charType,
        Character.valueOf(ctx.CHAR().getText().charAt(1)));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitContains(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.ContainsContext)
   */
  @Override
  public FixValue<?> visitContains(ContainsContext ctx) {
    FixValue<?> operand0 = visit(ctx.val);
    for (ExprContext memberExpr : ctx.member) {
      FixValue<?> member = visit(memberExpr);
      if ((Boolean) operand0.eq(member).getValue()) {
        return new FixValue<Boolean>(FixType.BooleanType, Boolean.TRUE);
      }
    }

    return new FixValue<Boolean>(FixType.BooleanType, Boolean.FALSE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitDecimal(io.fixprotocol.orchestra.dsl.antlr
   * .ScoreParser.DecimalContext)
   */
  @Override
  public FixValue<?> visitDecimal(DecimalContext ctx) {
    return new FixValue<BigDecimal>(FixType.floatType, new BigDecimal(ctx.DECIMAL().getText()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitEquality(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.EqualityContext)
   */
  @Override
  public FixValue<?> visitEquality(EqualityContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));

    try {
      switch (ctx.op.getText()) {
        case "==":
        case "eq":
          return operand0.eq(operand1);
        case "!=":
        case "ne":
          return operand0.ne(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitIndex(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.IndexContext)
   */
  @Override
  public FixValue<?> visitIndex(IndexContext ctx) {
    if (ctx.INT() != null) {
      pathStep.setIndex(Integer.parseInt(ctx.INT().getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitInteger(io.fixprotocol.orchestra.dsl.antlr
   * .ScoreParser.IntegerContext)
   */
  @Override
  public FixValue<?> visitInteger(IntegerContext ctx) {
    return new FixValue<Integer>(FixType.intType, Integer.parseInt(ctx.INT().getText()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitLogicalAnd(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.LogicalAndContext)
   */
  @Override
  public FixValue<?> visitLogicalAnd(LogicalAndContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));
    try {
      switch (ctx.op.getText()) {
        case "&&":
        case "and":
          return operand0.and(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitLogicalOr(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.LogicalOrContext)
   */
  @Override
  public FixValue<?> visitLogicalOr(LogicalOrContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));

    try {
      switch (ctx.op.getText()) {
        case "||":
        case "or":
          return operand0.or(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitMulDiv(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.MulDivContext)
   */
  @Override
  public FixValue<?> visitMulDiv(MulDivContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));

    try {
      switch (ctx.op.getText()) {
        case "*":
          return operand0.multiply(operand1);
        case "/":
          return operand0.divide(operand1);
        case "%":
        case "mod":
          return operand0.mod(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitParens(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.ParensContext)
   */
  @Override
  public FixValue<?> visitParens(ParensContext ctx) {
    return visit(ctx.expr());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitPred(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.PredContext)
   */
  @Override
  public FixValue<?> visitPred(PredContext ctx) {
    TerminalNode id = ctx.ID();
    ExprContext expr = ctx.expr();
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitQual(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.QualContext)
   */
  @Override
  public FixValue<?> visitQual(QualContext ctx) {
    pathStep = new PathStep(ctx.ID().getText());

    IndexContext indexContext = ctx.index();
    if (indexContext != null) {
      visitIndex(indexContext);
    }
    PredContext predContext = ctx.pred();
    if (predContext != null) {
      String id = predContext.ID().getText();
      ExprContext expr = predContext.expr();
      // todo evaluate predicate expression
    }

    FixNode node = currentScope.resolve(pathStep);
    if (node instanceof Scope) {
      currentScope = (Scope) node;
      return null;
    } else if (node == null) {
      return null;
    } else {
      return (FixValue<?>) node;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitRange(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.RangeContext)
   */
  @Override
  public FixValue<?> visitRange(RangeContext ctx) {
    FixValue<?> val = visit(ctx.val);
    FixValue<?> min = visit(ctx.min);
    FixValue<?> max = visit(ctx.max);

    return val.ge(min).and(val.le(max));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitRelational(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.RelationalContext)
   */
  @Override
  public FixValue<?> visitRelational(RelationalContext ctx) {
    FixValue<?> operand0 = visit(ctx.expr(0));
    FixValue<?> operand1 = visit(ctx.expr(1));

    try {
      switch (ctx.op.getText()) {
        case "<":
        case "lt":
          return operand0.lt(operand1);
        case "<=":
        case "le":
          return operand0.le(operand1);
        case ">":
        case "gt":
          return operand0.gt(operand1);
        case ">=":
        case "ge":
          return operand0.ge(operand1);
      }
    } catch (Exception ex) {
      errorListener
          .onError(String.format("Semantic error; %s at '%s'", ex.getMessage(), ctx.getText()));
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitString(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.StringContext)
   */
  @Override
  public FixValue<?> visitString(StringContext ctx) {
    final String text = ctx.STRING().getText();
    return new FixValue<String>(FixType.StringType, text.substring(1, text.length() - 1));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitVar(io.fixprotocol.orchestra.dsl.antlr.
   * ScoreParser.VarContext)
   */
  @Override
  public FixValue<?> visitVar(VarContext ctx) {
    FixValue<?> value = null;
    currentScope = symbolResolver;
    pathStep = new PathStep(ctx.scope.getText());
    FixNode node = currentScope.resolve(pathStep);
    if (node instanceof Scope) {
      currentScope = (Scope) node;
      List<QualContext> qualifiers = ctx.qual();
      for (QualContext qualifier : qualifiers) {
        value = visitQual(qualifier);
      }
    } else {
      errorListener.onError(
          String.format("Unknown symbol scope; %s at '%s'", pathStep.getName(), ctx.getText()));
    }
    return value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.ScoreVisitor#visitVariable(io.fixprotocol.orchestra.dsl.
   * antlr.ScoreParser.VariableContext)
   */
  @Override
  public FixValue<?> visitVariable(VariableContext ctx) {
    return visit(ctx.var());
  }

}
