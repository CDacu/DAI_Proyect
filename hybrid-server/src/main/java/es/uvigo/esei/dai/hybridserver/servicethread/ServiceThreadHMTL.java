package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import java.net.Socket;

public class ServiceThreadHMTL extends AbstractServiceThread implements Runnable{
    public ServiceThreadHMTL(Socket socket, PageController pages) {
        super(socket, pages);
    }
}