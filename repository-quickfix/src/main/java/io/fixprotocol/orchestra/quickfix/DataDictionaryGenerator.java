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
package io.fixprotocol.orchestra.quickfix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import io.fixprotocol._2020.orchestra.repository.CodeSetType;
import io.fixprotocol._2020.orchestra.repository.CodeType;
import io.fixprotocol._2020.orchestra.repository.ComponentRefType;
import io.fixprotocol._2020.orchestra.repository.ComponentType;
import io.fixprotocol._2020.orchestra.repository.FieldRefType;
import io.fixprotocol._2020.orchestra.repository.FieldType;
import io.fixprotocol._2020.orchestra.repository.GroupRefType;
import io.fixprotocol._2020.orchestra.repository.GroupType;
import io.fixprotocol._2020.orchestra.repository.MessageType;
import io.fixprotocol._2020.orchestra.repository.PresenceT;
import io.fixprotocol._2020.orchestra.repository.Repository;

/**
 * Generates a QuickFIX data dictionary from a FIX Orchestra file
 * <p>
 * This format is consumable by the C++, Java and .NET versions of QuickFIX.
 * 
 * @author Don Mendelson
 *
 */
public class DataDictionaryGenerator {

  private static class KeyValue<T> {
    final String key;
    final T value;

    public KeyValue(String key, T value) {
      this.key = key;
      this.value = value;
    }
  }

  private static final int SPACES_PER_LEVEL = 2;

  /**
   * Runs a DataDictionaryGenerator with command line arguments
   * <p>
   * The data dictionary format is consumable by QuickFIX, QuickFIX/J and QuickFIX/n.
   * 
   * @param args command line arguments. The first argument is the name of a FIX Orchestra file. An
   *        optional second argument is the target directory for generated files. It defaults to
   *        directory "spec".
   * @throws IOException
   * @throws JAXBException
   */
  public static void main(String[] args) throws IOException, JAXBException {
    DataDictionaryGenerator generator = new DataDictionaryGenerator();
    if (args.length >= 1) {
      File inputFile = new File(args[0]);
      File outputDir;
      if (args.length >= 2) {
        outputDir = new File(args[1]);
      } else {
        outputDir = new File("spec");
      }
      try (FileInputStream inputStream = new FileInputStream(inputFile)) {
        generator.generate(inputStream, outputDir);
      }
    } else {
      generator.usage();
    }
  }

  private final Map<String, CodeSetType> codeSets = new HashMap<>();
  private final Map<Integer, ComponentType> components = new HashMap<>();
  private final Map<Integer, GroupType> groups = new HashMap<>();
  private final Map<Integer, FieldType> fields = new HashMap<>();

  public void generate(InputStream inputFile, File outputDir) throws JAXBException, IOException {
    final Repository repository = unmarshal(inputFile);
    generate(repository, outputDir);
  }

  public void generate(Repository repository, File outputDir) throws IOException {
    final List<CodeSetType> codeSetList = repository.getCodeSets().getCodeSet();
    for (CodeSetType codeSet : codeSetList) {
      codeSets.put(codeSet.getName(), codeSet);
    }

    final List<ComponentType> componentList = repository.getComponents().getComponent();
    for (ComponentType component : componentList) {
      components.put(component.getId().intValue(), component);
    }

    final List<GroupType> groupList = repository.getGroups().getGroup();
    for (GroupType group : groupList) {
      groups.put(group.getId().intValue(), group);
    }

    final List<FieldType> fieldList = repository.getFields().getField();
    for (FieldType fieldType : fieldList) {
      fields.put(fieldType.getId().intValue(), fieldType);
    }

    String version = repository.getVersion();
    // Split off EP portion of version in the form "FIX.5.0SP2_EP216"
    String[] parts = version.split("_");
    if (parts.length > 0) {
      version = parts[0];
    }
    int major = 0;
    int minor = 0;
    String regex = "(FIX\\.)(?<major>\\d+)(\\.)(?<minor>\\d+)(.*)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(version);
    if (matcher.find()) {
      major = Integer.parseInt(matcher.group("major"));
      minor = Integer.parseInt(matcher.group("minor"));

      String versionPath = version.replaceAll("[\\.]", "");
      File file = getSpecFilePath(outputDir, versionPath, ".xml");
      outputDir.mkdirs();
      try (FileWriter writer = new FileWriter(file)) {
        writeElement(writer, "fix", 0, false, new KeyValue<Integer>("major", major),
                new KeyValue<Integer>("minor", minor));
        writeElement(writer, "header", 1, true);
        writeElement(writer, "trailer", 1, true);
        writeElement(writer, "messages", 1, false);
        final List<MessageType> messageList = repository.getMessages().getMessage();
        for (MessageType messageType : messageList) {
          writeMessage(writer, messageType);
        }
        writeElementEnd(writer, "messages", 1);
        writeElement(writer, "components", 1, false);

        for (ComponentType componentType : componentList) {
          writeComponent(writer, componentType);
        }

        for (GroupType groupType : groupList) {
          writeGroup(writer, groupType);
        }

        writeElementEnd(writer, "components", 1);
        writeElement(writer, "fields", 1, false);
        for (FieldType fieldType : fieldList) {
          writeField(writer, fieldType);
        }
        writeElementEnd(writer, "fields", 1);
        writeElementEnd(writer, "fix", 0);
      }
    } else {
      System.err.format("Failed to parse FIX major and minor version in %s%n", version);
    }
  }

  private File getSpecFilePath(File outputDir, String versionPath, String extension) {
    StringBuilder sb = new StringBuilder();
    sb.append(versionPath);
    sb.append(extension);
    return new File(outputDir, sb.toString());
  }

  private String indent(int level) {
    char[] chars = new char[level * SPACES_PER_LEVEL];
    Arrays.fill(chars, ' ');
    return new String(chars);
  }

  private boolean isAdmin(String category) {
    return category != null && category.equals("Session");
  }

  private String toConstantName(String symbolicName) {
    StringBuilder sb = new StringBuilder(symbolicName);
    for (int i = symbolicName.length() - 1; i > 0; i--) {
      if (Character.isUpperCase(sb.charAt(i)) && !Character.isUpperCase(sb.charAt(i - 1))) {
        sb.insert(i, '_');
      }
    }
    return sb.toString().toUpperCase();
  }

  private Repository unmarshal(InputStream inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }

  private void usage() {
    System.out.format("Usage: java %s <input-file> <output-dir>", this.getClass().getName());
  }

  private Writer writeCode(Writer writer, CodeType code) throws IOException {
    writeElement(writer, "value", 3, true, new KeyValue<String>("enum", code.getValue()),
            new KeyValue<String>("description", toConstantName(code.getName())));
    return writer;
  }

  private Writer writeComponent(Writer writer, ComponentRefType componentRefType)
      throws IOException {
    ComponentType component = components.get(componentRefType.getId().intValue());
    writeElement(writer, "component", 3, true, new KeyValue<String>("name", component.getName()),
            new KeyValue<String>("required",
                    componentRefType.getPresence().equals(PresenceT.REQUIRED) ? "Y" : "N"));
    return writer;
  }

  private Writer writeComponent(Writer writer, ComponentType componentType) throws IOException {
    writeElement(writer, "component", 2, false,
            new KeyValue<String>("name", componentType.getName()));
    List<Object> members = componentType.getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        writeField(writer, fieldRefType);
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        writeGroup(writer, groupRefType);
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRefType = (ComponentRefType) member;
        writeComponent(writer, componentRefType);
      }
    }
    writeElementEnd(writer, "component", 2);
    return writer;
  }

  private Writer writeElement(Writer writer, String name, int level, boolean isEmpty)
      throws IOException {
    writer.write(String.format("%s<%s", indent(level), name));
    if (isEmpty) {
      writer.write("/>\n");
    } else {
      writer.write(">\n");
    }
    return writer;
  }

  private Writer writeElement(Writer writer, String name, int level, boolean isEmpty,
      KeyValue<?>... attributes) throws IOException {
    writer.write(String.format("%s<%s", indent(level), name));
    for (int i = 0; i < attributes.length; i++) {
      writer.write(String.format(" %s=\"%s\"", attributes[i].key, attributes[i].value.toString()));
    }
    if (isEmpty) {
      writer.write("/>\n");
    } else {
      writer.write(">\n");
    }
    return writer;
  }

  private Writer writeElementEnd(Writer writer, String name, int level) throws IOException {
    writer.write(String.format("%s</%s>%n", indent(level), name));
    return writer;
  }

  private Writer writeField(Writer writer, FieldRefType fieldRefType) throws IOException {
    FieldType field = fields.get(fieldRefType.getId().intValue());
    writeElement(writer, "field", 3, true, new KeyValue<String>("name", field.getName()),
            new KeyValue<String>("required",
                    fieldRefType.getPresence().equals(PresenceT.REQUIRED) ? "Y" : "N"));
    return writer;
  }

  private Writer writeField(Writer writer, FieldType fieldType) throws IOException {
    String type = fieldType.getType();
    CodeSetType codeSet = codeSets.get(type);
    String fixType = codeSet == null ? type : codeSet.getType();
    writeElement(writer, "field", 2, codeSet == null,
            new KeyValue<Integer>("number", fieldType.getId().intValue()),
            new KeyValue<String>("name", fieldType.getName()),
            new KeyValue<String>("type", fixType.toUpperCase()));
    if (codeSet != null) {
      for (CodeType code : codeSet.getCode()) {
        writeCode(writer, code);
      }
      writeElementEnd(writer, "field", 2);
    }
    return writer;
  }

  private Writer writeGroup(Writer writer, GroupRefType groupRefType) throws IOException {
    GroupType group = groups.get(groupRefType.getId().intValue());
    writeElement(writer, "component", 3, true, new KeyValue<String>("name", group.getName()),
            new KeyValue<String>("required",
                    groupRefType.getPresence().equals(PresenceT.REQUIRED) ? "Y" : "N"));
    return writer;
  }

  private Writer writeGroup(Writer writer, GroupType groupType) throws IOException {
    writeElement(writer, "component", 2, false, new KeyValue<String>("name", groupType.getName()));
    FieldType numInGroupField = fields.get(groupType.getNumInGroup().getId().intValue());
    writeElement(writer, "group", 3, false,
            new KeyValue<String>("name", numInGroupField.getName()));
    List<Object> members = groupType.getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        writeField(writer, fieldRefType);
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        writeGroup(writer, groupRefType);
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRefType = (ComponentRefType) member;
        writeComponent(writer, componentRefType);
      }
    }
    writeElementEnd(writer, "group", 3);
    writeElementEnd(writer, "component", 2);
    return writer;
  }

  private Writer writeMessage(Writer writer, MessageType messageType) throws IOException {
    boolean isAdminMessage = isAdmin(messageType.getCategory());
    String msgcat = isAdminMessage ? "admin" : "app";
    writeElement(writer, "message", 2, false, new KeyValue<String>("name", messageType.getName()),
            new KeyValue<String>("msgtype", messageType.getMsgType()),
            new KeyValue<String>("msgcat", msgcat));

    List<Object> members = messageType.getStructure().getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        writeField(writer, fieldRefType);
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        writeGroup(writer, groupRefType);
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRefType = (ComponentRefType) member;
        ComponentType componentType =
            components.get(((ComponentRefType) member).getId().intValue());
        if (!isAdminMessage && !isAdmin(componentType.getCategory())) {
          writeComponent(writer, componentRefType);
        }
      }
    }
    writeElementEnd(writer, "message", 2);
    return writer;
  }

}
