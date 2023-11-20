package es.uvigo.esei.dai.hybridserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HTMLDaoMap implements HTMLDao {

	private final Map<String, String> daoMap;
	
	public HTMLDaoMap() {
		
		this.daoMap = new HashMap<String, String>();
	}

	public HTMLDaoMap(Map<String, String> pages) {
	
		this.daoMap = pages;
	}

	@Override
	public String get(String uuid) throws PageNotFoundException {

		String content = daoMap.get(uuid);
		
		if (content == null) {
			throw new PageNotFoundException();			
		}

		return content;
	}

	@Override
	public List<String> list() {
        Set<String> keySet = daoMap.keySet();
        return new ArrayList<>(keySet);
	}

	@Override
	public void delete(String uuid) {
		daoMap.remove(uuid);
	}

	@Override
	public String create(String content) {
		
		
		UUID uuid = UUID.randomUUID();
		String uuidAsString = uuid.toString();
		
		daoMap.put(uuidAsString, content);	
		
		return uuidAsString;
	}

}
