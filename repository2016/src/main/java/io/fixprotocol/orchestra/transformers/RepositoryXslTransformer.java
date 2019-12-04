package io.fixprotocol.orchestra.transformers;

import java.io.File;
import java.util.Arrays;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import net.sf.saxon.TransformerFactoryImpl;

/**
 * Invokes XSL Transform
 * May be used to convert FIX Repository 2010 Edition unified repositories to Orchestra schema by
 * invoking script unified2orchestra.xslt
 *
 * @author Uditha Wijerathna
 */
public class RepositoryXslTransformer {
    public static void main(String[] args) throws TransformerException {
        if (args.length < 3) {
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
        RepositoryXslTransformer transformer = new RepositoryXslTransformer();
        transformer.transform(args);
    }

    public void transform(String[] args)
        throws TransformerException {
      File xsltFile = new File(args[0]);
      File inputXml = new File(args[1]);
      File outputXml = new File(args[2]);
      String[] parameters = Arrays.copyOfRange(args, 3, args.length);     
      transform(xsltFile, inputXml, outputXml, parameters);
    }

    public void transform(File xsltFile, File inputXml, File outputXml, String[] parameters)
        throws TransformerException {
      outputXml.getParentFile().mkdirs();

      Source xmlSource = new javax.xml.transform.stream.StreamSource(inputXml);
      Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
      Result result = new javax.xml.transform.stream.StreamResult(outputXml);

      TransformerFactory transFact = new TransformerFactoryImpl();
      Transformer trans = transFact.newTransformer(xsltSource);
      
      for (int i = 0; i < parameters.length; i++) {
        String[] parts = parameters[i].split("=");
        trans.setParameter(parts[0], parts[1]);
      }
       
      trans.transform(xmlSource, result);
    }
}
