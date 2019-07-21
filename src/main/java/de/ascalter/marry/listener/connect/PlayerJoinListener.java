package de.ascalter.marry.listener.connect;

import de.ascalter.marry.Marry;
import de.ascalter.marry.player.MarryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerJoinListener implements Listener {

  @EventHandler
  public void expectJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    Marry.getInstance().getExecutorService().execute(() -> {
      MarryPlayer marryPlayer = new MarryPlayer(player.getUniqueId());
      marryPlayer.setName(player.getName());
      marryPlayer.setMarried(Marry.getInstance().getMySQL().getMarried(player.getUniqueId()));
      try {
        for (UUID uuid : Marry.getInstance().getMySQL().getRequests(player.getUniqueId())) {
          marryPlayer.getRequests().add(uuid);
        }
      } catch (SQLException exception) {
        Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
      }
      marryPlayer.setTargetName(Marry.getInstance().getMySQL().getTargetName(player.getUniqueId()));
      Marry.getInstance().getCachedPlayer().put(player.getUniqueId(), marryPlayer);
    });
  }
}
