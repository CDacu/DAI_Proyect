package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;
import java.net.Socket;
import java.util.List;

public abstract class AbstractServiceThread implements Runnable{

    protected final Socket socket;
    protected final PageController pages;
    protected final HTTPResponse response;
    protected final HTTPRequest request;
    protected final StringBuilder contentBuilder;

    protected AbstractServiceThread(Socket socket, PageController pages, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        this.socket = socket;
        this.pages = pages;
        this.response = response;
        this.request = request;
        this.contentBuilder = contentBuilder;
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
            String pageContent = pages.get(request.getResourceParameters().get("uuid"));
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
                            .append("/html?uuid=").append(uuid).append("\">").append(uuid).append("</a></li>");
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
        String pageContent = request.getResourceParameters().get("html");
        String uuidPage;

        if(pageContent == null){
            response.setStatus(HTTPResponseStatus.S400);
            contentBuilder.append("<h2>Bad Request</h2>");
        }else{
            try {
                uuidPage = pages.create(pageContent);
                response.setStatus(HTTPResponseStatus.S200);
                contentBuilder.append("<a href=\"http://localhost:").append(this.socket.getLocalPort())
                        .append("/html?uuid=").append(uuidPage).append("\"> - ").append(uuidPage).append("</a>");

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
        try {
            List<String> uuids = pages.list();
            response.setStatus(HTTPResponseStatus.S200);
            contentBuilder.append("<h2>Existing uuids</h2><ul>");

            for (String uuid : uuids) {
                contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort())
                        .append("/html?uuid=").append(uuid).append("\"> - ").append(uuid).append("</a></li>");
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
