package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

import java.sql.*;

public class XMLDaoDB extends AbstractDaoDB {
    public XMLDaoDB(Configuration configuration) {
        super(configuration.getDbURL(), configuration.getDbUser(), configuration.getDbPassword(), HTTPResourceName.XML);
    }
}
