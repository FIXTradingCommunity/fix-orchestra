/**
 *    Copyright 2016 FIX Protocol Ltd
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

import io.fixprotocol.orchestra.transformers.RepositoryXslTransformer;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class XslTransformerTest {
    @Test
    public void transformEP() throws IOException, TransformerException {
        String[] arr = new String[4];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("xsl/Repository2010to2016.xsl")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("FixRepository.xml")
                .getFile();
        String sourceDir = new File(arr[1]).getParent();
        arr[2] = String.format("%s/output.xml", System.getProperty("java.io.tmpdir"));
        // document function in XSLT expects a URI, not a file name (Saxon does not convert) 
        arr[3] = String.format("phrases-files=file:///%s/FIX.5.0SP2_EP216_en_phrases.xml", sourceDir.replace('\\', '/'));
        RepositoryXslTransformer.main(arr);
        File outFile = new File(arr[2]);
        Assert.assertTrue(outFile.exists());
    }
    
    @Test
    public void transformMultiProtocol() throws IOException, TransformerException {
        String[] arr = new String[4];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("xsl/Repository2010to2016.xsl")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("multiprotocol/FixRepository.xml")
                .getFile();
        String sourceDir = new File(arr[1]).getParent();
        arr[2] = String.format("%s/output.xml", System.getProperty("java.io.tmpdir"));
        // Add a phrases file as a URI for each version of FIX
        StringBuilder sb = new StringBuilder("phrases-files=");
        sb.append(String.format("file:///%s/FIX.4.0_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.4.1_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.4.2_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.4.3_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.4.4_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.5.0_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.5.0SP1_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIXT.1.1_en_phrases.xml,", sourceDir.replace('\\', '/')));
        sb.append(String.format("file:///%s/FIX.5.0SP2_en_phrases.xml", sourceDir.replace('\\', '/')));
         arr[3] = sb.toString();
        RepositoryXslTransformer.main(arr);
        File outFile = new File(arr[2]);
        Assert.assertTrue(outFile.exists());
    }
}
