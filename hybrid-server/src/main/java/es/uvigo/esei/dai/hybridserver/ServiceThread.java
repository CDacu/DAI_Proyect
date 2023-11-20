package es.uvigo.esei.dai.hybridserver;

import java.io.*;
import java.net.Socket;
import java.util.List;

import es.uvigo.esei.dai.hybridserver.http.*;

public class ServiceThread implements Runnable {

	private final Socket socket;
	private final HTMLDao pages;
	private HTMLController controller;

	public ServiceThread(Socket socket, HTMLDao pages) {
		this.socket = socket;
		this.pages = pages;
		this.controller = new HTMLController(pages);
	}

	public void run(){
		try (this.socket) {
			
			String pageContent;
			List<String> uuids;

			OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());
			InputStream input = socket.getInputStream();
			
			Reader reader = new BufferedReader(new InputStreamReader(input));

			this.controller = new HTMLController(pages);
			HTTPRequest request = null;
			HTTPResponse response = new HTTPResponse();

			StringBuilder contentBuilder = new StringBuilder();

			response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());
			contentBuilder.append("<!DOCTYPE html>");
			contentBuilder.append("<html lang=\"en\">");
			contentBuilder.append("<head><meta charset=\"UTF-8\"/><title>Hybrid Server</title></head>");
			contentBuilder.append("<body>");
			contentBuilder.append("<h1>Hybrid Server</h1>");
			
			try  {
				request = new HTTPRequest(reader);
				
			} catch (HTTPParseException e) {
				response.setStatus(HTTPResponseStatus.S400);
				contentBuilder.append("<h2>Bad HTTP request</h2>\n");
				response.putParameter("Content-Type", "text/html");
				contentBuilder.append("</body>");
				contentBuilder.append("</html>");
				response.setContent(String.valueOf(contentBuilder));
				response.print(outputWriter);
			}
			
			//Si no hay campo uuid sera null
			String uuidPage = request.getResourceParameters().get("uuid");
			
			//Si el resource name es vacio se enseña la welcome page
			if( request.getResourceName().isBlank() ) {
				response.setStatus(HTTPResponseStatus.S200);
				contentBuilder.append("<h2>Autor</h2><a>Carlos Dacunha Gonzalez</a>");
				response.putParameter("Content-Type", "text/html");
				contentBuilder.append("</body>");
				contentBuilder.append("</html>");
				response.setContent(String.valueOf(contentBuilder));
				response.print(outputWriter);
			
			//Si el resourcename no es vacio se mira el metodo, si es un get y la uuid no es nula (linea 61) hay que intentar devolver la pagina
			//con dicho uuid.	
			}else if( request.getMethod().equals(HTTPRequestMethod.GET) && uuidPage != null) {
				try {
					if( !(request.getResourceName().equalsIgnoreCase("html"))){
						response.setStatus(HTTPResponseStatus.S400);
						contentBuilder.append("<h2>Resource name not Valid</h2>");

					}else {						
						response.setStatus(HTTPResponseStatus.S200);
						pageContent = controller.get(request.getResourceParameters().get("uuid"));
						contentBuilder.append(pageContent);
					}
						
				} catch (PageNotFoundException e) {
					response.setStatus(HTTPResponseStatus.S404);
					contentBuilder.append("<h2>Page not Found</h2>");
					
				}catch( RuntimeException edb ){
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>Something went wrong with the database</h2>");

				} catch (DatabaseOfflineException e) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>Database is offline</h2>");

				}finally {
					response.putParameter("Content-Type", "text/html");
					contentBuilder.append("</body>\n");
					contentBuilder.append("</html>\n");
					response.setContent(String.valueOf(contentBuilder));
					response.print(outputWriter);
				}
				
			//Si la uuid es vacia (uuidPage == null) hay que mostrar la lista de uuids existentes en el almacenamiento del servidor
			}else if( request.getMethod().equals(HTTPRequestMethod.GET) && uuidPage == null ) {
				try{
					uuids = controller.list();
					contentBuilder.append("<h2>Existing uuids</h2><ul>");
                    for (String uuid : uuids) {
						contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort()).append("/html?uuid=").append(uuid).append("\">").append(uuid).append("</a></li>");
					}
					contentBuilder.append("</ul>");
					response.setStatus(HTTPResponseStatus.S200);
					
				//Las excepciones las puede lanzar el metodo controller.list() (linea 110)
				}catch( RuntimeException e) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>Something went wrong with the database</h2>");

				} catch (DatabaseOfflineException e) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>DataBase is offline</h2>");

				}finally {
					response.putParameter("Content-Type", "text/html");
					contentBuilder.append("</body>");
					contentBuilder.append("</html>");
					response.setContent(String.valueOf(contentBuilder));
					response.print(outputWriter);
				}
					
			//Si el metodo es post hay que agregar la pagina al servidor	
			}else if ( request.getMethod().equals(HTTPRequestMethod.POST ) ) {
				
				pageContent = request.getResourceParameters().get("html");
				
				//Si el valor del atributo html es nulo (linea 134) se trata de una peticion erronea
				if( pageContent == null ) {
					response.setStatus(HTTPResponseStatus.S400);
					contentBuilder.append("<h2>Bad Request</h2>");
					response.putParameter("Content-Type", "text/html");
					contentBuilder.append("</body>");
					contentBuilder.append("</html>");
					response.setContent(String.valueOf(contentBuilder));
					response.print(outputWriter);

				//De lo contrario se prosigue con normalidad
				}else {
					try {
						uuidPage = controller.add(pageContent);
						response.setStatus(HTTPResponseStatus.S200);
						contentBuilder.append("<a href=\"http://localhost:").append(this.socket.getLocalPort()).append("/html?uuid=").append(uuidPage).append("\"> - ").append(uuidPage).append("</a>");
						
				//Nuevamente el metodo del controller es el que puede lanzar excepciones (linea 146)
					}catch( RuntimeException edb) {
						response.setStatus(HTTPResponseStatus.S500);
						contentBuilder.append("<h2>Something went wrong with the database</h2>");

					}catch( DatabaseOfflineException e) {
						response.setStatus(HTTPResponseStatus.S500);
						contentBuilder.append("<h2>Database is offline</h2>");

					}finally {
						response.putParameter("Content-Type", "text/html");
						contentBuilder.append("</body>");
						contentBuilder.append("</html>");
						response.setContent(String.valueOf(contentBuilder));
						response.print(outputWriter);
					}
					
				}
				
				//Si el metodo de la peticion es DELETE se intenta eliminar la pagina del almacenamiento
			}else if ( request.getMethod().equals(HTTPRequestMethod.DELETE ) ) {

				try {
					if( controller.list().contains(uuidPage)) {
						controller.delete(uuidPage);
						response.setStatus(HTTPResponseStatus.S200);
						contentBuilder.append("<h2>Page with uuid: ").append(uuidPage).append(" deleted successfully</h2>");

					}else {
						response.setStatus(HTTPResponseStatus.S404);
						contentBuilder.append("<h2>Page not found</h2>");
					}
					
				//Tanto controller.list() como controller.delete() pueden lanzar el DatabaseOfflineException, delete puede lanzar a mayores
				//la excepcion PageNotFound para indicar que no encontro la pagina a eliminar
				}catch( RuntimeException edb ) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>Something went wrong with the database</h2>");

				}catch( DatabaseOfflineException e) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>DataBase is offline</h2>");

				}catch( PageNotFoundException e) {
					response.setStatus(HTTPResponseStatus.S404);
					contentBuilder.append("<h2>Page not found</h2>");

				}finally {
					response.putParameter("Content-Type", "text/html");
					contentBuilder.append("</body>");
					contentBuilder.append("</html>");
					response.setContent(String.valueOf(contentBuilder));
					response.print(outputWriter);
				}
				
			}else {
				try {
					uuids = controller.list();
					response.setStatus(HTTPResponseStatus.S200);
					contentBuilder.append("<h2>Existing uuids</h2><ul>");

                    for (String uuid : uuids) {
						contentBuilder.append("<li><a href=\"http://localhost:").append(this.socket.getLocalPort()).append("/html?uuid=").append(uuid).append("\"> - ").append(uuid).append("</a></li>");
                    }
					contentBuilder.append("</ul>");

				}catch( RuntimeException edb ) {
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>Something went wrong with the database</h2>");

				}catch( DatabaseOfflineException e){
					response.setStatus(HTTPResponseStatus.S500);
					contentBuilder.append("<h2>DataBase is offline</h2>");

				}finally {
					response.putParameter("Content-Type", "text/html");
					contentBuilder.append("</body>");
					contentBuilder.append("</html>");
					response.setContent(String.valueOf(contentBuilder));
					response.print(outputWriter);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
