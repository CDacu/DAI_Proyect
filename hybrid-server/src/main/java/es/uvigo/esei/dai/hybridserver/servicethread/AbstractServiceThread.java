package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

/*
*
* TODO : Separar el codigo en clases por defecto, para poder realizar
*  pequenas implementaciones especificas en cada clase particular
*
*/

public abstract class AbstractServiceThread implements Runnable{

    protected final Socket socket;
    protected final PageController pages;

    public AbstractServiceThread(Socket socket, PageController pages) {
        this.socket = socket;
        this.pages = pages;
    }

    public void run() {
        try(this.socket){
            OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());
            InputStream input = socket.getInputStream();

            Reader reader = new BufferedReader(new InputStreamReader(input));

            HTTPRequest request = null;
            HTTPResponse response = new HTTPResponse();

            StringBuilder contentBuilder = new StringBuilder();

            response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());
            contentBuilder.append("<!DOCTYPE html>");
            contentBuilder.append("<html lang=\"en\">");
            contentBuilder.append("<head><meta charset=\"UTF-8\"/><title>Hybrid Server</title></head>");
            contentBuilder.append("<body>");
            contentBuilder.append("<h1>Hybrid Server</h1>");

            boolean valid = true;

            try  {
                request = new HTTPRequest(reader);

            } catch (HTTPParseException e) {
                valid = false;
                response.setStatus(HTTPResponseStatus.S400);
                contentBuilder.append("<h2>Bad HTTP request</h2>\n");
            }

            if(valid){
                String uuidPage = request.getResourceParameters().get("uuid");
                String pageContent;
                List<String> uuids;

                switch (request.getMethod()){
                    case GET:
                        if(uuidPage != null){
                            try {
                                pageContent = pages.get(uuidPage);
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
                        }else{
                            try{
                                uuids = pages.list();
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
                        break;

                    case POST:
                        pageContent = request.getResourceParameters().get("html");
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
                        break;

                    case DELETE:
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
                        break;

                    default:
                        try {
                            uuids = pages.list();
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

            // TODO : Cuando se coloca cada una de las cabeceras MIME ¿?¿?
                // APPLICATION_XML, FORM, TEXT_HTML

            response.putParameter("Content-Type", "text/html");
            contentBuilder.append("</body>\n");
            contentBuilder.append("</html>\n");
            response.setContent(String.valueOf(contentBuilder));
            response.print(outputWriter);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

}
