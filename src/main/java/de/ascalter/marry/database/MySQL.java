package de.ascalter.marry.database;

import de.ascalter.marry.Marry;
import de.ascalter.marry.session.DatabaseSession;
import javafx.scene.transform.MatrixType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MySQL implements DatabaseSession {

  private Connection connection;

  @Override
  public void connect() throws SQLException {
    this.connection = DriverManager
        .getConnection("jdbc:mysql://" + Marry.getInstance().getCredential().getHostname()
                + ":" + Marry.getInstance().getCredential().getPort() + "/"
                + Marry.getInstance().getCredential().getDatabaseName()
                + "?autoReconnect=true&useUnicode=yes",
            Marry.getInstance().getCredential().getUser(),
            Marry.getInstance().getCredential().getPassword());
  }

  @Override
  public void disconnect() throws SQLException {
    if (isConnected()) {
      this.connection.close();
    }
  }

  @Override
  public void createTable() {
    Marry.getInstance().getExecutorService().execute(() -> {
      final Statement preparedStatement;
      try {
        preparedStatement = this.connection.createStatement();
        preparedStatement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS " + Marry.getInstance().getCredential().getPrefix()
                + "_MARRY (UUID VARCHAR(100), "
                + "PLAYERNAME VARCHAR(100), TARGETNAME VARCHAR(100),"
                + "MARRIED TINYINT(1), REQUESTS VARCHAR(100))");
      } catch (final SQLException exception) {
        exception.printStackTrace();
      }
    });
  }

  @Override
  public boolean isConnected() {
    return this.connection != null;
  }

  @Override
  public void registerPlayer(UUID uuid, String playername, String targetName, boolean married) {
    final PreparedStatement preparedStatement;
    try {
      preparedStatement = this.connection.prepareStatement("" +
          "INSERT INTO " + Marry.getInstance().getCredential().getPrefix()
          + "_MARRY (UUID, PLAYERNAME , TARGETNAME , MARRIED , REQUESTS) " +
          "VALUES(?, ?, ?, ?, ?)");
      preparedStatement.setString(1, uuid.toString());
      preparedStatement.setString(2, playername);
      preparedStatement.setString(3, targetName);
      preparedStatement.setBoolean(4, married);
      preparedStatement.setString(5, "[]");
      preparedStatement.execute();
      preparedStatement.close();
    } catch (SQLException exception) {
      exception.printStackTrace();
    }
  }

  @Override
  public boolean playerExists(UUID uuid) throws SQLException {
    final PreparedStatement preparedStatement = this.connection.prepareStatement(
        "SELECT * FROM " + Marry.getInstance().getCredential().getPrefix()
            + "_MARRY WHERE UUID = ?");
    preparedStatement.setString(1, uuid.toString());
    final ResultSet resultSet = preparedStatement.executeQuery();
    try {
      return resultSet.next();
    } finally {
      resultSet.close();
      preparedStatement.close();
    }
  }

  @Override
  public boolean playerExists(String playername) throws SQLException {
    final PreparedStatement preparedStatement = this.connection.prepareStatement(
        "SELECT * FROM " + Marry.getInstance().getCredential().getPrefix()
            + "_MARRY WHERE PLAYERNAME = ?");
    preparedStatement.setString(1, playername);
    final ResultSet resultSet = preparedStatement.executeQuery();
    try {
      return resultSet.next();
    } finally {
      resultSet.close();
      preparedStatement.close();
    }
  }

  @Override
  public String getTargetName(UUID uuid) {
    try {
      final PreparedStatement preparedStatement = this.connection.prepareStatement(
          "SELECT * FROM " + Marry.getInstance().getCredential().getPrefix()
              + "_MARRY WHERE UUID = ?");
      preparedStatement.setString(1, uuid.toString());
      final ResultSet resultSet = preparedStatement.executeQuery();
      try {
        resultSet.next();
        return resultSet.getString("TARGETNAME");
      } finally {
        resultSet.close();
        preparedStatement.close();
      }
    } catch (final SQLException exception) {
      exception.printStackTrace();
      return "NULL";
    }
  }

  @Override
  public void setTargetName(String targetname, UUID uuid) {
    Marry.getInstance().getExecutorService().execute(() -> {
      final PreparedStatement preparedStatement;
      try {
        preparedStatement = this.connection.prepareStatement(
            "UPDATE " + Marry.getInstance().getCredential().getPrefix()
                + "_MARRY SET TARGETNAME = ? WHERE UUID = ?");
        preparedStatement.setString(1, targetname);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
      } catch (final SQLException exception) {
        exception.printStackTrace();
      }
    });
  }

  @Override
  public boolean getMarried(UUID uuid) {
    try {
      final PreparedStatement preparedStatement = this.connection.prepareStatement(
          "SELECT * FROM " + Marry.getInstance().getCredential().getPrefix()
              + "_MARRY WHERE UUID = ?");
      preparedStatement.setString(1, uuid.toString());
      final ResultSet resultSet = preparedStatement.executeQuery();
      try {
        resultSet.next();
        return resultSet.getBoolean("MARRIED");
      } finally {
        resultSet.close();
        preparedStatement.close();
      }
    } catch (final SQLException exception) {
      exception.printStackTrace();
      return false;
    }
  }

  @Override
  public void setMarried(UUID uuid, boolean married) {
    Marry.getInstance().getExecutorService().execute(() -> {
      final PreparedStatement preparedStatement;
      try {
        preparedStatement = this.connection.prepareStatement(
            "UPDATE " + Marry.getInstance().getCredential().getPrefix()
                + "_MARRY SET MARRIED = ? WHERE UUID = ?");
        preparedStatement.setBoolean(1, married);
        preparedStatement.setString(2, uuid.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
      } catch (SQLException exception) {
        exception.printStackTrace();
      }
    });
  }

  @Override
  public List<UUID> getRequests(UUID uuid) throws SQLException {

    final List<UUID> list = new ArrayList<>();

    final PreparedStatement preparedStatement = this.connection.prepareStatement(
        "SELECT * FROM " + Marry.getInstance().getCredential().getPrefix()
            + "_MARRY WHERE UUID = ?");
    preparedStatement.setString(1, uuid.toString());

    final ResultSet resultSet = preparedStatement.executeQuery();
    if (resultSet != null) {
      if (resultSet.next()) {
        final String string = resultSet.getString("REQUESTS");

        if (string != null) {
          if (!(string.equalsIgnoreCase("[]"))) {
            for (final String requests : string.replace("[", "")
                .replace("]", "").split(", ")) {
              list.add(UUID.fromString(requests.trim()));
            }
          }
        }
      }
      return list;
    }
    return list;
  }

  @Override
  public void addRequest(UUID uuid, UUID target) {
    Marry.getInstance().getExecutorService().execute(() -> {
      final List<UUID> targetrequests;
      try {
        targetrequests = this.getRequests(target);

        if (!targetrequests.contains(uuid)) {
          targetrequests.add(uuid);
        }

        final PreparedStatement preparedStatement = this.connection.prepareStatement(
            "UPDATE " + Marry.getInstance().getCredential().getPrefix()
                + "_MARRY SET REQUESTS = ? WHERE UUID = ?");
        preparedStatement.setString(1, targetrequests.toString());
        preparedStatement.setString(2, target.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
      } catch (final SQLException exception) {
        exception.printStackTrace();
      }
    });
  }

  @Override
  public void removeRequest(UUID uuid, UUID target) {
    Marry.getInstance().getExecutorService().execute(() -> {
      final List<UUID> targetrequests;
      try {
        targetrequests = this.getRequests(target);
        if (targetrequests.contains(uuid)) {
          targetrequests.remove(uuid);
        }

        final PreparedStatement preparedStatement = this.connection.prepareStatement(
            "UPDATE " + Marry.getInstance().getCredential().getPrefix()
                + "_MARRY SET REQUESTS = ? WHERE UUID = ?");
        preparedStatement.setString(1, targetrequests.toString());
        preparedStatement.setString(2, target.toString());
        preparedStatement.executeUpdate();
        preparedStatement.close();
      } catch (final SQLException exception) {
        exception.printStackTrace();
      }
    });
  }
}
