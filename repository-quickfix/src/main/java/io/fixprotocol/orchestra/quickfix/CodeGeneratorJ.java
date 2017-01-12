package io.fixprotocol.orchestra.quickfix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;

/**
 * Generates message classes for QuickFIX/J from a FIX Orchestra file
 * <p>
 * Unlike the QuickFIX/J code generator, this utility works directly from a FIX Orchestra or FIX
 * Repository 2016 Edition file rather than from a QuickFIX data dictionary file.
 * <p>
 * For now, message validation in QuickFIX/J still requires a data dictionary file, but in future
 * versions, validations may be delegated to additional generated code that takes advantage to
 * conditional logic supported by Orchestra. For example, a validator may invoke an evaluation of an
 * expression for a conditionally required field.
 * 
 * @author Don Mendelson
 *
 */
public class CodeGeneratorJ {

  private static final List<String> DATE_TYPES =
      Arrays.asList("UTCTimestamp", "UTCTimeOnly", "UTCDateOnly", "LocalMktDate", "LocalMktTime");

  private static final String FIELD_PACKAGE = "quickfix.field";

  private static final long SERIALIZATION_VERSION = 552892318L;

  private static final int SPACES_PER_LEVEL = 2;

  /**
   * Runs a CodeGeneratorJ with command line arguments
   * 
   * @param args command line arguments. The first argument is the name of a FIX Orchestra file. An
   *        optional second argument is the target directory for generated code. It defaults to
   *        "target/generated-sources".
   */
  public static void main(String[] args) {
    CodeGeneratorJ generator = new CodeGeneratorJ();
    if (args.length >= 1) {
      File inputFile = new File(args[0]);
      File outputDir;
      if (args.length >= 2) {
        outputDir = new File(args[1]);
      } else {
        outputDir = new File("target/generated-sources");
      }
      generator.generate(inputFile, outputDir);
    } else {
      generator.usage();
    }
  }

  private final Map<String, CodeSetType> codeSets = new HashMap<>();
  private final Map<Integer, ComponentType> components = new HashMap<>();
  private final Map<Integer, FieldType> fields = new HashMap<>();
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

      final List<FieldType> fieldList = repository.getFields().getField();
      final File fileDir = getPackagePath(outputDir, FIELD_PACKAGE);
      fileDir.mkdirs();
      for (FieldType fieldType : fieldList) {
        fields.put(fieldType.getId().intValue(), fieldType);
        generateField(outputDir, fieldType, FIELD_PACKAGE);
      }
      final List<Protocol> protocols = repository.getProtocol();
      for (Protocol protocol : protocols) {
        String version = protocol.getVersion();
        // Split off EP portion of version
        String[] parts = version.split("_");
        if (parts.length > 0) {
          version = parts[0];
        }
        String versionPath = version.replaceAll("[\\.]", "").toLowerCase();
        final String componentPackage = getPackage("quickfix", versionPath, "component");
        final File componentDir = getPackagePath(outputDir, componentPackage);
        componentDir.mkdirs();
        final List<ComponentType> componentList = protocol.getComponents().getComponentOrGroup();
        for (ComponentType component : componentList) {
          if (component instanceof GroupType) {
            groups.put(component.getId().intValue(), (GroupType) component);
          } else {
            components.put(component.getId().intValue(), component);
          }
        }
        for (ComponentType component : componentList) {
          if (component instanceof GroupType) {
            generateGroup(outputDir, (GroupType) component, componentPackage);
          } else if (protocol.isHasComponents()) {
            generateComponent(outputDir, component, componentPackage);
          }
        }
        final String messagePackage = getPackage("quickfix", versionPath);
        final File messageDir = getPackagePath(outputDir, messagePackage);
        messageDir.mkdirs();
        final List<MessageType> messageList = protocol.getMessages().getMessage();
        for (MessageType message : messageList) {
          generateMessage(outputDir, message, messagePackage, componentPackage);
        }
        generateMessageBaseClass(outputDir, version, messagePackage);
        generateMessageFactory(outputDir, messagePackage, messageList);
        generateMessageCracker(outputDir, messagePackage, messageList);
      }
    } catch (JAXBException | IOException e) {
      e.printStackTrace();
    }
  }

  private void generateComponent(File outputDir, ComponentType componentType, String packageName)
      throws IOException {
    String name = componentType.getName();
    File file = getClassFilePath(outputDir, packageName, name);
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, packageName);
      writeImport(writer, "quickfix.FieldNotFound");
      writeImport(writer, "quickfix.Group");

      writeClassDeclaration(writer, name, "quickfix.MessageComponent");
      writeSerializationVersion(writer, SERIALIZATION_VERSION);
      writeMsgType(writer, "");

      List<Integer> componentFields = new ArrayList<>();
      List<Object> members = componentType.getComponentRefOrGroupRefOrFieldRef();
      componentFields.addAll(members.stream().filter(member -> member instanceof FieldRefType)
          .map(member -> ((FieldRefType) member).getId().intValue()).collect(Collectors.toList()));
      writeComponentFieldIds(writer, componentFields);

      List<Integer> componentGroupFields = new ArrayList<>();
      writeGroupFieldIds(writer, componentGroupFields);
      writeComponentNoArgConstructor(writer, name);

      writeMemberAccessors(writer, members, packageName, packageName);

      writeEndClassDeclaration(writer);
    }
  }

  private void generateField(File outputDir, FieldType fieldType, String packageName)
      throws IOException {
    String name = fieldType.getName();
    File file = getClassFilePath(outputDir, packageName, name);
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, packageName);
      String type = fieldType.getType();
      CodeSetType codeSet = codeSets.get(type);
      String fixType = codeSet == null ? type : codeSet.getType();

      if (DATE_TYPES.contains(fixType)) {
        writeImport(writer, "java.util.Date");
      }
      String baseClassname = getFieldBaseClass(fixType);
      if (baseClassname.equals("DecimalField")) {
        writeImport(writer, "java.math.BigDecimal");
      }
      String qualifiedBaseClassname = getQualifiedClassName("quickfix", baseClassname);
      writeImport(writer, qualifiedBaseClassname);
      writeClassDeclaration(writer, name, baseClassname);
      writeSerializationVersion(writer, SERIALIZATION_VERSION);
      int fieldId = fieldType.getId().intValue();
      writeFieldId(writer, fieldId);
      if (codeSet != null) {
        writeValues(writer, codeSet);
      }
      writeFieldNoArgConstructor(writer, name, fieldId);
      writeFieldArgConstructor(writer, name, fieldId, baseClassname);
      writeEndClassDeclaration(writer);
    }
  }

  private void generateGroup(File outputDir, GroupType groupType, String packageName)
      throws IOException {
    String name = groupType.getName();
    File file = getClassFilePath(outputDir, packageName, name);
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, packageName);
      writeImport(writer, "quickfix.FieldNotFound");
      writeImport(writer, "quickfix.Group");

      writeClassDeclaration(writer, name, "quickfix.MessageComponent");
      writeSerializationVersion(writer, SERIALIZATION_VERSION);
      writeMsgType(writer, "");

      List<Integer> componentFields = Collections.emptyList();
      writeComponentFieldIds(writer, componentFields);

      int numInGroupId = groupType.getNumInGroupId().intValue();
      List<Integer> componentGroupFields = new ArrayList<>();
      componentGroupFields.add(numInGroupId);
      writeGroupFieldIds(writer, componentGroupFields);

      writeComponentNoArgConstructor(writer, name);

      FieldType numInGroupField = fields.get(numInGroupId);
      String numInGroupFieldName = numInGroupField.getName();
      writeFieldAccessors(writer, numInGroupFieldName, numInGroupId);
      writeGroupInnerClass(writer, groupType, packageName, packageName);

      List<Object> members = groupType.getComponentRefOrGroupRefOrFieldRef();
      writeMemberAccessors(writer, members, packageName, packageName);

      writeEndClassDeclaration(writer);
    }
  }

  private void generateMessage(File outputDir, MessageType messageType, String messagePackage,
      String componentPackage) throws IOException {
    String messageClassname = messageType.getName();
    String context = messageType.getContext();
    if (!context.equals("base")) {
      messageClassname = messageClassname + context;
    }
    File file = getClassFilePath(outputDir, messagePackage, messageClassname);
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, messagePackage);
      writeImport(writer, "quickfix.FieldNotFound");
      writeImport(writer, "quickfix.field.*");
      writeImport(writer, "quickfix.Group");

      writeClassDeclaration(writer, messageClassname, "Message");
      writeSerializationVersion(writer, SERIALIZATION_VERSION);
      writeMsgType(writer, messageType.getMsgType());

      List<Object> members = messageType.getStructure().getComponentOrComponentRefOrGroup();
      writeMessageNoArgConstructor(writer, messageClassname);

      writeMemberAccessors(writer, members, messagePackage, componentPackage);

      writeEndClassDeclaration(writer);
    }
  }

  private void generateMessageBaseClass(File outputDir, String version, String messagePackage)
      throws IOException {
    File file = getClassFilePath(outputDir, messagePackage, "Message");
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, messagePackage);
      writeImport(writer, "quickfix.field.*");
      writeClassDeclaration(writer, "Message", "quickfix.Message");
      writeSerializationVersion(writer, SERIALIZATION_VERSION);
      writeMessageNoArgBaseConstructor(writer, "Message");
      writeProtectedMessageBaseConstructor(writer, "Message", getBeginString(version));
      writeMessageDerivedHeaderClass(writer);

      writeEndClassDeclaration(writer);
    }
  }

  private void generateMessageCracker(File outputDir, String messagePackage,
      List<MessageType> messageList) throws IOException {
    File file = getClassFilePath(outputDir, messagePackage, "MessageCracker");
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, messagePackage);
      writeImport(writer, "quickfix.*");
      writeImport(writer, "quickfix.field.*");
      writeClassDeclaration(writer, "MessageCracker");
      
      writer.write(String.format("%n%spublic void onMessage(quickfix.Message message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {%n", indent(1)));
      writer.write(String.format("%sthrow new UnsupportedMessageType();%n", indent(2)));
      writer.write(String.format("%s}%n", indent(1)));
      
      for (MessageType messageType : messageList) {
        String name = messageType.getName();
        String context = messageType.getContext();
        if (!context.equals("base")) {
          continue;
        }

        writer.write(String.format("%s/**%n", indent(1)));
        writer.write(String.format("%s * Callback for %s message.%n", indent(1), name));
        writer.write(String.format("%s * @param message%n", indent(1)));
        writer.write(String.format("%s * @param sessionID%n", indent(1)));
        writer.write(String.format("%s * @throws FieldNotFound%n", indent(1)));
        writer.write(String.format("%s * @throws UnsupportedMessageType%n", indent(1)));
        writer.write(String.format("%s * @throws IncorrectTagValue%n", indent(1)));
        writer.write(String.format("%s */%n", indent(1)));
        writer.write(String.format(
            "%n%spublic void onMessage(%s message, SessionID sessionID) throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {%n",
            indent(1), name));
        writer.write(String.format("%sthrow new UnsupportedMessageType();%n", indent(2)));
        writer.write(String.format("%s}%n", indent(1)));
      }

      String crackMethodName = "crack" + messagePackage.split("\\.")[1];
      writer.write(String.format(
          "%n%spublic void crack(quickfix.Message message, SessionID sessionID)%n", indent(1)));
      writer.write(String.format(
          "%sthrows UnsupportedMessageType, FieldNotFound, IncorrectTagValue {%n", indent(2)));
      writer.write(
          String.format("%s%s((Message) message, sessionID);%n", indent(2), crackMethodName));
      writer.write(String.format("%s}%n", indent(1)));

      writer.write(String.format("%n%spublic void %s(Message message, SessionID sessionID)%n",
          indent(1), crackMethodName));
      writer.write(String.format(
          "%sthrows UnsupportedMessageType, FieldNotFound, IncorrectTagValue {%n", indent(2)));
      writer.write(String.format("%sString type = message.getHeader().getString(MsgType.FIELD);%n",
          indent(2)));

      writer.write(String.format("%sswitch (type) {%n", indent(2)));
      for (MessageType messageType : messageList) {
        String name = messageType.getName();
        String context = messageType.getContext();
        if (!context.equals("base")) {
          continue;
        }
        writer.write(String.format("%scase %s.MSGTYPE:%n", indent(2), name));
        writer.write(String.format("%sonMessage((%s)message, sessionID);%n%sbreak;%n", indent(3),
            name, indent(3)));
      }

      writer.write(String.format("%sdefault:%n%sonMessage(message, sessionID);%n%s}%n%s}%n", indent(2),
          indent(3), indent(2), indent(1)));
      writeEndClassDeclaration(writer);
    }
  }

  private void generateMessageFactory(File outputDir, String messagePackage,
      List<MessageType> messageList) throws IOException {
    File file = getClassFilePath(outputDir, messagePackage, "MessageFactory");
    try (FileWriter writer = new FileWriter(file)) {
      writeFileHeader(writer);
      writePackage(writer, messagePackage);
      writeImport(writer, "quickfix.Message");
      writeImport(writer, "quickfix.Group");
      writer.write(String.format("%npublic class %s implements %s {%n", "MessageFactory", "quickfix.MessageFactory"));
      writeMessageCreateMethod(writer, messageList, messagePackage);
      writeGroupCreateMethod(writer, messageList, messagePackage);
      writeEndClassDeclaration(writer);
    }
  }

  private String getBeginString(String version) {
    if (version.startsWith("FIX.5")) {
      return "FIXT.1.1";
    } else {
      return version;
    }
  }

  private File getClassFilePath(File outputDir, String packageName, String className) {
    StringBuilder sb = new StringBuilder();
    sb.append(packageName.replace('.', File.separatorChar));
    sb.append(File.separatorChar);
    sb.append(className);
    sb.append(".java");
    return new File(outputDir, sb.toString());
  }

  private String getFieldBaseClass(String type) {
    String baseType;
    switch (type) {
      case "char":
        baseType = "CharField";
        break;
      case "Price":
      case "Amt":
      case "Qty":
      case "PriceOffset":
        baseType = "DecimalField";
        break;
      case "int":
      case "NumInGroup":
      case "SeqNum":
      case "Length":
      case "TagNum":
      case "DayOfMonth":
        baseType = "IntField";
        break;
      case "UTCTimestamp":
        baseType = "UtcTimeStampField";
        break;
      case "UTCTimeOnly":
      case "LocalMktTime":
        baseType = "UtcTimeOnlyField";
        break;
      case "UTCDateOnly":
      case "LocalMktDate":
        baseType = "UtcDateOnlyField";
        break;
      case "Boolean":
        baseType = "BooleanField";
        break;
      case "float":
      case "Percentage":
        baseType = "DoubleField";
        break;
      default:
        baseType = "StringField";
    }
    return baseType;
  }

  private void getGroupFields(ComponentType group, List<Integer> groupComponentFields) {
    List<Object> members = group.getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        groupComponentFields.add(((FieldRefType) member).getId().intValue());
      } else if (member instanceof GroupRefType) {
        int id = ((GroupRefType) member).getId().intValue();
        GroupType groupType = groups.get(id);
        if (groupType != null) {
          groupComponentFields.add(groupType.getNumInGroupId().intValue());
        } else {
          System.err.format("Group missing from repository; id=%d%n", id);
        }
      } else if (member instanceof ComponentRefType) {
        ComponentType componentType =
            components.get(((ComponentRefType) member).getId().intValue());
        getGroupFields(componentType, groupComponentFields);
      }
    }
  }

  private String getPackage(String... parts) {
    return String.join(".", parts);
  }

  private File getPackagePath(File outputDir, String packageName) {
    StringBuilder sb = new StringBuilder();
    sb.append(packageName.replace('.', File.separatorChar));
    return new File(outputDir, sb.toString());
  }

  private String getQualifiedClassName(String packageName, String className) {
    return String.format("%s.%s", packageName, className);
  }

  private String indent(int level) {
    char[] chars = new char[level * SPACES_PER_LEVEL];
    Arrays.fill(chars, ' ');
    return new String(chars);
  }

  private Repository unmarshal(File inputFile) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(inputFile);
  }

  private void usage() {
    System.out.format("Usage: java %s <input-file> <output-dir>", this.getClass().getName());
  }

  private Writer writeClassDeclaration(Writer writer, String name) throws IOException {
    writer.write(String.format("%npublic class %s {%n", name));
    return writer;
  }

  private Writer writeClassDeclaration(Writer writer, String name, String baseClassname)
      throws IOException {
    writer.write(String.format("%npublic class %s extends %s {%n", name, baseClassname));
    return writer;
  }

  private Writer writeComponentAccessors(Writer writer, ComponentRefType member, String packageName)
      throws IOException {
    String className = getQualifiedClassName(packageName, member.getName());
    writer.write(
        String.format("%n%spublic void set(%s component) {%n%ssetComponent(component);%n%s}%n",
            indent(1), className, indent(2), indent(1)));
    writer.write(String.format(
        "%n%spublic %s get(%s component) throws FieldNotFound {%n%sgetComponent(component);%n%sreturn component;%n%s}%n",
        indent(1), className, className, indent(2), indent(2), indent(1)));
    writer.write(String.format(
        "%n%spublic %s get%s() throws FieldNotFound {%n%sreturn get(new %s());%n%s}%n", indent(1),
        className, member.getName(), indent(2), className, indent(1)));
    return writer;
  }

  private Writer writeComponentFieldIds(Writer writer, List<Integer> componentFields)
      throws IOException {
    writer.write(String.format("%Sprivate int[] componentFields = {", indent(1)));
    for (Integer fieldId : componentFields) {
      writer.write(String.format("%d, ", fieldId));
    }
    writer.write(String.format("};%n"));
    writer.write(
        String.format("%sprotected int[] getFields() { return componentFields; }%n", indent(1)));
    return writer;
  }

  private Writer writeComponentNoArgConstructor(Writer writer, String className)
      throws IOException {
    writer.write(String.format("%n%spublic %s() {%n%ssuper();%n%s}%n", indent(1), className,
        indent(2), indent(1)));
    return writer;
  }

  private Writer writeEndClassDeclaration(Writer writer) throws IOException {
    writer.write("}\n");
    return writer;
  }

  private Writer writeFieldAccessors(Writer writer, String name, int id) throws IOException {
    String qualifiedClassName = getQualifiedClassName(FIELD_PACKAGE, name);

    writer.write(String.format("%n%spublic void set(%s value) {%n%ssetField(value);%n%s}%n",
        indent(1), qualifiedClassName, indent(2), indent(1)));
    writer.write(String.format(
        "%n%spublic %s get(%s value) throws FieldNotFound {%n%sgetField(value);%n%sreturn value;%n%s}%n",
        indent(1), qualifiedClassName, qualifiedClassName, indent(2), indent(2), indent(1)));
    writer.write(String.format(
        "%n%spublic %s get%s() throws FieldNotFound {%n%sreturn get(new %s());%n%s}%n", indent(1),
        qualifiedClassName, name, indent(2), qualifiedClassName, indent(1)));
    writer.write(
        String.format("%n%spublic boolean isSet(%s field) {%n%sreturn isSetField(field);%n%s}%n",
            indent(1), qualifiedClassName, indent(2), indent(1)));
    writer.write(String.format("%n%spublic boolean isSet%s() {%n%sreturn isSetField(%d);%n%s}%n",
        indent(1), name, indent(2), id, indent(1)));
    return writer;
  }

  private Writer writeFieldArgConstructor(Writer writer, String className, int fieldId,
      String baseClassname) throws IOException {
    switch (baseClassname) {
      case "BooleanField":
        writer.write(String.format("%n%spublic %s(Boolean data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        writer.write(String.format("%n%spublic %s(boolean data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "BytesField":
        writer.write(String.format("%n%spublic %s(byte[] data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "CharField":
        writer.write(String.format("%n%spublic %s(Character data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        writer.write(String.format("%n%spublic %s(char data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "DateField":
      case "UtcDateOnlyField":
        writer.write(String.format("%n%spublic %s(Date data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "UtcTimeOnlyField":
      case "UtcTimeStampField":
        writer.write(String.format("%n%spublic %s(Date data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "DecimalField":
        writer.write(String.format("%n%spublic %s(BigDecimal data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        writer.write(String.format(
            "%n%spublic %s(double data) {%n%ssuper(%d, BigDecimal.valueOf(data));%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      case "DoubleField":
        writer.write(String.format("%n%spublic %s(Double data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(2), indent(1)));
        writer.write(String.format("%n%spublic %s(double data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(2), indent(1)));
        break;
      case "IntField":
        writer.write(String.format("%n%spublic %s(Integer data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        writer.write(String.format("%n%spublic %s(int data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
        break;
      default:
        writer.write(String.format("%n%spublic %s(String data) {%n%ssuper(%d, data);%n%s}%n",
            indent(1), className, indent(2), fieldId, indent(1)));
    }
    return writer;
  }

  private Writer writeFieldId(Writer writer, int fieldId) throws IOException {
    writer.write(String.format("%n%spublic static final int FIELD = %d;%n", indent(1), fieldId));
    return writer;
  }

  private Writer writeFieldNoArgConstructor(Writer writer, String className, int fieldId)
      throws IOException {
    writer.write(String.format("%n%spublic %s() {%n%ssuper(%d);%n%s}%n", indent(1), className,
        indent(2), fieldId, indent(1)));
    return writer;
  }

  private Writer writeFileHeader(Writer writer) throws IOException {
    writer.write("/* Generated Java Source File */\n");
    return writer;
  }

  private void writeGroupCreateCase(Writer writer, String parentQualifiedName, GroupType groupType)
      throws IOException {
    String numInGroupFieldClassname =
        getQualifiedClassName(FIELD_PACKAGE, groupType.getNumInGroupName());
    writer.write(String.format("%scase %s.FIELD:%n", indent(3), numInGroupFieldClassname));
    writer.write(String.format("%sreturn new %s.%s();%n", indent(4), parentQualifiedName,
        groupType.getNumInGroupName()));
    List<Object> members = groupType.getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof GroupRefType) {
        int id = ((GroupRefType) member).getId().intValue();
        GroupType nestedGroupType = groups.get(id);
        if (groupType != null) {
          writeGroupCreateCase(writer,
              String.format("%s.%s", parentQualifiedName, groupType.getNumInGroupName()),
              nestedGroupType);
        } else {
          System.err.format("Group missing from repository; id=%d%n", id);
        }

      }
    }
  }

  private Writer writeGroupCreateMethod(Writer writer, List<MessageType> messageList,
      String messagePackage) throws IOException {
    writer.write(String.format(
        "%n%spublic Group create(String beginString, String msgType, int correspondingFieldID) {%n",
        indent(1)));
    writer.write(String.format("%sswitch (msgType) {%n", indent(2)));
    for (MessageType messageType : messageList) {
      String messageName = messageType.getName();
      writer
          .write(String.format("%scase %s.%s.MSGTYPE:%n", indent(1), messagePackage, messageName));
      writer.write(String.format("%sswitch (correspondingFieldID) {%n", indent(2)));

      List<Object> members = messageType.getStructure().getComponentOrComponentRefOrGroup();
      for (Object member : members) {
        if (member instanceof GroupRefType) {
          int id = ((GroupRefType) member).getId().intValue();
          GroupType groupType = groups.get(id);
          if (groupType != null) {
            String parentQualifiedName = getQualifiedClassName(messagePackage, messageName);
            writeGroupCreateCase(writer, parentQualifiedName, groupType);
          } else {
            System.err.format("Group missing from repository; id=%d%n", id);
          }

        }
      }
      writer.write(String.format("%s}%n%sbreak;%n", indent(2), indent(2)));
    }

    writer.write(String.format("%s}%n%sreturn null;%n%s}%n", indent(2), indent(2), indent(1)));

    return writer;
  }

  private Writer writeGroupFieldIds(Writer writer, List<Integer> componentFields)
      throws IOException {
    writer.write(String.format("%Sprivate int[] componentGroups = {", indent(1)));
    for (Integer fieldId : componentFields) {
      writer.write(String.format("%d, ", fieldId));
    }
    writer.write(String.format("};%n"));
    writer.write(String.format("%sprotected int[] getGroupFields() { return componentGroups; }%n",
        indent(1)));
    return writer;
  }

  private void writeGroupInnerClass(FileWriter writer, GroupType groupType, String packageName,
      String componentPackage) throws IOException {
    int numInGroupId = groupType.getNumInGroupId().intValue();
    String numInGroupFieldName = groupType.getNumInGroupName();
    writeStaticClassDeclaration(writer, numInGroupFieldName, "Group");
    writeSerializationVersion(writer, SERIALIZATION_VERSION);

    List<Integer> groupComponentFields = new ArrayList<>();
    getGroupFields(groupType, groupComponentFields);
    writeOrderFieldIds(writer, groupComponentFields);

    Integer firstFieldId = groupComponentFields.get(0);
    writeGroupNoArgConstructor(writer, numInGroupFieldName, numInGroupId, firstFieldId);

    List<Object> members = groupType.getComponentRefOrGroupRefOrFieldRef();
    writeMemberAccessors(writer, members, packageName, componentPackage);

    writeEndClassDeclaration(writer);
  }

  private Writer writeGroupNoArgConstructor(Writer writer, String className, int numInGrpId,
      int firstFieldId) throws IOException {
    writer.write(String.format("%n%spublic %s() {%n%ssuper(%d, %d, ORDER);%n%s}%n", indent(1),
        className, indent(2), numInGrpId, firstFieldId, indent(1)));
    return writer;
  }

  private Writer writeImport(Writer writer, String className) throws IOException {
    writer.write("import ");
    writer.write(className);
    writer.write(";\n");
    return writer;
  }

  private void writeMemberAccessors(FileWriter writer, List<Object> members, String packageName,
      String componentPackage) throws IOException {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        writeFieldAccessors(writer, fieldRefType.getName(), fieldRefType.getId().intValue());
      } else if (member instanceof GroupRefType) {
        writeComponentAccessors(writer, (ComponentRefType) member, componentPackage);
        int id = ((GroupRefType) member).getId().intValue();
        GroupType groupType = groups.get(id);
        if (groupType != null) {
          int numInGroupId = groupType.getNumInGroupId().intValue();
          String numInGroupName = groupType.getNumInGroupName();
          writeFieldAccessors(writer, numInGroupName, numInGroupId);
          writeGroupInnerClass(writer, groupType, packageName, componentPackage);
        } else {
          System.err.format("Group missing from repository; id=%d%n", id);
        }

      } else if (member instanceof ComponentRefType) {
        writeComponentAccessors(writer, (ComponentRefType) member, componentPackage);
      }
    }
  }

  // In this method, only create messages with base context
  private Writer writeMessageCreateMethod(Writer writer, List<MessageType> messageList,
      String packageName) throws IOException {
    writer.write(String.format("%n%spublic Message create(String beginString, String msgType) {%n",
        indent(1)));
    writer.write(String.format("%sswitch (msgType) {%n", indent(2)));
    for (MessageType messageType : messageList) {
      String name = messageType.getName();
      String context = messageType.getContext();
      if (!context.equals("base")) {
        continue;
      }
      writer.write(String.format("%scase %s.%s.MSGTYPE:%n", indent(2), packageName, name));
      writer.write(String.format("%sreturn new %s();%n", indent(3),
          getQualifiedClassName(packageName, name)));
    }

    writer.write(String.format("%s}%n%sreturn new quickfix.fix50sp2.Message();%n%s}%n", indent(2),
        indent(2), indent(1)));

    return writer;
  }

  private Writer writeMessageDerivedHeaderClass(Writer writer) throws IOException {
    writeStaticClassDeclaration(writer, "Header", "quickfix.Message.Header");
    writeSerializationVersion(writer, SERIALIZATION_VERSION);
    writer.write(String.format("%n%spublic Header(Message msg) {%n%n%s}%n", indent(1), indent(1)));
    writeEndClassDeclaration(writer);
    return writer;
  }

  private Writer writeMessageNoArgBaseConstructor(Writer writer, String className)
      throws IOException {
    writer.write(String.format("%n%spublic %s() {%n%sthis(null);%n%s}%n", indent(1), className,
        indent(2), indent(1)));
    return writer;
  }

  private Writer writeMessageNoArgConstructor(Writer writer, String className) throws IOException {
    writer.write(String.format(
        "%n%spublic %s() {%n%ssuper();%n%sgetHeader().setField(new quickfix.field.MsgType(MSGTYPE));%n%s}%n",
        indent(1), className, indent(2), indent(2), indent(1)));
    return writer;
  }

  private Writer writeMsgType(Writer writer, String msgType) throws IOException {
    writer.write(
        String.format("%n%spublic static final String MSGTYPE = \"%s\";%n", indent(1), msgType));
    return writer;
  }

  private Writer writeOrderFieldIds(Writer writer, List<Integer> componentFields)
      throws IOException {
    writer.write(String.format("%Sprivate static final int[]  ORDER = {", indent(1)));
    for (Integer fieldId : componentFields) {
      writer.write(String.format("%d, ", fieldId));
    }
    writer.write(String.format("0};%n"));
    return writer;
  }

  private Writer writePackage(Writer writer, String packageName) throws IOException {
    writer.write("package ");
    writer.write(packageName);
    writer.write(";\n");
    return writer;
  }

  private Writer writeProtectedMessageBaseConstructor(Writer writer, String className,
      String beginString) throws IOException {
    writer.write(String.format(
        "%sprotected %s(int[] fieldOrder) {%n%ssuper(fieldOrder);%n%sheader = new Header(this);%n%strailer = new Trailer();%n%sgetHeader().setField(new BeginString(\"%s\"));%n%s}%n",
        indent(1), className, indent(2), indent(2), indent(2), indent(2), beginString, indent(1)));
    return writer;
  }

  private Writer writeSerializationVersion(Writer writer, long serializationVersion)
      throws IOException {
    writer.write(String.format("%sstatic final long serialVersionUID = %dL;%n", indent(1),
        serializationVersion));
    return writer;
  }

  private Writer writeStaticClassDeclaration(Writer writer, String name, String baseClassname)
      throws IOException {
    writer.write(String.format("%npublic static class %s extends %s {%n", name, baseClassname));
    return writer;
  }

  private Writer writeValues(Writer writer, CodeSetType codeSet) throws IOException {
    String type = codeSet.getType();
    for (CodeType code : codeSet.getCode()) {
      switch (type) {
        case "Boolean":
          writer.write(String.format("%n%spublic static final boolean %s = %s;%n", indent(1),
              code.getName(), code.getValue().equals("Y")));
          break;
        case "char":
          writer.write(String.format("%n%spublic static final char %s = \'%s\';%n", indent(1),
              code.getName(), code.getValue()));
          break;
        case "int":
          writer.write(String.format("%n%spublic static final int %s = %s;%n", indent(1),
              code.getName(), code.getValue()));
          break;
        default:
          writer.write(String.format("%n%spublic static final String %s = \"%s\";%n", indent(1),
              code.getName(), code.getValue()));
      }

    }
    return writer;
  }


}
