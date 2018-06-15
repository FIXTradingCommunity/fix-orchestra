package io.fixprotocol.orchestra.transformers;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import net.sf.saxon.TransformerFactoryImpl;

/**
 * Filter an Orchestra repository by category
 * <p>
 * The parameter controls either inclusion or exclusion of a category. CodeSets,
 * Fields, Components, and Messages are selected.
 * <p>
 * Examples:
 * {@code +Session} means only include elements in the Session category
 * {@code -Session} means exclude the Session category; output every other category
 *
 */
public class SelectCategoryTransformer {
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
        File outputXml = new File(args[2]);
        outputXml.getParentFile().mkdirs();

        Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXml);
        Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
        Result result = new javax.xml.transform.stream.StreamResult(outputXml);

        TransformerFactory transFact = new TransformerFactoryImpl();
        Transformer trans = transFact.newTransformer(xsltSource);
        
        for (int i = 3; i < args.length; i++) {
          String[] parts = args[i].split("=");
          trans.setParameter(parts[0], parts[1]);
        }
        
        trans.transform(xmlSource, result);
    }
}
