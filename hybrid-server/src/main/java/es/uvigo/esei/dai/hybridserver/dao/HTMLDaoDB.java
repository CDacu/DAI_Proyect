package es.uvigo.esei.dai.hybridserver.dao;

public class HTMLDaoDB extends AbstractDaoDB implements PageController {
	public HTMLDaoDB(String dbURL, String dbUser, String dbPasswd) {
		super(dbURL, dbUser, dbPasswd, DBType.HTML);
	}
}
