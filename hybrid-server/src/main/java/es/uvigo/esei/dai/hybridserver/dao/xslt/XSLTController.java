package es.uvigo.esei.dai.hybridserver.dao.xslt;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;

import java.util.List;

public interface XSLTController {

    String get(String xsltuuid, String xsduuid) throws PageNotFoundException, DatabaseOfflineException;

    String getXSD(String uuid);

    List<String> list() throws DatabaseOfflineException;

    void delete(String xsltuuid, String xsduuid) throws PageNotFoundException, DatabaseOfflineException;

    String create(String content) throws DatabaseOfflineException;

}