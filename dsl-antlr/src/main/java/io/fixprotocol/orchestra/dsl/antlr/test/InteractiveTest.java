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
package io.fixprotocol.orchestra.dsl.antlr.test;

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

  /**
   * Runs an interactive test of the DSL
   *
   * @param args parameters to {@code TestRig}. If empty, defaults are applied.
   * @throws Exception if parameters are invalid
   */
  public static void main(String[] args) throws Exception {
    String[] testArgs = args;
    if (args.length < 2 || !args[0].contains("Score")) {
      testArgs = new String[] {"io.fixprotocol.orchestra.dsl.antlr.Score", "anyExpression", "-gui",
          "-tree", "-tokens"};
    }
    final InteractiveTest test = new InteractiveTest(testArgs);
    test.run();
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
