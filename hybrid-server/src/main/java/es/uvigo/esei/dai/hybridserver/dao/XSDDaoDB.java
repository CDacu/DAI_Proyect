package es.uvigo.esei.dai.hybridserver.dao;

public class XSDDaoDB extends AbstractDaoDB implements PageController {
    public XSDDaoDB(String dbURL, String dbUser, String dbPasswd) {
        super(dbURL, dbUser, dbPasswd, DBType.XSD);
    }
}
