package es.uvigo.esei.dai.hybridserver.webservice;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.dao.*;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;
import jakarta.jws.WebService;

import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "es.uvigo.esei.dai.hybridserver.webservice.HybridServerService",
            serviceName = "HybridServerService",
            targetNamespace = "http://hybridserver.dai.esei.uvigo.es/")
public class HybridServerServiceImpl implements HybridServerService {

    AbstractDaoDB html, xml, xsd, xslt;

    public HybridServerServiceImpl(Configuration configuration){
        html = new HTMLDaoDB(configuration);
        xml = new XMLDaoDB(configuration);
        xsd = new XSDDaoDB(configuration);
        xslt = new XSLTDaoDB(configuration);
    }

    @Override
    public String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        switch (table) {
            case HTML:
                return html.get(uuid, table);
            case XML:
                return xml.get(uuid, table);
            case XSD:
                return xsd.get(uuid, table);
            case XSLT:
                return xslt.get(uuid, table);
        }
        return null;
    }

    @Override
    public String[] list(HTTPResourceName table) throws DatabaseOfflineException {
        switch (table) {
            case HTML:
                return html.list(table).toArray(new String[0]);
            case XML:
                return xml.list(table).toArray(new String[0]);
            case XSD:
                return xsd.list(table).toArray(new String[0]);
            case XSLT:
                return xslt.list(table).toArray(new String[0]);
        }
        return null;
    }

    @Override
    public String getXSD(String uuid) throws PageNotFoundException, DatabaseOfflineException {
        return xslt.getXSDUUIDwithXSLT(uuid, HTTPResourceName.XSLT);
    }
}
