/*
 * Copyright 2019-2020 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import io.fixprotocol.orchestra.event.EventListener;

/**
 * Validates an Orchestra repository file 
 * 
 * <p>Validations include:</p>
 * <ul>
 * <li>Conformance to the repository XML schema</li>
 * <li>Syntax of Score DSL expressions</li>
 * <li>Syntax of markdown documentation</li>
 * <li>Conformance to FIX style rules or basic validation</li
 * </ul>
 * 
 * <p>Rules for other protocols may be added in future.</p>
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryValidator {

  public static class Builder {
    private String eventFile;
    private String inputFile;
    private String style = FIX_STYLE;

    public RepositoryValidator build() {
      return new RepositoryValidator(this);
    }

    public Builder eventLog(String eventFile) {
      this.eventFile = eventFile;
      return this;
    }

    public Builder inputFile(String inputFilename) {
      this.inputFile = inputFilename;
      return this;
    }
    
    public Builder style(String style) {
      this.style = style;
      return this;
    }
  }

  public static final String FIX_STYLE = "FIX";

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Execute RepositoryValidator with command line arguments
   *
   * @param args command line arguments
   *
   *        <pre>
   * Usage: RepositoryValidator [options] &lt;input-file&gt;
   * -e &lt;logfile&gt; name of event log
   *        </pre>
   *
   * @throws Exception if the file to validate cannot be found, read, or parsed
   */
  public static void main(String[] args) throws Exception {
    final Builder builder = RepositoryValidator.builder();

    for (int i = 0; i < args.length;) {
      if ("-e".equals(args[i])) {
        if (i < args.length - 1) {
          builder.eventLog(args[i + 1]);
          i++;
        }
      } else {
        builder.inputFile(args[i]);
      }
      i++;
    }
    final RepositoryValidator validator = builder.build();
    validator.validate();
  }

  private final String eventFile;
  private final String inputFile;
  private final String style;

  private RepositoryValidator(Builder builder) {
    this.eventFile = builder.eventFile;
    this.inputFile = builder.inputFile;
    this.style = builder.style;
  }

  public boolean validate() {
    try (EventListener eventLogger = FixRepositoryValidator
        .createLogger(eventFile != null ? new FileOutputStream(eventFile) : null)) {
      BasicRepositoryValidator impl;
      if (FIX_STYLE.equals(this.style)) {
        impl = new FixRepositoryValidator(eventLogger);
      } else {
        impl = new BasicRepositoryValidator(eventLogger);
      }
      return impl.validate(new FileInputStream(inputFile));
    } catch (final Exception e) {
      System.err.println(e.getMessage());
      return false;
    }
  }

}
