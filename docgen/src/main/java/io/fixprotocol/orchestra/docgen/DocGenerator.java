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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
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

    STWriterWrapper(Writer out) {
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
   *        is created.</li>
   *        <li>Path of error file. If not provided, outputs to <code>System.err</code></li>
   *        </ol>
   * @throws JAXBException if XML file unmarshalling fails
   * @throws IOException if input file is not found or cannot be read
   * @throws URISyntaxException if URI format is invalid
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.docgen.DocGenerator <input-file> [output-uri] [error-path]");
    } else {
      Reader inputReader = new FileReader(args[0]);

      URI outputRootUri;
      if (args.length > 1) {
        outputRootUri = new URI(args[1]);
      } else {
        outputRootUri = new URI("file:./doc");
      }

      PrintStream errorStream;
      if (args.length > 2) {
        File errFile = new File(args[2]);
        Files.createDirectories(errFile.toPath().getParent());
        errorStream = new PrintStream(errFile);
      } else {
        errorStream = System.err;
      }

      DocGenerator gen = new DocGenerator(inputReader, outputRootUri, errorStream);
      gen.generate();
    }
  }

  private PrintStream errorStream;
  private final ImgGenerator imgGenerator = new ImgGenerator();
  private URI outputRootUri;
  private final Repository repository;


  private final STGroup stGroup;

  private final STErrorListener templateErrorListener = new STErrorListener() {

    @Override
    public void compileTimeError(STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void internalError(STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void IOError(STMessage msg) {
      errorStream.println(msg.toString());
    }

    @Override
    public void runTimeError(STMessage msg) {
      errorStream.println(msg.toString());
    }

  };

  private final ValidationEventHandler unmarshallerErrorHandler = new ValidationEventHandler() {

    @Override
    public boolean handleEvent(ValidationEvent event) {
      errorStream.print(String.format("%s line %d col %d %s", severityToString(event.getSeverity()),
          event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(),
          event.getMessage()));
      return event.getSeverity() == ValidationEvent.WARNING;
    }

    private String severityToString(int severity) {
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
  private FileSystemManager fileSystemManager = new FileSystemManager();

  /**
   * Constructs a DocGenerator
   * 
   * @param inputReader of input Orchestra file
   * @param outputRootUri root of file system to write documentation files
   * @param errorStream output stream for errors
   * @throws JAXBException if a parsing error occurs
   * @throws IOException if a file cannot be accessed
   */
  public DocGenerator(Reader inputReader, URI outputRootUri, PrintStream errorStream)
      throws JAXBException, IOException {
    this.outputRootUri = outputRootUri;
    this.stGroup = new STGroupFile("templates/docgen.stg", '$', '$');
    // STGroup.verbose = true;
    this.repository = unmarshal(inputReader);
    this.errorStream = errorStream;
  }

  /**
   * Generates documentation
   * 
   * @throws Exception if documentation cannot be written to a file
   */
  public void generate() throws Exception {
    try {
      // Implementation note: consideration was given to supporting "jar:file:" scheme, but the
      // supporting FileSystem is not guaranteed to be installed.

      Path baseOutputPath = this.fileSystemManager.makeDirectory(new File(this.outputRootUri).toPath());
      createCss(baseOutputPath);

      generateMain(baseOutputPath, getTitle());
      generateMetadata(baseOutputPath, repository, repository.getMetadata().getAny());

      Path datatypesOutputPath = this.fileSystemManager.makeDirectory(baseOutputPath.resolve("datatypes"));
      generateDatatypeList(datatypesOutputPath, repository.getDatatypes().getDatatype());
      repository.getDatatypes().getDatatype().forEach(d -> {
        try {
          generateDatatype(datatypesOutputPath, d);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      Path fieldsOutputPath = this.fileSystemManager.makeDirectory(baseOutputPath.resolve("fields"));
      List<FieldType> sortedFieldList = repository.getFields().getField().stream()
          .sorted(Comparator.comparing(FieldType::getName)).collect(Collectors.toList());
      generateFieldsList(fieldsOutputPath, sortedFieldList);
      repository.getFields().getField().forEach(f -> {
        try {
          generateFieldDetail(fieldsOutputPath, f);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      List<CodeSetType> allCodeSets = repository.getCodeSets().getCodeSet();
      generateCodeSetList(datatypesOutputPath, allCodeSets.stream()
          .sorted(Comparator.comparing(CodeSetType::getName)).collect(Collectors.toList()));
      repository.getCodeSets().getCodeSet().forEach(cs -> {
        try {
          generateCodeSetDetail(datatypesOutputPath, cs);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      Path messagesDocPath = this.fileSystemManager.makeDirectory(baseOutputPath.resolve("messages"));
      Path messagesImgPath = this.fileSystemManager.makeDirectory(messagesDocPath.resolve("img"));

      final Optional<Categories> optCategories = Optional.ofNullable(repository.getCategories());

      final List<CategoryType> sortedCategoryList =
          optCategories.orElse(new Categories()).getCategory().stream()
              .filter(c -> c.getComponentType() == CatComponentTypeT.MESSAGE).sorted((o1, o2) -> {
                final String sectionValue1 = o1.getSection() != null ? o1.getSection().value() : "";
                final String sectionValue2 = o2.getSection() != null ? o2.getSection().value() : "";
                int retv = sectionValue1.compareTo(sectionValue2);
                if (retv == 0) {
                  retv = o1.getId().compareTo(o2.getId());
                }
                return retv;
              }).collect(Collectors.toList());

      generateCategories(messagesDocPath, "Message Categories", sortedCategoryList);

      List<MessageType> sortedMessageList =
          repository.getMessages().getMessage().stream().sorted((o1, o2) -> {
            int retv = o1.getName().compareTo(o2.getName());
            if (retv == 0) {
              retv = o1.getScenario().compareTo(o2.getScenario());
            }
            return retv;
          }).collect(Collectors.toList());

      final Optional<Actors> actors = Optional.ofNullable(repository.getActors());

      final List<ActorType> actorList = actors.orElse(new Actors()).getActorOrFlow().stream()
          .filter(af -> af instanceof ActorType).map(af -> (ActorType) af)
          .collect(Collectors.toList());
      generateActorsList(messagesDocPath, actorList);
      actorList.forEach(a -> {
        try {
          generateActorDetail(messagesDocPath, messagesImgPath, a);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });


      final List<FlowType> flowList =
          actors.orElse(new Actors()).getActorOrFlow().stream().filter(af -> af instanceof FlowType)
              .map(af -> (FlowType) af).collect(Collectors.toList());
      generateFlowsList(messagesDocPath, flowList);
      flowList.forEach(f -> {
        try {
          generateFlowDetail(messagesDocPath, f);

          generateMessageListByFlow(messagesDocPath, f, sortedMessageList);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });


      generateAllMessageList(messagesDocPath, sortedMessageList);

      sortedCategoryList.forEach(c -> {
        try {
          generateMessageListByCategory(messagesDocPath, c, sortedMessageList);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

      List<ComponentType> componentList = repository.getComponents().getComponent();
      List<GroupType> groupList = repository.getGroups().getGroup();
      List<String> componentsAndGroupsList =
          componentList.stream().map(ComponentType::getName).collect(Collectors.toList());
      componentsAndGroupsList
          .addAll(groupList.stream().map(GroupType::getName).collect(Collectors.toList()));
      List<String> sortedComponentNameList =
          componentsAndGroupsList.stream().sorted().collect(Collectors.toList());
      generateAllComponentsList(messagesDocPath, sortedComponentNameList);
      componentList.forEach(c -> {
        try {
          generateComponentDetail(messagesDocPath, c);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      groupList.forEach(c -> {
        try {
          generateGroupDetail(messagesDocPath, c);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      repository.getMessages().getMessage().forEach(m -> {
        try {
          generateMessageDetail(messagesDocPath, messagesImgPath, m);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw e;
      }
    }
  }
  
  private void createCss(Path baseOutputPath) throws IOException {
    Path pathCss = baseOutputPath.resolve("orchestra.css");
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream in = classLoader.getResourceAsStream("orchestra.css")) {
      this.fileSystemManager.copyStreamToPath(in, pathCss);
    }
  }

  private void generateActorDetail(Path messagesDocPath, Path messagesImgPath, ActorType actor)
      throws Exception {

    List<Object> stateMachines = actor.getFieldOrFieldRefOrComponent().stream()
        .filter(o -> o instanceof StateMachineType).collect(Collectors.toList());

    Path path = messagesDocPath.resolve(String.format("%s.html", actor.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      ST stActor = stGroup.getInstanceOf("actorStart");
      stActor.add("actor", actor);
      stActor.write(writer, templateErrorListener);

      List<Object> members = actor.getFieldOrFieldRefOrComponent();
      generateMembers(members, writer);

      ST stActor2 = stGroup.getInstanceOf("actorPart2");
      stActor2.add("actor", actor);
      stActor2.write(writer, templateErrorListener);

      for (Object stateMachine : stateMachines) {
        ST stStates = stGroup.getInstanceOf("stateMachine");
        stStates.add("states", stateMachine);
        stStates.write(writer, templateErrorListener);
      }
    }

    for (Object stateMachine : stateMachines) {
      imgGenerator.generateUMLStateMachine(messagesImgPath, fileSystemManager,
          (StateMachineType) stateMachine, templateErrorListener);
    }
  }

  private void generateActorsList(Path messagesDocPath, List<ActorType> actorList)
      throws Exception {
    ST st = stGroup.getInstanceOf("actors");
    st.add("actors", actorList);
    st.add("title", "All Actors");
    Path path = messagesDocPath.resolve("AllActors.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateAllComponentsList(Path messagesDocPath, List<String> componentNameList)
      throws Exception {
    ST st = stGroup.getInstanceOf("components");
    st.add("componentNames", componentNameList);
    st.add("title", "All Components");
    Path path = messagesDocPath.resolve("AllComponents.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateAllMessageList(Path messagesDocPath, List<MessageType> messageList)
      throws Exception {
    ST st = stGroup.getInstanceOf("messages");
    st.add("messages", messageList);
    st.add("title", "All Messages");
    Path path = messagesDocPath.resolve("AllMessages.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateCategories(Path messagesDocPath, String title,
      List<CategoryType> categoriesList) throws Exception {
    ST st = stGroup.getInstanceOf("categories");
    st.add("title", title);
    st.add("categories", categoriesList);
    Path path = messagesDocPath.resolve("MessageCategories.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateCodeSetDetail(Path datatypesOutputPath, CodeSetType codeSet)
      throws Exception {
    Path path = datatypesOutputPath.resolve(String.format("%s.html", codeSet.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      ST stCodesetStart = stGroup.getInstanceOf("codeSetStart");
      stCodesetStart.add("codeSet", codeSet);
      stCodesetStart.write(writer, templateErrorListener);

      List<CodeType> codeList = codeSet.getCode();
      for (CodeType code : codeList) {
        ST stCode = stGroup.getInstanceOf("code");
        stCode.add("code", code);
        stCode.add("supported", supportedMap.get(code.getSupported()));
        stCode.write(writer, templateErrorListener);
      }

      ST stCodesetEnd = stGroup.getInstanceOf("codeSetEnd");
      stCodesetEnd.add("codeSet", codeSet);
      stCodesetEnd.write(writer, templateErrorListener);
    }
  }

  private void generateCodeSetList(Path datatypesOutputPath, List<CodeSetType> codeSetList)
      throws Exception {
    ST st = stGroup.getInstanceOf("codeSets");
    st.add("codeSets", codeSetList);
    st.add("title", "All Code Sets");
    Path path = datatypesOutputPath.resolve("AllCodeSets.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateComponentDetail(Path messagesDocPath, ComponentType component)
      throws Exception {
    ST stComponentStart;
    stComponentStart = stGroup.getInstanceOf("componentStart");
    stComponentStart.add("component", component);
    ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    List<Object> members = component.getComponentRefOrGroupRefOrFieldRef();

    Path path = messagesDocPath.resolve(String.format("%s.html", component.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      stComponentStart.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stComponentEnd.write(writer, templateErrorListener);
    }
  }

  private void generateDatatype(Path datatypesOutputPath, Datatype datatype) throws Exception {
    ST st = stGroup.getInstanceOf("datatype");
    st.add("datatype", datatype);
    Path path = datatypesOutputPath.resolve(String.format("%s.html", datatype.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateDatatypeList(Path datatypesOutputPath, List<Datatype> datatypeList)
      throws Exception {
    ST st = stGroup.getInstanceOf("datatypes");
    st.add("datatypes", datatypeList);
    st.add("title", "All Datatypes");
    Path path = datatypesOutputPath.resolve("AllDatatypes.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFieldDetail(Path fieldsOutputPath, FieldType field) throws Exception {
    ST st = stGroup.getInstanceOf("field");
    st.add("field", field);
    Path path = fieldsOutputPath.resolve(String.format("%s.html", field.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFieldsList(Path fieldsOutputPath, List<FieldType> fieldList)
      throws Exception {
    ST st = stGroup.getInstanceOf("fields");
    st.add("fields", fieldList);
    st.add("title", "All Fields");
    Path path = fieldsOutputPath.resolve("AllFields.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFlowDetail(Path messagesDocPath, FlowType flow) throws Exception {
    ST st = stGroup.getInstanceOf("flow");
    st.add("flow", flow);
    Path path = messagesDocPath.resolve(String.format("%s.html", flow.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateFlowsList(Path messagesDocPath, List<FlowType> flowList) throws Exception {
    ST st = stGroup.getInstanceOf("flows");
    st.add("flows", flowList);
    st.add("title", "All Flows");
    Path path = messagesDocPath.resolve("AllFlows.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateGroupDetail(Path messagesDocPath, GroupType group) throws Exception {
    ST stGroupStart;
    stGroupStart = stGroup.getInstanceOf("groupStart");
    stGroupStart.add("groupType", group);

    ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    List<Object> members = group.getComponentRefOrGroupRefOrFieldRef();

    Path path = messagesDocPath.resolve(String.format("%s.html", group.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      stGroupStart.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stComponentEnd.write(writer, templateErrorListener);
    }
  }

  private void generateMain(Path baseOutputPath, String title) throws Exception {
    ST st = stGroup.getInstanceOf("main");
    st.add("title", title);
    Path path = baseOutputPath.resolve("index.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMembers(List<Object> members, STWriter writer) {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldType field = getField(((FieldRefType) member).getId().intValue());
        ST stField = stGroup.getInstanceOf("fieldMember");
        stField.add("field", field);
        if (((FieldRefType) member).getSupported() == SupportType.SUPPORTED) {
          stField.add("presence", getFieldPresence((FieldRefType) member));
        } else {
          stField.add("presence", supportedMap.get(((FieldRefType) member).getSupported()));
        }
        stField.add("assign", ((FieldRefType) member).getAssign());
        stField.write(writer, templateErrorListener);
      } else if (member instanceof GroupRefType) {
        GroupType component = getGroup(((GroupRefType) member).getId().intValue());
        ST stComponent = stGroup.getInstanceOf("componentMember");
        stComponent.add("component", component);
        if (((ComponentRefType) member).getSupported() == SupportType.SUPPORTED) {
          stComponent.add("presence",
              ((ComponentRefType) member).getPresence().value().toLowerCase());
        } else {
          stComponent.add("presence", supportedMap.get(((ComponentRefType) member).getSupported()));
        }
        stComponent.write(writer, templateErrorListener);
      } else if (member instanceof ComponentRefType) {
        ComponentType component = getComponent(((ComponentRefType) member).getId().intValue());
        ST stComponent = stGroup.getInstanceOf("componentMember");
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

  private void generateMessageDetail(Path messagesDocPath, Path messagesImgPath,
      MessageType message) throws Exception {
    ST stMessageStart = stGroup.getInstanceOf("messageStart");
    ST stMessagePart2 = stGroup.getInstanceOf("messagePart2");
    ST stMessageEnd = stGroup.getInstanceOf("messageEnd");
    stMessageStart.add("message", message);
    stMessagePart2.add("message", message);
    stMessageEnd.add("message", message);

    List<ResponseType> responses = null;
    final Responses responses2 = message.getResponses();
    if (responses2 != null) {
      responses = responses2.getResponse();
    }
    List<Object> members = message.getStructure().getComponentRefOrGroupRefOrFieldRef();

    Path path = messagesDocPath
        .resolve(String.format("%s-%s.html", message.getName(), message.getScenario()));
    try (STWriterWrapper writer = getWriter(path)) {
      stMessageStart.write(writer, templateErrorListener);
      if (responses != null) {
        generateResponses(responses, writer);
      }

      stMessagePart2.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stMessageEnd.write(writer, templateErrorListener);
    }

    if (responses != null) {
      FlowType flow = getFlow(message.getFlow());
      imgGenerator.generateUMLSequence(messagesImgPath, fileSystemManager, message, flow, responses,
          templateErrorListener);
    }
  }

  private void generateMessageListByCategory(Path messagesDocPath, CategoryType category,
      List<MessageType> messageList) throws Exception {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> category.getId().equals(m.getCategory())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", category.getId()));
    Path path = messagesDocPath.resolve(String.format("%sMessages.html", category.getId()));
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMessageListByFlow(Path messagesDocPath, FlowType flow,
      List<MessageType> messageList) throws Exception {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> flow.getName().equals(m.getFlow())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", flow.getName()));
    Path path = messagesDocPath.resolve(String.format("%sMessages.html", flow.getName()));
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateMetadata(Path baseOutputPath, Repository repository,
      List<JAXBElement<SimpleLiteral>> elementList) throws Exception {
    ST st = stGroup.getInstanceOf("metadata");
    st.add("repository", repository);
    st.add("elementList", elementList);
    Path path = baseOutputPath.resolve("metadata.html");
    try (STWriterWrapper writer = getWriter(path)) {
      st.write(writer, templateErrorListener);
    }
  }

  private void generateResponses(List<ResponseType> responseList, STWriter writer) {
    for (ResponseType response : responseList) {
      List<Object> responses = response.getMessageRefOrAssignOrTrigger();
      for (Object responseRef : responses) {
        if (responseRef instanceof MessageRefType) {
          MessageRefType messageRef = (MessageRefType) responseRef;
          ST st = stGroup.getInstanceOf("messageResponse");
          st.add("message", messageRef.getName());
          st.add("scenario", messageRef.getScenario());
          st.add("when", response.getWhen());
          st.write(writer, templateErrorListener);
        }
      }
    }
  }

  private ComponentType getComponent(int componentId) {
    List<ComponentType> components = repository.getComponents().getComponent();
    for (ComponentType component : components) {
      if (component.getId().intValue() == componentId) {
        return component;
      }
    }
    return null;
  }

  private FieldType getField(int id) {
    List<FieldType> fields = repository.getFields().getField();
    for (FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType;
      }
    }
    return null;
  }

  private String getFieldPresence(FieldRefType fieldRef) {
    switch (fieldRef.getPresence()) {
      case CONSTANT:
        return String.format("constant %s", fieldRef.getValue());
      case FORBIDDEN:
        return "forbidden";
      case IGNORED:
        return "ignored";
      case OPTIONAL:
        List<FieldRuleType> rules = fieldRef.getRule();
        for (FieldRuleType rule : rules) {
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

  private FlowType getFlow(String name) {
    List<Object> afList = repository.getActors().getActorOrFlow();
    for (Object obj : afList) {
      if (obj instanceof FlowType) {
        FlowType flow = (FlowType) obj;
        if (flow.getName().equals(name)) {
          return flow;
        }
      }
    }
    return null;
  }

  private GroupType getGroup(int componentId) {
    List<GroupType> groups = repository.getGroups().getGroup();
    for (GroupType group : groups) {
      if (group.getId().intValue() == componentId) {
        return group;
      }
    }
    return null;
  }

  private String getTitle() {
    String title = "Orchestra";
    List<JAXBElement<SimpleLiteral>> metadata = repository.getMetadata().getAny();
    for (JAXBElement<SimpleLiteral> element : metadata) {
      if (element.getName().getLocalPart().equals("title")) {
        title = String.join(" ", element.getValue().getContent());
        break;
      }
    }
    return title;
  }
  private Repository unmarshal(Reader reader) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    jaxbUnmarshaller.setEventHandler(unmarshallerErrorHandler);
    return (Repository) jaxbUnmarshaller.unmarshal(reader);
  }
  
  private STWriterWrapper getWriter(Path path) throws IOException {
    return new STWriterWrapper(this.fileSystemManager.getWriter(path));
  }
}
