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
package io.fixprotocol.orchestra.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * FIX data types
 * <p>
 * Types are hard-coded for now. In the future, consider populating dynamically from datatypes in
 * Orchestra file.
 *
 * @author Don Mendelson
 *
 */
public enum FixType {
  Amt {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  BooleanType {
    @Override
    public Boolean fromString(String text) {
      return Boolean.parseBoolean(text);
    }

    @Override
    public FixType getBaseType() {
      return BooleanType;
    }

    @Override
    public Class<Boolean> getValueClass() {
      return Boolean.class;
    }
  },
  charType {
    @Override
    public Character fromString(String text) {
      return text.charAt(0);
    }

    @Override
    public FixType getBaseType() {
      return charType;
    }

    @Override
    public Class<Character> getValueClass() {
      return Character.class;
    }
  },
  Country {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  Currency {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  data {
    @Override
    public byte[] fromString(String text) {
      return text.getBytes();
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<byte[]> getValueClass() {
      return byte[].class;
    }
  },
  DayOfMonth {
    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }

    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  Exchange {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  floatType {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  intType {
    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }

    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  Language {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  Length {
    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }


    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  LocalMktDate {
    @Override
    public LocalDate fromString(String text) {
      return LocalDate.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalDate> getValueClass() {
      return LocalDate.class;
    }
  },
  LocalMktTime {
    @Override
    public LocalTime fromString(String text) {
      return LocalTime.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalTime> getValueClass() {
      return LocalTime.class;
    }
  },
  MonthYear {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  MultipleCharValue {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  MultipleStringValue {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  NumInGroup {
    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }


    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  Percentage {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  Price {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  PriceOffset {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  Qty {
    @Override
    public BigDecimal fromString(String text) {
      return new BigDecimal(text);
    }

    @Override
    public FixType getBaseType() {
      return floatType;
    }

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
    }
  },
  SeqNum {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  StringType {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  TagNum {
    @Override
    public Integer fromString(String text) {
      return Integer.parseInt(text);
    }


    @Override
    public FixType getBaseType() {
      return intType;
    }


    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }
  },
  TZTimeOnly {
    @Override
    public ZonedDateTime fromString(String text) {
      return ZonedDateTime.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<ZonedDateTime> getValueClass() {
      return ZonedDateTime.class;
    }
  },
  TZTimestamp {
    @Override
    public ZonedDateTime fromString(String text) {
      return ZonedDateTime.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<ZonedDateTime> getValueClass() {
      return ZonedDateTime.class;
    }
  },
  UTCDateOnly {
    @Override
    public LocalDate fromString(String text) {
      return LocalDate.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalDate> getValueClass() {
      return LocalDate.class;
    }
  },
  UTCTimeOnly {
    @Override
    public LocalTime fromString(String text) {
      return LocalTime.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<LocalTime> getValueClass() {
      return LocalTime.class;
    }
  },
  UTCTimestamp {
    @Override
    public Instant fromString(String text) {
      return Instant.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<Instant> getValueClass() {
      return Instant.class;
    }
  },
  XMLData {
    @Override
    public String fromString(String text) {
      return text;
    }

    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<String> getValueClass() {
      return String.class;
    }
  },
  /**
   * Not currently a FIX data type (but should be since there are fields with duration semantics)
   */
  Duration {
    @Override
    public java.time.Duration fromString(String text) {
      return java.time.Duration.parse(text);
    }

    @Override
    public FixType getBaseType() {
      return null;
    }

    @Override
    public Class<?> getValueClass() {
      return java.time.Duration.class;
    }
  };

  /**
   * Returns the enum value for its name
   * <p>
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
   * Converts a String to an instance of the default storage class
   * 
   * @param text value serialized as a String
   * @return an object the class returned by {@link #getValueClass()}
   */
  public abstract Object fromString(String text);

  /**
   * The base FIXType according to the FIX specification
   * <p>
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
}
