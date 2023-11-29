package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.dao.AbstractDaoDB;
import es.uvigo.esei.dai.hybridserver.dao.DBType;
import es.uvigo.esei.dai.hybridserver.dao.PageController;

public class XSLTDaoDB extends AbstractDaoDB implements PageController {
    public XSLTDaoDB(String dbURL, String dbUser, String dbPasswd) {
        super(dbURL, dbUser, dbPasswd, DBType.XSLT);
    }

    // TODO : Hacer funcionalidades (Tener en cuenta estructura DB : uuid, xsd, content)
}
