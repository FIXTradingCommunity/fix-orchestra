/*
 * Copyright 2019-2022 FIX Protocol Ltd
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
import java.io.OutputStream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.event.EventListenerFactory;
import io.fixprotocol.orchestra.event.TeeEventListener;


/**
 * Validates an Orchestra repository file
 * 
 * <p>
 * Validations include:
 * </p>
 * <ul>
 * <li>Conformance to the repository XML schema</li>
 * <li>Syntax of Score DSL expressions</li>
 * <li>Syntax of markdown documentation</li>
 * <li>Conformance to FIX style rules or basic validation</li
 * </ul>
 * 
 * <p>
 * Rules for other protocols may be added in future.
 * </p>
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

  /**
   * Basic validation only validates XML schema conformance
   */
  public static final String BASIC_STYLE = "BASIC";

  /**
   * FIX validation performs basic validation plus conformance to FIX style rules
   */
  public static final String FIX_STYLE = "FIX";

  public static Builder builder() {
    return new Builder();
  }

  public static EventListener createLogger(OutputStream jsonOutputStream) {
    final Logger logger = LogManager.getLogger(RepositoryValidator.class);
    final EventListenerFactory factory = new EventListenerFactory();
    TeeEventListener eventListener = null;
    try {
      eventListener = new TeeEventListener();
      final EventListener logEventLogger = factory.getInstance("LOG4J");
      logEventLogger.setResource(logger);
      eventListener.addEventListener(logEventLogger);
      if (jsonOutputStream != null) {
        final EventListener jsonEventLogger = factory.getInstance("JSON");
        jsonEventLogger.setResource(jsonOutputStream);
        eventListener.addEventListener(jsonEventLogger);
      }
    } catch (Exception e) {
      logger.error("Error creating event listener", e);
    }
    return eventListener;
  }

  /**
   * Execute RepositoryValidator with command line arguments
   *
   * @param args command line arguments
   *
   *        <pre>
usage: RepositoryValidator [options] &lt;input-file&gt;
 -?,--help             display usage
 -e,--eventlog &lt;arg&gt;   path of JSON event file
 -s,--style &lt;arg&gt;      validation style
   *        </pre>
   */
  public static void main(String[] args) {
    RepositoryValidator validator;
    try {
      validator = RepositoryValidator.parseArgs(args).build();
      validator.validate();
    } catch (ParseException e) {
      System.exit(1);
    }
  }

  static Builder parseArgs(String[] args) throws ParseException {
    final Options options = new Options();
    options.addOption(Option.builder("e").desc("path of JSON event file").longOpt("eventlog")
        .numberOfArgs(1).build());
    options.addOption(
        Option.builder("s").desc("validation style").longOpt("style").numberOfArgs(1).build());
    options.addOption(Option.builder("?").desc("display usage").longOpt("help").build());

    final DefaultParser parser = new DefaultParser();
    CommandLine cmd;

    final Builder builder = new Builder();

    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("?")) {
        showHelp(options);
        System.exit(0);
      }

      builder.inputFile = !cmd.getArgList().isEmpty() ? cmd.getArgList().get(0) : null;

      if (cmd.hasOption("e")) {
        builder.eventFile = cmd.getOptionValue("e");
      }

      if (cmd.hasOption("s")) {
        builder.style = cmd.getOptionValue("s");
      }

      return builder;
    } catch (final ParseException e) {
      System.err.println(e.getMessage());
      showHelp(options);
      throw e;
    }
  }

  static void showHelp(final Options options) {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("RepositoryValidator [options] <input-file>", options);
  }

  final Logger logger = LogManager.getLogger(RepositoryValidator.class);
  private final String eventFile;
  private final String inputFile;
  private final String style;

  private RepositoryValidator(Builder builder) {
    this.eventFile = builder.eventFile;
    this.inputFile = builder.inputFile;
    this.style = builder.style;
  }

  public boolean validate() {
    try (EventListener eventLogger =
        createLogger(eventFile != null ? new FileOutputStream(eventFile) : null)) {
      BasicRepositoryValidator impl;
      if (FIX_STYLE.equalsIgnoreCase(this.style)) {
        impl = new FixRepositoryValidator(eventLogger);
      } else {
        impl = new BasicRepositoryValidator(eventLogger);
      }
      if (inputFile == null) {
        throw new IllegalArgumentException("No input file specified");
      }
      return impl.validate(new FileInputStream(inputFile));
    } catch (final Exception e) {
      logger.fatal("RepositoryValidator failed", e);
      return false;
    }
  }

}
