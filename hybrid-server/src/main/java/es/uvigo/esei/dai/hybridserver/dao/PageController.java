package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import java.util.List;

public interface PageController {
    String get(String uuid) throws PageNotFoundException, DatabaseOfflineException;

    List<String> list() throws DatabaseOfflineException;

    void delete(String uuid) throws PageNotFoundException, DatabaseOfflineException;

    String create(String content) throws DatabaseOfflineException;

    String getXSD(String uuid) throws PageNotFoundException, DatabaseOfflineException;

    /*
    * String getXSD(String uuid)
    * Surge el problema de seguridad, debe ser implementada por defecto,
    *   pero el resto no deberian poder usarlas, solo XSLT
    *  (Tambien se puede poner un return null y ya, pero no se que tan bien quedara)
    *
    * List<String> create(String content)
    *  XSLT debe devolver uuid y xsd, haciendo falta una lista
    *  (Tambien se puede utilizar un # como separador para facilitar la devolucion)
    */
}
