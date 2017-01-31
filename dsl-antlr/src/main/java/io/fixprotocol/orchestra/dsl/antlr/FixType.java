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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * FIX data types 
 * <br/>
 * Types are hard-coded for now. In the future, consider populating dynamically from datatypes in
 * Orchestra file.
 * 
 * @author Don Mendelson
 *
 */
public enum FixType {
  intType {
    @Override
    public FixType getBaseType() {
      return intType;
    }

    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
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
  },
  Amt {
    @Override
    public FixType getBaseType() {
      return floatType;
    }    

    @Override
    public Class<BigDecimal> getValueClass() {
      return BigDecimal.class;
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
  },
  data {
    @Override
    public FixType getBaseType() {
      return StringType;
    }

    @Override
    public Class<byte []> getValueClass() {
      return byte[].class;
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
  };

  public abstract FixType getBaseType();
  public abstract Class<?> getValueClass();
}
