package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;

import java.util.List;

public interface PageController {
    String get(String uuid) throws PageNotFoundException, DatabaseOfflineException;

    List<String> list() throws DatabaseOfflineException;

    void delete(String uuid) throws PageNotFoundException, DatabaseOfflineException;

    String create(String content) throws DatabaseOfflineException;
}
