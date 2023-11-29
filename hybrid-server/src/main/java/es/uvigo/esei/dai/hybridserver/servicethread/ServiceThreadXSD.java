package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import java.net.Socket;

public class ServiceThreadXSD extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSD(Socket socket, PageController pages, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, pages, response, request, contentBuilder);
    }
}