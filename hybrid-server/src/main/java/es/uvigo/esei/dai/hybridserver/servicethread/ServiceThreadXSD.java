package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;

import java.net.Socket;

public class ServiceThreadXSD extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSD(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XSD);
    }

}