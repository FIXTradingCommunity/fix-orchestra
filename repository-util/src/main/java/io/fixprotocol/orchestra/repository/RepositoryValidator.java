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
 * Validates an Orchestra file against the repository schema
 *
 * @author Don Mendelson
 *
 */
public class RepositoryValidator {

  public static class Builder {
    private String inputFile;
    private String eventFile;

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
  }

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

  private final String inputFile;
  private final String eventFile;

  private RepositoryValidator(Builder builder) {
    this.eventFile = builder.eventFile;
    this.inputFile = builder.inputFile;
  }

  public boolean validate() {
    try (EventListener eventLogger = FixRepositoryValidator
        .createLogger(eventFile != null ? new FileOutputStream(eventFile) : null)) {
      final FixRepositoryValidator impl = new FixRepositoryValidator(eventLogger);
      return impl.validate(new FileInputStream(inputFile));
    } catch (final Exception e) {
      System.err.println(e.getMessage());
      return false;
    }
  }

}
