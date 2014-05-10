package org.saintandreas;

import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class XmlFu {
  private static final JsonNodeFactory JSON_FACTORY = JsonNodeFactory.instance;
  private static final SAXParserFactory SAX_FACTORY = SAXParserFactory.newInstance();

  public static void main(String argv[]) throws ParserConfigurationException, SAXException, IOException {
    SAXParser saxParser = SAX_FACTORY.newSAXParser();

    DefaultHandler handler = new DefaultHandler() {
      int depth = 0;
      Stack<ObjectNode> nodes = new Stack<ObjectNode>();
      ObjectNode node = null;
      int topCount = 0;
      final int startDepth = 1;
      boolean print = false;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
          if (depth < startDepth) {
            return;
          }
          ObjectNode newNode = JSON_FACTORY.objectNode();
          newNode.put("type", qName);
          for (int i = 0; i < attributes.getLength(); ++i) {
            if (attributes.getValue(i).equals("aeroway")) {
              print = true;
            }
            newNode.put(attributes.getLocalName(i), attributes.getValue(i));
          }
          if (startDepth < depth) {
            node.put(qName, newNode);
            nodes.push(node);
          } else {
            assert(null == node);
          }
          node = newNode;
        } finally {
          ++depth;
        }
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
        --depth;
        if (depth > startDepth) {
          node = nodes.pop();
        } else if (depth == startDepth){
          if (print) {
            System.out.println(node.toString());
          }
          print = false;
          node = null;
        }
      }

      @Override
      public void characters(char ch[], int start, int length) throws SAXException {
      }

    };
    saxParser.parse("f:/Downloads/Gis/seattle.osm", handler);
  }

}
