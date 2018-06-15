/**
 *    Copyright 2016-2018 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.fixprotocol.orchestra.transformers;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class SelectCategoryTransformerTest {
    @Test
    public void selectSessions() throws IOException, TransformerException {
        String[] arr = new String[4];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("xsl/select_category.xslt")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("FixRepository2016.xml")
                .getFile();
        // send output to target so it will get cleaned
        arr[2] = "target/test/Session.xml";
        arr[3] = "selection=+Session";
        SelectCategoryTransformer.main(arr);
        File outFile = new File(arr[2]);
        Assert.assertTrue(outFile.exists());
    }
    
    @Test
    public void selectNotSessions() throws IOException, TransformerException {
        String[] arr = new String[4];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("xsl/select_category.xslt")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("FixRepository2016.xml")
                .getFile();
        // send output to target so it will get cleaned
        arr[2] = "target/test/Application.xml";
        arr[3] = "selection=-Session";
        SelectCategoryTransformer.main(arr);
        File outFile = new File(arr[2]);
        Assert.assertTrue(outFile.exists());
    }
}
