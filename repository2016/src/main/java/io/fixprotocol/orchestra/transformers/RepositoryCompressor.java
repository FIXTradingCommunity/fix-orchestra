package io.fixprotocol.orchestra.transformers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.purl.dc.elements._1.ObjectFactory;
import org.purl.dc.elements._1.SimpleLiteral;
import org.purl.dc.terms.ElementOrRefinementContainer;

import io.fixprotocol._2016.fixrepository.Abbreviations;
import io.fixprotocol._2016.fixrepository.Actors;
import io.fixprotocol._2016.fixrepository.Categories;
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
 * <ul>
 * <li>Only selected messages are retained with one of the following filters</li>
 * <ul>
 * <li>Messages that have a flow</li>
 * <li>Messages that have a specified category</li>
 * <li>Messages that <em>not</em> in a specified category</li>
 * </ul>
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

  static class HasCategory implements Predicate<MessageType> {

    private String category;

    public HasCategory(String category) {
      this.category = category;
    }

    @Override
    public boolean test(MessageType m) {
      return category.equals(m.getCategory());
    }

  }

  static class NotCategory implements Predicate<MessageType> {

    private String category;

    public NotCategory(String category) {
      this.category = category;
    }

    @Override
    public boolean test(MessageType m) {
      return !category.equals(m.getCategory());
    }

  }

  public static void main(String[] args) throws IOException, JAXBException {
    if (args.length < 2) {
      usage();
    } else {
      try (InputStream is = new FileInputStream(args[0]);
          OutputStream os = new FileOutputStream(args[1])) {

        RepositoryCompressor compressor = new RepositoryCompressor();

        if (args.length < 3) {
          compressor.compress(is, os, hasFlow());
        } else if (args[2].startsWith("+")) {
          String category = args[2].substring(1);
          compressor.compress(is, os, new HasCategory(category));
        } else if (args[2].startsWith("-")) {
          String category = args[2].substring(1);
          compressor.compress(is, os, new NotCategory(category));
        } else {
          usage();
          throw new IllegalArgumentException();
        }
      }
    }
  }

  public static void usage() {
    System.err.println(
        "Usage: java io.fixprotocol.orchestra.transformers.RepositoryCompressor <input-filename> <output-filename> [+|-]<category>");
  }

  static Predicate<? super MessageType> hasFlow() {
    return m -> m.getFlow() != null;
  }

  private final List<BigInteger> componentIdList = new ArrayList<>();
  private List<ComponentType> componentList = new ArrayList<>();
  private final List<BigInteger> fieldIdList = new ArrayList<>();
  private final List<BigInteger> groupIdList = new ArrayList<>();
  private List<GroupType> groupList;

  public void compress(InputStream is, OutputStream os,
      Predicate<? super MessageType> messagePredicate) throws JAXBException {
    final Repository inRepository = unmarshal(is);
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
    outRepository.setAbbreviations((Abbreviations) inRepository.getAbbreviations().clone());
    outRepository.setCategories((Categories) inRepository.getCategories().clone());
    outRepository.setSections((Sections) inRepository.getSections().clone());
    outRepository.setDatatypes((Datatypes) inRepository.getDatatypes().clone());
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

}
