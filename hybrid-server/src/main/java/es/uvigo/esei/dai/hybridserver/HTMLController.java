package es.uvigo.esei.dai.hybridserver;

import java.util.List;

public class HTMLController {

	private HTMLDao dao;
	
	public HTMLController(HTMLDao dao) {
		
		this.dao = dao;
	}

	public String get(String uuid) throws PageNotFoundException, DatabaseOfflineException {
		
		return dao.get(uuid);
	}

	public List<String> list() throws DatabaseOfflineException {
		
		return dao.list();
	}

	public void delete(String uuid) throws DatabaseOfflineException, PageNotFoundException {
		
		dao.delete(uuid);
	}

	public String add(String content) throws DatabaseOfflineException {
		
		return dao.create(content);
	}

}