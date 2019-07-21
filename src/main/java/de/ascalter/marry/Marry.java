package de.ascalter.marry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.ascalter.api.API;
import de.ascalter.api.utils.messages.Messages;
import de.ascalter.marry.commands.MarryCommand;
import de.ascalter.marry.commands.UnMarryCommand;
import de.ascalter.marry.database.DatabaseCredential;
import de.ascalter.marry.database.MySQL;
import de.ascalter.marry.listener.PlayerLoginListener;
import de.ascalter.marry.listener.PlayerQuitListener;
import de.ascalter.marry.listener.connect.PlayerJoinListener;
import de.ascalter.marry.listener.move.PlayerMoveListener;
import de.ascalter.marry.player.MarryPlayer;
import de.ascalter.marry.session.DatabaseSession;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Marry extends JavaPlugin {

  @Getter
  private static Marry instance;

  @Getter
  private DatabaseSession mySQL = new MySQL();

  @Getter
  private DatabaseCredential credential;

  private File databaseFile;

  @Getter
  private String prefix = "§c§lMarry §7» §7";

  @Getter
  private ExecutorService executorService = Executors.newCachedThreadPool();

  @Getter
  private ConcurrentHashMap<UUID, MarryPlayer> cachedPlayer = new ConcurrentHashMap<>();


  @Override
  public void onLoad() {
    instance = this;
    try {
      this.loadDatabaseConfiguration();
    } catch (IOException exception) {
      Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
    }
  }

  @Override
  public void onEnable() {
    try {
      this.mySQL.connect();
      this.mySQL.createTable();
      Bukkit.getLogger().log(Level.INFO, "Connection success.");
    } catch (SQLException exception) {
      Bukkit.getLogger()
          .log(Level.WARNING, "Connection to database (" + this.credential.getDatabaseName()
              + ") failed.");
      Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
    }

    Arrays.asList(new PlayerLoginListener(), new PlayerQuitListener(),
        new PlayerJoinListener(), new PlayerMoveListener()).forEach(listener ->
        Bukkit.getPluginManager().registerEvents(listener, this));

    getCommand("marry").setExecutor(new MarryCommand());
    getCommand("unmarry").setExecutor(new UnMarryCommand());

    registerMessages();
  }

  @Override
  public void onDisable() {
    try {
      this.mySQL.disconnect();
    } catch (SQLException exception) {
      Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
    }
  }

  private void loadDatabaseConfiguration() throws IOException {
    this.databaseFile = new File("plugins/Marry/", "Database.json");
    Files.createDirectories(Paths.get(databaseFile.getParent()));

    if (databaseFile.exists()) {
      this.credential = new Gson().fromJson(new FileReader(databaseFile), DatabaseCredential.class);
    } else {
      this.credential = new DatabaseCredential("localhost", 3306,
          "Marry", "root", "password", "FREEBUILD");
      FileWriter writer = new FileWriter(databaseFile);
      writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(credential));
      writer.flush();
      writer.close();
    }
  }

  private void registerMessages() {
    Messages messages = API.getInstance().getMessages();

    messages.registerMessage("marry.info.yourname", "Dein Name§8: §a%player%");
    messages.registerMessage("marry.info.married", "Verheiratet§8: §aJa");
    messages.registerMessage("marry.info.married.with", "Mit§8: §b%target%");
    messages.registerMessage("marry.info.unmarried", "Verheiratet§8: §cNein");
    messages.registerMessage("marry.norequests", "§cDu hast keine Heiratsanfragen bekommen.");
    messages.registerMessage("marry.yourrequests", "§7Deine Heiratsanfragen§8:");
    messages.registerMessage("marry.requests.list", "§8- §a%player%");
    messages.registerMessage("marry.notexists", "§cDer Spieler war noch nie auf dem Netzwerk.");
    messages
        .registerMessage("marry.isequal", "§cDu kannst dir selber keine Heiratsanfrage senden.");
    messages.registerMessage("marry.ismarried", "§cDu bist bereits verheiratet.");
    messages.registerMessage("marry.request.sent",
        "§cDu hast diesen Spieler bereits eine Heiratsanfrage gesendet.");
    messages.registerMessage("marry.request.sent.target",
        "§cDu hast bereits eine Heiratsanfrage von " + "%player% §cerhalten.");
    messages.registerMessage("marry.request.send.target",
        "Du hast den Spieler §b" + "%target%" + " §7eine §cHeiratsanfrage §7gesendet§8.");
    messages.registerMessage("marry.request.send.player",
        "§7Du hast eine §cHeiratsanfrage §7von §b" + "%player%" + " §7bekommen§8.");
    messages.registerMessage("marry.hisname", "Sein Name§8: §a%player%");
    messages.registerMessage("marry.no.marry.request",
        "§cDu hast von diesem Spieler keine Heiratsanfrage erhalten.");
    messages.registerMessage("marry.ismarried.with",
        "Der Spieler §b" + "%player%" + " §7ist bereits verheiratet§8.");
    messages.registerMessage("marry.married.target",
        "Du bist nun mit §b" + "%target%" + " §7verheiratet§8.");
    messages.registerMessage("marry.married.player",
        "Du bist nun mit §b" + "%player%" + " §7verheiratet§8.");
    messages.registerMessage("marry.request.deny.target",
        "§cDu hast die Heiratsanfrage von " + "%target%" + " abgelehnt.");
    messages.registerMessage("marry.request.deny.player",
        "§cDer Spieler " + "%player%" + " §chat deine Heiratsanfrage abgelehnt.");
    messages.registerMessage("marry.isnotmarried", "§cDu bist derzeit nicht verheiratet.");
    messages.registerMessage("marry.isnotmarried.with.player",
        "§cDu bist mit diesem Spieler nicht verheiratet.");
    messages.registerMessage("marry.divorse.target",
        "§cDu hast dich von " + "%target%" + " §cgeschieden.");
    messages.registerMessage("marry.divorse.player",
        "§cDer Spieler " + "%player%" + " §chat sich von dir geschieden.");
    messages.registerMessage("marry.request.deny.player",
        "§cDer Spieler " + "%player%" + " hat deine Heiratsanfrage abgelehnt.");
    messages.registerMessage("marry.isnotmarried", "§cDu bist derzeit nicht verheiratet.");
    messages.registerMessage("marry.isnotmarried.with.player",
        "§cDu bist mit diesem Spieler nicht verheiratet.");
    messages.registerMessage("marry.divorse.target",
        "§cDu hast dich von " + "%target%" + " §cgeschieden.");
    messages.registerMessage("marry.divorse.player",
        "§cDer Spieler " + "%player%" + " §chat sich von dir geschieden.");
    messages.registerMessage("marry.divorce.player",
        "§cDer Spieler %player% §chat sich von dir geschieden.");
    messages
        .registerMessage("marry.divorce.target", "§cDu hast dich von %target% §cscheiden lassen.");
  }
}
