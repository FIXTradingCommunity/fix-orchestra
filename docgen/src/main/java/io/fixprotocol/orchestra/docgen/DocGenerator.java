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
package io.fixprotocol.orchestra.docgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.purl.dc.elements._1.SimpleLiteral;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STWriter;
import org.stringtemplate.v4.misc.STMessage;

import io.fixprotocol._2016.fixrepository.ActorType;
import io.fixprotocol._2016.fixrepository.Actors;
import io.fixprotocol._2016.fixrepository.CatComponentTypeT;
import io.fixprotocol._2016.fixrepository.Categories;
import io.fixprotocol._2016.fixrepository.CategoryType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldRuleType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.FlowType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.MessageType.Responses;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.ResponseType;
import io.fixprotocol._2016.fixrepository.StateMachineType;
import io.fixprotocol._2016.fixrepository.SupportType;

/**
 * @author Don Mendelson
 *
 */
public class DocGenerator {

  // without this, output was not getting flushed since ST was not closing the file
  private class STWriterWrapper extends NoIndentWriter implements AutoCloseable {

    STWriterWrapper(final Writer out) {
      super(out);
    }

    @Override
    public void close() throws Exception {
      this.out.flush();
    }

  }

  private final static Map<SupportType, String> supportedMap = new HashMap<>();

  static {
    supportedMap.put(SupportType.SUPPORTED, "&#10003;"); // check mark
    supportedMap.put(SupportType.FORBIDDEN, "&#10007;"); // ballot x
    supportedMap.put(SupportType.IGNORED, "&ndash;");
  }

  /**
   * Generates documentation
   * 
   * @param args command line arguments
   *        <ol>
   *        <li>Name of an Orchestra input file.
   *        <li>URI of root output directory. It will be created if it does not exist. Defaults to
   *        <code>file:./doc</code>. If the URI path ends in <code>.zip</code>, then a zip archive
   *        is created. If the URI path contains <code>temp</code>, then a temporary file is
   *        created.</li>
   *        <li>Path of error file. If not provided, outputs to <code>System.err</code></li>
   *        </ol>
   * @throws JAXBException if XML file unmarshalling fails
   * @throws IOException if input file is not found or cannot be read
   */
  public static void main(final String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.docgen.DocGenerator <input-file> [output-uri] [error-path]");
    } else {
      final InputStream inputStream = new FileInputStream(args[0]);

      final String outputRootDir;
      if (args.length > 1) {
        outputRootDir = args[1];
      } else {
        outputRootDir = "./doc";
      }

      final PrintStream errorStream;
      if (args.length > 2) {
        final File errFile = new File(args[2]);
        Files.createDirectories(errFile.toPath().getParent());
        errorStream = new PrintStream(errFile);
      } else {
        errorStream = System.err;
      }

      final DocGenerator gen = new DocGenerator(inputStream, outputRootDir, errorStream);
      gen.generate();
    }
  }

  private final PrintStream errorStream;
  private final ImgGenerator imgGenerator = new ImgGenerator();
  private final InputStream inputStream;
  private final String outputRootDir;
  private PathManager pathManager;
  private Repository repository;

  private final STGroup stGroup;
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

  /**
   * Constructs a DocGenerator
   * 
   * @param inputStream input Orchestra file
   * @param outputRootDir root of file system to write documentation files. If the path ends in
   *        <code>.zip</code>, then a zip archive is created. If the path contains
   *        <code>temp</code>, then a temporary file is created.
   * @param errorStream output stream for errors
   */
  public DocGenerator(final InputStream inputStream, final String outputRootDir, final PrintStream errorStream) {
    this.outputRootDir = outputRootDir;
    this.stGroup = new STGroupFile("templates/docgen.stg", '$', '$');
    // STGroup.verbose = true;
    this.inputStream = inputStream;
    this.errorStream = errorStream;
  }

  /**
   * Generates documentation
   * 
   * @throws Exception if input cannot be read or output cannot be written to a file
   */
  public void generate() throws Exception {
    this.repository = unmarshal(inputStream);

    // Implementation note: consideration was given to supporting "jar:file:" scheme, but the
    // supporting FileSystem is not guaranteed to be installed.

    pathManager = getPathManager(outputRootDir);

    final Path baseOutputPath = pathManager.makeRootPath(outputRootDir);
    createCss(baseOutputPath);

    generateMain(baseOutputPath, getTitle());
    generateMetadata(baseOutputPath, repository, repository.getMetadata().getAny());

    final Path datatypesOutputPath = pathManager.makeDirectory(baseOutputPath.resolve("datatypes"));
    generateDatatypeList(datatypesOutputPath, repository.getDatatypes().getDatatype());
    repository.getDatatypes().getDatatype().forEach(d -> {
      try {
        generateDatatype(datatypesOutputPath, d);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });
    final Path fieldsOutputPath = pathManager.makeDirectory(baseOutputPath.resolve("fields"));
    final List<FieldType> sortedFieldList = repository.getFields().getField().stream()
        .sorted(Comparator.comparing(FieldType::getName)).collect(Collectors.toList());
    generateFieldsList(fieldsOutputPath, sortedFieldList);
    repository.getFields().getField().forEach(f -> {
      try {
        generateFieldDetail(fieldsOutputPath, f);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });

    final List<CodeSetType> allCodeSets = repository.getCodeSets().getCodeSet();
    generateCodeSetList(datatypesOutputPath, allCodeSets.stream()
        .sorted(Comparator.comparing(CodeSetType::getName)).collect(Collectors.toList()));
    repository.getCodeSets().getCodeSet().forEach(cs -> {
      try {
        generateCodeSetDetail(datatypesOutputPath, cs);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });

    final Path messagesDocPath = pathManager.makeDirectory(baseOutputPath.resolve("messages"));
    final Path messagesImgPath = pathManager.makeDirectory(messagesDocPath.resolve("img"));

    final Optional<Categories> optCategories = Optional.ofNullable(repository.getCategories());

    final List<CategoryType> sortedCategoryList =
        optCategories.orElse(new Categories()).getCategory().stream()
            .filter(c -> c.getComponentType() == CatComponentTypeT.MESSAGE).sorted((o1, o2) -> {
              final String sectionValue1 = o1.getSection() != null ? o1.getSection() : "";
              final String sectionValue2 = o2.getSection() != null ? o2.getSection() : "";
              int retv = sectionValue1.compareTo(sectionValue2);
              if (retv == 0) {
                retv = o1.getName().compareTo(o2.getName());
              }
              return retv;
            }).collect(Collectors.toList());

    generateCategories(messagesDocPath, "Message Categories", sortedCategoryList);

    final List<MessageType> sortedMessageList =
        repository.getMessages().getMessage().stream().sorted((o1, o2) -> {
          int retv = o1.getName().compareTo(o2.getName());
          if (retv == 0) {
            retv = o1.getScenario().compareTo(o2.getScenario());
          }
          return retv;
        }).collect(Collectors.toList());

    final Optional<Actors> actors = Optional.ofNullable(repository.getActors());

    final List<ActorType> actorList =
        actors.orElse(new Actors()).getActorOrFlow().stream().filter(af -> af instanceof ActorType)
            .map(af -> (ActorType) af).collect(Collectors.toList());
    generateActorsList(messagesDocPath, actorList);
    actorList.forEach(a -> {
      try {
        generateActorDetail(messagesDocPath, messagesImgPath, a);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });


    final List<FlowType> flowList = actors.orElse(new Actors()).getActorOrFlow().stream()
        .filter(af -> af instanceof FlowType).map(af -> (FlowType) af).collect(Collectors.toList());
    generateFlowsList(messagesDocPath, flowList);
    flowList.forEach(f -> {
      try {
        generateFlowDetail(messagesDocPath, f);

        generateMessageListByFlow(messagesDocPath, f, sortedMessageList);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });


    generateAllMessageList(messagesDocPath, sortedMessageList);

    sortedCategoryList.forEach(c -> {
      try {
        generateMessageListByCategory(messagesDocPath, c, sortedMessageList);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });

    final List<ComponentType> componentList = repository.getComponents().getComponent();
    final List<ComponentType> sortedComponentList =
        componentList.stream().sorted(new Comparator<ComponentType>() {

          @Override
          public int compare(ComponentType o1, ComponentType o2) {
            int retv = o1.getName().compareTo(o2.getName());
            if (retv == 0) {
              retv = o1.getScenario().compareTo(o2.getScenario());
            }
            return retv;
          }
        }).collect(Collectors.toList());

    generateAllComponentsList(messagesDocPath, sortedComponentList);
    componentList.forEach(c -> {
      try {
        generateComponentDetail(messagesDocPath, c);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });

    final List<GroupType> groupList = repository.getGroups().getGroup();
    final List<GroupType> sortedGroupList = groupList.stream().sorted(new Comparator<GroupType>() {

      @Override
      public int compare(GroupType o1, GroupType o2) {
        int retv = o1.getName().compareTo(o2.getName());
        if (retv == 0) {
          retv = o1.getScenario().compareTo(o2.getScenario());
        }
        return retv;
      }
    }).collect(Collectors.toList());
    generateAllGroupsList(messagesDocPath, sortedGroupList);

    groupList.forEach(c -> {
      try {
        generateGroupDetail(messagesDocPath, c);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });
    repository.getMessages().getMessage().forEach(m -> {
      try {
        generateMessageDetail(messagesDocPath, messagesImgPath, m);
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    });
    pathManager.close();
  }

  private void createCss(final Path baseOutputPath) throws IOException {
    final Path pathCss = baseOutputPath.resolve("orchestra.css");
    final ClassLoader classLoader = getClass().getClassLoader();
    try (final InputStream in = classLoader.getResourceAsStream("orchestra.css")) {
      this.pathManager.copyStreamToPath(in, pathCss);
    }
  }

  private void generateActorDetail(final Path messagesDocPath, final Path messagesImgPath, final ActorType actor)
      throws Exception {

    final List<Object> stateMachines = actor.getFieldOrFieldRefOrComponent().stream()
        .filter(o -> o instanceof StateMachineType).collect(Collectors.toList());

    final Path path = messagesDocPath.resolve(String.format("%s.html", actor.getName()));
    try (final STWriterWrapper writer = getWriter(path)) {
      final ST stActor = stGroup.getInstanceOf("actorStart");
      stActor.add("actor", actor);
      stActor.write(writer, templateErrorListener);

      final List<Object> members = actor.getFieldOrFieldRefOrComponent();
      generateMembers(members, writer);

      final ST stActor2 = stGroup.getInstanceOf("actorPart2");
      stActor2.add("actor", actor);
      stActor2.write(writer, templateErrorListener);

      for (final Object stateMachine : stateMachines) {
        final ST stStates = stGroup.getInstanceOf("stateMachine");
        stStates.add("states", stateMachine);
        stStates.write(writer, templateErrorListener);
      }
    }

    for (final Object stateMachine : stateMachines) {
      imgGenerator.generateUMLStateMachine(messagesImgPath, pathManager,
          (StateMachineType) stateMachine, templateErrorListener);
    }
  }

  private void generateActorsList(final Path messagesDocPath, final List<ActorType> actorList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("actors");
    st.add("actors", actorList);
    st.add("title", "All Actors");
    final Path path = messagesDocPath.resolve("AllActors.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateAllComponentsList(final Path messagesDocPath, final List<ComponentType> componentList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("components");
    st.add("components", componentList);
    st.add("title", "All Components");
    final Path path = messagesDocPath.resolve("AllComponents.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }
  
  private void generateAllGroupsList(final Path messagesDocPath, final List<GroupType> componentList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("groups");
    st.add("groups", componentList);
    st.add("title", "All Groups");
    final Path path = messagesDocPath.resolve("AllGroups.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateAllMessageList(final Path messagesDocPath, final List<MessageType> messageList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("messages");
    st.add("messages", messageList);
    st.add("title", "All Messages");
    final Path path = messagesDocPath.resolve("AllMessages.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateCategories(final Path messagesDocPath, final String title,
                                  final List<CategoryType> categoriesList) throws Exception {
    final ST st = stGroup.getInstanceOf("categories");
    st.add("title", title);
    st.add("categories", categoriesList);
    final Path path = messagesDocPath.resolve("MessageCategories.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateCodeSetDetail(final Path datatypesOutputPath, final CodeSetType codeSet)
      throws Exception {
    final Path path = datatypesOutputPath.resolve(String.format("%s-%s.html", codeSet.getName(), codeSet.getScenario()));
    try (final STWriterWrapper writer = getWriter(path)) {
      final ST stCodesetStart = stGroup.getInstanceOf("codeSetStart");
      stCodesetStart.add("codeSet", codeSet);
      stCodesetStart.write(writer, templateErrorListener);

      final List<CodeType> codeList = codeSet.getCode();
      for (final CodeType code : codeList) {
        final ST stCode = stGroup.getInstanceOf("code");
        stCode.add("code", code);
        stCode.add("supported", supportedMap.get(code.getSupported()));
        stCode.write(writer, templateErrorListener);
      }

      final ST stCodesetEnd = stGroup.getInstanceOf("codeSetEnd");
      stCodesetEnd.add("codeSet", codeSet);
      stCodesetEnd.write(writer, templateErrorListener);
    }
  }

  private void generateCodeSetList(final Path datatypesOutputPath, final List<CodeSetType> codeSetList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("codeSets");
    st.add("codeSets", codeSetList);
    st.add("title", "All Code Sets");
    final Path path = datatypesOutputPath.resolve("AllCodeSets.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateComponentDetail(final Path messagesDocPath, final ComponentType component)
      throws Exception {
    final ST stComponentStart;
    stComponentStart = stGroup.getInstanceOf("componentStart");
    stComponentStart.add("component", component);
    final ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    final List<Object> members = component.getComponentRefOrGroupRefOrFieldRef();

    final Path path = messagesDocPath.resolve(String.format("%s-%s.html", component.getName(), component.getScenario()));
    try (final STWriterWrapper writer = getWriter(path)) {
      stComponentStart.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stComponentEnd.write(writer, templateErrorListener);
    }
  }

  private void generateDatatype(final Path datatypesOutputPath, final Datatype datatype) throws Exception {
    final ST st = stGroup.getInstanceOf("datatype");
    st.add("datatype", datatype);
    final Path path = datatypesOutputPath.resolve(String.format("%s.html", datatype.getName()));
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateDatatypeList(final Path datatypesOutputPath, final List<Datatype> datatypeList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("datatypes");
    st.add("datatypes", datatypeList);
    st.add("title", "All Datatypes");
    final Path path = datatypesOutputPath.resolve("AllDatatypes.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFieldDetail(final Path fieldsOutputPath, final FieldType field) throws Exception {
    final ST st = stGroup.getInstanceOf("field");
    st.add("field", field);
    st.add("typeLink", getTypeLink(field.getType(), field.getScenario()));
    final Path path = fieldsOutputPath.resolve(String.format("%s-%s.html", field.getName(), field.getScenario()));
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFieldsList(final Path fieldsOutputPath, final List<FieldType> fieldList)
      throws Exception {
    final ST st = stGroup.getInstanceOf("fields");
    st.add("fields", fieldList);
    st.add("title", "All Fields");
    final Path path = fieldsOutputPath.resolve("AllFields.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFlowDetail(final Path messagesDocPath, final FlowType flow) throws Exception {
    final ST st = stGroup.getInstanceOf("flow");
    st.add("flow", flow);
    final Path path = messagesDocPath.resolve(String.format("%s.html", flow.getName()));
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFlowsList(final Path messagesDocPath, final List<FlowType> flowList) throws Exception {
    final ST st = stGroup.getInstanceOf("flows");
    st.add("flows", flowList);
    st.add("title", "All Flows");
    final Path path = messagesDocPath.resolve("AllFlows.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateGroupDetail(final Path messagesDocPath, final GroupType group) throws Exception {
    final ST stGroupStart;
    stGroupStart = stGroup.getInstanceOf("groupStart");
    stGroupStart.add("groupType", group);
    final FieldType field = getField(group.getNumInGroup().getId().intValue());
    stGroupStart.add("fieldType", field);

    final ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    final List<Object> members = group.getComponentRefOrGroupRefOrFieldRef();

    final Path path = messagesDocPath.resolve(String.format("%s-%s.html", group.getName(), group.getScenario()));
    try (final STWriterWrapper writer = getWriter(path)) {
      stGroupStart.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stComponentEnd.write(writer, templateErrorListener);
    }
  }

  private void generateMain(final Path baseOutputPath, final String title) throws Exception  {
    final ST st = stGroup.getInstanceOf("main");
    st.add("title", title);
    final Path path = baseOutputPath.resolve("index.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMembers(final List<Object> members, final STWriter writer) {
    for (final Object member : members) {
      if (member instanceof FieldRefType) {
        final FieldType field = getField(((FieldRefType) member).getId().intValue());
        final ST stField = stGroup.getInstanceOf("fieldMember");
        stField.add("field", field);
        if (((FieldRefType) member).getSupported() == SupportType.SUPPORTED) {
          stField.add("presence", getFieldPresence((FieldRefType) member));
        } else {
          stField.add("presence", supportedMap.get(((FieldRefType) member).getSupported()));
        }
        stField.add("assign", ((FieldRefType) member).getAssign());
        stField.write(writer, templateErrorListener);
      } else if (member instanceof GroupRefType) {
        final GroupType component = getGroup(((GroupRefType) member).getId().intValue());
        final ST stComponent = stGroup.getInstanceOf("componentMember");
        stComponent.add("component", component);
        if (((ComponentRefType) member).getSupported() == SupportType.SUPPORTED) {
          stComponent.add("presence",
              ((ComponentRefType) member).getPresence().value().toLowerCase());
        } else {
          stComponent.add("presence", supportedMap.get(((ComponentRefType) member).getSupported()));
        }
        stComponent.write(writer, templateErrorListener);
      } else if (member instanceof ComponentRefType) {
        final ComponentType component = getComponent(((ComponentRefType) member).getId().intValue());
        final ST stComponent = stGroup.getInstanceOf("componentMember");
        stComponent.add("component", component);
        if (((ComponentRefType) member).getSupported() == SupportType.SUPPORTED) {
          stComponent.add("presence",
              ((ComponentRefType) member).getPresence().value().toLowerCase());
        } else {
          stComponent.add("presence", supportedMap.get(((ComponentRefType) member).getSupported()));
        }
        stComponent.write(writer, templateErrorListener);
      }
    }
  }

  private void generateMessageDetail(final Path messagesDocPath, final Path messagesImgPath,
                                     final MessageType message) throws Exception {
    final ST stMessageStart = stGroup.getInstanceOf("messageStart");
    final ST stMessagePart2 = stGroup.getInstanceOf("messagePart2");
    final ST stMessageEnd = stGroup.getInstanceOf("messageEnd");
    stMessageStart.add("message", message);
    stMessagePart2.add("message", message);
    stMessageEnd.add("message", message);

    List<ResponseType> responses = null;
    final Responses responses2 = message.getResponses();
    if (responses2 != null) {
      responses = responses2.getResponse();
    }
    final List<Object> members = message.getStructure().getComponentRefOrGroupRefOrFieldRef();

    final Path path = messagesDocPath
        .resolve(String.format("%s-%s.html", message.getName(), message.getScenario()));
    try (final STWriterWrapper writer = getWriter(path)) {
      stMessageStart.write(writer, templateErrorListener);
      if (responses != null) {
        generateResponses(responses, writer);
      }

      stMessagePart2.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stMessageEnd.write(writer, templateErrorListener);
    }

    if (responses != null) {
      final FlowType flow = getFlow(message.getFlow());
      imgGenerator.generateUMLSequence(messagesImgPath, pathManager, message, flow, responses,
          templateErrorListener);
    }
  }

  private void generateMessageListByCategory(final Path messagesDocPath, final CategoryType category,
                                             final List<MessageType> messageList) throws Exception {
    final ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> category.getName().equals(m.getCategory())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", category.getName()));
    final Path path = messagesDocPath.resolve(String.format("%sMessages.html", category.getName()));
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMessageListByFlow(final Path messagesDocPath, final FlowType flow,
                                         final List<MessageType> messageList) throws Exception {
    final ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> flow.getName().equals(m.getFlow())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", flow.getName()));
    final Path path = messagesDocPath.resolve(String.format("%sMessages.html", flow.getName()));
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMetadata(final Path baseOutputPath, final Repository repository,
                                final List<JAXBElement<SimpleLiteral>> elementList) throws Exception {
    final ST st = stGroup.getInstanceOf("metadata");
    st.add("repository", repository);
    st.add("elementList", elementList);
    final Path path = baseOutputPath.resolve("metadata.html");
    try (final STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateResponses(final List<ResponseType> responseList, final STWriter writer) {
    for (final ResponseType response : responseList) {
      final List<Object> responses = response.getMessageRefOrAssignOrTrigger();
      for (final Object responseRef : responses) {
        if (responseRef instanceof MessageRefType) {
          final MessageRefType messageRef = (MessageRefType) responseRef;
          final ST st = stGroup.getInstanceOf("messageResponse");
          st.add("message", messageRef.getName());
          st.add("scenario", messageRef.getScenario());
          st.add("when", response.getWhen());
          st.write(writer, templateErrorListener);
        }
      }
    }
  }

  private ComponentType getComponent(final int componentId) {
    final List<ComponentType> components = repository.getComponents().getComponent();
    for (final ComponentType component : components) {
      if (component.getId().intValue() == componentId) {
        return component;
      }
    }
    return null;
  }

  private FieldType getField(final int id) {
    final List<FieldType> fields = repository.getFields().getField();
    for (final FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType;
      }
    }
    return null;
  }
  
  private String getFieldPresence(final FieldRefType fieldRef) {
    switch (fieldRef.getPresence()) {
      case CONSTANT:
        return String.format("constant %s", fieldRef.getValue());
      case FORBIDDEN:
        return "forbidden";
      case IGNORED:
        return "ignored";
      case OPTIONAL:
        final List<FieldRuleType> rules = fieldRef.getRule();
        for (final FieldRuleType rule : rules) {
          if (rule.getPresence() == PresenceT.REQUIRED) {
            return String.format("required when %s", rule.getWhen());
          }
        }
        return "optional";
      case REQUIRED:
        return "required";
    }
    return "";
  }

  private FlowType getFlow(final String name) {
    final List<Object> afList = repository.getActors().getActorOrFlow();
    for (final Object obj : afList) {
      if (obj instanceof FlowType) {
        final FlowType flow = (FlowType) obj;
        if (flow.getName().equals(name)) {
          return flow;
        }
      }
    }
    return null;
  }

  private GroupType getGroup(final int componentId) {
    final List<GroupType> groups = repository.getGroups().getGroup();
    for (final GroupType group : groups) {
      if (group.getId().intValue() == componentId) {
        return group;
      }
    }
    return null;
  }

  private PathManager getPathManager(final String path) {
    final ZipFileManager zipFileManager = new ZipFileManager();
    if (zipFileManager.isSupported(path)) {
      return zipFileManager;
    } else {
      return new FileManager();
    }
  }

  private String getTitle() {
    String title = "Orchestra";
    final List<JAXBElement<SimpleLiteral>> metadata = repository.getMetadata().getAny();
    for (final JAXBElement<SimpleLiteral> element : metadata) {
      if (element.getName().getLocalPart().equals("title")) {
        title = String.join(" ", element.getValue().getContent());
        break;
      }
    }
    return title;
  }

  private String getTypeLink(String type, String scenario) {
    List<Datatype> datatypes = repository.getDatatypes().getDatatype();
    for (final Datatype datatype : datatypes) {
      if (datatype.getName().equals(type)) {
        return String.format("../datatypes/%s.html", type);
      }
    }
    return String.format("../datatypes/%s-%s.html", type, scenario);
  }

  private STWriterWrapper getWriter(final Path path) throws IOException {
    return new STWriterWrapper(this.pathManager.getWriter(path));
  }
  
  private Repository unmarshal(final InputStream inputStream) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    jaxbUnmarshaller.setEventHandler(unmarshallerErrorHandler);
    return (Repository) jaxbUnmarshaller.unmarshal(inputStream);
  }
}
