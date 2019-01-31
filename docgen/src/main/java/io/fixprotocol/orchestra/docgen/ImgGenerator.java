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

  public void generateUMLStateMachine(Path messagesImgPath, PathManager fileSystemManager, StateMachineType stateMachine,
      STErrorListener errorListener) throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final NoIndentWriter writer = new NoIndentWriter(stringWriter);

    final ST stStates = stGroup.getInstanceOf("stateMachine");
    stStates.add("stateMachine", stateMachine);
    stStates.write(writer, errorListener);

    final String umlString = stringWriter.toString();

    final SourceStringReader reader = new SourceStringReader(umlString);
    final Path path = messagesImgPath.resolve(String.format("%s.png", stateMachine.getName()));
    final OutputStream out = fileSystemManager.getOutputStream(path);
    reader.generateImage(out);
    out.flush();
  }

  public void generateUMLSequence(Path messagesImgPath, PathManager fileSystemManager, MessageType message, FlowType flow,
      List<ResponseType> responseList, STErrorListener errorListener) throws IOException {
    final StringWriter stringWriter = new StringWriter();
    final NoIndentWriter writer = new NoIndentWriter(stringWriter);

    final ST stSequence = stGroup.getInstanceOf("sequence");
    stSequence.add("message", message);
    stSequence.add("flow", flow);
    stSequence.write(writer, errorListener);
    generateResponses(responseList, writer, errorListener);
    final ST stEnd = stGroup.getInstanceOf("sequenceEnd");
    stEnd.add("message", message);
    stEnd.write(writer, errorListener);

    final String umlString = stringWriter.toString();

    final SourceStringReader reader = new SourceStringReader(umlString);
    final Path path = messagesImgPath.resolve(String.format("%s-%s.png", message.getName(), message.getScenario()));
    final OutputStream out = fileSystemManager.getOutputStream(path);
    reader.generateImage(out);
    out.flush();
  }

  private void generateResponses(List<ResponseType> responseList, STWriter writer,
      STErrorListener errorListener) {
    for (int i = 0; i < responseList.size(); i++) {
      final ResponseType response = responseList.get(i);
      final List<Object> responses = response.getMessageRefOrAssignOrTrigger();
      for (int j = 0; j < responses.size(); j++) {
        final Object responseRef = responses.get(j);
        if (responseRef instanceof MessageRefType) {
          final MessageRefType messageRef = (MessageRefType) responseRef;
          final ST st = stGroup.getInstanceOf("messageResponse");
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
