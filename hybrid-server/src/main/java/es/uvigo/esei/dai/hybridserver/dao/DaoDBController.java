package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

import java.util.List;

public interface DaoDBController {
    String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException;

    List<String> list(HTTPResourceName table) throws DatabaseOfflineException;

    void delete(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException;

    String create(String content, HTTPResourceName table) throws DatabaseOfflineException;

    String create(String xsd, String content, HTTPResourceName table) throws DatabaseOfflineException;

    String getXSDUUIDwithXSD(String xslt, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException;

    String getXSDUUIDwithXSLT(String xslt, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException;
}
