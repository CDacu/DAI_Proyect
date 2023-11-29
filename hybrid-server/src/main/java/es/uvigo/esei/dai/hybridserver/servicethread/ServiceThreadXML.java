package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import java.net.Socket;

public class ServiceThreadXML extends AbstractServiceThread implements Runnable{
    public ServiceThreadXML(Socket socket, PageController pages) {
        super(socket, pages);
    }
}