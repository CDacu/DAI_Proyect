package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.xslt.XSLTController;

import java.net.Socket;

public class ServiceThreadXSLT implements Runnable{

    private final Socket socket;

    private XSLTController pages;

    public ServiceThreadXSLT(Socket socket, XSLTController pages) {
        this.socket = socket;
        this.pages = pages;
    }

    @Override
    public void run() {
        // TODO : Implementar GET, POST y DELETE
    }
}