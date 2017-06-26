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
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import io.fixprotocol._2016.fixrepository.CategoryType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;
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
   *        <li>Name of output directory--will be created if it does not exist, defaults to
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
  private final File outputDir;
  private final Repository repository;
  private final STGroup stGroup;


  /*
   * @throws JAXBException
   * 
   */
  public DocGenerator(InputStream is, File outputDir) throws JAXBException, IOException {
    outputDir.mkdir();
    if (!outputDir.isDirectory()) {
      throw new IOException("Output not a directory");
    }
    this.outputDir = outputDir;
    stGroup = new STGroupFile("templates/docgen.stg", '$', '$');
    STGroup.verbose = true;
    repository = unmarshal(is);
  }

  public void generate() throws IOException {
    generateSectionsList(repository.getSections().getSection());
    repository.getSections().getSection().forEach(s -> {
      try {
        generateCategoryListBySection(s, repository.getCategories().getCategory());
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    });
    
    repository.getDatatypes().getDatatype().forEach(d -> {
      try {
        generateDatatype(d);
      } catch (IOException e2) {
        // TODO Auto-generated catch block
        e2.printStackTrace();
      }
    });
    
    List<FieldType> sortedFieldList = repository.getFields().getField().stream()
        .sorted(new FieldTypeComparator()).collect(Collectors.toList());
    generateFieldsList(sortedFieldList);
    repository.getFields().getField().forEach(f -> {
      try {
        generateFieldDetail(f);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    });

    repository.getCodeSets().forEach(csl -> csl.getCodeSet().forEach(cs -> {
      try {
        generateCodeSetDetail(cs);
      } catch (IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
    }));

    generateProtocolList(repository.getProtocol());
    repository.getProtocol().forEach(p -> {
      try {
        List<MessageType> sortedMessageList = p.getMessages().getMessage().stream()
            .sorted(new MessageTypeComparator()).collect(Collectors.toList());
        generateAllMessageList(p, sortedMessageList);
        repository.getCategories().getCategory().forEach(c -> {
          try {
            generateMessageListByCategory(p, c, sortedMessageList);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        });
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      p.getComponents().getComponentOrGroup().forEach(c -> {
        try {
          generateComponentDetail(p, c);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
      p.getMessages().getMessage().forEach(m -> {
        try {
          generateMessageDetail(p, m);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });

    });

  }

  private void generateDatatype(Datatype datatype) throws IOException {
    ST st = stGroup.getInstanceOf("datatype");
    st.add("datatype", datatype);
    File outputFile = new File(outputDir, String.format("%s.html", 
        datatype.getName()));
    st.write(outputFile, errorListener, encoding);
  }
  
  private void generateComponentDetail(Protocol protocol, ComponentType component) throws IOException {
    ST st = stGroup.getInstanceOf("component");
    st.add("protocol", protocol);
    st.add("component", component);
    File outputFile = new File(outputDir, String.format("%s%s.html", protocol.getName(),
        component.getName()));
    st.write(outputFile, errorListener, encoding);
   }

  private void generateCodeSetDetail(CodeSetType codeSet) throws IOException {
    ST st = stGroup.getInstanceOf("codeSet");
    st.add("codeSet", codeSet);
    File outputFile = new File(outputDir, String.format("%s.html", codeSet.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFieldDetail(FieldType field) throws IOException {
    ST st = stGroup.getInstanceOf("field");
    st.add("field", field);
    File outputFile = new File(outputDir, String.format("%s.html", field.getName()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateFieldsList(List<FieldType> fieldList) throws IOException {
    ST st = stGroup.getInstanceOf("fields");
    st.add("fields", fieldList);
    File outputFile = new File(outputDir, "AllFields.html");
    st.write(outputFile, errorListener, encoding);
   }

  private void generateAllMessageList(Protocol protocol, List<MessageType> messageList)
      throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    st.add("protocol", protocol);
    st.add("messages", messageList);
    File outputFile = new File(outputDir, protocol.getName() + "AllMessages.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateCategoryListBySection(SectionType section, List<CategoryType> categoryList)
      throws IOException {
    ST st = stGroup.getInstanceOf("categories");
    final List<CategoryType> filteredCategoryList = categoryList.stream().filter(c -> c.getSection() == section.getId())
        .collect(Collectors.toList());
    st.add("categories", filteredCategoryList);
    File outputFile = new File(outputDir, section.getName() + "Categories.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateMessageDetail(Protocol protocol, MessageType message) throws IOException {
    ST st = stGroup.getInstanceOf("message");
    st.add("protocol", protocol);
    st.add("message", message);
    File outputFile = new File(outputDir, String.format("%s%s-%s.html", protocol.getName(),
        message.getName(), message.getScenario()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateMessageListByCategory(Protocol protocol, CategoryType category,
      List<MessageType> messageList) throws IOException {
    ST st = stGroup.getInstanceOf("messages");
    st.add("protocol", protocol);
    final List<MessageType> filteredMessageList = messageList.stream().filter(m -> category.getId().equals(m.getCategory()))
        .collect(Collectors.toList());
    st.add("messages", filteredMessageList);
    File outputFile = new File(outputDir,
        String.format("%s%sMessages.html", protocol.getName(), category.getId()));
    st.write(outputFile, errorListener, encoding);
  }

  private void generateProtocolList(List<Protocol> protocolList) throws IOException {
    ST st = stGroup.getInstanceOf("protocols");
    st.add("protocols", protocolList);
    File outputFile = new File(outputDir, "protocols.html");
    st.write(outputFile, errorListener, encoding);
  }

  private void generateSectionsList(List<SectionType> sectionList) throws IOException {
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
