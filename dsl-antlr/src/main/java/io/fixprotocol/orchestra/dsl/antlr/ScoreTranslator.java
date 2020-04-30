/*
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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AddSubContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AssignmentContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.CharacterContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ContainsContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DateonlyContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DecimalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.DurationContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.EqualityContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ExistContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ExprContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IndexContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.IntegerContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalAndContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalNotContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.LogicalOrContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.MulDivContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.ParensContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.PredContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.QualContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RangeContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.RelationalContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.StringContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.TimeonlyContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.TimestampContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.UnaryMinusContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VarContext;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.VariableContext;
import io.fixprotocol.orchestra.dsl.datetime.DateTimeFormatters;


/**
 * Parses Score DSL expressions and translates them to natural language
 *
 * @author Don Mendelson
 *
 */
class ScoreTranslator extends AbstractParseTreeVisitor<String> implements ScoreVisitor<String> {

  private static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap() {
    return Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue);
  }

  private static <K, V> Map.Entry<K, V> entry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  private static String ordinal(int i) {
    final String[] sufixes =
        new String[] {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
    switch (i % 100) {
      case 11:
      case 12:
      case 13:
        return i + "th";
      default:
        return i + sufixes[i % 10];
    }
  }

  // todo: add language code as parameter to construct map
  private static Map<String, String> tokenMap() {
    return Collections
        .unmodifiableMap(Stream
            .of(entry("eq", "equals"), entry("==", "equals"), entry("ne", "does not equal"),
                entry("!=", "does not equal"), entry("+", "plus"), entry("-", "minus"),
                entry("<", "less than"), entry("lt", "less than"),
                entry("<=", "less than or equal to"), entry("le", "less than or equal to"),
                entry(">", "greater than"), entry("gt", "greater than"),
                entry(">=", "greater than or equal to"), entry("ge", "greater than or equal to"),
                entry("*", "times"), entry("/", "divided by"), entry("%", "modulo"),
                entry("mod", "modulo"), entry("&&", "and"), entry("and", "and"), entry("||", "or"),
                entry("or", "or"), entry("!", "not"), entry("not", "not"), entry("=", "is set to"),
                entry("if", "if"), entry("exists", "exists"), entry("between", "between"),
                entry("in", "equals one of"), entry("where", "where"), entry("$", "variable"),
                entry("in.", "incoming"), entry("out.", "outgoing"), entry(/* code */ "^", ""))
            .collect(entriesToMap()));
  }

  private final SemanticErrorListener errorListener;
  private final Map<String, String> tokenMap;
  private boolean trace = false;

  /**
   * Constructor with default SemanticErrorListener
   */
  public ScoreTranslator() {
    this(new BaseSemanticErrorListener());
  }

  /**
   * Constructor
   *
   * @param errorListener listens for semantic errors
   */
  public ScoreTranslator(SemanticErrorListener errorListener) {
    this.errorListener = errorListener;
    this.tokenMap = tokenMap();
  }


  /**
   * @return the trace
   */
  public boolean isTrace() {
    return trace;
  }

  /**
   * @param trace the trace to set
   */
  public void setTrace(boolean trace) {
    this.trace = trace;
  }

  @Override
  public String visitAddSub(AddSubContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));

    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);
  }

  @Override
  public String visitAnyExpression(AnyExpressionContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public String visitAssignment(AssignmentContext ctx) {
    final String val = visit(ctx.expr());
    if (val == null) {
      errorListener.onError(
          String.format("Semantic error; missing val for assignment at '%s'", ctx.getText()));
      return null;
    }
    final String var = visitVar(ctx.var());

    return String.format("%s %s %s", var, translateToken("="), val);
  }

  @Override
  public String visitCharacter(CharacterContext ctx) {
    return ctx.CHAR().getText().substring(1);
  }

  @Override
  public String visitContains(ContainsContext ctx) {
    final String val = visit(ctx.val);
    final List<String> memberStrings =
        ctx.member.stream().map(this::visit).collect(Collectors.toList());
    return String.format("%s %s %s %s", translateToken("if"), val, translateToken("in"),
        String.join(", ", memberStrings));
  }

  @Override
  public String visitDateonly(DateonlyContext ctx) {
    return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
        .format(LocalDate.parse(ctx.DATE().getText()));
  }

  @Override
  public String visitDecimal(DecimalContext ctx) {
    return ctx.DECIMAL().getText();
  }

  @Override
  public String visitDuration(DurationContext ctx) {
    // Remove PT prefix, expand units
    return Duration.parse(ctx.PERIOD().getText()).toString().substring(2).replace("D", " days")
        .replace("H", " hours").replace("M", " minutes").replace("S", " seconds");
  }

  @Override
  public String visitEquality(EqualityContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));

    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);
  }

  @Override
  public String visitExist(ExistContext ctx) {
    return String.format("%s %s %s", translateToken("if"), visit(ctx.var()),
        translateToken("exists"));
  }

  @Override
  public String visitIndex(IndexContext ctx) {
    if (ctx.UINT() != null) {
      return ordinal(Integer.parseInt(ctx.UINT().getText()));
    }
    return "";
  }

  @Override
  public String visitInteger(IntegerContext ctx) {
    return ctx.UINT().getText();
  }

  @Override
  public String visitLogicalAnd(LogicalAndContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));
    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);
  }

  @Override
  public String visitLogicalNot(LogicalNotContext ctx) {
    return String.format("%s %s", translateToken("!"), visit(ctx.expr()));
  }

  @Override
  public String visitLogicalOr(LogicalOrContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));
    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);

  }

  @Override
  public String visitMulDiv(MulDivContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));
    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);
  }

  @Override
  public String visitParens(ParensContext ctx) {
    return visit(ctx.expr());
  }

  @Override
  public String visitPred(PredContext ctx) {
    final TerminalNode id = ctx.ID();
    final ExprContext expr = ctx.expr();
    return null;
  }

  @Override
  public String visitQual(QualContext ctx) {
    final String id = ctx.ID().getText();

    final IndexContext indexContext = ctx.index();
    if (indexContext != null) {
      return String.format("%s %s", visitIndex(indexContext), id);
    }
    final PredContext predContext = ctx.pred();
    if (predContext != null) {
      final String predId = predContext.ID().getText();
      final ExprContext expr = predContext.expr();
      return String.format("%s %s %s %s %s", id, translateToken("where"), predId,
          translateToken("=="), visit(expr));
    }
    return id;
  }

  @Override
  public String visitRange(RangeContext ctx) {
    final String val = visit(ctx.val);
    final String min = visit(ctx.min);
    final String max = visit(ctx.max);

    return String.format("%s %s %s %s %s %s", translateToken("if"), val, translateToken("between"),
        min, translateToken("and"), max);
  }

  @Override
  public String visitRelational(RelationalContext ctx) {
    final String operand0 = visit(ctx.expr(0));
    final String operand1 = visit(ctx.expr(1));
    return String.format("%s %s %s", operand0, translateToken(ctx.op.getText()), operand1);
  }

  @Override
  public String visitString(StringContext ctx) {
    return ctx.STRING().getText().substring(1, ctx.STRING().getText().length() - 1);
  }

  @Override
  public String visitTimeonly(TimeonlyContext ctx) {
    // Remove initial T and timestamp for Java, even though ISO require them
    // and the translate to localized format
    final LocalTime localTime = LocalTime.parse(ctx.TIME().getText(), DateTimeFormatters.TIME_ONLY);
    return DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withZone(ZoneId.systemDefault())
        .format(localTime);
  }

  @Override
  public String visitTimestamp(TimestampContext ctx) {
    // Parse as ISO and the translate to localized format
    final Instant instant =
        DateTimeFormatters.DATE_TIME.parse(ctx.DATETIME().getText(), Instant::from);
    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.of("Z"))
        .format(instant);
  }

  @Override
  public String visitUnaryMinus(UnaryMinusContext ctx) {
    // Keep minus symbol instead of word
    return String.format("-%s", visit(ctx.expr()));
  }

  @Override
  public String visitVar(VarContext ctx) {
    String scopeText;
    if (ctx.scope == null) {
      // implicit scope
      scopeText = "in.";
    } else {
      scopeText = ctx.scope.getText();
    }

    final List<QualContext> qualifiers = ctx.qual();

    final List<String> qualStrings =
        qualifiers.stream().map(this::visit).collect(Collectors.toList());
    return String.format("%s %s", translateToken(scopeText), String.join("-", qualStrings));
  }

  @Override
  public String visitVariable(VariableContext ctx) {
    return visit(ctx.var());
  }

  private Object translateToken(String token) {
    return tokenMap.get(token);
  }

}
