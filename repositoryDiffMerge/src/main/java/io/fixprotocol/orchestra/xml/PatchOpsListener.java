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
package io.fixprotocol.orchestra.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 * Writes XML diffs as patch operations specified by IETF RFC 5261
 * 
 * @author Don Mendelson
 * @see <a href="https://tools.ietf.org/html/rfc5261">An Extensible Markup Language (XML) Patch
 *      Operations Framework Utilizing XML Path Language (XPath) Selectors</a>
 */
public class PatchOpsListener implements XmlDiffListener {

  private final OutputStreamWriter writer;

  /**
   * @throws IOException
   * 
   */
  public PatchOpsListener(OutputStream out) throws IOException {
    writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    writer.write("<diff>\n");
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.function.Consumer#accept(java.lang.Object)
   */
  @Override
  public void accept(Event t) {
    try {
      switch (t.getDifference()) {
        case ADD:
          int lastSlash = t.getXpath().lastIndexOf('/');
          if (lastSlash != -1 && t.getXpath().charAt(lastSlash + 1) == '@') {
            // add attribute
            writer.write(String.format("<add sel='%s' type='%s'>%s</add>%n",
                t.getXpath().substring(0, lastSlash), t.getXpath().substring(lastSlash + 1),
                t.getValue()));
          } else {
            // add element
            writer.write(String.format("<add sel='%s'>%s</add>%n", t.getXpath(), t.getValue()));
          }
          break;
        case REPLACE:
          writer
              .write(String.format("<replace sel='%s'>%s</replace>%n", t.getXpath(), t.getValue()));
          break;
        case REMOVE:
          writer.write(String.format("<remove sel='%s'/>%n", t.getXpath()));
          break;
        default:
          break;
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public void close() throws Exception {
    try {
      writer.write("</diff>\n");
    } catch (IOException e) {
      // already closed
    }
    writer.close();
  }

}
