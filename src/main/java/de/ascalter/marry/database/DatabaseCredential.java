package de.ascalter.marry.database;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public final class DatabaseCredential {

  private final String hostname;
  private final int port;
  private final String databaseName;
  private final String user;
  private final String password;
  private final String prefix;
}
