package de.ascalter.marry.commands;

import de.ascalter.api.API;
import de.ascalter.api.utils.messages.Language;
import de.ascalter.marry.Marry;
import de.ascalter.marry.player.MarryPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public final class MarryCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label,
      String[] args) {

    if (!(sender instanceof Player)) {
      sender.sendMessage("You must be a player.");
      sender.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
          .getMessage(API.getInstance().getLanguage(sender), "general.noconsole"));
      return true;
    }

    Player player = (Player) sender;

    Language language = API.getInstance().getLanguage(player);

    if (args.length == 1) {
      MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer()
          .get(player.getUniqueId());
      if (args[0].equalsIgnoreCase("info")) {
        player.sendMessage(Marry.getInstance().getPrefix()
            + API.getInstance().getMessages().getMessage(language, "marry.info.yourname")
            .replace("%player%",
                API.getInstance().getDisplayManager().getDisplayname(player.getName())));
        if (marryPlayer.isMarried()) {
          player.sendMessage(Marry.getInstance().getPrefix() +
              API.getInstance().getMessages().getMessage(language, "marry.info.married"));
          player.sendMessage(Marry.getInstance().getPrefix() +
              API.getInstance().getMessages().getMessage(language, "marry.info.married.with")
                  .replace("%target%", marryPlayer.getTargetName()));
        } else {
          player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.info.unmarried"));
        }
      } else if (args[0].equalsIgnoreCase("requests")) {
        if (marryPlayer.getRequests().isEmpty()) {
          player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.norequests"));
        } else {
          player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.yourrequests"));
          for (UUID uuid : marryPlayer.getRequests()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.requests.list").replace("%player%",
                    API.getInstance().getDisplayManager().getDisplayname(offlinePlayer.getName())));
          }
        }
      } else {
        String target = args[0];
        if (Bukkit.getPlayer(target) == null) {
          try {
            if (!Marry.getInstance().getMySQL().playerExists(target)) {
              player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.notexists"));
              return true;
            }
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }

        }

        if (Bukkit.getPlayer(target) != null) {
          if (player.getUniqueId().equals(Bukkit.getPlayer(target).getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.isequal"));
            return true;
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          if (player.getUniqueId().equals(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.isequal"));
            return true;
          }
        }

        if (marryPlayer.isMarried()) {
          player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.ismarried"));
          return true;
        }

        if (Bukkit.getPlayer(target) != null) {
          MarryPlayer targetPlayer = Marry.getInstance().getCachedPlayer()
              .get(Bukkit.getPlayer(target).getUniqueId());
          if (targetPlayer.getRequests().contains(player.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() +
                API.getInstance().getMessages().getMessage(language, "marry.request.sent"));
            return true;
          }
        } else {
          try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            if (Marry.getInstance().getMySQL().getRequests(offlinePlayer.getUniqueId())
                .contains(player.getUniqueId())) {
              player.sendMessage(Marry.getInstance().getPrefix() +
                  API.getInstance().getMessages().getMessage(language, "marry.request.sent"));
              return true;
            }
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }
        }
        if (Bukkit.getPlayer(target) != null) {
          if (marryPlayer.getRequests().contains(Bukkit.getPlayer(target).getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.request.sent.target")
                .replace("%player%", API.getInstance().getDisplayManager().getDisplayname(target)));
            return true;
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          if (marryPlayer.getRequests().contains(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.request.sent.target")
                .replace("%player%", API.getInstance().getDisplayManager().getDisplayname(target)));
            return true;
          }
        }

        player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
            .getMessage(language, "marry.request.send.target")
            .replace("%target%", API.getInstance().getDisplayManager().getDisplayname(target)));

        if (Bukkit.getPlayer(target) != null) {
          Bukkit.getPlayer(target).sendMessage(
              Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.request.send.player").replace("%player%",
                      API.getInstance().getDisplayManager().getDisplayname(player.getName())));
          TextComponent accept = new TextComponent();
          TextComponent deny = new TextComponent();

          accept.setText(Marry.getInstance().getPrefix() + "§8[§aANNEHMEN§8]");
          accept.setClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marry accept " + player.getName()));
          accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder("§7Heiratsanfrage annehmen von §a" + player.getName())
                  .create()));

          deny.setText(Marry.getInstance().getPrefix() + "§8[§cABLEHNEN§8]");
          deny.setClickEvent(
              new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/marry deny " + player.getName()));
          deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
              new ComponentBuilder("§7Heiratsanfrage ablehnen von §c" + player.getName())
                  .create()));

          Bukkit.getPlayer(target).spigot().sendMessage(accept);
          Bukkit.getPlayer(target).spigot().sendMessage(deny);

          MarryPlayer targetPlayer = Marry.getInstance().getCachedPlayer()
              .get(Bukkit.getPlayer(target).getUniqueId());
          targetPlayer.getRequests().add(player.getUniqueId());
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          try {
            Marry.getInstance().getMySQL()
                .addRequest(player.getUniqueId(), offlinePlayer.getUniqueId());
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }
        }
      }
    } else if (args.length == 2) {
      if (args[0].equalsIgnoreCase("info")) {
        String targetName = args[1];

        if (Bukkit.getPlayer(targetName) == null) {
          try {
            if (!Marry.getInstance().getMySQL().playerExists(targetName)) {
              player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.notexists"));
              return true;
            }
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }

        }

        player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
            .getMessage(language, "marry.hisname")
            .replace("%player%", API.getInstance().getDisplayManager().getDisplayname(targetName)));

        if (Bukkit.getPlayer(targetName) != null) {
          MarryPlayer targetPlayer = Marry.getInstance().getCachedPlayer()
              .get(Bukkit.getPlayer(targetName).getUniqueId());
          if (targetPlayer.isMarried()) {
            player.sendMessage(Marry.getInstance().getPrefix() +
                API.getInstance().getMessages().getMessage(language, "marry.info.married"));
            player.sendMessage(Marry.getInstance().getPrefix() +
                API.getInstance().getMessages().getMessage(language, "marry.info.married.with")
                    .replace("%target%", targetPlayer.getTargetName()));
          } else {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.info.unmarried"));
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
          if (Marry.getInstance().getMySQL().getMarried(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() +
                API.getInstance().getMessages().getMessage(language, "marry.info.married"));
            player.sendMessage(Marry.getInstance().getPrefix() +
                API.getInstance().getMessages().getMessage(language, "marry.info.married.with")
                    .replace("%target%",
                        Marry.getInstance().getMySQL().getTargetName(offlinePlayer.getUniqueId())));
          } else {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.info.unmarried"));
          }
        }

      } else if (args[0].equalsIgnoreCase("accept")) {
        String target = args[1];

        if (Bukkit.getPlayer(target) == null) {
          try {
            if (!Marry.getInstance().getMySQL().playerExists(target)) {
              player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.notexists"));
              return true;
            }
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }
        }

        MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer()
            .get(player.getUniqueId());

        if (Bukkit.getPlayer(target) != null) {
          if (!marryPlayer.getRequests().contains(Bukkit.getPlayer(target).getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.no.marry.request"));
            return true;
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          if (!marryPlayer.getRequests().contains(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.no.marry.request"));
            return true;
          }
        }

        if (marryPlayer.isMarried()) {
          player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
              .getMessage(language, "marry.ismarried"));
          return true;
        }

        if (Bukkit.getPlayer(target) != null) {
          MarryPlayer targetplayer = Marry.getInstance().getCachedPlayer()
              .get(Bukkit.getPlayer(target).getUniqueId());
          if (targetplayer.isMarried()) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.ismarried.with")
                .replace("%player%", API.getInstance().getDisplayManager().getDisplayname(target)));
            return true;
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          if (Marry.getInstance().getMySQL().getMarried(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.ismarried.with")
                .replace("%player%", API.getInstance().getDisplayManager().getDisplayname(target)));
            return true;
          }
        }
        marryPlayer.setMarried(true);
        marryPlayer.setTargetName(target);
        player.sendMessage(Marry.getInstance().getPrefix() +
            API.getInstance().getMessages().getMessage(language, "marry.married.target")
                .replace("%target%", target));

        if (Bukkit.getPlayer(target) != null) {
          Bukkit.getPlayer(target).sendMessage(
              Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.married.player").replace("%player%",
                      API.getInstance().getDisplayManager().getDisplayname(player.getName())));

          MarryPlayer targetplayer = Marry.getInstance().getCachedPlayer()
              .get(Bukkit.getPlayer(target).getUniqueId());
          targetplayer.setMarried(true);
          targetplayer.setTargetName(marryPlayer.getName());

          UUID uuidID = null;
          for (UUID uuid : marryPlayer.getRequests()) {
            if (Bukkit.getPlayer(target).getUniqueId().equals(uuid)) {
              uuidID = uuid;
              break;
            }
          }
          marryPlayer.getRequests().remove(uuidID);
        } else {
          try {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
            marryPlayer.getRequests().remove(offlinePlayer.getUniqueId());
            Marry.getInstance().getMySQL().setMarried(offlinePlayer.getUniqueId(), true);
            Marry.getInstance().getMySQL()
                .setTargetName(player.getName(), offlinePlayer.getUniqueId());
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }
        }

      } else if (args[0].equalsIgnoreCase("deny")) {
        String target = args[1];

        if (Bukkit.getPlayer(target) == null) {
          try {
            if (!Marry.getInstance().getMySQL().playerExists(target)) {
              player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.notexists"));
              return true;
            }
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }

        }

        MarryPlayer marryPlayer = Marry.getInstance().getCachedPlayer()
            .get(player.getUniqueId());
        if (Bukkit.getPlayer(target) != null) {
          if (!marryPlayer.getRequests().contains(Bukkit.getPlayer(target).getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.no.marry.request"));
            return true;
          }
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          if (!marryPlayer.getRequests().contains(offlinePlayer.getUniqueId())) {
            player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                .getMessage(language, "marry.no.marry.request"));
            return true;
          }
        }

        if (Bukkit.getPlayer(target) != null) {
          UUID uuidID = null;
          for (UUID uuid : marryPlayer.getRequests()) {
            if (Bukkit.getPlayer(target).getUniqueId().equals(uuid)) {
              uuidID = uuid;
              break;
            }
          }
          marryPlayer.getRequests().remove(uuidID);
        } else {
          OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(target);
          try {
            Marry.getInstance().getMySQL()
                .removeRequest(offlinePlayer.getUniqueId(), player.getUniqueId());
          } catch (SQLException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Error Message: " + exception.getMessage());
          }
        }

        player.sendMessage(Marry.getInstance().getPrefix() + API.getInstance().getMessages()
            .getMessage(language, "marry.request.deny.target")
            .replace("%target%", API.getInstance().getDisplayManager().getDisplayname(target)));

        if (Bukkit.getPlayer(target) != null) {
          Bukkit.getPlayer(target).sendMessage(
              Marry.getInstance().getPrefix() + API.getInstance().getMessages()
                  .getMessage(language, "marry.request.deny.player").replace("%player%",
                      API.getInstance().getDisplayManager().getDisplayname(target)));
        }

      } else {
        player.sendMessage(Marry.getInstance().getPrefix()
            + "§cVerwendung§8: §a/marry info <Spieler>");
        player.sendMessage(Marry.getInstance().getPrefix() + "/marry accept <Spieler>");
        player.sendMessage(Marry.getInstance().getPrefix() + "/marry deny <Spieler>");
      }
    } else {
      player.sendMessage(Marry.getInstance().getPrefix() + "§cVerwendung§8: §a/marry <Spieler>");
      player.sendMessage(Marry.getInstance().getPrefix() + "§a/marry accept <Spieler>");
      player.sendMessage(Marry.getInstance().getPrefix() + "§a/marry deny <Spieler>");
      player.sendMessage(Marry.getInstance().getPrefix() + "§a/marry info <Spieler>");
      player.sendMessage(Marry.getInstance().getPrefix() + "§a/marry info");
      player.sendMessage(Marry.getInstance().getPrefix() + "§a/marry requests");
    }
    return true;
  }
}
