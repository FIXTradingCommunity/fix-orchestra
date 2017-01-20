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
import java.io.PrintStream;

/**
 * Generates an HTML report of FIX Repository differences
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryDiffReporter extends XmlDiff {
  private class HtmlDiffListener implements XmlDiffListener {

    private boolean headerGenerated = false;
    private boolean firstRow = true;

    @Override
    public void accept(Event t) {
      if (!headerGenerated) {
        generateHeader();
      }

      String type = XpathUtil.getElementLocalName(t.getName());
      // Skip an element that is just a container
      if (type.equals("annotation")) {
        return;
      }
      boolean isAttribute = XpathUtil.isAttribute(t.getName());

      if (isAttribute) {
        String attribute = XpathUtil.getAttribute(t.getName());
        // Name already shown
        if (attribute.equals("name")) {
          return;
        }
        out.format("<br/>%s=%s", attribute, t.getValue());
      } else if (type.equals("documentation")) {
        out.format("<br/>%s", t.getValue());
      } else {
        if (!firstRow) {
          out.format("</td></tr>%n");
        } else {
          firstRow = false;
        }
        out.format("<tr>");
        String name = XpathUtil.getElementPredicate(t.getName());
        String parent = XpathUtil.getParentPredicate(t.getName());
        String parentType = XpathUtil.getParentLocalName(t.getName());

        switch (t.getDifference()) {
          case ADD:
            out.format("<td>%s</td><td>%s</td><td>Add</td><td>%s</td><td>%s</td><td>%s", parentType,
                parent, type, name,
                t.getValue() != null && t.getValue().length() > 0 ? t.getValue() : "");
            break;
          case CHANGE:
            out.format("<td>%s</td><td>%s</td><td>Change</td><td>%s</td><td>%s</td><td>%s",
                parentType, parent, type, name, t.getValue());
            break;
          case REMOVE:
            out.format("<td>%s</td><td>%s</td><td>Remove</td><td>%s</td><td>%s</td><td>%s",
                parentType, parent, type, name, t.getValue() != null ? t.getValue() : "");
            break;
          case EQUAL:
            break;
        }

      }
    }

    @Override
    public void close() throws Exception {
      // generate footer
      out.format("</table>%n");
      out.format("</body>%n");
      out.format("</html>%n");
    }


    /**
     * 
     */
    private void generateHeader() {
      out.format("<!DOCTYPE html>%n");
      out.format("<html>%n");
      out.format("<head>%n");
      out.format("<title>Repository Differences</title>%n");
      out.format("</head>%n");
      out.format("<body>%n");
      out.format("<table border=\"1\">%n");
      out.format(
          "<tr><th>Parent Type</th><th>Parent Element</th><th>Event</th><th>Type</th><th>Name</th><th>Value</th></tr>%n");
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
      tool.diff(new FileInputStream(args[0]), new FileInputStream(args[1]),
          args.length > 2 ? new PrintStream(args[2]) : System.out);
    }
  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: RepositoryDiffReporter <xml-file1> <xml-file2> [html-file]");
  }



  public RepositoryDiffReporter() {
    setListener(new HtmlDiffListener());
  }
}
