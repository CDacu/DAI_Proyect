package es.uvigo.esei.dai.hybridserver.dao;

public class XMLDaoDB extends AbstractDaoDB implements PageController {
    public XMLDaoDB(String dbURL, String dbUser, String dbPasswd) {
        super(dbURL, dbUser, dbPasswd, DBType.XML);
    }
}
