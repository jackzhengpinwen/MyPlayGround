package com.zpw.myplayground.injkit.internal;

import org.w3c.dom.Document;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

class Util {
  static Document createEmptyDocument() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    return documentBuilder.newDocument();
  }

  static void assertPathIsDirectory(File path) {
    if (!path.isDirectory()) {
      throw new IllegalArgumentException(String.format("'%s' is no directory", path));
    }
  }
}
