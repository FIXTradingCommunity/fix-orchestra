package io.fixprotocol.orchestra.transformers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.purl.dc.elements._1.ObjectFactory;
import org.purl.dc.elements._1.SimpleLiteral;
import org.purl.dc.terms.ElementOrRefinementContainer;
import io.fixprotocol._2016.fixrepository.Actors;
import io.fixprotocol._2016.fixrepository.Categories;
import io.fixprotocol._2016.fixrepository.CategoryType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Components;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.Fields;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.Groups;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Messages;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.Sections;


/**
 * Selectively compresses an Orchestra file <br>
 * Copies selected elements to a new file.
 * Only selected messages are retained with one of the following filters
 * <ul>
 * <li>Messages that have a flow</li>
 * <li>Messages that have a specified category</li>
 * <li>Messages that <em>not</em> in a specified category</li>
 * </ul>
 * <ul>
 * <li>Field and components are copied only if they are contained by the selected messages.</li>
 * <li>Abbreviations, categories, sections, actors, datatypes, and metadata are copied as-is (not
 * compressed).</li>
 * <li>Attributes of copied elements are unchanged.</li>
 * </ul>
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryCompressor {
  
  static class HasFlow implements Predicate<MessageType> {

    private final String flow;

    public HasFlow(String flow) {
      this.flow = flow;
    }

    @Override
    public boolean test(MessageType m) {
      return flow.equals(m.getFlow());
    }
  }

  static class HasCategory implements Predicate<MessageType> {

    private final String category;

    public HasCategory(String category) {
      this.category = category;
    }

    @Override
    public boolean test(MessageType m) {
      return category.equals(m.getCategory());
    }
  }

  static class NotCategory implements Predicate<MessageType> {

    private final String category;

    public NotCategory(String category) {
      this.category = category;
    }

    @Override
    public boolean test(MessageType m) {
      return !category.equals(m.getCategory());
    }
  }
 
  static class HasSection implements Predicate<MessageType> {

    private final String section;
    private final BiPredicate<String, String> testCategory;

    public HasSection(String section, BiPredicate<String,String> testCategory) {
      this.section = section;
      this.testCategory = testCategory;
    }

    @Override
    public boolean test(MessageType m) {
      return this.testCategory.test(m.getCategory(), this.section);
    }
  }
  
  static class NotSection implements Predicate<MessageType> {

    private final String section;
    private final BiPredicate<String, String> testCategory;

    public NotSection(String section, BiPredicate<String,String> testCategory) {
      this.section = section;
      this.testCategory = testCategory;
    }

    @Override
    public boolean test(MessageType m) {
      return !this.testCategory.test(m.getCategory(), this.section);
    }
  }
  
  public static void main(String[] args) throws IOException, JAXBException  {
    RepositoryCompressor compressor = new RepositoryCompressor();
    compressor.parseArgs(args);
  }

  private void parseArgs( String[] args) throws IOException, JAXBException {
    Options options = new Options();  
    options.addOption(Option.builder("i").desc("path of input file").longOpt("input").numberOfArgs(1).required().build());
    options.addOption(Option.builder("o").desc("path of output file").longOpt("output").numberOfArgs(1).required().build());
    options.addOption(Option.builder("c").desc("select messages by category").longOpt("category").numberOfArgs(1).build());
    options.addOption(Option.builder("s").desc("select messages by section").longOpt("section").numberOfArgs(1).build());
    options.addOption(Option.builder("f").desc("select messages by flow").longOpt("flow").numberOfArgs(1).build());
    options.addOption(Option.builder().desc("select messages except category").longOpt("notcategory").numberOfArgs(1).build());
    options.addOption(Option.builder().desc("select messages except section").longOpt("notsection").numberOfArgs(1).build());
    options.addOption(Option.builder("?").desc("display usage").longOpt("help").numberOfArgs(1).build());
    
    DefaultParser parser = new DefaultParser();
    CommandLine cmd;
    String inputFile;
    String outputFile;     
    Predicate<MessageType> messagePredicate = null;
    
    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("?")) {
        usage(options);
        System.exit(0);
      }
      inputFile = cmd.getOptionValue("i");
      outputFile = cmd.getOptionValue("o");

      if (cmd.hasOption("c")) {
        String category = cmd.getOptionValue("c");
        if (messagePredicate == null) {
          messagePredicate = new HasCategory(category);
        } else {
          messagePredicate = messagePredicate.and(new HasCategory(category));
        }
      }
      
      if (cmd.hasOption("notcategory")) {
        String category = cmd.getOptionValue("notcategory");
        if (messagePredicate == null) {
          messagePredicate = new NotCategory(category);
        } else {
          messagePredicate = messagePredicate.and(new NotCategory(category));
        }
      }
      
      if (cmd.hasOption("s")) {
        String section = cmd.getOptionValue("s");
        if (messagePredicate == null) {
          messagePredicate = new HasSection(section, isCategoryInSection);
        } else {
          messagePredicate = messagePredicate.and(new HasSection(section, isCategoryInSection));
        }
      }
      
      if (cmd.hasOption("notsection")) {
        String section = cmd.getOptionValue("notsection");
        if (messagePredicate == null) {
          messagePredicate = new NotSection(section, isCategoryInSection);
        } else {
          messagePredicate = messagePredicate.and(new NotSection(section, isCategoryInSection));
        }
      }
      
      if (cmd.hasOption("f")) {
        String flow = cmd.getOptionValue("f");
        if (messagePredicate == null) {
          messagePredicate = new HasFlow(flow);
        } else {
          messagePredicate = messagePredicate.and(new HasFlow(flow));
        }
      }
      
      if (messagePredicate == null) {
        System.err.println("Must select one or more selection criteria: category / section / flow");
        usage(options);
        System.exit(1);
      }

      compress(inputFile, outputFile, messagePredicate);

    } catch (ParseException e) {
      System.err.println(e.getMessage());
      usage(options);
      System.exit(1);
    }   
  }

  private void usage(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("RepositoryCompressor", options);
  }

  static Predicate<? super MessageType> hasFlow() {
    return m -> m.getFlow() != null;
  }

  private final List<BigInteger> componentIdList = new ArrayList<>();
  private List<ComponentType> componentList = new ArrayList<>();
  private final List<BigInteger> fieldIdList = new ArrayList<>();
  private final List<BigInteger> groupIdList = new ArrayList<>();
  private List<GroupType> groupList;
  private Repository inRepository;

  public void compress(String inputFile, String outputFile,
      Predicate<? super MessageType> messagePredicate) throws IOException, JAXBException  {
    
    try (InputStream is = new FileInputStream(inputFile);
        OutputStream os = new FileOutputStream(outputFile)) {
      compress(is, os,  messagePredicate);
    }
  }
  
  public void compress(InputStream is, OutputStream os,
      Predicate<? super MessageType> messagePredicate) throws JAXBException  {
    inRepository = unmarshal(is);
    final Repository outRepository = new Repository();
    inRepository.copyTo(null, outRepository, AttributeCopyStrategy.INSTANCE);

    ElementOrRefinementContainer metadata =
        (ElementOrRefinementContainer) inRepository.getMetadata().clone();
    List<JAXBElement<SimpleLiteral>> literals = metadata.getAny();
    ObjectFactory objectFactory = new ObjectFactory();
    SimpleLiteral contributor = new SimpleLiteral();
    contributor.getContent().add("RepositoryCompressor");
    literals.add(objectFactory.createContributor(contributor));
    outRepository.setMetadata(metadata);
    outRepository.setCategories((Categories) inRepository.getCategories().clone());
    outRepository.setSections((Sections) inRepository.getSections().clone());
    outRepository.setDatatypes((Datatypes) inRepository.getDatatypes().clone());
    outRepository.setActors((Actors) inRepository.getActors().clone());
    final Components components = inRepository.getComponents();
    if (components != null) {
      Components inComponents = (Components) components.clone();
      componentList = inComponents.getComponent();
    }
    Groups inGroups = (Groups) inRepository.getGroups().clone();
    groupList = inGroups.getGroup();

    Messages inMessages = (Messages) inRepository.getMessages().clone();
    List<MessageType> messageList = inMessages.getMessage();
    List<MessageType> filteredMessages =
        messageList.stream().filter(messagePredicate).collect(Collectors.toList());
    filteredMessages.forEach(m -> walk(m.getStructure().getComponentRefOrGroupRefOrFieldRef()));

    List<BigInteger> distinctFieldIds =
        fieldIdList.stream().distinct().collect(Collectors.toList());
    Fields inFields = (Fields) inRepository.getFields().clone();
    List<FieldType> fieldsWithFlow = inFields.getField().stream()
        .filter(f -> distinctFieldIds.contains(f.getId())).collect(Collectors.toList());
    Fields outFields = new Fields();
    outFields.getField().addAll(fieldsWithFlow);
    outRepository.setFields(outFields);

    List<String> typeList =
        fieldsWithFlow.stream().map(FieldType::getType).distinct().collect(Collectors.toList());
    CodeSets inCodeSets = (CodeSets) inRepository.getCodeSets().clone();
    List<CodeSetType> codeSetsWithFlow = inCodeSets.getCodeSet().stream()
        .filter(cs -> typeList.contains(cs.getName())).collect(Collectors.toList());
    CodeSets outCodeSets = new CodeSets();
    outCodeSets.getCodeSet().addAll(codeSetsWithFlow);
    outRepository.setCodeSets(outCodeSets);

    List<BigInteger> distinctComponentsIds =
        componentIdList.stream().distinct().collect(Collectors.toList());
    List<ComponentType> componentsWithFlow = componentList.stream()
        .filter(c -> distinctComponentsIds.contains(c.getId())).collect(Collectors.toList());
    Components outComponents = new Components();
    outComponents.getComponent().addAll(componentsWithFlow);
    outRepository.setComponents(outComponents);

    List<BigInteger> distinctGroupIds =
        groupIdList.stream().distinct().collect(Collectors.toList());
    List<GroupType> groupWithFlow = groupList.stream()
        .filter(c -> distinctGroupIds.contains(c.getId())).collect(Collectors.toList());
    Groups outGroups = new Groups();
    outGroups.getGroup().addAll(groupWithFlow);
    outRepository.setGroups(outGroups);

    Messages outMessages = new Messages();
    outMessages.getMessage().addAll(filteredMessages);
    outRepository.setMessages(outMessages);
    marshal(outRepository, os);
  }

  private ComponentType getComponent(BigInteger id) {
    for (ComponentType component : componentList) {
      if (component.getId().equals(id)) {
        return component;
      }
    }
    return null;
  }

  private List<Object> getComponentMembers(BigInteger id) {
    ComponentType component = getComponent(id);
    if (component != null) {
      return component.getComponentRefOrGroupRefOrFieldRef();
    } else {
      return null;
    }
  }



  private GroupType getGroup(BigInteger id) {
    for (GroupType group : groupList) {
      if (group.getId().equals(id)) {
        return group;
      }
    }
    return null;
  }

  private List<Object> getGroupMembers(BigInteger id) {
    GroupType component = getGroup(id);
    if (component != null) {
      return component.getComponentRefOrGroupRefOrFieldRef();
    } else {
      return null;
    }
  }

  private void marshal(Repository jaxbElement, OutputStream os) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.setProperty("jaxb.formatted.output", true);
    jaxbMarshaller.marshal(jaxbElement, os);
  }

  private Repository unmarshal(InputStream is) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }

  private void walk(List<Object> list) {
    for (Object obj : list) {
      if (obj instanceof GroupRefType) {
        GroupRefType groupRef = (GroupRefType) obj;
        GroupType group = getGroup(groupRef.getId());
        if (group == null) {
          System.err.format("Group missing for groupRef; ID=%d%n", groupRef.getId().intValue());
          return;
        }
        fieldIdList.add(group.getNumInGroup().getId());
        groupIdList.add(groupRef.getId());
        // recursion on referenced component
        walk(getGroupMembers(groupRef.getId()));
      } else if (obj instanceof ComponentRefType) {
        ComponentRefType componentRef = (ComponentRefType) obj;
        componentIdList.add(componentRef.getId());
        // recursion on referenced component
        walk(getComponentMembers(componentRef.getId()));
      } else if (obj instanceof FieldRefType) {
        FieldRefType fieldRef = (FieldRefType) obj;
        fieldIdList.add(fieldRef.getId());
      }
    }
  }
  
  private BiPredicate<String,String> isCategoryInSection = (String category, String section) -> {
    List<CategoryType> categories = this.inRepository.getCategories().getCategory();
    for (CategoryType categoryType : categories) {
      if (categoryType.getId().equals(category) && categoryType.getSection().equals(section)) {
        return true;
      }
    }
    return false;
  };


}
