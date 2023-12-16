/**
 *  HybridServer
 *  Copyright (C) 2023 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class XMLConfigurationLoader {
  public Configuration load(Reader reader) throws Exception {

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = schemaFactory.newSchema(new File("configuration.xsd"));

    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setValidating(false);
    documentBuilderFactory.setNamespaceAware(true);
    documentBuilderFactory.setSchema(schema);

    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    documentBuilder.setErrorHandler(new DefaultHandler()); // TODO : En la resolucion de la Kata6 es SimpleErrorHandler()

    Document configuration = documentBuilder.parse(new InputSource(reader));

    List<ServerConfiguration> servers = new ArrayList<>();
    Element serverElement;

    NodeList serversNode = configuration.getElementsByTagName("servers").item(0).getChildNodes();
    for (int i = 0; i < serversNode.getLength(); i++) {
      Node serverNode = serversNode.item(i);
      if(serverNode.getNodeType() == Node.ELEMENT_NODE){
        serverElement = (Element) serverNode;
        servers.add(new ServerConfiguration(
                // TODO : Comprobar que existe
                serverElement.getAttribute("name"),
                serverElement.getAttribute("wsdl"),
                serverElement.getAttribute("namespace"),
                serverElement.getAttribute("service"),
                serverElement.getAttribute("httpAddress")
        ));
      }
    }

    return new Configuration(
            Integer.parseInt(configuration.getElementsByTagName("http").item(0).getFirstChild().getNodeValue()),
            Integer.parseInt(configuration.getElementsByTagName("numClients").item(0).getFirstChild().getNodeValue()),
            configuration.getElementsByTagName("webservice").item(0).getFirstChild().getNodeValue(),
            configuration.getElementsByTagName("user").item(0).getFirstChild().getNodeValue(),
            configuration.getElementsByTagName("password").item(0).getFirstChild().getNodeValue(),
            configuration.getElementsByTagName("url").item(0).getFirstChild().getNodeValue(),
            servers
    );
  }
}
