package es.uvigo.esei.dai.hybridserver.dao.xslt;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// TODO : Hacer funcionalidades (Tener en cuenta estructura DB : uuid, xsd, content)

public class XSLTDaoDB implements XSLTController {

    String dbURL, dbUser, dbPasswd;

    public XSLTDaoDB(String dbURL, String dbUser, String dbPasswd) {
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPasswd = dbPasswd;
    }

    @Override
    public String get(String xsltuuid, String xsduuid) throws PageNotFoundException, DatabaseOfflineException {
        return null;
    }

    @Override
    public String getXSD(String uuid) {
        return null;
    }

    @Override
    public List<String> list() throws DatabaseOfflineException {
        return null;
    }

    @Override
    public void delete(String xsltuuid ,String xsduuid) throws DatabaseOfflineException, PageNotFoundException {

    }

    @Override
    public String create(String content) throws DatabaseOfflineException {
        return null;
    }

}
