/*
 * Copyright 2017-2020 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.model.quickfix;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import io.fixprotocol._2020.orchestra.repository.CodeSetType;
import io.fixprotocol._2020.orchestra.repository.FieldRefType;
import io.fixprotocol._2020.orchestra.repository.GroupRefType;
import io.fixprotocol._2020.orchestra.repository.GroupType;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.message.CodeSetScope;
import io.fixprotocol.orchestra.model.FixNode;
import io.fixprotocol.orchestra.model.FixType;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.FixValueFactory;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.BytesField;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.Group;

/**
 * @author Don Mendelson
 *
 */
abstract class AbstractMessageScope {

  private final Evaluator evaluator;
  private final FieldMap fieldMap;
  private final RepositoryAccessor repository;
  private final SymbolResolver symbolResolver;

  protected AbstractMessageScope(FieldMap fieldMap, RepositoryAccessor repository,
      SymbolResolver symbolResolver, Evaluator evaluator) {
    this.fieldMap = fieldMap;
    this.repository = repository;
    this.symbolResolver = symbolResolver;
    this.evaluator = evaluator;
  }

  protected void assignField(FieldRefType fieldRefType, FixValue fixValue) {
    final int id = fieldRefType.getId().intValue();
    final String scenario = fieldRefType.getScenario();
    String dataTypeString = repository.getFieldDatatype(id, scenario);
    final CodeSetType codeSet = repository.getCodeset(dataTypeString, scenario);
    if (codeSet != null) {
      dataTypeString = codeSet.getType();
    }
    final FixType dataType = FixType.forName(dataTypeString);
    switch (dataType) {
      case StringType:
      case MultipleCharValue:
      case MultipleStringValue:
      case Country:
      case Currency:
      case Exchange:
      case MonthYear:
      case XMLData:
      case Language:
        fieldMap.setString(id, (String) fixValue.getValue());
        break;
      case BooleanType:
        fieldMap.setBoolean(id, (boolean) fixValue.getValue());
        break;
      case charType:
        fieldMap.setChar(id, (char) fixValue.getValue());
        break;
      case intType:
      case Length:
      case TagNum:
      case SeqNum:
      case NumInGroup:
      case DayOfMonth:
        fieldMap.setInt(id, (int) fixValue.getValue());
        break;
      case Amt:
      case floatType:
      case Qty:
      case Price:
      case PriceOffset:
      case Percentage:
        fieldMap.setDecimal(id, (BigDecimal) fixValue.getValue());
        break;
      case UTCTimestamp:
      case TZTimestamp:
        fieldMap.setUtcTimeStamp(id, (LocalDateTime) fixValue.getValue());
        break;
      case UTCTimeOnly:
      case TZTimeOnly:
      case LocalMktTime:
        fieldMap.setUtcTimeOnly(id, (LocalTime) fixValue.getValue());
        break;
      case UTCDateOnly:
      case LocalMktDate:
        fieldMap.setUtcDateOnly(id, (LocalDate) fixValue.getValue());
        break;
      case data:
        final BytesField bytesField = new BytesField(id, (byte[]) fixValue.getValue());
        fieldMap.setField(bytesField);
        break;
      case Duration:
        // todo
        break;
    }
  }

  protected RepositoryAccessor getRepository() {
    return repository;
  }

  @SuppressWarnings("unchecked")
  protected FixNode resolveField(FieldRefType fieldRefType) {
    final int id = fieldRefType.getId().intValue();
    final String scenario = fieldRefType.getScenario();
    final String name = repository.getFieldName(id, scenario);
    @SuppressWarnings("rawtypes")
    FixValue fixValue = null;
    String dataTypeString = repository.getFieldDatatype(id, scenario);
    final CodeSetType codeSet = repository.getCodeset(dataTypeString, scenario);
    if (codeSet != null) {
      dataTypeString = codeSet.getType();
      symbolResolver.nest(new PathStep("^"), new CodeSetScope(codeSet));
    }

    final FixType dataType = FixType.forName(dataTypeString);
    try {
      switch (dataType) {
        case StringType:
        case MultipleCharValue:
        case MultipleStringValue:
        case Country:
        case Currency:
        case Exchange:
        case MonthYear:
        case XMLData:
        case Language:
          fixValue = new FixValue<String>(name, dataType);
          ((FixValue<String>) fixValue).setValue(fieldMap.getString(id));
          break;
        case BooleanType:
          fixValue = new FixValue<Boolean>(name, dataType);
          ((FixValue<Boolean>) fixValue).setValue(fieldMap.getBoolean(id));
          break;
        case charType:
          fixValue = new FixValue<Character>(name, dataType);
          ((FixValue<Character>) fixValue).setValue(fieldMap.getChar(id));
          break;
        case intType:
        case Length:
        case TagNum:
        case SeqNum:
        case NumInGroup:
        case DayOfMonth:
          fixValue = new FixValue<Integer>(name, dataType);
          ((FixValue<Integer>) fixValue).setValue(fieldMap.getInt(id));
          break;
        case Amt:
        case floatType:
        case Qty:
        case Price:
        case PriceOffset:
        case Percentage:
          fixValue = new FixValue<BigDecimal>(name, dataType);
          ((FixValue<BigDecimal>) fixValue).setValue(fieldMap.getDecimal(id));
          break;
        case UTCTimestamp:
        case TZTimestamp:
          fixValue = new FixValue<LocalDateTime>(name, dataType);
          ((FixValue<LocalDateTime>) fixValue).setValue(fieldMap.getUtcTimeStamp(id));
          break;
        case UTCTimeOnly:
        case TZTimeOnly:
        case LocalMktTime:
          fixValue = new FixValue<LocalTime>(name, dataType);
          ((FixValue<LocalTime>) fixValue).setValue(LocalTime.from(fieldMap.getUtcTimeOnly(id)));
          break;
        case UTCDateOnly:
        case LocalMktDate:
          fixValue = new FixValue<LocalDate>(name, dataType);
          ((FixValue<LocalDate>) fixValue).setValue(LocalDate.from(fieldMap.getUtcTimeOnly(id)));
          break;
        case data:
          fixValue = new FixValue<byte[]>(name, dataType);
          final BytesField bytesField = new BytesField(id);
          fieldMap.getField(bytesField);
          ((FixValue<byte[]>) fixValue).setValue(bytesField.getValue());
          break;
        case Duration:
          // todo
          break;
      }
    } catch (final FieldNotFound e) {
      // Set default value if field is not present
      final String defaultValue = fieldRefType.getValue();
      if (defaultValue != null) {
        final Class<?> valueClass = dataType.getValueClass();
        try {
          fixValue = FixValueFactory.create(null, dataType, valueClass);
          fixValue.setValue(valueClass.cast(dataType.fromString(defaultValue)));
        } catch (final ModelException e1) {

        }
      }
    }
    return fixValue;
  }

  protected FixNode resolveGroup(PathStep pathStep, GroupRefType groupRefType) {
    final GroupType groupType = repository.getGroup(groupRefType);
    final int index = pathStep.getIndex();
    final String predicate = pathStep.getPredicate();
    if (index != PathStep.NO_INDEX) {
      Group group;
      try {
        // Both PathStep and QuickFIX use one-based index for group entries
        group = fieldMap.getGroup(index, (groupType.getNumInGroup().getId().intValue()));
      } catch (final FieldNotFound e) {
        return null;
      }
      return new GroupInstanceScope(group, groupType, repository, symbolResolver, evaluator);
    } else if (predicate != null) {
      final List<Group> groups = fieldMap.getGroups(groupType.getNumInGroup().getId().intValue());
      for (final Group group : groups) {
        final GroupInstanceScope scope =
            new GroupInstanceScope(group, groupType, repository, symbolResolver, evaluator);
        final Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT);
        local.nest(new PathStep(groupType.getName()), scope);
        FixValue<?> fixValue;
        try {
          fixValue = evaluator.evaluate(predicate);
          if (fixValue.getValue() == Boolean.TRUE) {
            return scope;
          }
        } catch (final ScoreException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      return null;
    } else
      return null;
  }

}
