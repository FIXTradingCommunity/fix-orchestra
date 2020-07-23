/*
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
package io.fixprotocol.orchestra.dsl.antlr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.gui.TestRig;

/**
 * Interactive tester for the Score DSL
 * <p>
 * To start a test session, enter this command line:
 *
 * <pre>
 * java -jar dsl-antlr-[version]-jar-with-dependencies.jar
 * </pre>
 *
 * The interactive tester evaluates expressions that you enter. It waits until the end of input to
 * start evaluation. To end input, type Ctrl-D on Linux or Ctrl-Z on Windows. If an expression does
 * not conform to the Score DSL, the tester will report "mismatched input".
 *
 * @author Don Mendelson
 *
 */
public class InteractiveTest implements Runnable {

  private static final String[] DEFAULT_ARGS = {"-gui", "-tree", "-tokens"};
  private static final String DEFAULT_GRAMMAR = "io.fixprotocol.orchestra.dsl.antlr.Score";
  private static final String DEFAULT_RULE = "anyExpression";

  /**
   * Runs an interactive test of the DSL
   *
   * @param args command line arguments
   *        <ul>
   *        <li>-f filename of file to parse - defaults to stdin</li>
   *        <li>-g grammar name - defaults to {@code io.fixprotocol.orchestra.dsl.antlr.Score}</li>
   *        <li>-r start rule name - defaults to {@code anyExpression}</li>
   *        </ul>
   * @throws Exception if arguments are invalid
   */
  public static void main(String[] args) throws Exception {
    String fileName = null;
    String grammarName = DEFAULT_GRAMMAR;
    String ruleName = DEFAULT_RULE;
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "-f":
          fileName = args[++i];
          break;
        case "-g":
          grammarName = args[++i];
          break;
        case "-r":
          ruleName = args[++i];
          break;
        default:
          System.err.println("Invalid argument " + args[i]);
          usage();
      }
    }

    final List<String> defaultArgsList = Arrays.asList(DEFAULT_ARGS);
    final List<String> testArgsList = new ArrayList<>();
    testArgsList.add(grammarName);
    testArgsList.add(ruleName);
    testArgsList.addAll(defaultArgsList);
    if (fileName != null) {
      testArgsList.add(fileName);
    }
    final String[] testArgs = new String[testArgsList.size()];
    testArgsList.toArray(testArgs);
    final InteractiveTest test = new InteractiveTest(testArgs);
    test.run();
  }

  private static void usage() {
    System.err.println(
        "java io.fixprotocol.orchestra.dsl.antlr.InteractiveTest [-i <fileName>] [-g <grammarName] [-r ruleName]");
    System.exit(1);
  }

  private final TestRig testRig;

  /**
   * Instantiates the ANTLR4 test rig with parameters
   *
   * @param args arguments to {@code TestRig}
   * @throws Exception if parameters are invalid
   *
   */
  public InteractiveTest(String[] args) throws Exception {
    testRig = new TestRig(args);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    try {
      testRig.process();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

}
