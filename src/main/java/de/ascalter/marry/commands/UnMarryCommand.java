package de.ascalter.marry.commands;

import de.ascalter.api.API;
import de.ascalter.api.utils.messages.Language;
import de.ascalter.marry.Marry;
import de.ascalter.marry.player.MarryPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.logging.Level;

public final class UnMarryCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {

    if (!(sender instanceof Player)) {
      Bukkit.getServer().getConsoleSender().sendMessage("You must be a player.");
      return true;
    }

    Player player = (Player) sender;

    if (args.length != 1) {
      player.sendMessage(Marry.getInstance().getPrefix() + "§cBenutze§8: §a/unmarry <Spieler>");
      return true;
    }

    MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer().get(player.getUniqueId());
    Language language = API.getInstance().getLanguage(player);

    if (!marryPlayer.isMarried()) {
      player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
          .getMessage(language, "marry.isnotmarried"));
      return true;
    }

    String target = args[0];

    if (!marryPlayer.getTargetName().equalsIgnoreCase(target)) {
      player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
          .getMessage(language, "marry.isnotmarried.with.player"));
      return true;
    }

    marryPlayer.setMarried(false);
    marryPlayer.setTargetName("/");

    if (Bukkit.getPlayer(target) != null) {
      MarryPlayer targetPlayer = Marry.getInstance().getCachedPlayer()
          .get(Bukkit.getPlayer(target).getUniqueId());
      targetPlayer.setMarried(false);
      targetPlayer.setTargetName("/");
    } else {
      try {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
        Marry.getInstance().getMySQL().setMarried(offlinePlayer.getUniqueId(), false);
        Marry.getInstance().getMySQL().setTargetName("/", offlinePlayer.getUniqueId());
      } catch (SQLException exception) {
        Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
      }
    }

    player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
        .getMessage(language, "marry.divorce.target")
        .replace("%target%", API.getInstance().getDisplayManager().getDisplayname(target)));

    if (Bukkit.getPlayer(target) != null) {
      Bukkit.getPlayer(target).sendMessage(
          Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.divorce.player").replace("%player%",
                  API.getInstance().getDisplayManager().getDisplayname(player.getName())));
    }
    return true;
  }
}
