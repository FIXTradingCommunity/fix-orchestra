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
package io.fixprotocol.orchestra.docgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

  /**
   * Generates documentation
   * 
   * @param args command line arguments
   *        <ol>
   *        <li>Name of Orchestra input file in Repository 2016 Edition format
   *        <li>Name of base output directory--will be created if it does not exist, defaults to
   *        "doc"</li>
   *        </ol>
   * @throws JAXBException if XML file unmarshaling fails
   * @throws IOException if input file is not found or cannot be read
   */
  public static void main(String[] args) throws JAXBException, IOException {
    if (args.length < 1) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.docgen.DocGenerator <input-filename> [output-dir]");
    } else {
      try (InputStream is = new FileInputStream(args[0])) {

        File outputDir;
        if (args.length > 1) {
          outputDir = new File(args[1]);
        } else {
          outputDir = new File("doc");
        }
        DocGenerator gen = new DocGenerator(is, outputDir);
        gen.generate();
      }
    }
  }

  private final File baseOutputDir;
  private final String encoding = "UTF-8";
  private final ImgGenerator imgGenerator = new ImgGenerator();

  private final STErrorListener templateErrorListener = new STErrorListener() {

    @Override
    public void compileTimeError(STMessage msg) {
      System.err.println(msg.toString());
    }

    @Override
    public void internalError(STMessage msg) {
      System.err.println(msg.toString());
    }

    @Override
    public void IOError(STMessage msg) {
      System.err.println(msg.toString());
    }

    @Override
    public void runTimeError(STMessage msg) {
      System.err.println(msg.toString());
    }

  };
  private final Repository repository;
  private final STGroup stGroup;
  private ValidationEventHandler unmarshallerErrorHandler = new ValidationEventHandler() {

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

    @Override
    public boolean handleEvent(ValidationEvent event) {
      System.err.print(String.format("%s line %d col %d %s", severityToString(event.getSeverity()),
          event.getLocator().getLineNumber(), event.getLocator().getColumnNumber(),
          event.getMessage()));
      return event.getSeverity() == ValidationEvent.WARNING;
    }

  };


  private final static Map<SupportType, String> supportedMap = new HashMap<>();

  static {
    supportedMap.put(SupportType.SUPPORTED, "&#10003;"); // check mark
    supportedMap.put(SupportType.FORBIDDEN, "&#10007;"); // ballot x
    supportedMap.put(SupportType.IGNORED, "&ndash;");
  }

  /**
   * Constructs a DocGenerator
   * 
   * @param is an input stream to consume a Repository
   * @param baseOutputDir directory to write documentation files
   * @throws JAXBException if a parsing error occurs
   * @throws IOException if a file cannot be accessed
   */
  public DocGenerator(InputStream is, File baseOutputDir) throws JAXBException, IOException {
    this.baseOutputDir = makeDirectory(baseOutputDir);
    this.stGroup = new STGroupFile("templates/docgen.stg", '$', '$');
    // STGroup.verbose = true;
    this.repository = unmarshal(is);
  }

  /**
   * Generates documentation
   * 
   * @throws IOException if documentation cannot be written to a file
   */
  public void generate() throws IOException {
    try {
      createCss(baseOutputDir);

      generateMain(baseOutputDir, getTitle());
      generateMetadata(baseOutputDir, repository, repository.getMetadata().getAny());

      File datatypesOutputDir = makeDirectory(new File(baseOutputDir, "datatypes"));
      generateDatatypeList(datatypesOutputDir, repository.getDatatypes().getDatatype());
      repository.getDatatypes().getDatatype().forEach(d -> {
        try {
          generateDatatype(datatypesOutputDir, d);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      File fieldsOutputDir = makeDirectory(new File(baseOutputDir, "fields"));
      List<FieldType> sortedFieldList = repository.getFields().getField().stream()
          .sorted(Comparator.comparing(FieldType::getName)).collect(Collectors.toList());
      generateFieldsList(fieldsOutputDir, sortedFieldList);
      repository.getFields().getField().forEach(f -> {
        try {
          generateFieldDetail(fieldsOutputDir, f);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      List<CodeSetType> allCodeSets = repository.getCodeSets().getCodeSet();
      generateCodeSetList(datatypesOutputDir, allCodeSets.stream()
          .sorted(Comparator.comparing(CodeSetType::getName)).collect(Collectors.toList()));
      repository.getCodeSets().getCodeSet().forEach(cs -> {
        try {
          generateCodeSetDetail(datatypesOutputDir, cs);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      File messagesDocDir = makeDirectory(new File(baseOutputDir, "messages"));
      File messagesImgDir = makeDirectory(new File(messagesDocDir, "img"));

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

      generateCategories(messagesDocDir, "Message Categories", sortedCategoryList);

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
      generateActorsList(messagesDocDir, actorList);
      actorList.forEach(a -> {
        try {
          generateActorDetail(messagesDocDir, messagesImgDir, a);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });


      final List<FlowType> flowList =
          actors.orElse(new Actors()).getActorOrFlow().stream().filter(af -> af instanceof FlowType)
              .map(af -> (FlowType) af).collect(Collectors.toList());
      generateFlowsList(messagesDocDir, flowList);
      flowList.forEach(f -> {
        try {
          generateFlowDetail(messagesDocDir, f);

          generateMessageListByFlow(messagesDocDir, f, sortedMessageList);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });


      generateAllMessageList(messagesDocDir, sortedMessageList);

      sortedCategoryList.forEach(c -> {
        try {
          generateMessageListByCategory(messagesDocDir, c, sortedMessageList);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      List<ComponentType> sortedComponentList =
          repository.getComponents().getComponentOrGroup().stream()
              .sorted(Comparator.comparing(ComponentType::getName)).collect(Collectors.toList());
      generateAllComponentsList(messagesDocDir, sortedComponentList);
      repository.getComponents().getComponentOrGroup().forEach(c -> {
        try {
          generateComponentDetail(messagesDocDir, c);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      repository.getMessages().getMessage().forEach(m -> {
        try {
          generateMessageDetail(messagesDocDir, messagesImgDir, m);
        } catch (IOException e) {
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

  private void createCss(File outputDir) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream in = classLoader.getResourceAsStream("orchestra.css")) {
      Files.copy(in, new File(outputDir, "orchestra.css").toPath(),
          StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private void generateActorDetail(File docDir, File imgDir, ActorType actor) throws IOException {
    File outputFile = new File(docDir, String.format("%s.html", actor.getName()));

    try (OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);

      ST stActor = stGroup.getInstanceOf("actorStart");
      stActor.add("actor", actor);
      stActor.write(writer, templateErrorListener);

      List<Object> members = actor.getFieldOrFieldRefOrComponent();
      generateMembers(members, writer);

      ST stActor2 = stGroup.getInstanceOf("actorPart2");
      stActor2.add("actor", actor);
      stActor2.write(writer, templateErrorListener);

      List<Object> stateMachines = actor.getFieldOrFieldRefOrComponent().stream()
          .filter(o -> o instanceof StateMachineType).collect(Collectors.toList());

      for (Object stateMachine : stateMachines) {
        ST stStates = stGroup.getInstanceOf("stateMachine");
        stStates.add("states", stateMachine);
        stStates.write(writer, templateErrorListener);
        imgGenerator.generateUMLStateMachine(imgDir, (StateMachineType) stateMachine,
            templateErrorListener);
      }
    }
  }

  private void generateActorsList(File outputDir, List<ActorType> actorList) throws IOException {
    ST st = stGroup.getInstanceOf("actors");
    st.add("actors", actorList);
    st.add("title", "All Actors");
    File outputFile = new File(outputDir, "AllActors.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateAllComponentsList(File outputDir, List<ComponentType> componentList)
      throws IOException {
    ST st = stGroup.getInstanceOf("components");
    st.add("components", componentList);
    st.add("title", "All Components");
    File outputFile = new File(outputDir, "AllComponents.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateAllMessageList(File outputDir, List<MessageType> messageList)
      throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    st.add("messages", messageList);
    st.add("title", "All Messages");
    File outputFile = new File(outputDir, "AllMessages.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateCodeSetDetail(File outputDir, CodeSetType codeSet) throws IOException {
    File outputFile = new File(outputDir, String.format("%s.html", codeSet.getName()));

    try (OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
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

  private void generateCodeSetList(File outputDir, List<CodeSetType> codeSetList)
      throws IOException {
    ST st = stGroup.getInstanceOf("codeSets");
    st.add("codeSets", codeSetList);
    st.add("title", "All Code Sets");
    File outputFile = new File(outputDir, "AllCodeSets.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateComponentDetail(File outputDir, ComponentType component) throws IOException {
    ST stComponentStart;
    if (component instanceof GroupType) {
      stComponentStart = stGroup.getInstanceOf("groupStart");
    } else {
      stComponentStart = stGroup.getInstanceOf("componentStart");
    }
    stComponentStart.add("component", component);
    ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    stComponentEnd.add("component", component);
    File outputFile = new File(outputDir, String.format("%s.html", component.getName()));
    List<Object> members = component.getComponentRefOrGroupRefOrFieldRef();

    try (OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
      stComponentStart.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stComponentEnd.write(writer, templateErrorListener);
    }
  }

  private void generateDatatype(File outputDir, Datatype datatype) throws IOException {
    ST st = stGroup.getInstanceOf("datatype");
    st.add("datatype", datatype);
    File outputFile = new File(outputDir, String.format("%s.html", datatype.getName()));
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateDatatypeList(File outputDir, List<Datatype> datatypeList)
      throws IOException {
    ST st = stGroup.getInstanceOf("datatypes");
    st.add("datatypes", datatypeList);
    st.add("title", "All Datatypes");
    File outputFile = new File(outputDir, "AllDatatypes.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateFieldDetail(File outputDir, FieldType field) throws IOException {
    ST st = stGroup.getInstanceOf("field");
    st.add("field", field);
    File outputFile = new File(outputDir, String.format("%s.html", field.getName()));
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateFieldsList(File outputDir, List<FieldType> fieldList) throws IOException {
    ST st = stGroup.getInstanceOf("fields");
    st.add("fields", fieldList);
    st.add("title", "All Fields");
    File outputFile = new File(outputDir, "AllFields.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateFlowDetail(File outputDir, FlowType flow) throws IOException {
    ST st = stGroup.getInstanceOf("flow");
    st.add("flow", flow);
    File outputFile = new File(outputDir, String.format("%s.html", flow.getName()));
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateFlowsList(File outputDir, List<FlowType> flowList) throws IOException {
    ST st = stGroup.getInstanceOf("flows");
    st.add("flows", flowList);
    st.add("title", "All Flows");
    File outputFile = new File(outputDir, "AllFlows.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateMain(File outputDir, String title) throws IOException {
    ST st = stGroup.getInstanceOf("main");
    st.add("title", title);
    File outputFile = new File(outputDir, "index.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateCategories(File outputDir, String title, List<CategoryType> categoriesList)
      throws IOException {
    ST st = stGroup.getInstanceOf("categories");
    st.add("title", title);
    st.add("categories", categoriesList);
    File outputFile = new File(outputDir, "MessageCategories.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateMembers(List<Object> members, NoIndentWriter writer) {
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

  private void generateMessageDetail(File messagesDocDir, File messagesImgDir, MessageType message)
      throws IOException {
    ST stMessageStart = stGroup.getInstanceOf("messageStart");
    ST stMessagePart2 = stGroup.getInstanceOf("messagePart2");
    ST stMessageEnd = stGroup.getInstanceOf("messageEnd");
    stMessageStart.add("message", message);
    stMessagePart2.add("message", message);
    stMessageEnd.add("message", message);
    File outputFile = new File(messagesDocDir,
        String.format("%s-%s.html", message.getName(), message.getScenario()));

    List<ResponseType> responses = null;
    final Responses responses2 = message.getResponses();
    if (responses2 != null) {
      responses = responses2.getResponse();
    }
    List<Object> members = message.getStructure().getComponentOrComponentRefOrGroup();

    try (OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
      stMessageStart.write(writer, templateErrorListener);
      if (responses != null) {
        generateResponses(responses, writer);
        File imgFile = new File(messagesImgDir,
            String.format("%s-%s.png", message.getName(), message.getScenario()));
        try (OutputStreamWriter imgFileWriter =
            new OutputStreamWriter(new FileOutputStream(imgFile), "UTF-8")) {
          FlowType flow = getFlow(message.getFlow());
          imgGenerator.generateUMLSequence(messagesImgDir, message, flow, responses,
              templateErrorListener);
        }
      }
      stMessagePart2.write(writer, templateErrorListener);
      generateMembers(members, writer);
      stMessageEnd.write(writer, templateErrorListener);
    }
  }

  private void generateMessageListByCategory(File outputDir, CategoryType category,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> category.getId().equals(m.getCategory())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", category.getId()));
    File outputFile = new File(outputDir, String.format("%sMessages.html", category.getId()));
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateMessageListByFlow(File outputDir, FlowType flow,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> flow.getName().equals(m.getFlow())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    st.add("title", String.format("%s Messages", flow.getName()));
    File outputFile = new File(outputDir, String.format("%sMessages.html", flow.getName()));
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateMetadata(File outputDir, Repository repository,
      List<JAXBElement<SimpleLiteral>> elementList) throws IOException {
    ST st = stGroup.getInstanceOf("metadata");
    st.add("repository", repository);
    st.add("elementList", elementList);
    File outputFile = new File(outputDir, "metadata.html");
    st.write(outputFile, templateErrorListener, encoding);
  }

  private void generateResponses(List<ResponseType> responseList, NoIndentWriter writer) {
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
    List<ComponentType> components = repository.getComponents().getComponentOrGroup();
    for (ComponentType component : components) {
      if (component.getId().intValue() == componentId) {
        return component;
      }
    }
    return null;
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
      case CONDITIONAL:
        List<FieldRuleType> rules = fieldRef.getRule();
        for (FieldRuleType rule : rules) {
          if (rule.getPresence() == PresenceT.REQUIRED) {
            return String.format("required when %s", rule.getWhen());
          }
        }
        return "conditional";
      case CONSTANT:
        return String.format("constant %s", fieldRef.getValue());
      case FORBIDDEN:
        return "forbidden";
      case IGNORED:
        return "ignored";
      case OPTIONAL:
        return "optional";
      case REQUIRED:
        return "required";
    }
    return "";
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

  private File makeDirectory(File dir) throws IOException {
    dir.mkdirs();
    if (!dir.isDirectory()) {
      throw new IOException(dir.toString() + " not a directory or is inaccessible");
    }
    return dir;
  }

  private Repository unmarshal(InputStream is) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    jaxbUnmarshaller.setEventHandler(unmarshallerErrorHandler);
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }
}
