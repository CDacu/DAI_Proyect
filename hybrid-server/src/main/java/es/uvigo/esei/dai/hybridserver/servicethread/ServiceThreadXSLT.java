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
            response.setStatus(HTTPResponseStatus.S200);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.APPLICATION_XML.getMime());
            contentBuilder.append(pageContent);

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
        }
    }

    protected void executePOST() {
        String pageContent = request.getResourceParameters().get("xslt");
        String xsd = request.getResourceParameters().get("xsd");
        String uuidPage;

        if (pageContent == null || xsd == null) {
            response.setStatus(HTTPResponseStatus.S400);
            openHTMLHeader();
            contentBuilder.append("<h2>Bad Request</h2>");
            closeHTMLHeader();
        } else {
            boolean valid = false;
            try {
                String xsdContent = pages.getXSDUUIDwithXSD(xsd, HTTPResourceName.XSLT);
                valid = true;
            } catch (PageNotFoundException e) {
                response.setStatus(HTTPResponseStatus.S404);
                openHTMLHeader();
                contentBuilder.append("<h2>Page not found</h2>");
                closeHTMLHeader();

            } catch (DatabaseOfflineException e) {
                response.setStatus(HTTPResponseStatus.S500);
                openHTMLHeader();
                contentBuilder.append("<h2>DataBase is offline</h2>");
                closeHTMLHeader();
            }

            if (valid) {
                try {
                    uuidPage = pages.create(xsd, pageContent);
                    response.setStatus(HTTPResponseStatus.S200);
                    String hyperlink = "<a href=\"" + type.getType().toLowerCase() + "?uuid=" + uuidPage + "\">" + uuidPage + "</a>";
                    contentBuilder.append(hyperlink);

                } catch (DatabaseOfflineException e) {
                    response.setStatus(HTTPResponseStatus.S500);
                    openHTMLHeader();
                    contentBuilder.append("<h2>Database is offline</h2>");
                    closeHTMLHeader();

                } catch (RuntimeException edb) {
                    response.setStatus(HTTPResponseStatus.S500);
                    openHTMLHeader();
                    contentBuilder.append("<h2>Something went wrong with the database</h2>");
                    closeHTMLHeader();
                }
            }
        }
    }
}