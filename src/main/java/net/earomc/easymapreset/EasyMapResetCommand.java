package net.earomc.easymapreset;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.IOException;
import java.util.List;

import static net.earomc.easymapreset.EasyMapReset.PREFIX;

public class EasyMapResetCommand implements CommandExecutor {

    private final EasyMapReset plugin;

    public EasyMapResetCommand(EasyMapReset plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (sender.hasPermission("earomc.easymapreset.save")) {
                if (args.length == 0) {
                    PluginDescriptionFile description = plugin.getDescription();
                    player.sendMessage(PREFIX + "This server is running §bEasyMapReset §7version §b" +
                            description.getVersion() + " §7by §b" + toEnumeration(description.getAuthors()));
                }

                if (args.length == 1) {
                    if (args[0].equals("save")) {
                        try {
                            World world = player.getWorld();
                            player.sendMessage(PREFIX + "§aSaving world " + world.getName() + " ...");
                            world.save();
                            plugin.createBackup(world);
                            player.sendMessage(PREFIX + "§aSaved world " + world.getName() + ".");
                        } catch (IOException e) {
                            player.sendMessage(PREFIX + "§cUnable to save world. Check console for more information.");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return true;
    }

    private static String toEnumeration(List<String> list) {
        StringBuilder sb = new StringBuilder();
        if (list.size() == 2) return sb.append(list.get(0)).append(" and ").append(list.get(1)).toString();
        for (int i = 0; i < list.size(); i++) {
            if (i < list.size() - 2){
                sb.append(list.get(i)).append(", ");
            }
            if (i == list.size() - 2) {
                sb.append(list.get(i)).append(" ");
            }
            if (i == list.size() - 1) {
                sb.append("and ").append(list.get(list.size() - 1));
            }
        }
        return sb.toString();
    }
}
