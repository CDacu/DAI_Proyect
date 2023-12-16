package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;

import java.net.Socket;
import java.util.List;

public class ServiceThreadXSD extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSD(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XSD);
    }

    protected void executeGETwithUUID(){
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

        } catch (RuntimeException e){
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
        }
    }

}