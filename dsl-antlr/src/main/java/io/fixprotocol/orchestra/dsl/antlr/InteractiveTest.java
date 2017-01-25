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
package io.fixprotocol.orchestra.dsl.antlr;

import org.antlr.v4.gui.TestRig;

/**
 * @author Don Mendelson
 *
 */
public class InteractiveTest implements Runnable {

  private TestRig testRig;

  /**
   * @throws Exception 
   * 
   */
  public InteractiveTest(String[] args) throws Exception {
    testRig = new TestRig(args);
  }

  /**
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    String[] testArgs = args;
    if (args.length < 2 || !args[0].contains("Score")) {
      testArgs = new String[] {"io.fixprotocol.orchestra.dsl.antlr.Score", "anyExpression", "-gui","-tree"};
    }
    InteractiveTest test = new InteractiveTest(testArgs);
    test.run();
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    try {
      testRig.process();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
