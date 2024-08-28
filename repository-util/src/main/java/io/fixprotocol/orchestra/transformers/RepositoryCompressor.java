/*
 * Copyright 2017-2022 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.transformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.purl.dc.elements._1.ObjectFactory;
import org.purl.dc.elements._1.SimpleLiteral;
import org.purl.dc.terms.ElementOrRefinementContainer;
import io.fixprotocol._2024.orchestra.repository.Actors;
import io.fixprotocol._2024.orchestra.repository.Categories;
import io.fixprotocol._2024.orchestra.repository.CategoryType;
import io.fixprotocol._2024.orchestra.repository.CodeSetType;
import io.fixprotocol._2024.orchestra.repository.CodeSets;
import io.fixprotocol._2024.orchestra.repository.ComponentRefType;
import io.fixprotocol._2024.orchestra.repository.ComponentType;
import io.fixprotocol._2024.orchestra.repository.Components;
import io.fixprotocol._2024.orchestra.repository.Datatypes;
import io.fixprotocol._2024.orchestra.repository.FieldRefType;
import io.fixprotocol._2024.orchestra.repository.FieldType;
import io.fixprotocol._2024.orchestra.repository.Fields;
import io.fixprotocol._2024.orchestra.repository.GroupRefType;
import io.fixprotocol._2024.orchestra.repository.GroupType;
import io.fixprotocol._2024.orchestra.repository.Groups;
import io.fixprotocol._2024.orchestra.repository.MessageType;
import io.fixprotocol._2024.orchestra.repository.Messages;
import io.fixprotocol._2024.orchestra.repository.Repository;
import io.fixprotocol._2024.orchestra.repository.Sections;

/**
 * Selectively compresses an Orchestra file <br>
 * Copies selected elements to a new file. Only selected messages are retained with one of the
 * following filters
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

  public static class Builder {

    private String inputFile;
    private Predicate<MessageType> messagePredicate;
    private String outputFile;
    private String logFile;

    public RepositoryCompressor build() {
      return new RepositoryCompressor(this);
    }

    public Builder eventLog(String logFile) {
      this.logFile = logFile;
      return this;
    }

    Builder inputFile(String inputFile) {
      this.inputFile = inputFile;
      return this;
    }

    Builder messagePredicate(Predicate<MessageType> messagePredicate) {
      this.messagePredicate = messagePredicate;
      return this;
    }

    Builder outputFile(String outputFile) {
      this.outputFile = outputFile;
      return this;
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
  static class HasSection implements Predicate<MessageType> {

    private final String section;
    private final BiPredicate<String, String> testCategory;

    public HasSection(String section, BiPredicate<String, String> testCategory) {
      this.section = section;
      this.testCategory = testCategory;
    }

    @Override
    public boolean test(MessageType m) {
      return this.testCategory.test(m.getCategory(), this.section);
    }
  }

  static class IsCategoryInSection implements BiPredicate<String, String> {

    private List<CategoryType> categories;

    public void setCategories(List<CategoryType> categories) {
      this.categories = categories;
    }

    @Override
    public boolean test(String category, String section) {
      for (final CategoryType categoryType : categories) {
        if (categoryType.getName().equals(category) && categoryType.getSection().equals(section)) {
          return true;
        }
      }
      return false;
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

  static class NotSection implements Predicate<MessageType> {

    private final String section;
    private final BiPredicate<String, String> testCategory;

    public NotSection(String section, BiPredicate<String, String> testCategory) {
      this.section = section;
      this.testCategory = testCategory;
    }

    @Override
    public boolean test(MessageType m) {
      return !this.testCategory.test(m.getCategory(), this.section);
    }
  }

  private static final Logger logger = LogManager.getLogger(RepositoryCompressor.class);
  static final IsCategoryInSection isCategoryInSection = new IsCategoryInSection();

  public static Builder builder() {
    return new Builder();
  }

  /**
   * usage: RepositoryCompressor
   *
   * <pre>
   * -?,--help display usage
   * -c,--category &lt;arg&gt; select messages by category
   * -f,--flow &lt;arg&gt; select messages by flow
   * -i,--input &lt;arg&gt; path of input file
   * -n,--notcategory &lt;arg&gt; select messages except category
   * -o,--output &lt;arg&gt; path of output file
   * -s,--section &lt;arg&gt; select messages by section
   * -x,--notsection &lt;arg&gt; select messages except section
   * </pre>
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    RepositoryCompressor compressor;
    try {
      compressor = RepositoryCompressor.parseArgs(args).build();
      System.exit(compressor.compress() ? 0 : 1);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  public static Builder parseArgs(String[] args) throws ParseException {
    final Options options = new Options();
    options.addOption(Option.builder("i").desc("path of input file").longOpt("input")
        .numberOfArgs(1).required().build());
    options.addOption(Option.builder("o").desc("path of output file").longOpt("output")
        .numberOfArgs(1).required().build());
    options.addOption(Option.builder("c").desc("select messages by category").longOpt("category")
        .numberOfArgs(1).build());
    options.addOption(Option.builder("s").desc("select messages by section").longOpt("section")
        .numberOfArgs(1).build());
    options.addOption(Option.builder("f").desc("select messages by flow").longOpt("flow")
        .numberOfArgs(1).build());
    options.addOption(Option.builder("n").desc("select messages except category")
        .longOpt("notcategory").numberOfArgs(1).build());
    options.addOption(Option.builder("x").desc("select messages except section")
        .longOpt("notsection").numberOfArgs(1).build());
    options.addOption(Option.builder("?").desc("display usage").longOpt("help").build());

    final DefaultParser parser = new DefaultParser();
    CommandLine cmd;

    final Builder builder = new Builder();


    try {
      cmd = parser.parse(options, args);

      if (cmd.hasOption("?")) {
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("RepositoryCompressor", options);
        System.exit(0);
      }

      builder.inputFile = cmd.getOptionValue("i");
      builder.outputFile = cmd.getOptionValue("o");

      if (cmd.hasOption("c")) {
        final String category = cmd.getOptionValue("c");
        builder.messagePredicate = new HasCategory(category);
      }

      if (cmd.hasOption("notcategory")) {
        final String category = cmd.getOptionValue("notcategory");
        if (builder.messagePredicate == null) {
          builder.messagePredicate = new NotCategory(category);
        } else {
          builder.messagePredicate = builder.messagePredicate.and(new NotCategory(category));
        }
      }

      if (cmd.hasOption("s")) {
        final String section = cmd.getOptionValue("s");
        if (builder.messagePredicate == null) {
          builder.messagePredicate = new HasSection(section, isCategoryInSection);
        } else {
          builder.messagePredicate =
              builder.messagePredicate.and(new HasSection(section, isCategoryInSection));
        }
      }

      if (cmd.hasOption("notsection")) {
        final String section = cmd.getOptionValue("notsection");
        if (builder.messagePredicate == null) {
          builder.messagePredicate = new NotSection(section, isCategoryInSection);
        } else {
          builder.messagePredicate =
              builder.messagePredicate.and(new NotSection(section, isCategoryInSection));
        }
      }

      if (cmd.hasOption("f")) {
        final String flow = cmd.getOptionValue("f");
        if (builder.messagePredicate == null) {
          builder.messagePredicate = new HasFlow(flow);
        } else {
          builder.messagePredicate = builder.messagePredicate.and(new HasFlow(flow));
        }
      }

      if (builder.messagePredicate == null) {
        logger.fatal(
            "RepositoryCompressor invalid arguments; Must select one or more selection criteria: category / section / flow");
        throw new ParseException(
            "Must select one or more selection criteria: category / section / flow");
      }
      return builder;
    } catch (final ParseException e) {
      logger.fatal("RepositoryCompressor invalid arguments", e);
      throw e;
    }
  }

  static Predicate<? super MessageType> hasFlow() {
    return m -> m.getFlow() != null;
  }

  private final List<BigInteger> componentIdList = new ArrayList<>();
  private List<ComponentType> componentList = new ArrayList<>();
  private final List<BigInteger> fieldIdList = new ArrayList<>();
  private final List<BigInteger> groupIdList = new ArrayList<>();
  private List<GroupType> groupList;
  private final String inputFile;
  private final Predicate<MessageType> messagePredicate;
  private final String outputFile;
  private final File logFile;

  protected RepositoryCompressor(Builder builder) {
    this.inputFile = builder.inputFile;
    this.outputFile = builder.outputFile;
    this.messagePredicate = builder.messagePredicate;
    this.logFile = builder.logFile != null ? new File(builder.logFile) : null;
  }

  public boolean compress() {

    try (InputStream is = new FileInputStream(this.inputFile);
        OutputStream os = new FileOutputStream(this.outputFile)) {
      return compress(is, os, this.messagePredicate);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      
      return false;
    }
  }

  private boolean compress(InputStream is, OutputStream os,
      Predicate<? super MessageType> messagePredicate) {
    try {
      final Repository inRepository = unmarshal(is);
      final Categories categories = inRepository.getCategories();
      isCategoryInSection.setCategories(categories.getCategory());
      final Repository outRepository = new Repository();
      inRepository.copyTo(null, outRepository, AttributeCopyStrategy.INSTANCE);

      final ElementOrRefinementContainer metadata =
          (ElementOrRefinementContainer) inRepository.getMetadata().clone();
      final List<JAXBElement<SimpleLiteral>> literals = metadata.getAny();
      final ObjectFactory objectFactory = new ObjectFactory();
      final SimpleLiteral contributor = new SimpleLiteral();
      contributor.getContent().add("RepositoryCompressor");
      literals.add(objectFactory.createContributor(contributor));
      outRepository.setMetadata(metadata);

      if (categories != null) {
        outRepository.setCategories((Categories) categories.clone());
      }
      final Sections sections = inRepository.getSections();
      if (sections != null) {
        outRepository.setSections((Sections) sections.clone());
      }
      final Datatypes datatypes = inRepository.getDatatypes();
      if (datatypes != null) {
        outRepository.setDatatypes((Datatypes) datatypes.clone());
      }
      final Actors actors = inRepository.getActors();
      if (actors != null) {
        outRepository.setActors((Actors) actors.clone());
      }
      final Components components = inRepository.getComponents();
      if (components != null) {
        final Components inComponents = (Components) components.clone();
        componentList = inComponents.getComponent();
      }
      final Groups groups = inRepository.getGroups();
      if (groups != null) {
        final Groups inGroups = (Groups) groups.clone();
        groupList = inGroups.getGroup();
      }


      final Messages messages = inRepository.getMessages();
      final List<MessageType> messageList;
      if (messages != null) {
        final Messages inMessages = (Messages) messages.clone();
        messageList = inMessages.getMessage();
      } else {
        messageList = Collections.emptyList();
      }
      final List<MessageType> filteredMessages =
          messageList.stream().filter(messagePredicate).collect(Collectors.toList());
      filteredMessages.forEach(m -> walk(m.getStructure().getComponentRefOrGroupRefOrFieldRef()));

      final List<BigInteger> distinctFieldIds =
          fieldIdList.stream().distinct().collect(Collectors.toList());
      final Fields inFields = (Fields) inRepository.getFields().clone();
      final List<FieldType> fieldsWithFlow = inFields.getField().stream()
          .filter(f -> distinctFieldIds.contains(f.getId())).collect(Collectors.toList());
      final Fields outFields = new Fields();
      outFields.getField().addAll(fieldsWithFlow);
      outRepository.setFields(outFields);

      final List<String> typeList =
          fieldsWithFlow.stream().map(FieldType::getType).distinct().collect(Collectors.toList());
      final CodeSets inCodeSets = (CodeSets) inRepository.getCodeSets().clone();
      final List<CodeSetType> codeSetsWithFlow = inCodeSets.getCodeSet().stream()
          .filter(cs -> typeList.contains(cs.getName())).collect(Collectors.toList());
      final CodeSets outCodeSets = new CodeSets();
      outCodeSets.getCodeSet().addAll(codeSetsWithFlow);
      outRepository.setCodeSets(outCodeSets);

      final List<BigInteger> distinctComponentsIds =
          componentIdList.stream().distinct().collect(Collectors.toList());
      final List<ComponentType> componentsWithFlow = componentList.stream()
          .filter(c -> distinctComponentsIds.contains(c.getId())).collect(Collectors.toList());
      final Components outComponents = new Components();
      outComponents.getComponent().addAll(componentsWithFlow);
      outRepository.setComponents(outComponents);

      final List<BigInteger> distinctGroupIds =
          groupIdList.stream().distinct().collect(Collectors.toList());
      final List<GroupType> groupWithFlow = groupList.stream()
          .filter(c -> distinctGroupIds.contains(c.getId())).collect(Collectors.toList());
      final Groups outGroups = new Groups();
      outGroups.getGroup().addAll(groupWithFlow);
      outRepository.setGroups(outGroups);

      final Messages outMessages = new Messages();
      outMessages.getMessage().addAll(filteredMessages);
      outRepository.setMessages(outMessages);

      marshal(outRepository, os);
      return true;
    } catch (JAXBException e) {
      logger.fatal("RepositoryCompressor failed", e);
      return false;
    }
  }

  private ComponentType getComponent(BigInteger id) {
    for (final ComponentType component : componentList) {
      if (component.getId().equals(id)) {
        return component;
      }
    }
    return null;
  }

  private List<Object> getComponentMembers(BigInteger id) {
    final ComponentType component = getComponent(id);
    if (component != null) {
      return component.getComponentRefOrGroupRefOrFieldRef();
    } else {
      return null;
    }
  }

  private GroupType getGroup(BigInteger id) {
    for (final GroupType group : groupList) {
      if (group.getId().equals(id)) {
        return group;
      }
    }
    return null;
  }

  private List<Object> getGroupMembers(BigInteger id) {
    final GroupType component = getGroup(id);
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
    for (final Object obj : list) {
      if (obj instanceof GroupRefType) {
        final GroupRefType groupRef = (GroupRefType) obj;
        final GroupType group = getGroup(groupRef.getId());
        if (group == null) {
          logger.error("Group missing for groupRef; ID={}", groupRef.getId().intValue());
          return;
        }
        fieldIdList.add(group.getNumInGroup().getId());
        groupIdList.add(groupRef.getId());
        // recursion on referenced component
        walk(getGroupMembers(groupRef.getId()));
      } else if (obj instanceof ComponentRefType) {
        final ComponentRefType componentRef = (ComponentRefType) obj;
        componentIdList.add(componentRef.getId());
        // recursion on referenced component
        walk(getComponentMembers(componentRef.getId()));
      } else if (obj instanceof FieldRefType) {
        final FieldRefType fieldRef = (FieldRefType) obj;
        fieldIdList.add(fieldRef.getId());
      }
    }
  }

}
