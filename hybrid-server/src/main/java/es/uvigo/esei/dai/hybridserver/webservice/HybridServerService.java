package es.uvigo.esei.dai.hybridserver.webservice;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

import java.util.List;

@WebService(serviceName = "HybridServerService", targetNamespace = "http://hybridserver.dai.esei.uvigo.es/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface HybridServerService {

    @WebMethod
    public String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException;

    @WebMethod
    public String[] list(HTTPResourceName table) throws DatabaseOfflineException;

    @WebMethod
    public String getXSD(String uuid) throws PageNotFoundException, DatabaseOfflineException;

}
