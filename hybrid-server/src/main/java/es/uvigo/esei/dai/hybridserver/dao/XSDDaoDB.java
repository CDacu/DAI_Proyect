package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

public class XSDDaoDB extends AbstractDaoDB {
    public XSDDaoDB(Configuration configuration) {
        super(configuration.getDbURL(), configuration.getDbUser(), configuration.getDbPassword(), HTTPResourceName.XSD);
    }
}
