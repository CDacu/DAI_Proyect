package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;

import java.net.Socket;

public class ServiceThreadXML extends AbstractServiceThread implements Runnable{
    public ServiceThreadXML(Socket socket, Configuration configuration, HTTPResponse response, HTTPRequest request, StringBuilder contentBuilder) {
        super(socket, configuration, response, request, contentBuilder, HTTPResourceName.XML);
    }

    // TODO : XML NECESITA UN NUEVO GET
    // En las solicitudes GET, el cliente podrá incluir el parámetro xslt con el identificador de una plantilla XSLT
    // alojada en el sistema. En tal caso, el sistema deberá recuperar dicha plantilla y el esquema asociados
    // (ver siguiente punto), validar el XML con el esquema y, en el caso de que se supere la validación,
    // devolver el resultado de transformar el documento XML con la plantilla XSLT.

}