package net.earomc.easymapreset.utils;

import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class WorldUtils {

    private boolean kickPlayers;
    private String kickReason;
    private String teleportMessage;

    public void removePlayers(String worldName) {
        World world = Bukkit.getWorld(worldName);
        for (Entity en : world.getEntities()) {
            if (en instanceof Player) {
                Player player = (Player) en;
                if (this.kickPlayers) {
                    player.kickPlayer(this.kickReason);
                } else {
                    // Teleport to another world
                    World otherWorld = Bukkit.getWorlds().stream().filter(w1 -> !w1.equals(world)).findFirst().orElse(null);
                    if (otherWorld != null) {
                        player.teleport(otherWorld.getSpawnLocation());
                        if (!StringUtils.isEmpty(this.teleportMessage))
                            player.sendMessage(this.teleportMessage);
                    }
                }
            }
        }
    }
}
