package de.ascalter.marry.session;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public interface DatabaseSession {
    void connect() throws SQLException;

    void disconnect() throws SQLException;

    void createTable() throws SQLException;

    boolean isConnected();

    void registerPlayer(UUID uuid, String playername, String targetName, boolean married) throws SQLException;

    boolean playerExists(UUID uuid) throws SQLException;

    boolean playerExists(String playername) throws SQLException;

    String getTargetName(UUID name);

    void setTargetName(String tragetname, UUID uuid) throws SQLException;

    boolean getMarried(UUID uuid);

    void setMarried(UUID uuid, boolean married) throws SQLException;

    List<UUID> getRequests(UUID uuid) throws SQLException;

    void addRequest(UUID uuid, UUID target) throws SQLException;

    void removeRequest(UUID uuid, UUID target) throws SQLException;
}
