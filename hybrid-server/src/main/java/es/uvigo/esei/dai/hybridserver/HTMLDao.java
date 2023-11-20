package es.uvigo.esei.dai.hybridserver;

import java.util.List;

public interface HTMLDao {

	String get(String uuid) throws PageNotFoundException, DatabaseOfflineException;

	List<String> list() throws DatabaseOfflineException;

	void delete(String str) throws PageNotFoundException, DatabaseOfflineException;

	String create(String str) throws DatabaseOfflineException;

}
