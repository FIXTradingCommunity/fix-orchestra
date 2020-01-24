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
package io.fixprotocol.orchestra.states;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;
import io.fixprotocol._2020.orchestra.repository.ActorType;
import io.fixprotocol._2020.orchestra.repository.Actors;
import io.fixprotocol._2020.orchestra.repository.Repository;
import io.fixprotocol._2020.orchestra.repository.StateMachineType;
import io.fixprotocol._2020.orchestra.repository.StateType;
import io.fixprotocol._2020.orchestra.repository.TransitionType;


public class StateGenerator {

  // without this, output was not getting flushed since ST was not closing the file
  private static class STWriterWrapper extends AutoIndentWriter implements AutoCloseable {

    STWriterWrapper(final Writer out) {
      super(out);
    }

    @Override
    public void close() throws Exception {
      this.out.flush();
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.states.StateGenerator <input-file> <package> [output-uri] [error-path]");
    } else {
      final InputStream inputStream = new FileInputStream(args[0]);

      final String srcPackage = args[1];

      final String srcDir;
      if (args.length > 2) {
        srcDir = args[2];
      } else {
        srcDir = "target/generated-sources/java";
      }

      final Path rootPath = new File(srcDir).toPath();

      final PrintStream errorStream;
      if (args.length > 3) {
        final File errFile = new File(args[3]);
        Files.createDirectories(errFile.toPath().getParent());
        errorStream = new PrintStream(errFile);
      } else {
        errorStream = System.err;
      }

      final StateGenerator gen = new StateGenerator(inputStream, rootPath, srcPackage, errorStream);
      gen.generate();
    }
  }

  private final PrintStream errorStream;

  private final InputStream inputStream;

  private Repository repository;
  private final String srcPackage;
  private Path srcPath;
  private final STGroupFile stGroup;
  private final STErrorListener templateErrorListener = new STErrorListener() {

    @Override
    public void compileTimeError(final STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void internalError(final STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void IOError(final STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void runTimeError(final STMessage msg) {
      errorStream.println(msg.toString());
    }

  };

  private final ValidationEventHandler unmarshallerErrorHandler = new ValidationEventHandler() {

    @Override
    public boolean handleEvent(final ValidationEvent event) {
      errorStream.print(String.format("%s line %d col %d %s", severityToString(event.getSeverity()),
          event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(),
          event.getMessage()));
      return event.getSeverity() == ValidationEvent.WARNING;
    }

    private String severityToString(final int severity) {
      switch (severity) {
        case ValidationEvent.WARNING:
          return "WARN ";
        case ValidationEvent.ERROR:
          return "ERROR";
        default:
          return "FATAL";
      }
    }

  };

  public StateGenerator(InputStream inputStream, Path rootPath, String srcPackage,
      PrintStream errorStream) {
    this.srcPath = rootPath;
    this.srcPackage = srcPackage;
    this.stGroup = new STGroupFile("templates/stategen.stg", '$', '$');
    // STGroup.verbose = true;
    this.inputStream = inputStream;
    this.errorStream = errorStream;
  }

  public void generate() throws Exception {
    this.repository = unmarshal(inputStream);
    this.srcPath = makePackage(srcPath, srcPackage);

    final Optional<Actors> actors = Optional.ofNullable(repository.getActors());
    final List<ActorType> actorList =
        actors.orElse(new Actors()).getActorOrFlow().stream().filter(af -> af instanceof ActorType)
            .map(af -> (ActorType) af).collect(Collectors.toList());
    actorList.forEach(actor -> {
      try {
        final List<Object> stateMachines = actor.getFieldOrFieldRefOrComponent().stream()
            .filter(o -> o instanceof StateMachineType).collect(Collectors.toList());

        for (final Object stateMachine : stateMachines) {
          generateStateMachine((StateMachineType) stateMachine);
        }
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void generateObjectInterface(StateMachineType stateMachine) throws Exception {
    String name = stateMachine.getName();
    try (final STWriterWrapper writer =
        getWriter(srcPath.resolve(String.format("Has%s.java", name)))) {
      final ST stInterface = stGroup.getInstanceOf("has_interface");
      stInterface.add("stateMachine", stateMachine);
      stInterface.add("package", this.srcPackage);
      stInterface.write(writer, templateErrorListener);
    }
  }

  private void generateStateCase(StateMachineType stateMachine, final STWriterWrapper writer,
      StateType state) {
    List<TransitionType> transitions = state.getTransition();
    if (!transitions.isEmpty()) {
      final ST stState = stGroup.getInstanceOf("state_case");
      stState.add("stateMachine", stateMachine);
      stState.add("state", state);
      stState.add("package", this.srcPackage);
      stState.write(writer, templateErrorListener);
    }
  }

  private void generateStateMachine(StateMachineType stateMachine) throws Exception {
    generateObjectInterface(stateMachine);
    generateStatesEnum(stateMachine);
    generateTransitionInterface(stateMachine);
    StateType initial = stateMachine.getInitial();
    generateTransitionsEnum(stateMachine, initial);
    List<StateType> states = stateMachine.getState();
    for (StateType state : states) {
      generateTransitionsEnum(stateMachine, state);
    }
    generateStateMachineClass(stateMachine);
  }

  private void generateStateMachineClass(StateMachineType stateMachine) throws Exception {
    String name = stateMachine.getName();
    try (final STWriterWrapper writer =
        getWriter(srcPath.resolve(String.format("%sStateMachine.java", name)))) {
      final ST stInterface1 = stGroup.getInstanceOf("stateMachinePart1");
      stInterface1.add("stateMachine", stateMachine);
      stInterface1.add("package", this.srcPackage);
      stInterface1.write(writer, templateErrorListener);
      StateType initial = stateMachine.getInitial();
      generateStateCase(stateMachine, writer, initial);
      List<StateType> states = stateMachine.getState();
      for (StateType state : states) {
        generateStateCase(stateMachine, writer, state);
      }
      final ST stInterface2 = stGroup.getInstanceOf("stateMachinePart2");
      stInterface2.add("stateMachine", stateMachine);
      stInterface2.write(writer, templateErrorListener);
    }
  }

  private void generateStatesEnum(StateMachineType stateMachine) throws Exception {
    String name = stateMachine.getName();
    try (
        final STWriterWrapper writer = getWriter(srcPath.resolve(String.format("%s.java", name)))) {
      final ST stInterface = stGroup.getInstanceOf("state_enum");
      stInterface.add("stateMachine", stateMachine);
      stInterface.add("package", this.srcPackage);
      stInterface.write(writer, templateErrorListener);
    }
  }

  private void generateTransitionInterface(StateMachineType stateMachine) throws Exception {
    String name = stateMachine.getName();
    try (final STWriterWrapper writer =
        getWriter(srcPath.resolve(String.format("%sTransition.java", name)))) {
      final ST stInterface = stGroup.getInstanceOf("transition_interface");
      stInterface.add("stateMachine", stateMachine);
      stInterface.add("package", this.srcPackage);
      stInterface.write(writer, templateErrorListener);
    }
  }

  private void generateTransitionsEnum(StateMachineType stateMachine, StateType state)
      throws Exception {
    String stateMachineName = stateMachine.getName();
    String stateName = state.getName();
    List<TransitionType> transitions = state.getTransition();
    if (!transitions.isEmpty()) {
      try (final STWriterWrapper writer = getWriter(
          srcPath.resolve(String.format("%s%sTransition.java", stateMachineName, stateName)))) {
        final ST stInterface = stGroup.getInstanceOf("transition_enum");
        stInterface.add("stateMachine", stateMachine);
        stInterface.add("state", state);
        stInterface.add("package", this.srcPackage);
        stInterface.write(writer, templateErrorListener);
      }
    }
  }

  private STWriterWrapper getWriter(Path path) throws IOException {
    return new STWriterWrapper(new FileWriter(path.toString()));
  }

  private Path makeDirectory(Path path) throws IOException {
    // default file attributes
    return Files.createDirectories(path);
  }

  private Path makePackage(Path root, String name) throws IOException {
    Path packagePath = root;
    String[] parts = name.split("\\.");
    if (parts.length == 0) {
      parts = new String[] {name};
    }
    for (String part : parts) {
      packagePath = packagePath.resolve(part);
    }
    return makeDirectory(packagePath);
  }

  private Repository unmarshal(final InputStream inputStream) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    jaxbUnmarshaller.setEventHandler(unmarshallerErrorHandler);
    return (Repository) jaxbUnmarshaller.unmarshal(inputStream);
  }
}
