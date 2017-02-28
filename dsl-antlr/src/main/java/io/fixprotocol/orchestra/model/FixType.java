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
package io.fixprotocol.orchestra.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * FIX data types <br/>
 * Types are hard-coded for now. In the future, consider populating dynamically from datatypes in
 * Orchestra file.
 * 
 * @author Don Mendelson
 *
 */
public enum FixType {
  Amt {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  BooleanType {
    @Override
    public FixType getBaseType() {
      return BooleanType;
    }

    @Override
    public Class<Boolean> getValueClass() {
      return Boolean.class;
    }

    @Override
    public Boolean fromString(String text) {
      return Boolean.parseBoolean(text);
    }
  },
  charType {
    @Override
    public FixType getBaseType() {
      return charType;
    }

    @Override
    public Class<Character> getValueClass() {
      return Character.class;
    }

    @Override
    public Character fromString(String text) {
      return text.charAt(0);
    }
  },
  Country {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  Currency {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  data {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<byte[]> getValueClass() {
      return byte[].class;
    }

    @Override
    public byte[] fromString(String text) {
      return text.getBytes();
    }
  },
  DayOfMonth {
    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }

    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }
  },
  Exchange {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  floatType {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  intType {
    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }

    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }
  },
  Language {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  Length {
    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }


    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }
  },
  LocalMktDate {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalDate> getValueClass() {
      return LocalDate.class;
    }

    @Override
    public LocalDate fromString(String text) {
      return LocalDate.parse(text);
    }
  },
  LocalMktTime {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalTime> getValueClass() {
      return LocalTime.class;
    }

    @Override
    public LocalTime fromString(String text) {
      return LocalTime.parse(text);
    }
  },
  MonthYear {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  MultipleCharValue {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  MultipleStringValue {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  NumInGroup {
    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }


    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }
  },
  Percentage {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  Price {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  PriceOffset {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  Qty {
    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }

    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }
  },
  SeqNum {
    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  StringType {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
       return text;
    }
  },
  TagNum {
    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }


    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }
  },
  TZTimeOnly {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<ZonedDateTime> getValueClass() {
      return ZonedDateTime.class;
    }

    @Override
    public ZonedDateTime fromString(String text) {
      return ZonedDateTime.parse(text);
    }
  },
  TZTimestamp {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<ZonedDateTime> getValueClass() {
      return ZonedDateTime.class;
    }

    @Override
    public ZonedDateTime fromString(String text) {
      return ZonedDateTime.parse(text);
    }
  },
  UTCDateOnly {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalDate> getValueClass() {
      return LocalDate.class;
    }

    @Override
    public LocalDate fromString(String text) {
      return LocalDate.parse(text);
    }
  },
  UTCTimeOnly {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalTime> getValueClass() {
      return LocalTime.class;
    }

    @Override
    public LocalTime fromString(String text) {
      return LocalTime.parse(text);
    }
  },
  UTCTimestamp {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<Instant> getValueClass() {
      return Instant.class;
    }

    @Override
    public Instant fromString(String text) {
      return Instant.parse(text);
    }
  },
  XMLData {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public String fromString(String text) {
      return text;
    }
  },
  /**
   * Not currently a FIX data type (but should be since there are fields with duration semantics)
   */
  Duration {
    @Override
    public FixType getBaseType() {
      return null;
    }

    @Override
    public Class<?> getValueClass() {
      return java.time.Duration.class;
    }

    @Override
    public java.time.Duration fromString(String text) {
      return java.time.Duration.parse(text);
    }
  } 
  ;

  /**
   * Returns the enum value for its name 
   * <br/>
   * Semantically the same as {@code enum.valueOf()} but Java doesn't allow that method to be
   * overridden. This method is necessary because some of the FIX data types are Java keywords.
   * 
   * @param name a FIX data type name
   * @return an enum value
   * @throws IllegalArgumentException - if there is no constant with the specified name
   */
  public static FixType forName(String name) {
    String dataTypeString = name;
    switch (dataTypeString) {
      case "String":
        dataTypeString = "StringType";
        break;
      case "Boolean":
        dataTypeString = "BooleanType";
        break;
      case "char":
        dataTypeString = "charType";
        break;
      case "int":
        dataTypeString = "intType";
        break;
      case "float":
        dataTypeString = "floatType";
        break;
    }
    return FixType.valueOf(dataTypeString);
  }

  /**
   * The base FIXType according to the FIX specification <br/>
   * Unfortunately, the FIX taxonomy of types is largely lexical, not semantic.
   * 
   * @return base data type
   */
  public abstract FixType getBaseType();

  /**
   * The default class used for storage of the FIX data type in this implementation
   * 
   * @return a Java class
   */
  public abstract Class<?> getValueClass();
  
  /**
   * Converts a String to an instance of the default storage class
   * @param text value serialized as a String
   * @return an object the class returned by {@link #getValueClass()}
   */
  public abstract Object fromString(String text);
}
