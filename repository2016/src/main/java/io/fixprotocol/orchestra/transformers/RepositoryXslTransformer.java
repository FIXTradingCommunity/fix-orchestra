package io.fixprotocol.orchestra.transformers;

import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Simple transformer application to convert 2010 repositories to 2016 version
 *
 * @author Uditha Wijerathna
 */
public class RepositoryXslTransformer {
    public static void main(String[] args) throws TransformerException, IOException {
        if (args.length < 4) {
            System.out.println(
                "Usage : $<application> [xsl_file_path] [input_xml_file_path] [output_file_path] [param=value]");
            return;
        }
        System.out.println("Received args : \n" +
                "xslFile = "+args[0]+"\n" +
                "inputXml = "+args[1]+"\n" +
                "outputXml = "+args[2]);
        for (int i = 3; i < args.length; i++) {
          System.out.format("Parameter : %s%n", args[i]);  
        }
        File xsltFile = new File(args[0]);
        File inputXml = new File(args[1]);

        Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXml);
        Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
        StringWriter sw = new StringWriter();

        Result result = new javax.xml.transform.stream.StreamResult(sw);

        TransformerFactory transFact = new TransformerFactoryImpl();
        Transformer trans = transFact.newTransformer(xsltSource);
        
        for (int i = 3; i < args.length; i++) {
          String[] parts = args[i].split("=");
          trans.setParameter(parts[0], parts[1]);
        }
        
        trans.transform(xmlSource, result);
        FileWriter output = new FileWriter(args[2]);
        output.write(sw.toString());
        output.close();
    }
}
