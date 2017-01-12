package io.fixprotocol.orchestra.quickfix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;

/**
 * Generates a QuickFIX data dictionary from a FIX Orchestra file
 * <p>
 * This format is consumable by the C++, Java and .NET versions of QuickFIX.
 * @author Don Mendelson
 *
 */
public class DataDictionaryGenerator {

  private class KeyValue<T> {
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
   */
  public static void main(String[] args) {
    DataDictionaryGenerator generator = new DataDictionaryGenerator();
    if (args.length >= 1) {
      File inputFile = new File(args[0]);
      File outputDir;
      if (args.length >= 2) {
        outputDir = new File(args[1]);
      } else {
        outputDir = new File("spec");
      }
      generator.generate(inputFile, outputDir);
    } else {
      generator.usage();
    }
  }
  private final Map<String, CodeSetType> codeSets = new HashMap<>();
  private final Map<Integer, ComponentType> components = new HashMap<>();

  private final Map<Integer, GroupType> groups = new HashMap<>();

  public void generate(File inputFile, File outputDir) {
    try {
      final Repository repository = unmarshal(inputFile);
      final List<CodeSets> codeSetsList = repository.getCodeSets();
      for (CodeSets codeSetsCollection : codeSetsList) {
        final List<CodeSetType> codeSetList = codeSetsCollection.getCodeSet();
        for (CodeSetType codeSet : codeSetList) {
          codeSets.put(codeSet.getName(), codeSet);
        }
      }
      final List<Protocol> protocols = repository.getProtocol();
      for (Protocol protocol : protocols) {
        
        final List<ComponentType> componentList = protocol.getComponents().getComponentOrGroup();
        for (ComponentType component : componentList) {
          if (component instanceof GroupType) {
            groups.put(component.getId().intValue(), (GroupType) component);
          } else {
            components.put(component.getId().intValue(), component);
          }
        }
        String version = protocol.getVersion();
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
            final List<MessageType> messageList = protocol.getMessages().getMessage();
            for (MessageType messageType : messageList) {
              writeMessage(writer, messageType);
            }
            writeElementEnd(writer, "messages", 1);
            writeElement(writer, "components", 1, false);
            for (ComponentType component : componentList) {
              if (component instanceof GroupType) {
                writeGroup(writer, (GroupType) component);
              } else if (protocol.isHasComponents()) {
                writeComponent(writer, component);
              }
            }
            writeElementEnd(writer, "components", 1);
            writeElement(writer, "fields", 1, false);
            final List<FieldType> fieldList = repository.getFields().getField();
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
    } catch (JAXBException | IOException e) {
      e.printStackTrace();
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

  private Repository unmarshal(File inputFile) throws JAXBException {
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
    writeElement(writer, "component", 3, true,
        new KeyValue<String>("name", componentRefType.getName()), new KeyValue<String>("required",
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
  private Writer writeElement(Writer writer, String name, int level, boolean isEmpty) throws IOException {
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
    writeElement(writer, "field", 3, true, new KeyValue<String>("name", fieldRefType.getName()),
        new KeyValue<String>("required",
            fieldRefType.getPresence().equals(PresenceT.REQUIRED) ? "Y" : "N"));
    return writer;
  }
  
  private Writer writeField(Writer writer, FieldType fieldType) throws IOException {
    String type = fieldType.getType();
    CodeSetType codeSet = codeSets.get(type);
    String fixType = codeSet == null ? type : codeSet.getType();
    writeElement(writer, "field", 2, codeSet==null, new KeyValue<Integer>("number", fieldType.getId().intValue()),
        new KeyValue<String>("name", fieldType.getName()),
        new KeyValue<String>("type", fixType.toUpperCase()));
    if (codeSet!=null) {
      for (CodeType code : codeSet.getCode()) {
        writeCode(writer, code);
      }
      writeElementEnd(writer, "field", 2);
    }
    return writer;
  }

  private Writer writeGroup(Writer writer, GroupRefType componentRefType)
      throws IOException {
    writeElement(writer, "component", 3, true,
        new KeyValue<String>("name", componentRefType.getName()), new KeyValue<String>("required",
            componentRefType.getPresence().equals(PresenceT.REQUIRED) ? "Y" : "N"));
    return writer;
  }

  private Writer writeGroup(Writer writer, GroupType groupType) throws IOException {
    writeElement(writer, "component", 2, false,
        new KeyValue<String>("name", groupType.getName()));
    writeElement(writer, "group", 3, false,
        new KeyValue<String>("name", groupType.getNumInGroupName()));
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

    List<Object> members = messageType.getStructure().getComponentOrComponentRefOrGroup();
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
