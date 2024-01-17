package es.uvigo.esei.dai.hybridserver.dao;

import es.uvigo.esei.dai.hybridserver.configuration.Configuration;
import es.uvigo.esei.dai.hybridserver.exception.DatabaseOfflineException;
import es.uvigo.esei.dai.hybridserver.exception.PageNotFoundException;
import es.uvigo.esei.dai.hybridserver.http.HTTPResourceName;

import java.sql.*;
import java.util.UUID;

public class XSLTDaoDB extends AbstractDaoDB {
    public XSLTDaoDB(Configuration configuration) {
        super(configuration.getDbURL(), configuration.getDbUser(), configuration.getDbPassword(), HTTPResourceName.XSLT);
    }

    public String create(String xsd, String content, HTTPResourceName table) throws DatabaseOfflineException {
        try (Connection connection = DriverManager.getConnection(dbURL, dbUser, dbPasswd)) {

            try (PreparedStatement statement = connection
                    .prepareStatement("INSERT INTO "+ table.getType() +" (uuid, xsd, content) " + "VALUES (?, ?, ?)")) {

                UUID uuid = UUID.randomUUID();
                String uuidString = uuid.toString();

                statement.setString(1, uuidString);
                statement.setString(2, xsd);
                statement.setString(3, content);

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
}
