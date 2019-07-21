package de.ascalter.marry.listener;

import de.ascalter.marry.Marry;
import de.ascalter.marry.database.MySQL;
import de.ascalter.marry.player.MarryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.sql.SQLException;
import java.util.logging.Level;

public final class PlayerLoginListener implements Listener {

  @EventHandler
  public void expectLogin(PlayerLoginEvent event) {
    Player player = event.getPlayer();

    Marry.getInstance().getExecutorService().execute(() -> {
      try {
        if (!Marry.getInstance().getMySQL().playerExists(player.getUniqueId())) {
          Marry.getInstance().getMySQL().registerPlayer(player.getUniqueId(), player.getName(),
              "/", false);
        }
        if (Marry.getInstance().getMySQL().getMarried(player.getUniqueId())) {
          String target = Marry.getInstance().getMySQL().getTargetName(player.getUniqueId());
          if (Bukkit.getPlayer(target) != null) {
            MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer()
                .get(Bukkit.getPlayer(target).getUniqueId());
            marryPlayer.setTargetName(player.getName());
          } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            Marry.getInstance().getMySQL()
                .setTargetName(player.getName(), offlinePlayer.getUniqueId());
          }
        }
      } catch (SQLException exception) {
        Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
      }
    });
  }
}
