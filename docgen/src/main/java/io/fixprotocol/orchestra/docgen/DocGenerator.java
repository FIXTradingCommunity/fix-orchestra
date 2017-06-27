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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import io.fixprotocol._2016.fixrepository.CategoryType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldRuleType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
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
  private final File baseOutputDir;
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


  private File makeDirectory(File dir) throws IOException {
    dir.mkdir();
    if (!dir.isDirectory()) {
      throw new IOException(dir.toString() + " not a directory or is inaccessible");
    }
    return dir;
  }

  public void generate() throws IOException {
    generateSectionsList(baseOutputDir, repository.getSections().getSection());
    repository.getSections().getSection().forEach(s -> {
      try {
        generateCategoryListBySection(baseOutputDir, s, repository.getCategories().getCategory());
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    });

    File datatypesOutputDir = makeDirectory(new File(baseOutputDir, "datatypes"));
    generateDatatypeList(datatypesOutputDir, repository.getDatatypes().getDatatype());
    repository.getDatatypes().getDatatype().forEach(d -> {
      try {
        generateDatatype(datatypesOutputDir, d);
      } catch (IOException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      }
    });

    File fieldsOutputDir = makeDirectory(new File(baseOutputDir, "fields"));
    List<FieldType> sortedFieldList = repository.getFields().getField().stream()
        .sorted(new FieldTypeComparator()).collect(Collectors.toList());
    generateFieldsList(fieldsOutputDir, sortedFieldList);
    repository.getFields().getField().forEach(f -> {
      try {
        generateFieldDetail(fieldsOutputDir, f);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    });

    repository.getCodeSets().forEach(csl -> csl.getCodeSet().forEach(cs -> {
      try {
        generateCodeSetDetail(datatypesOutputDir, cs);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }));

    generateProtocolList(baseOutputDir, repository.getProtocol());
    repository.getProtocol().forEach(p -> {
      try {
        File protocolOutputDir = makeDirectory(new File(baseOutputDir, p.getName()));
        List<MessageType> sortedMessageList = p.getMessages().getMessage().stream()
            .sorted(new MessageTypeComparator()).collect(Collectors.toList());
        generateAllMessageList(protocolOutputDir, sortedMessageList);
        repository.getCategories().getCategory().forEach(c -> {

          try {
            generateMessageListByCategory(protocolOutputDir, c, sortedMessageList);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        });

        p.getComponents().getComponentOrGroup().forEach(c -> {

          try {
            generateComponentDetail(protocolOutputDir, c);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        });
        p.getMessages().getMessage().forEach(m -> {

          try {
            generateMessageDetail(protocolOutputDir, m);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });



      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    });
  }

  private void generateAllMessageList(File outputDir, List<MessageType> messageList) throws IOException {
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

  private void generateComponentDetail(File outputDir, ComponentType component)
      throws IOException {
    ST stComponentStart = stGroup.getInstanceOf("componentStart");
    stComponentStart.add("component", component);
    ST stComponentEnd = stGroup.getInstanceOf("componentEnd");
    stComponentEnd.add("component", component);
    File outputFile =
        new File(outputDir, String.format("%s.html", component.getName()));
    List<Object> members = component.getComponentRefOrGroupRefOrFieldRef();
    
    try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
      stComponentStart.write(writer, errorListener);
      generateMembers(members, writer);
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
    File outputFile = new File(outputDir, "Datatypes.html");
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

  private void generateMessageDetail(File outputDir, MessageType message)
      throws IOException {
    ST stMessageStart = stGroup.getInstanceOf("messageStart");
    ST stMessagePart2 = stGroup.getInstanceOf("messagePart2");
    ST stMessageEnd = stGroup.getInstanceOf("messageEnd");
    stMessageStart.add("message", message);
    stMessagePart2.add("message", message);
    stMessageEnd.add("message", message);
    File outputFile = new File(outputDir, String.format("%s-%s.html", 
        message.getName(), message.getScenario()));
    
    List<ResponseType> responses = null;
    final Responses responses2 = message.getResponses();
    if (responses2 != null) {
      responses = responses2.getResponse();
    }
    List<Object> members = message.getStructure().getComponentOrComponentRefOrGroup();

    try (OutputStreamWriter fileWriter = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
      NoIndentWriter writer = new NoIndentWriter(fileWriter);
      stMessageStart.write(writer, errorListener);
      if (responses!= null) {
        generateResponses(responses, writer);
      }
      stMessagePart2.write(writer, errorListener);
      generateMembers(members, writer);
      stMessageEnd.write(writer, errorListener);
    }
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

  private void generateMembers(List<Object> members, NoIndentWriter writer) {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        ST stField = stGroup.getInstanceOf("fieldMember");
        stField.add("fieldRef", member);
        stField.add("presence", getFieldPresence((FieldRefType) member));
        stField.write(writer, errorListener);
      } else if (member instanceof ComponentRefType || member instanceof GroupRefType) {
        ST stComponent = stGroup.getInstanceOf("componentMember");
        stComponent.add("componentRef", member);
        stComponent.add("presence", ((ComponentRefType)member).getPresence().value().toLowerCase());
        stComponent.write(writer, errorListener);
      }
    }
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

  private void generateMessageListByCategory(File outputDir, CategoryType category,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    final List<MessageType> filteredMessageList = messageList.stream()
        .filter(m -> category.getId().equals(m.getCategory())).collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    File outputFile = new File(outputDir,
        String.format("%sMessages.html", category.getId()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateProtocolList(File outputDir, List<Protocol> protocolList)
      throws IOException {
    ST st = stGroup.getInstanceOf("protocols");
    st.add("protocols", protocolList);
    File outputFile = new File(outputDir, "protocols.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateSectionsList(File outputDir, List<SectionType> sectionList)
      throws IOException {
    ST st = stGroup.getInstanceOf("sections");
    st.add("sections", sectionList);
    File outputFile = new File(outputDir, "sections.html");
    st.write(outputFile, errorListener, encoding);
  }

  private Repository unmarshal(InputStream is) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }

}
