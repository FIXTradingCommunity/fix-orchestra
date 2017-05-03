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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Generates an HTML report of FIX Repository differences
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryDiffReporter extends XmlDiff {
  /**
   * @throws TransformerConfigurationException if a configuration error occurs
   */
  public RepositoryDiffReporter() throws TransformerConfigurationException {
    super();
  }

  class HtmlDiffListener implements XmlDiffListener {

    private boolean headerGenerated = false;
    private boolean firstRow = true;
    private final OutputStreamWriter out;

    /**
     * @param out
     */
    public HtmlDiffListener(OutputStream out) {
      this.out = new OutputStreamWriter(out);
    }

    @Override
    public void accept(Event t) {
      try {
        if (!headerGenerated) {
          generateHeader();
        }

        if (!firstRow) {
          out.write(String.format("</td></tr>%n"));
        } else {
          firstRow = false;
        }
        out.write(String.format("<tr>"));


        switch (t.getDifference()) {
          case ADD:
            if (t.getValue() instanceof Attr) {
              out.write(
                  String.format("<td>%s</td><td>%s</td><td>Add</td><td></td><td>%s</td><td>%s",
                      XpathUtil.getParentLocalName(t.getXpath()),
                      XpathUtil.getParentPredicate(t.getXpath()), 
                      t.getValue().getNodeName(),
                      t.getValue().getNodeValue()));
            } else {
              Element element = (Element) t.getValue();
              String text = null;
              NodeList children = element.getChildNodes();
              for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (Node.TEXT_NODE == child.getNodeType()) {
                  text = child.getNodeValue();
                  break;
                }
              }

              out.write(
                  String.format("<td>%s</td><td>%s</td><td>Add</td><td>%s</td><td>%s</td><td>%s",
                      XpathUtil.getElementLocalName(t.getXpath()),
                      XpathUtil.getElementPredicate(t.getXpath()), 
                      t.getValue().getNodeName(),
                      element.getAttribute("name"),
                      text != null ? text : ""));

            }
            break;
          case REPLACE:
            if (t.getValue() instanceof Attr) {
              out.write(
                  String.format("<td>%s</td><td>%s</td><td>Add</td><td></td><td>%s</td><td>%s",
                      XpathUtil.getParentLocalName(t.getXpath()),
                      XpathUtil.getParentPredicate(t.getXpath()), t.getValue().getNodeName(),
                      t.getValue().getNodeValue()));
            } else {
              Element element = (Element) t.getValue();
              String text = null;
              NodeList children = element.getChildNodes();
              for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (Node.TEXT_NODE == child.getNodeType()) {
                  text = child.getNodeValue();
                  break;
                }
              }
              out.write(
                  String.format("<td>%s</td><td>%s</td><td>Add</td><td>%s</td><td>%s</td><td>%s",
                      XpathUtil.getParentLocalName(t.getXpath()),
                      XpathUtil.getParentPredicate(t.getXpath()), t.getValue().getNodeName(),
                      text != null ? text : ""));
            }
            break;
          case REMOVE:
            out.write(
                String.format("<td>%s</td><td>%s</td><td>Remove</td><td>%s</td><td>%s</td><td>",
                    XpathUtil.getParentLocalName(t.getXpath()),
                    XpathUtil.getParentPredicate(t.getXpath()), 
                    XpathUtil.getElementLocalName(t.getXpath()),
                    XpathUtil.getElementPredicate(t.getXpath())));
            break;
        }

      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    @Override
    public void close() throws Exception {
      // generate footer
      out.write(String.format("</table>%n"));
      out.write(String.format("</body>%n"));
      out.write(String.format("</html>%n"));
    }


    private void generateHeader() throws IOException {
      out.write(String.format("<!DOCTYPE html>%n"));
      out.write(String.format("<html>%n"));
      out.write(String.format("<head>%n"));
      out.write(String.format("<title>Repository Differences</title>%n"));
      out.write(String.format("</head>%n"));
      out.write(String.format("<body>%n"));
      out.write(String.format("<table border=\"1\">%n"));
      out.write(String.format(
          "<tr><th>Parent Type</th><th>Parent Element</th><th>Event</th><th>Type</th><th>Name</th><th>Value</th></tr>%n"));
      headerGenerated = true;
    }
  }

  /**
   * Compares two XML files and produces an HTML report. By default, report is sent to console.
   * 
   * @param args file names of two XML files to compare and optional name of difference file. If
   *        diff file is not provided, then output goes to console.
   * @throws Exception if an IO or parsing error occurs
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      usage();
    } else {
      RepositoryDiffReporter tool = new RepositoryDiffReporter();
      try (
          HtmlDiffListener aListener = tool.new HtmlDiffListener(
              args.length > 2 ? new FileOutputStream(args[2]) : System.out);
          InputStream is1 = new FileInputStream(args[0]);
          InputStream is2 = new FileInputStream(args[1])) {
        tool.setListener(aListener);
        tool.diff(is1, is2);
      }
    }
  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: RepositoryDiffReporter <xml-file1> <xml-file2> [html-file]");
  }

}
