/*
 * Copyright 2017-2020 FIX Protocol Ltd
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

import io.fixprotocol._2022.orchestra.repository.FlowType;
import io.fixprotocol._2022.orchestra.repository.MessageRefType;
import io.fixprotocol._2022.orchestra.repository.MessageType;
import io.fixprotocol._2022.orchestra.repository.ResponseType;
import io.fixprotocol._2022.orchestra.repository.StateMachineType;
import io.fixprotocol._2022.orchestra.repository.Synchronization;
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
