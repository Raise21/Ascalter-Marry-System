package de.ascalter.marry.listener.move;

import de.ascalter.marry.Marry;
import de.ascalter.marry.player.MarryPlayer;
import org.bukkit.Effect;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public final class PlayerMoveListener implements Listener {

  @EventHandler
  public void expectMove(PlayerMoveEvent event) {
    Player player = event.getPlayer();

    MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer().get(player.getUniqueId());

    for (Entity entity : player.getNearbyEntities(5D, 5D, 5D)) {
      if (entity instanceof Player) {
        if (entity == null) {
          return;
        }
        Player target = ((Player) entity).getPlayer();
        MarryPlayer tagetPlayer = Marry.getInstance().getCachedPlayer().get(target.getUniqueId());
        if (tagetPlayer.isMarried() && marryPlayer.isMarried()) {
          if (tagetPlayer.getTargetName().equalsIgnoreCase(player.getName())
              && marryPlayer.getTargetName().equalsIgnoreCase(target.getName())) {
            if (player.isSneaking()) {
              player.getWorld().playEffect(player.getLocation(), Effect.HEART, 1, 2);
            }
            if (target.isSneaking()) {
              target.getWorld().playEffect(target.getLocation(), Effect.HEART, 1, 2);
            }
          }
        }
      }
    }
  }
}
