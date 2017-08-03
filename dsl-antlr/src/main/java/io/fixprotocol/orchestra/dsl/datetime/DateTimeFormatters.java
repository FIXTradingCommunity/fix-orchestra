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
package io.fixprotocol.orchestra.dsl.datetime;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Flexible date-time formatters for ISO 8601 formats
 * @author Don Mendelson
 *
 */
public final class DateTimeFormatters {

  /**
   * Formatter for ISO 8601 date-time.
   * 
   * Java has ISO_DATE_TIME, but it doesn't handle time zone offset.
   */
  public static DateTimeFormatter DATE_TIME = new DateTimeFormatterBuilder()
      .append(DateTimeFormatter.ISO_LOCAL_DATE)
      .appendLiteral('T')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart().appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .optionalStart()
      .appendFraction(NANO_OF_SECOND, 0, 9, true)
      .optionalStart()
      .appendZoneOrOffsetId()
      .toFormatter();

  /**
   * Formatter for ISO 8601 time of day only.
   * 
   * Java has ISO_LOCAL_TIME, but it doesn't handle the leading 'T' or time zone.
   */
  public static DateTimeFormatter TIME_ONLY = new DateTimeFormatterBuilder()
      .appendLiteral('T')
      .appendValue(HOUR_OF_DAY, 2)
      .appendLiteral(':')
      .appendValue(MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(SECOND_OF_MINUTE, 2)
      .optionalStart()
      .appendFraction(NANO_OF_SECOND, 0, 9, true)
      .optionalStart()
      .appendZoneOrOffsetId()
      .toFormatter();
}

