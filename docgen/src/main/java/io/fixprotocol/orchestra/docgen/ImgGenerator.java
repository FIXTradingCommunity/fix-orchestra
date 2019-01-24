package io.fixprotocol.orchestra.docgen;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.List;

import org.stringtemplate.v4.NoIndentWriter;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.STWriter;

import io.fixprotocol._2016.fixrepository.FlowType;
import io.fixprotocol._2016.fixrepository.MessageRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.ResponseType;
import io.fixprotocol._2016.fixrepository.StateMachineType;
import io.fixprotocol._2016.fixrepository.Synchronization;
import net.sourceforge.plantuml.SourceStringReader;

public class ImgGenerator {

  private final STGroupFile stGroup;

  public ImgGenerator() {
    this.stGroup = new STGroupFile("templates/imggen.stg", '$', '$');
  }

  public void generateUMLStateMachine(Path messagesImgPath, FileSystemManager fileSystemManager, StateMachineType stateMachine,
      STErrorListener errorListener) throws IOException {
    StringWriter stringWriter = new StringWriter();
    NoIndentWriter writer = new NoIndentWriter(stringWriter);

    ST stStates = stGroup.getInstanceOf("stateMachine");
    stStates.add("stateMachine", stateMachine);
    stStates.write(writer, errorListener);

    String umlString = stringWriter.toString();

    SourceStringReader reader = new SourceStringReader(umlString);
    Path path = messagesImgPath.resolve(String.format("%s.png", stateMachine.getName()));
    OutputStream out = fileSystemManager.getOutputStream(path);
    reader.generateImage(out);
    out.flush();
  }

  public void generateUMLSequence(Path messagesImgPath, FileSystemManager fileSystemManager, MessageType message, FlowType flow,
      List<ResponseType> responseList, STErrorListener errorListener) throws IOException {
    StringWriter stringWriter = new StringWriter();
    NoIndentWriter writer = new NoIndentWriter(stringWriter);

    ST stSequence = stGroup.getInstanceOf("sequence");
    stSequence.add("message", message);
    stSequence.add("flow", flow);
    stSequence.write(writer, errorListener);
    generateResponses(responseList, writer, errorListener);
    ST stEnd = stGroup.getInstanceOf("sequenceEnd");
    stEnd.add("message", message);
    stEnd.write(writer, errorListener);

    String umlString = stringWriter.toString();

    SourceStringReader reader = new SourceStringReader(umlString);
    Path path = messagesImgPath.resolve(String.format("%s-%s.png", message.getName(), message.getScenario()));
    OutputStream out = fileSystemManager.getOutputStream(path);
    reader.generateImage(out);
    out.flush();
  }

  private void generateResponses(List<ResponseType> responseList, STWriter writer,
      STErrorListener errorListener) {
    for (int i = 0; i < responseList.size(); i++) {
      ResponseType response = responseList.get(i);
      List<Object> responses = response.getMessageRefOrAssignOrTrigger();
      for (int j = 0; j < responses.size(); j++) {
        Object responseRef = responses.get(j);
        if (responseRef instanceof MessageRefType) {
          MessageRefType messageRef = (MessageRefType) responseRef;
          ST st = stGroup.getInstanceOf("messageResponse");
          st.add("messageName", messageRef.getName());
          st.add("scenarioName", messageRef.getScenario());
          st.add("async", response.getSync() == Synchronization.ASYNCHRONOUS);
          st.add("name", response.getName());
          st.add("isFirstAlt", i == 0 && responseList.size() > 1 && j == 0);
          st.add("isAlt", (i != 0) && responseList.size() > 1 && j == 0);
          st.write(writer, errorListener);
        }
      }
    }
  }
}
