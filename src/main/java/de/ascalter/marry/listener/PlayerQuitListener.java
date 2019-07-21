package de.ascalter.marry.listener;

import de.ascalter.marry.Marry;
import de.ascalter.marry.player.MarryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerQuitListener implements Listener {

  @EventHandler
  public void expectQuit(PlayerQuitEvent event) throws SQLException {
    Player player = event.getPlayer();
    MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer().get(player.getUniqueId());

    try {
      Marry.getInstance().getMySQL()
          .setTargetName(marryPlayer.getTargetName(), marryPlayer.getUuid());
      Marry.getInstance().getMySQL().setMarried(marryPlayer.getUuid(), marryPlayer.isMarried());
    } catch (SQLException exception) {
      Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
    }

    for (UUID uuid : Marry.getInstance().getMySQL().getRequests(player.getUniqueId())) {
      Marry.getInstance().getMySQL().removeRequest(uuid, player.getUniqueId());
    }

    if (marryPlayer.getRequests().isEmpty()) {
      return;
    }

    for (UUID uuid : marryPlayer.getRequests()) {
      try {
        Marry.getInstance().getMySQL().addRequest(uuid, player.getUniqueId());
      } catch (SQLException exception) {
        Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
      }
    }
    Marry.getInstance().getCachedPlayer().remove(player.getUniqueId());
  }
}
