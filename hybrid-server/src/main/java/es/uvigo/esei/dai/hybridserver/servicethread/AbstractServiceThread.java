package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.dao.*;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;

import java.net.Socket;
import java.util.List;

public abstract class AbstractServiceThread implements Runnable{

    protected final Socket socket;
    protected final DaoDBServerController pages;
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

        this.pages = new DaoDBServerController(daoDB, configuration);
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

    protected void executeGETwithoutUUID(){
            try{
                List<String> uuids = pages.list(type);
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
            openHTMLHeader();
            contentBuilder.append("<h2>Bad Request</h2>");
            closeHTMLHeader();
        }else{
            try {
                uuidPage = pages.create(pageContent, type);
                response.setStatus(HTTPResponseStatus.S200);
                String hyperlink = "<a href=\"" + type.getType().toLowerCase() + "?uuid=" + uuidPage + "\">" + uuidPage + "</a>";
                contentBuilder.append(hyperlink);

            }catch( DatabaseOfflineException e) {
                response.setStatus(HTTPResponseStatus.S500);
                openHTMLHeader();
                contentBuilder.append("<h2>Database is offline</h2>");
                closeHTMLHeader();

            }catch( RuntimeException edb) {
                response.setStatus(HTTPResponseStatus.S500);
                openHTMLHeader();
                contentBuilder.append("<h2>Something went wrong with the database</h2>");
                closeHTMLHeader();
            }
        }
    }

    protected void executeDELETE(){
        String uuidPage = request.getResourceParameters().get("uuid");

        try {
            if( pages.list(type).contains(uuidPage)) {
                pages.delete(uuidPage, type);
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
        response.setStatus(HTTPResponseStatus.S501);
        contentBuilder.append("<h2>Not Implemented</h2>");
    }

    protected void openHTMLHeader() {
        response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
        contentBuilder.append("<html><body><h1>Hybrid Server</h1>");
    }

    protected void closeHTMLHeader() {
        contentBuilder.append("</body></html>");
    }
}
