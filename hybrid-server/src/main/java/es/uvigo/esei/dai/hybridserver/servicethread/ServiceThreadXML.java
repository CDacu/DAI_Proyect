package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.exception.SimpleErrorHandler;
import es.uvigo.esei.dai.hybridserver.http.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;

public class ServiceThreadXML extends AbstractServiceThread implements Runnable{
    public ServiceThreadXML(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XML);
    }

    public void run() {
        switch (request.getMethod()) {
            case GET:
                if (request.getResourceParameters().get("uuid") != null) {
                    executeGETwithUUID();
                } else {
                    openHTMLHeader();
                    executeGETwithoutUUID();
                    closeHTMLHeader();
                }
                break;

            case POST:
                executePOST();
                break;

            case DELETE:
                openHTMLHeader();
                executeDELETE();
                closeHTMLHeader();
                break;

            default:
                openHTMLHeader();
                executeDefault();
                closeHTMLHeader();
        }
    }

    protected void executeGETwithUUID() {
        try {
            String pageContent = pages.get(request.getResourceParameters().get("uuid"), type);

            if (request.getResourceParameters().get("xslt") != null) {

                String xsltContent = pages.get(request.getResourceParameters().get("xslt"), HTTPResourceName.XSLT);
                String xsdUUID = pages.getXSDUUIDwithXSLT(request.getResourceParameters().get("xslt"), HTTPResourceName.XSLT);
                String xsdContent = pages.get(xsdUUID, HTTPResourceName.XSD);

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(xsdContent)));

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setValidating(false);
                documentBuilderFactory.setNamespaceAware(true);
                documentBuilderFactory.setSchema(schema);

                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                documentBuilder.setErrorHandler(new SimpleErrorHandler());
                documentBuilder.parse(new InputSource(new StringReader(pageContent)));

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(xsltContent)));

                StringWriter stringWriter = new StringWriter();
                transformer.transform(new StreamSource(new StringReader(pageContent)), new StreamResult(stringWriter));

                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                contentBuilder.append(stringWriter);

            } else {
                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
                contentBuilder.append(pageContent);
            }

        } catch (PageNotFoundException e) {
            response.setStatus(HTTPResponseStatus.S404);
            openHTMLHeader();
            contentBuilder.append("<h2>Page not Found</h2>");
            closeHTMLHeader();

        } catch (DatabaseOfflineException e) {
            response.setStatus(HTTPResponseStatus.S500);
            openHTMLHeader();
            contentBuilder.append("<h2>Database is offline</h2>");
            closeHTMLHeader();

        } catch (RuntimeException e) {
            response.setStatus(HTTPResponseStatus.S500);
            openHTMLHeader();
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
            closeHTMLHeader();

        } catch (ParserConfigurationException | IOException | SAXException e) {
            response.setStatus(HTTPResponseStatus.S400);
            openHTMLHeader();
            contentBuilder.append("<h2>Something went wrong during the xml validation</h2>");
            closeHTMLHeader();

        } catch (TransformerException e) {
            response.setStatus(HTTPResponseStatus.S400);
            openHTMLHeader();
            contentBuilder.append("<h2>Something went wrong during the xml transformation</h2>");
            closeHTMLHeader();
        }
    }
}