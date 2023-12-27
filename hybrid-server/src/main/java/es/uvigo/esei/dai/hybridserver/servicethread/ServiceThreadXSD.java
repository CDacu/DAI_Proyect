package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;

import java.net.Socket;

public class ServiceThreadXSD extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSD(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XSD);
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

    protected void executeGETwithUUID(){
        try {
            String pageContent = pages.get(request.getResourceParameters().get("uuid"), type);
            response.setStatus(HTTPResponseStatus.S200);
            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(),MIME.APPLICATION_XML.getMime());
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

        } catch (RuntimeException e){
            response.setStatus(HTTPResponseStatus.S500);
            openHTMLHeader();
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
            closeHTMLHeader();
        }
    }

}