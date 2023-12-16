package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.dao.*;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;

import java.net.Socket;
import java.util.List;

public abstract class AbstractServiceThread implements Runnable{

    protected final Socket socket;
    protected final ServerController pages;
    protected final HTTPResponse response;
    protected final HTTPRequest request;
    protected final StringBuilder contentBuilder;
    protected final HTTPResourceName type;

    protected AbstractServiceThread(Socket socket, Configuration configuration, HTTPResponse response,
                                    HTTPRequest request, StringBuilder contentBuilder, HTTPResourceName type) {
        this.socket = socket;

        AbstractDaoDB daoDB = new HTMLDaoDB(configuration);
        // Only created to avoid an error (new ServerController without daoDB initiated)

        switch (type) {
            case HTML:
                daoDB = new HTMLDaoDB(configuration);
                break;
            case XML:
                daoDB = new XMLDaoDB(configuration);
                break;
            case XSD:
                daoDB = new XSDDaoDB(configuration);
                break;
            case XSLT:
                daoDB = new XSLTDaoDB(configuration);
                break;
        }

        this.pages = new ServerController(daoDB, configuration);
        this.response = response;
        this.request = request;
        this.contentBuilder = contentBuilder;
        this.type = type;
    }

    public void run() {
        switch (request.getMethod()){
            case GET:
                if(request.getResourceParameters().get("uuid") != null){
                    executeGETwithUUID();
                }else {
                    executeGETwithoutUUID();
                }
                break;
            case POST:
                    executePOST();
                    break;

            case DELETE:
                executeDELETE();
                break;

            default:
                executeDefault();
        }
    }

    protected void executeGETwithUUID(){
        try {
            String pageContent = pages.get(request.getResourceParameters().get("uuid"), type);
            response.setStatus(HTTPResponseStatus.S200);
            contentBuilder.append(pageContent);

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

    protected void executeGETwithoutUUID(){
            try{
                List<String> uuids = pages.list();
                contentBuilder.append("<h2>Existing uuids</h2><ul>");
                for (String uuid : uuids) {
                    contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort())
                            .append("/").append(type.getType().toLowerCase()).append("?uuid=").append(uuid)
                            .append("\">").append(uuid).append("</a></li>");
                }
                contentBuilder.append("</ul>");
                response.setStatus(HTTPResponseStatus.S200);

            }catch (DatabaseOfflineException e) {
                response.setStatus(HTTPResponseStatus.S500);
                contentBuilder.append("<h2>DataBase is offline</h2>");

            }catch( RuntimeException e) {
                response.setStatus(HTTPResponseStatus.S500);
                contentBuilder.append("<h2>Something went wrong with the database</h2>");
            }
    }

    protected void executePOST(){
        String pageContent = request.getResourceParameters().get(type.getType().toLowerCase());
        String uuidPage;

        if(pageContent == null){
            response.setStatus(HTTPResponseStatus.S400);
            contentBuilder.append("<h2>Bad Request</h2>");
        }else{
            try {
                uuidPage = pages.create(pageContent);
                response.setStatus(HTTPResponseStatus.S200);
                contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort())
                        .append("/").append(type.getType().toLowerCase()).append("?uuid=").append(uuidPage)
                        .append("\">").append(uuidPage).append("</a></li>");

            }catch( DatabaseOfflineException e) {
                response.setStatus(HTTPResponseStatus.S500);
                contentBuilder.append("<h2>Database is offline</h2>");

            }catch( RuntimeException edb) {
                response.setStatus(HTTPResponseStatus.S500);
                contentBuilder.append("<h2>Something went wrong with the database</h2>");
            }
        }
    }

    protected void executeDELETE(){
        String uuidPage = request.getResourceParameters().get("uuid");

        try {
            if( pages.list().contains(uuidPage)) {
                pages.delete(uuidPage);
                response.setStatus(HTTPResponseStatus.S200);
                contentBuilder.append("<h2>Page with uuid: ").append(uuidPage).append(" deleted successfully</h2>");

            }else {
                response.setStatus(HTTPResponseStatus.S404);
                contentBuilder.append("<h2>Page not found</h2>");
            }

        }catch( PageNotFoundException e) {
            response.setStatus(HTTPResponseStatus.S404);
            contentBuilder.append("<h2>Page not found</h2>");

        }catch( DatabaseOfflineException e) {
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>DataBase is offline</h2>");

        }catch( RuntimeException edb ) {
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
        }
    }

    protected void executeDefault(){

        // TODO : Deberia ser cambiado a un 501 Not Implemented ¿?

        try {
            List<String> uuids = pages.list();
            response.setStatus(HTTPResponseStatus.S200);
            contentBuilder.append("<h2>Existing uuids</h2><ul>");

            for (String uuid : uuids) {
                contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort())
                        .append("/").append(type.getType().toLowerCase()).append("?uuid=").append(uuid)
                        .append("\">").append(uuid).append("</a></li>");
            }
            contentBuilder.append("</ul>");

        }catch( DatabaseOfflineException e){
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>DataBase is offline</h2>");

        }catch( RuntimeException edb ) {
            response.setStatus(HTTPResponseStatus.S500);
            contentBuilder.append("<h2>Something went wrong with the database</h2>");
        }
    }
}
