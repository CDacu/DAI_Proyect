package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.UUID;

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

            System.out.println("Checkpoint 1");

            if (request.getResourceParameters().get("xslt") != null) {

                System.out.println(request.getResourceParameters().get("xslt"));

                String xsltContent = pages.get(request.getResourceParameters().get("xslt"), HTTPResourceName.XSLT);
                String xsdUUID = pages.getXSDUUID(request.getResourceParameters().get("xslt"), HTTPResourceName.XSLT);
                // TODO: No esta encontrando el uuid, cuando este deberia estar
                String xsdContent = pages.get(xsdUUID, HTTPResourceName.XSD);

                System.out.println("Checkpoint 2");

                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new StreamSource(new StringReader(xsdContent)));

                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                documentBuilderFactory.setValidating(false);
                documentBuilderFactory.setNamespaceAware(true);
                documentBuilderFactory.setSchema(schema);

                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                documentBuilder.setErrorHandler(new DefaultHandler());
                documentBuilder.parse(new InputSource(new StringReader(pageContent)));

                System.out.println("Checkpoint 3");

                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer(new StreamSource(new StringReader(xsltContent)));
                StringWriter stringWriter = new StringWriter();

                transformer.transform(new StreamSource(new StringReader(pageContent)), new StreamResult(stringWriter));

                response.setStatus(HTTPResponseStatus.S200);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
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