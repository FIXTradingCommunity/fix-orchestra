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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.purl.dc.elements._1.SimpleLiteral;
import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import io.fixprotocol._2016.fixrepository.ActorType;
import io.fixprotocol._2016.fixrepository.CategoryType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
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
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.ResponseType;
import io.fixprotocol._2016.fixrepository.SectionType;

/**
 * @author Don Mendelson
 *
 */
public class DocGenerator {

  private static final class CodeSetComparator implements Comparator<CodeSetType> {
    @Override
    public int compare(CodeSetType o1, CodeSetType o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }
  
  private static final class FieldTypeComparator implements Comparator<FieldType> {
    @Override
    public int compare(FieldType o1, FieldType o2) {
      return o1.getName().compareTo(o2.getName());
    }
  }

  private static final class MessageTypeComparator implements Comparator<MessageType> {
    @Override
    public int compare(MessageType o1, MessageType o2) {
      int retv = o1.getName().compareTo(o2.getName());
      if (retv == 0) {
        retv = o1.getScenario().compareTo(o2.getScenario());
      }
      return retv;
    }
  }

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
          outputDir = new File("args[1]");
        } else {
          outputDir = new File("doc");
        }
        DocGenerator gen = new DocGenerator(is, outputDir);
        gen.generate();
      }
    }
  }

  private final File baseOutputDir;
  private String encoding = "UTF-8";
  private STErrorListener errorListener = new STErrorListener() {

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


  /*
   * @throws JAXBException
   * 
   */
  public DocGenerator(InputStream is, File baseOutputDir) throws JAXBException, IOException {
    this.baseOutputDir = makeDirectory(baseOutputDir);
    this.stGroup = new STGroupFile("templates/docgen.stg", '$', '$');
    // STGroup.verbose = true;
    this.repository = unmarshal(is);
  }

  public void generate() throws IOException {
    // createCss(baseOutputDir);
   
    generateSectionsList(baseOutputDir, repository.getSections().getSection());

    try {
      repository.getSections().getSection().forEach(s -> {
        try {
          generateCategoryListBySection(baseOutputDir, s, repository.getCategories().getCategory());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

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
          .sorted(new FieldTypeComparator()).collect(Collectors.toList());
      generateFieldsList(fieldsOutputDir, sortedFieldList);
      repository.getFields().getField().forEach(f -> {
        try {
          generateFieldDetail(fieldsOutputDir, f);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

      List<CodeSetType> allCodeSets = new ArrayList<>();
      repository.getCodeSets().forEach(csl -> allCodeSets.addAll(csl.getCodeSet()));
      generateCodeSetList(datatypesOutputDir, allCodeSets.stream()
          .sorted(new CodeSetComparator()).collect(Collectors.toList()));
      repository.getCodeSets().forEach(csl -> csl.getCodeSet().forEach(cs -> {
        try {
          generateCodeSetDetail(datatypesOutputDir, cs);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }));

      generateMain(baseOutputDir, getTitle(), repository.getProtocol());
      repository.getProtocol().forEach(p -> {
        try {
          File protocolOutputDir = makeDirectory(new File(baseOutputDir, p.getName()));

          List<MessageType> sortedMessageList = p.getMessages().getMessage().stream()
              .sorted(new MessageTypeComparator()).collect(Collectors.toList());
          
          final List<ActorType> actorList =
              p.getActors().getActorOrFlow().stream().filter(af -> af instanceof ActorType)
                  .map(af -> (ActorType) af).collect(Collectors.toList());
          generateActorsList(protocolOutputDir, actorList);
          actorList.forEach(a -> {
            try {
              generateActorDetail(protocolOutputDir, a);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

          final List<FlowType> flowList =
              p.getActors().getActorOrFlow().stream().filter(af -> af instanceof FlowType)
                  .map(af -> (FlowType) af).collect(Collectors.toList());
          generateFlowsList(protocolOutputDir, flowList);
          flowList.forEach(f -> {
            try {
              generateFlowDetail(protocolOutputDir, f);
              generateMessageListByFlow(protocolOutputDir, f, sortedMessageList);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });


          generateAllMessageList(protocolOutputDir, sortedMessageList);
          repository.getCategories().getCategory().forEach(c -> {

            try {
              generateMessageListByCategory(protocolOutputDir, c, sortedMessageList);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }

          });

          p.getComponents().getComponentOrGroup().forEach(c -> {

            try {
              generateComponentDetail(protocolOutputDir, c, p.getName());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
          p.getMessages().getMessage().forEach(m -> {

            try {
              generateMessageDetail(protocolOutputDir, m, p.getName());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (RuntimeException e) {
      Throwable cause = e.getCause();
      if (cause instanceof IOException) {
        throw (IOException) cause;
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

  private void generateActorDetail(File outputDir, ActorType actor) throws IOException {
    ST st = stGroup.getInstanceOf("actor");
    st.add("actor", actor);
    File outputFile = new File(outputDir, String.format("%s.html", actor.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateActorsList(File outputDir, List<ActorType> actorList) throws IOException {
    ST st = stGroup.getInstanceOf("actors");
    st.add("actors", actorList);
    File outputFile = new File(outputDir, "AllActors.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateAllMessageList(File outputDir, List<MessageType> messageList)
      throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    st.add("messages", messageList);
    File outputFile = new File(outputDir, "AllMessages.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateCategoryListBySection(File outputDir, SectionType section,
      List<CategoryType> categoryList) throws IOException {
    ST st = stGroup.getInstanceOf("categories");
    final List<CategoryType> filteredCategoryList = categoryList.stream()
        .filter(c -> c.getSection() == section.getId()).collect(Collectors.toList());
    st.add("categories", filteredCategoryList);
    File outputFile = new File(outputDir, section.getName() + "Categories.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateCodeSetDetail(File outputDir, CodeSetType codeSet) throws IOException {
    ST st = stGroup.getInstanceOf("codeSet");
    st.add("codeSet", codeSet);
    File outputFile = new File(outputDir, String.format("%s.html", codeSet.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateCodeSetList(File outputDir, List<CodeSetType> codeSetList) throws IOException {
    ST st = stGroup.getInstanceOf("codeSets");
    st.add("codeSets", codeSetList);
    File outputFile = new File(outputDir, "AllCodeSets.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateComponentDetail(File outputDir, ComponentType component, String protocolName)
      throws IOException {
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
      stComponentStart.write(writer, errorListener);
      generateMembers(protocolName, members, writer);
      stComponentEnd.write(writer, errorListener);
    }
  }

  private void generateDatatype(File outputDir, Datatype datatype) throws IOException {
    ST st = stGroup.getInstanceOf("datatype");
    st.add("datatype", datatype);
    File outputFile = new File(outputDir, String.format("%s.html", datatype.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateDatatypeList(File outputDir, List<Datatype> datatypeList)
      throws IOException {
    ST st = stGroup.getInstanceOf("datatypes");
    st.add("datatypes", datatypeList);
    File outputFile = new File(outputDir, "AllDatatypes.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFieldDetail(File outputDir, FieldType field) throws IOException {
    ST st = stGroup.getInstanceOf("field");
    st.add("field", field);
    File outputFile = new File(outputDir, String.format("%s.html", field.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFieldsList(File outputDir, List<FieldType> fieldList) throws IOException {
    ST st = stGroup.getInstanceOf("fields");
    st.add("fields", fieldList);
    File outputFile = new File(outputDir, "AllFields.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFlowDetail(File outputDir, FlowType flow) throws IOException {
    ST st = stGroup.getInstanceOf("flow");
    st.add("flow", flow);
    File outputFile = new File(outputDir, String.format("%s.html", flow.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFlowsList(File outputDir, List<FlowType> flowList) throws IOException {
    ST st = stGroup.getInstanceOf("flows");
    st.add("flows", flowList);
    File outputFile = new File(outputDir, "AllFlows.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateMain(File outputDir, String title, List<Protocol> protocolList) throws IOException {
    ST st = stGroup.getInstanceOf("main");
    st.add("title", title);
    st.add("protocols", protocolList);
    File outputFile = new File(outputDir, "index.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateMembers(String protocolName, List<Object> members, NoIndentWriter writer) {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldType field = getField(((FieldRefType) member).getId().intValue());
        ST stField = stGroup.getInstanceOf("fieldMember");
        stField.add("field", field);
        stField.add("presence", getFieldPresence((FieldRefType) member));
        stField.write(writer, errorListener);
      } else if (member instanceof ComponentRefType || member instanceof GroupRefType) {
        ComponentType component =
            getComponent(protocolName, ((ComponentRefType) member).getId().intValue());
        ST stComponent = stGroup.getInstanceOf("componentMember");
        stComponent.add("component", component);
        stComponent.add("presence",
            ((ComponentRefType) member).getPresence().value().toLowerCase());
        stComponent.write(writer, errorListener);
      }
    }
  }

  private void generateMessageDetail(File outputDir, MessageType message, String protocolName)
      throws IOException {
    ST stMessageStart = stGroup.getInstanceOf("messageStart");
    ST stMessagePart2 = stGroup.getInstanceOf("messagePart2");
    ST stMessageEnd = stGroup.getInstanceOf("messageEnd");
    stMessageStart.add("message", message);
    stMessagePart2.add("message", message);
    stMessageEnd.add("message", message);
    File outputFile =
        new File(outputDir, String.format("%s-%s.html", message.getName(), message.getScenario()));

    List<ResponseType> responses = null;
    final Responses responses2 = message.getResponses();
    if (responses2 != null) {
      responses = responses2.getResponse();
    }
    List<Object> members = message.getStructure().getComponentOrComponentRefOrGroup();

    try (OutputStreamWriter fileWriter =
        new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
      stMessageStart.write(writer, errorListener);
      if (responses != null) {
        generateResponses(responses, writer);
      }
      stMessagePart2.write(writer, errorListener);
      generateMembers(protocolName, members, writer);
      stMessageEnd.write(writer, errorListener);
    }
  }

  private void generateMessageListByCategory(File outputDir, CategoryType category,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> category.getId().equals(m.getCategory())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    File outputFile = new File(outputDir, String.format("%sMessages.html", category.getId()));
    st.write(outputFile, errorListener, encoding);
  }
  
  private void generateMessageListByFlow(File outputDir, FlowType flow,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> flow.getName().equals(m.getFlow())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    File outputFile = new File(outputDir, String.format("%sMessages.html", flow.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateResponses(List<ResponseType> responseList, NoIndentWriter writer) {
    for (ResponseType response : responseList) {
      List<Object> responses = response.getMessageRefOrAssignOrTransitionRef();
      for (Object responseRef : responses) {
        if (responseRef instanceof MessageRefType) {
          MessageRefType messageRef = (MessageRefType) responseRef;
          ST st = stGroup.getInstanceOf("messageResponse");
          st.add("message", messageRef.getName());
          st.add("scenario", messageRef.getScenario());
          st.add("when", response.getWhen());
          st.write(writer, errorListener);
        }
      }
    }
  }

  private void generateSectionsList(File outputDir, List<SectionType> sectionList)
      throws IOException {
    ST st = stGroup.getInstanceOf("sections");
    st.add("sections", sectionList);
    File outputFile = new File(outputDir, "sections.html");
    st.write(outputFile, errorListener, encoding);
  }

  private ComponentType getComponent(String protocolName, int componentId) {
    Protocol protocol = null;
    for (Protocol p : repository.getProtocol()) {
      if (p.getName().equals(protocolName)) {
        protocol = p;
      }
    }
    if (protocol == null) {
      return null;
    }

    List<ComponentType> components = protocol.getComponents().getComponentOrGroup();
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
    dir.mkdir();
    if (!dir.isDirectory()) {
      throw new IOException(dir.toString() + " not a directory or is inaccessible");
    }
    return dir;
  }
  
  private Repository unmarshal(InputStream is) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }
}
