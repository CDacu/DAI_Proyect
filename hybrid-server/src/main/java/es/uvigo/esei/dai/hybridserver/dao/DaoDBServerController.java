package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

import java.util.List;

public class DaoDBServerController implements DaoDBController {

    AbstractDaoDB page;
    Configuration configuration;

    public DaoDBServerController(AbstractDaoDB page, Configuration configuration) {
        this.page = page;
        this.configuration = configuration;
    }

    @Override
    public String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        return page.get(uuid, table);
    }

    @Override
    public List<String> list() throws DatabaseOfflineException {
        return page.list();
    }

    @Override
    public void delete(String uuid) throws PageNotFoundException, DatabaseOfflineException {
        page.delete(uuid);
    }

    @Override
    public String create(String content) throws DatabaseOfflineException {
        return page.create(content);
    }

    @Override
    public String create(String xsd, String content) throws DatabaseOfflineException {
        return page.create(xsd, content);
    }

    @Override
    public String getXSDUUIDwithXSD(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        return page.getXSDUUIDwithXSD(uuid, table);
    }

    public String getXSDUUIDwithXSLT(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {
        return page.getXSDUUIDwithXSLT(uuid, table);
    }

}
