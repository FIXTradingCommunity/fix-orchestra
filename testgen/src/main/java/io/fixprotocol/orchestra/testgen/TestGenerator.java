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
package io.fixprotocol.orchestra.testgen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.stringtemplate.v4.AutoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

import io.fixprotocol._2016.fixrepository.ActorType;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FlowType;
import io.fixprotocol._2016.fixrepository.MessageRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.MessageType.Responses;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.ResponseType;
import io.fixprotocol.orchestra.quickfix.DataDictionaryGenerator;


/**
 * @author Don Mendelson
 *
 */
public class TestGenerator {
  /**
   * Generates test features
   * 
   * @param args command line arguments
   *        <ol>
   *        <li>Name of Orchestra input file in Repository 2016 Edition format
   *        <li>Name of resources output directory--will be created if it does not exist, defaults
   *        to "src/test/resources"</li>
   *        <li>Name of source output directory--will be created if it does not exist, defaults to
   *        "src/test/java"</li> *
   *        </ol>
   * @throws JAXBException if XML file unmarshaling fails
   * @throws IOException if input file is not found or cannot be read
   */
  public static void main(String[] args) throws IOException, JAXBException {
    if (args.length < 1) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.testgen.TestGenerator <input-filename> [resources-dir] [source-dir]");
    } else {
      try (InputStream is = new FileInputStream(args[0])) {

        File resourcesDir;
        if (args.length > 1) {
          resourcesDir = new File("args[1]");
        } else {
          resourcesDir = new File("target/generated-cukes");
        }

        File sourceDir;
        if (args.length > 2) {
          sourceDir = new File("args[2]");
        } else {
          sourceDir = new File("target/generated-cukes/java");
        }
        TestGenerator gen = new TestGenerator(is, resourcesDir, sourceDir);
        gen.generate();
      }
    }
  }

  private final STErrorListener errorListener = new STErrorListener() {

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
  private final File resourcesDir;
  private final File sourceDir;

  /**
   * Constructs a TestGenerator
   * 
   * @param is an input stream to consume a Repository
   * @param resourcesDir directory to write test resources
   * @param sourceDir directory to write generated code
   * @throws JAXBException if a parsing error occurs
   * @throws IOException if a file cannot be accessed
   */
  public TestGenerator(InputStream is, File resourcesDir, File sourceDir)
      throws IOException, JAXBException {
    this.resourcesDir = makeDirectory(resourcesDir);
    this.sourceDir = makeDirectory(sourceDir);  
    
    // STGroup.verbose = true;
    this.repository = unmarshal(is);
  }

  /**
   * Generates test features
   * 
   * @throws IOException if test features cannot be written to a file
   */
  public void generate() throws IOException {
    generateResources();
    generateSource();
  }

  private void generateFeature(AutoIndentWriter writer, Repository repository, FlowType flow,
      ActorType actor) {
    final STGroupFile stFeatureGroup = new STGroupFile("templates/feature.stg");

    ST st = stFeatureGroup.getInstanceOf("feature");
    st.add("repository", repository);
    st.add("flow", flow);
    st.add("actor", actor);
    st.write(writer, errorListener);
    
    repository.getMessages().getMessage().stream()
    .filter(m -> flow.getName().equals(m.getFlow())).forEach(message -> {
      Responses responses = message.getResponses();
      if (responses != null)
        responses.getResponse().forEach(response -> {
          generateFeatureScenario(writer, stFeatureGroup, repository, actor, message, response);
        });
    });
  }

  private void generateFeatureMessageElements(AutoIndentWriter writer, STGroupFile stFeatureGroup, List<Object> messageElements) {
    for (Object obj : messageElements) {
      if (obj instanceof FieldRefType) {
        FieldRefType fieldRef = (FieldRefType) obj;
        if (fieldRef.getAssign() != null) {
          ST st = stFeatureGroup.getInstanceOf("fieldValue");
          st.add("fieldRef", fieldRef);
          st.add("value", fieldRef.getAssign());
          st.write(writer, errorListener);
        } else if (fieldRef.getPresence() == PresenceT.CONSTANT) {
          ST st = stFeatureGroup.getInstanceOf("fieldValue");
          st.add("fieldRef", fieldRef);
          st.add("value", fieldRef.getValue());
          st.write(writer, errorListener);
        } else if (fieldRef.getPresence() == PresenceT.REQUIRED) {
          ST st = stFeatureGroup.getInstanceOf("fieldRequired");
          st.add("fieldRef", fieldRef);
          st.write(writer, errorListener);
        }
      }
    }
  };

  private void generateFeatureScenario(AutoIndentWriter writer, STGroupFile stFeatureGroup, Repository repository, ActorType actor,
      MessageType message, ResponseType response) {
    ST st = stFeatureGroup.getInstanceOf("scenario");
    st.add("actor", actor);
    st.add("message", message);
    st.add("response", response);
    st.write(writer, errorListener);
    List<Object> messageElements = message.getStructure().getComponentOrComponentRefOrGroup();
    generateFeatureMessageElements(writer, stFeatureGroup, messageElements);
    List<Object> actions = response.getMessageRefOrAssignOrTrigger();
    for (Object action : actions) {
      if (action instanceof MessageRefType) {
        MessageRefType messageRef = (MessageRefType) action;
        ST stMessage = stFeatureGroup.getInstanceOf("messageRef");
        stMessage.add("actor", actor);
        stMessage.add("messageRef", messageRef);
        stMessage.write(writer, errorListener);

        MessageType responseMessage = findMessage(messageRef.getName(), messageRef.getScenario());
        List<Object> responseMessageElements =
            responseMessage.getStructure().getComponentOrComponentRefOrGroup();
        generateFeatureMessageElements(writer, stFeatureGroup, responseMessageElements);
      }
    }
  }
  
  private void generateDataDictionaries() throws IOException {
    DataDictionaryGenerator generator = new DataDictionaryGenerator();
    File dataDictionaryDir = new File(resourcesDir, "quickfixj");
    generator.generate(repository, dataDictionaryDir);
  }

  private void generateResources() throws IOException {
    
    generateDataDictionaries();
    
    try {
      repository.getActors().getActorOrFlow().stream().filter(o -> o instanceof FlowType)
          .forEach(o -> {
            FlowType flow = (FlowType) o;
            String sourceName = flow.getSource();
            Optional<Object> opt = repository.getActors().getActorOrFlow().stream()
                .filter(af -> af instanceof ActorType)
                .filter(a -> ((ActorType) a).getName().equals(sourceName)).findFirst();
            ActorType actor = (ActorType) opt.orElseThrow(
                () -> new IllegalArgumentException("Actor missing for flow " + flow.getName()));

            File outputFile = new File(resourcesDir,
                String.format("%s-%s.feature", repository.getName(), actor.getName()));
            try (OutputStreamWriter fileWriter =
                new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
              AutoIndentWriter writer = new AutoIndentWriter(fileWriter);

              generateFeature(writer, repository, flow, actor);

            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });
    } catch (RuntimeException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else {
        throw e;
      }
    }
  }

  private void generateSource() {
    // TODO Auto-generated method stub
    
  }

  private Repository unmarshal(InputStream is) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }

  MessageType findMessage(String name, String scenario) {
    for (MessageType message : repository.getMessages().getMessage()) {
      if (message.getName().equals(name) && message.getScenario().equals(scenario)) {
        return message;
      }
    }
    return null;
  }

  File makeDirectory(File dir) throws IOException {
    dir.mkdir();
    if (!dir.isDirectory()) {
      throw new IOException(dir.toString() + " not a directory or is inaccessible");
    }
    return dir;
  }
}
