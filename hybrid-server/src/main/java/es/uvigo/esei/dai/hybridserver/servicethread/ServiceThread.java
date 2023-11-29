package es.uvigo.esei.dai.hybridserver.servicethread;

import java.io.*;
import java.net.Socket;
import es.uvigo.esei.dai.hybridserver.dao.PageController;
import es.uvigo.esei.dai.hybridserver.dao.xslt.XSLTController;
import es.uvigo.esei.dai.hybridserver.http.*;

public class ServiceThread implements Runnable {

	private final Socket socket;
	private final PageController pagesHTML, pagesXML, pagesXSD;
	private final XSLTController pagesXSLT;

	public ServiceThread(Socket socket, PageController pagesHTML, PageController pagesXML, PageController pagesXSD, XSLTController pagesXSLT) {
		this.socket = socket;
		this.pagesHTML = pagesHTML;
		this.pagesXML = pagesXML;
		this.pagesXSD = pagesXSD;
		this.pagesXSLT = pagesXSLT;
	}

	public void run(){
		try(this.socket){
			OutputStreamWriter outputWriter = new OutputStreamWriter(socket.getOutputStream());
			InputStream input = socket.getInputStream();

			Reader reader = new BufferedReader(new InputStreamReader(input));

			HTTPRequest request = null;
			HTTPResponse response = new HTTPResponse();

			StringBuilder contentBuilder = new StringBuilder();

			boolean valid = true;

			response.setVersion(HTTPHeaders.HTTP_1_1.getHeader());
			contentBuilder.append("<!DOCTYPE html>");
			contentBuilder.append("<html lang=\"en\">");
			contentBuilder.append("<head><meta charset=\"UTF-8\"/><title>Hybrid Server</title></head>");
			contentBuilder.append("<body>");
			contentBuilder.append("<h1>Hybrid Server</h1>");

			try  {
				request = new HTTPRequest(reader);

			} catch (HTTPParseException e) {
				valid = false;
				response.setStatus(HTTPResponseStatus.S400);
				contentBuilder.append("<h2>Bad HTTP request</h2>\n");
			}

			if(valid){
				if( request.getResourceName().isBlank() ) {
					response.setStatus(HTTPResponseStatus.S200);
					contentBuilder.append("<h2>Author</h2><a>Carlos Dacunha Gonzalez</a>");

				}else{
					switch (request.getResourceName()){
						case "html":
							ServiceThreadHMTL serviceThreadHMTL = new ServiceThreadHMTL(socket, pagesHTML);
							serviceThreadHMTL.run();
							break;

						case "xml":
							ServiceThreadXML serviceThreadXML = new ServiceThreadXML(socket, pagesXML);
							serviceThreadXML.run();
							break;

						case "xsd":
							ServiceThreadXSD serviceThreadXSD = new ServiceThreadXSD(socket, pagesXSD);
							serviceThreadXSD.run();
							break;

						case "xslt":
							ServiceThreadXSLT serviceThreadXSLT = new ServiceThreadXSLT(socket, pagesXSLT);
							serviceThreadXSLT.run();
							break;

						default:
							response.setStatus(HTTPResponseStatus.S400);
							contentBuilder.append("<h2>Resource name not Valid</h2>");
					}
				}
			}
			response.putParameter("Content-Type", "text/html");
			contentBuilder.append("</body>");
			contentBuilder.append("</html>");
			response.setContent(String.valueOf(contentBuilder));
			response.print(outputWriter);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
