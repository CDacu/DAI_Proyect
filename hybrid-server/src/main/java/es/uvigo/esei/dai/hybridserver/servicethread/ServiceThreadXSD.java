package es.uvigo.esei.dai.hybridserver.servicethread;

import es.uvigo.esei.dai.hybridserver.dao.PageController;
import java.net.Socket;

public class ServiceThreadXSD extends AbstractServiceThread implements Runnable{
    public ServiceThreadXSD(Socket socket, PageController pages) {
        super(socket, pages);
    }
}