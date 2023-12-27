package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractDaoDB {
    protected String dbURL, dbUser, dbPasswd;
    protected HTTPResourceName type;

    public AbstractDaoDB(String dbURL, String dbUser, String dbPasswd, HTTPResourceName type) {
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPasswd = dbPasswd;
        this.type = type;
    }

    public String get(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT content FROM "+ table.getType() +" WHERE uuid=?")) {

                String content;

                statement.setString(1, uuid);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        content = resultSet.getString("content");
                    } else {
                        throw new PageNotFoundException();
                    }
                }

                return content;

            } catch (SQLException e) {
                throw new RuntimeException("Error getting the content of the uuid: " + uuid, e);
            }
        } catch (SQLException e) {
            throw new DatabaseOfflineException("Error trying to establish a connection to the DB", e);
        }
    }

    public List<String> list() throws DatabaseOfflineException {

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM "+ type.getType())) {
                List<String> pages = new ArrayList<>();

                ResultSet result = statement.executeQuery();
                while (result.next()) {
                    pages.add(result.getString("uuid"));
                }

                return pages;
            } catch (SQLException e) {
                throw new RuntimeException("Error listing pages", e);
            }

        } catch (SQLException e) {
            throw new DatabaseOfflineException("Error trying to establish a connection to the DB", e);
        }
    }

    public void delete(String uuid) throws DatabaseOfflineException, PageNotFoundException {

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM "+ type.getType() +" WHERE uuid=?")) {
                statement.setString(1, uuid);

                if (statement.executeUpdate() != 1) {
                    throw new PageNotFoundException("Error deleting the page with the uuid:" + uuid);
                }

            } catch (SQLException e) {
                throw new RuntimeException("Error deleting the page with the uuid: " + uuid, e);
            }
        } catch (SQLException e) {
            throw new DatabaseOfflineException("Error trying to establish a connection to the DB", e);
        }
    }

    public String create(String content) throws DatabaseOfflineException {

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO "+ type.getType() +" (uuid, content) " + "VALUES (?, ?)")) {

                UUID uuid = UUID.randomUUID();
                String uuidString = uuid.toString();

                statement.setString(1, uuidString);
                statement.setString(2, content);

                if (statement.executeUpdate() != 1) {
                    throw new RuntimeException("Error while inserting the new page, uuid: " + uuidString);
                }

                return uuidString;

            } catch (SQLException e) {
                throw new RuntimeException("Error creating page", e);
            }
        } catch (SQLException e) {
            throw new DatabaseOfflineException("Error trying to establish a connection to the DB", e);
        }
    }

    // Este no tienen implementacion por defecto, dado que unicamente deben ser usados en XSLT

    public String create(String xsd, String content) throws DatabaseOfflineException {
        return null;
    }

    public String getXSDUUID(String uuid, HTTPResourceName table) throws PageNotFoundException, DatabaseOfflineException {

        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection.prepareStatement("SELECT xsd FROM " + table.getType() + " WHERE xsd=?")) {

                String xsd;

                statement.setString(1, uuid);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        xsd = resultSet.getString("xsd");
                    } else {
                        throw new PageNotFoundException();
                    }
                }

                return xsd;

            } catch (SQLException e) {
                throw new RuntimeException("Error getting the xsd of the uuid: " + uuid, e);
            }
        } catch (SQLException e) {
            throw new DatabaseOfflineException("Error trying to establish a connection to the DB", e);
        }
    }
}
