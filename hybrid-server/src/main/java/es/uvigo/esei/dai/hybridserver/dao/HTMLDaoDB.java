package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

public class HTMLDaoDB extends AbstractDaoDB {
	public HTMLDaoDB(Configuration configuration) {
		super(configuration.getDbURL(), configuration.getDbUser(), configuration.getDbPassword(), HTTPResourceName.HTML);
	}
}
