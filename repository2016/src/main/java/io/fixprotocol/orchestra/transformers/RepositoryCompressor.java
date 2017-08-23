package io.fixprotocol.orchestra.transformers;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Messages;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.Sections;

/**
 * Selectively compresses a Repository 2016 Edition file <br>
 * Copies selected elements to a new file.
 * <ul>
 * <li>Only messages with a flow are retained.</li>
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

  public static void main(String[] args) throws IOException, JAXBException {
    if (args.length < 2) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.transformers.Compressor <input-filename> <output-filename>");
    } else {
      try (InputStream is = new FileInputStream(args[0]);
          OutputStream os = new FileOutputStream(args[1])) {

        RepositoryCompressor compressor = new RepositoryCompressor();
        compressor.compress(is, os);
      }
    }
  }

  private final List<BigInteger> componentIdList = new ArrayList<>();
  private List<ComponentType> componentList;
  private final List<BigInteger> fieldIdList = new ArrayList<>();

  public void compress(InputStream is, OutputStream os) throws JAXBException {
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
    outRepository.setActors((Actors) inRepository.getActors().clone());
    Components inComponents = (Components) inRepository.getComponents().clone();
    componentList = inComponents.getComponentOrGroup();

    Messages inMessages = (Messages) inRepository.getMessages().clone();
    List<MessageType> messageList = inMessages.getMessage();
    List<MessageType> messagesWithFlow =
        messageList.stream().filter(m -> m.getFlow() != null).collect(Collectors.toList());
    messagesWithFlow.forEach(m -> walk(m.getStructure().getComponentOrComponentRefOrGroup()));

    List<BigInteger> distinctFieldIds =
        fieldIdList.stream().distinct().collect(Collectors.toList());
    Fields inFields = (Fields) inRepository.getFields().clone();
    List<FieldType> fieldsWithFlow = inFields.getField().stream()
        .filter(f -> distinctFieldIds.contains(f.getId())).collect(Collectors.toList());
    Fields outFields = new Fields();
    outFields.getField().addAll(fieldsWithFlow);
    outRepository.setFields(outFields);

    List<String> typeList =
        fieldsWithFlow.stream().map(f -> f.getType()).distinct().collect(Collectors.toList());
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
    outComponents.getComponentOrGroup().addAll(componentsWithFlow);
    outRepository.setComponents(outComponents);

    Messages outMessages = new Messages();
    outMessages.getMessage().addAll(messagesWithFlow);
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
        GroupType group = (GroupType) getComponent(groupRef.getId());
        fieldIdList.add(group.getNumInGroupId());
        componentIdList.add(groupRef.getId());
        // recursion on referenced component
        walk(getComponentMembers(groupRef.getId()));
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
