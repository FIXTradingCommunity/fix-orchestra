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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
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
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol._2016.fixrepository.ResponseType;


/**
 * @author Don Mendelson
 *
 */
public class TestGenerator {
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

  private final File baseOutputDir;
  private final STGroupFile stGroup;
  private final Repository repository;

  /**
   * Constructs a TestGenerator
   * 
   * @param is an input stream to consume a Repository
   * @param baseOutputDir directory to write test files
   * @throws JAXBException if a parsing error occurs
   * @throws IOException if a file cannot be accessed
   */
  public TestGenerator(InputStream is, File baseOutputDir) throws IOException, JAXBException {
    this.baseOutputDir = makeDirectory(baseOutputDir);
    this.stGroup = new STGroupFile("templates/testgen.stg");
    // STGroup.verbose = true;
    this.repository = unmarshal(is);
  }

  /**
   * Generates test features
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
  public static void main(String[] args) throws IOException, JAXBException {
    if (args.length < 1) {
      System.err.println(
          "Usage: java io.fixprotocol.orchestra.testgen.TestGenerator <input-filename> [output-dir]");
    } else {
      try (InputStream is = new FileInputStream(args[0])) {

        File outputDir;
        if (args.length > 1) {
          outputDir = new File("args[1]");
        } else {
          outputDir = new File("test");
        }
        TestGenerator gen = new TestGenerator(is, outputDir);
        gen.generate();
      }
    }
  }

  /**
   * Generates test features
   * 
   * @throws IOException if test features cannot be written to a file
   */
  public void generate() throws IOException {
    makeDirectory(baseOutputDir);


    repository.getActors().getActorOrFlow().stream().filter(o -> o instanceof FlowType)
        .forEach(o -> {
          FlowType flow = (FlowType) o;
          String sourceName = flow.getSource();
          Optional<Object> opt = repository.getActors().getActorOrFlow().stream()
              .filter(af -> af instanceof ActorType).findFirst();
          opt.ifPresent(obj -> {
            ActorType actor = (ActorType) obj;
            if (sourceName.equals(actor.getName())) {
              File outputFile = new File(baseOutputDir,
                  String.format("%s-%s.feature", repository.getName(), actor.getName()));
              try (OutputStreamWriter fileWriter =
                  new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8")) {
                AutoIndentWriter writer = new AutoIndentWriter(fileWriter);

                generateFeature(writer, repository, flow, actor);
                repository.getMessages().getMessage().stream()
                    .filter(m -> flow.getName().equals(m.getFlow())).forEach(message -> {
                      message.getResponses().getResponse().forEach(response -> {
                        generateScenario(writer, repository, actor, message, response);
                      });
                    });
              } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          });
        });
  }

  private void generateScenario(AutoIndentWriter writer, Repository repository, ActorType actor,
      MessageType message, ResponseType response) {
    ST st = stGroup.getInstanceOf("scenario");
    st.add("actor", actor);
    st.add("message", message);
    st.add("response", response);
    st.write(writer, errorListener);
    List<Object> messageElements = message.getStructure().getComponentOrComponentRefOrGroup();
    generateMessageElements(writer, messageElements);
    List<Object> actions = response.getMessageRefOrAssignOrTransitionRef();
    for (Object action : actions) {
      if (action instanceof MessageRefType) {
        MessageRefType messageRef = (MessageRefType) action;
        ST stMessage = stGroup.getInstanceOf("messageRef");
        stMessage.add("actor", actor);
        stMessage.add("messageRef", messageRef);
        stMessage.write(writer, errorListener);

        MessageType responseMessage = findMessage(messageRef.getName(), messageRef.getScenario());
        List<Object> responseMessageElements =
            responseMessage.getStructure().getComponentOrComponentRefOrGroup();
        generateMessageElements(writer, responseMessageElements);
      }
    }
  }

  private void generateMessageElements(AutoIndentWriter writer, List<Object> messageElements) {
    for (Object obj : messageElements) {
      if (obj instanceof FieldRefType) {
        FieldRefType fieldRef = (FieldRefType) obj;
        if (fieldRef.getAssign() != null || (fieldRef.getPresence() != PresenceT.IGNORED
            && fieldRef.getPresence() != PresenceT.OPTIONAL)) {
          ST st = stGroup.getInstanceOf("field");
          st.add("fieldRef", fieldRef);
          st.write(writer, errorListener);
        }
      }
    }
  }

  private MessageType findMessage(String name, String scenario) {
    for (MessageType message : repository.getMessages().getMessage()) {
      if (message.getName().equals(name) && message.getScenario().equals(scenario)) {
        return message;
      }
    }
    return null;
  }

  private void generateFeature(AutoIndentWriter writer, Repository repository, FlowType flow,
      ActorType actor) {
    ST st = stGroup.getInstanceOf("feature");
    st.add("repository", repository);
    st.add("flow", flow);
    st.add("actor", actor);
    st.write(writer, errorListener);
  }

  private File makeDirectory(File dir) throws IOException {
    dir.mkdir();
    if (!dir.isDirectory()) {
      throw new IOException(dir.toString() + " not a directory or is inaccessible");
    }
    return dir;
  }

  private Repository unmarshal(InputStream is) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }
}
