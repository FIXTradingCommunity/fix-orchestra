# Orchestra Documentation Generator

This utility generates documentation for an Orchestra file that can be view in any web browser. The output of the generator may be used locally or from a web server.

## Useage

To run the documentation generator, enter this command line:

```
java io.fixprotocol.orchestra.docgen.DocGenerator <filename> 
```

Open `index.html` in a browser to view the documentation. There is no other run-time requirement.

## File Preparation

The input file must conform to the Orchestra XML Schema. Since the metadata section is viewable in generated documentation, be sure to populate it in the Orchestra file with important identification. In particular, the "title" element should be populated; it is presented by the generator as the title of the documentation.