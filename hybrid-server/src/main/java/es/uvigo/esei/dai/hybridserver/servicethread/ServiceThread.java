package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.http.*;

import java.io.*;
import java.net.Socket;

public class ServiceThread implements Runnable {

	private final Socket socket;
	Configuration configuration;

	public ServiceThread(Socket socket, Configuration configuration) {
		this.socket = socket;
		this.configuration = configuration;
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
			response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());

			try  {
				request = new HTTPRequest(reader);

			} catch (HTTPParseException e) {
				valid = false;
				response.setStatus(HTTPResponseStatus.S400);
                response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                contentBuilder.append("<html>\n" + "<body>\n" + "<h1>Hybrid Server</h1>\n");
				contentBuilder.append("<h2>Bad HTTP request</h2>\n");
                contentBuilder.append("</body>\n" + "</html>");
			}

			if(valid){
				if( request.getResourceName().isBlank() ) {
					response.setStatus(HTTPResponseStatus.S200);
                    response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                    contentBuilder.append("<html>\n" + "<body>\n" + "<h1>Hybrid Server</h1>\n");
                    contentBuilder.append("<h2>Author</h2><a>Carlos Dacunha Gonzalez</a>\n");
                    contentBuilder.append("</body>\n" + "</html>");

				}else{
					AbstractServiceThread serviceThread;
					switch (request.getResourceName()){
						case "html":
							serviceThread = new ServiceThreadHMTL(socket, configuration, response, request, contentBuilder);
							serviceThread.run();
							break;

						case "xml":
							serviceThread = new ServiceThreadXML(socket, configuration, response, request, contentBuilder);
							serviceThread.run();
							break;

						case "xsd":
							serviceThread = new ServiceThreadXSD(socket, configuration, response, request, contentBuilder);
							serviceThread.run();
							break;

						case "xslt":
							serviceThread = new ServiceThreadXSLT(socket, configuration, response, request, contentBuilder);
							serviceThread.run();
							break;

						default:
							response.setStatus(HTTPResponseStatus.S400);
                            response.putParameter(HTTPHeaders.CONTENT_TYPE.getHeader(), MIME.TEXT_HTML.getMime());
                            contentBuilder.append("<html>\n" + "<body>\n" + "<h1>Hybrid Server</h1>\n");
                            contentBuilder.append("<h2>Resource name not Valid</h2>\n");
                            contentBuilder.append("</body>\n" + "</html>");

                    }
				}
			}

			response.setContent(String.valueOf(contentBuilder));
			response.print(outputWriter);

		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(e.getMessage());
		}
	}
}
