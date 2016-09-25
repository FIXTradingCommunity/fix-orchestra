package com.fixtrading.orchestra.transformers;

import org.junit.Assert;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;

public class XslTransformerTest {
    @Test
    public void testTransformer() throws IOException, TransformerException {
        String[] arr = new String[3];
        arr[0] = Thread.currentThread().getContextClassLoader().getResource("xsl/Repository2010to2016.xsl")
                .getFile();
        arr[1] = Thread.currentThread().getContextClassLoader().getResource("FixRepository.xml")
                .getFile();
        arr[2] = System.getProperty("java.io.tmpdir").concat("/output.xml");
        RepositoryXslTransformer.main(arr);
        File outFile = new File(arr[2]);
        Assert.assertTrue(outFile.exists());
    }
}
