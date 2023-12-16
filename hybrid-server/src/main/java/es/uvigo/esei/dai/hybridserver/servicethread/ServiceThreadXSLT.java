package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;

import java.net.Socket;

public class ServiceThreadXSLT extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSLT(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XSLT);
    }

    protected void executeGETwithUUID() {

        // TODO : Por implementar (Es necesario dar algun formato ¿?, añadir info de xsd ¿?, algo distinto¿?)

        try {
            String pageContent = pages.get(request.getResourceParameters().get("uuid"), type);
            response.setStatus(HTTPResponseStatus.S200);
            contentBuilder.append(pageContent);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(),MIME.APPLICATION_XML.getMime());

        } catch (PageNotFoundException e) {
            response.setStatus(HTTPResponseStatus.S404);
            contentBuilder.append("<h2>Page not Found</h2>");

        } catch (DatabaseOfflineException e) {
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>Database is offline</h2>");

        } catch (RuntimeException e) {
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
        }
    }

    protected void executePOST() {
        String pageContent = request.getResourceParameters().get(type.getType().toLowerCase());
        String xsd = request.getResourceParameters().get("xsd");
        String uuidPage;

        if (pageContent == null || xsd == null) {
            response.setStatus(HTTPResponseStatus.S400);
            contentBuilder.append("<h2>Bad Request</h2>");
        } else {
            boolean valid = false;
            try {
                String xsdContent = pages.get("xsd", HTTPResourceName.XSD);

                // TODO : EL XSD DEBERIA VALIDARSE (QUE CORRESPONDA AL XSLT A AÑADIR) ¿?¿?

                valid = true;
            } catch (PageNotFoundException e) {
                response.setStatus(HTTPResponseStatus.S404);
                contentBuilder.append("<h2>Page not found</h2>");

            } catch (DatabaseOfflineException e) {
                response.setStatus(HTTPResponseStatus.S500);
                contentBuilder.append("<h2>DataBase is offline</h2>");
            }

            if (valid) {
                try {
                    uuidPage = pages.create(xsd, pageContent);
                    response.setStatus(HTTPResponseStatus.S200);
                    contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort())
                            .append("/").append(type.getType().toLowerCase()).append("?uuid=").append(uuidPage)
                            .append("\">").append(uuidPage).append("</a></li>");

                } catch (DatabaseOfflineException e) {
                    response.setStatus(HTTPResponseStatus.S500);
                    contentBuilder.append("<h2>Database is offline</h2>");

                } catch (RuntimeException edb) {
                    response.setStatus(HTTPResponseStatus.S500);
                    contentBuilder.append("<h2>Something went wrong with the database</h2>");
                }
            }
        }
    }
}