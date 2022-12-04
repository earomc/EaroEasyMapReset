package net.earomc.easymapreset;

import com.avaje.ebean.validation.Email;
import net.earomc.easymapreset.utils.FileManager;
import net.earomc.easymapreset.utils.WorldUtils;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class EasyMapReset extends JavaPlugin implements Listener {

    public static final String PREFIX = "§5EasyMapReset §8» §7";
    private File mapsBackupFolder;
    private FileManager fileManager;
    private WorldUtils worldUtils;
    private final ConsoleCommandSender console = Bukkit.getConsoleSender();


    @Override
    public void onEnable() {
        //register commands
        Bukkit.getScheduler().runTask(this, () -> getCommand("easymapreset").setExecutor(new EasyMapResetCommand(this)));
        this.fileManager = new FileManager(this, "settings.yml");

        // create map backup folder.
        this.mapsBackupFolder = new File(this.getDataFolder(), "mapsBackup");
        if (!this.mapsBackupFolder.exists()) {
            if (!this.mapsBackupFolder.mkdir()) {
                Bukkit.getConsoleSender().sendMessage(PREFIX + "Could not create mapsBackup folder! Check permissions!");
            }
        }

        // set default config values.
        this.fileManager.setIfAbsent("worlds", Collections.singletonList("changeme"));
        this.fileManager.setIfAbsent("kickPlayers", false);
        this.fileManager.setIfAbsent("kickReason", "&b[World unloaded]");
        this.fileManager.setIfAbsent("teleportMessage", "&b[World unloaded]");
        this.fileManager.setIfAbsent("forceBackup", false);
        this.fileManager.saveConfig();

        boolean forceBackup = this.fileManager.getBoolean("forceBackup");

        this.worldUtils = new WorldUtils(this.fileManager.getBoolean("kickPlayers"),
                this.fileManager.getString("kickReason").replace('&', '§'),
                this.fileManager.getString("teleportMessage").replace('&', '§'));


        int backupsCreated = 0;
        for (String worldName : this.fileManager.getStringList("worlds")) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                this.getLogger().warning("World " + worldName + " you entered in the settings.yml couldn't be found.");
                continue;
            }
            File worldFile = getWorldFile(world);
            File backupWorldFile = getBackupWorldFile(worldName);
            if (!forceBackup && backupWorldFile != null) {
                // if backup exists and backup isn't forced: send a message and do nothing.
                console.sendMessage(PREFIX + "Found a backup for world §b" + worldName + "§7! If you want to backup the map every time set §bforceBackup §7to true");
            } else {
                // if no backup exists or a backup is forced: try to create backup
                try {
                    createBackup(worldFile);
                    backupsCreated++;
                } catch (IOException e) {
                    console.sendMessage(PREFIX + "World §b" + worldName + " §7couldn't be saved. You deleted the mapsBackup folder or the given world does not exist.");
                }
            }
            Bukkit.unloadWorld(world, false);
            world.setAutoSave(false);
        }
        if (backupsCreated > 0) Bukkit.getConsoleSender().sendMessage(PREFIX + "Successfully saved §b" + backupsCreated + " §7world" + (backupsCreated > 1 ? "s" : "") + "!");


        //register events
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    private void createBackup(@NotNull File worldFile) throws IOException {
        FileUtils.copyDirectoryToDirectory(worldFile, this.mapsBackupFolder);
    }
    public void createBackup(World world) throws IOException {
        createBackup(getWorldFile(world));
    }
    @Nullable
    private File getBackupWorldFile(String worldName) {
        File file = new File(this.mapsBackupFolder, worldName);
        if (file.exists()) return file;
        return null;
    }

    @Nullable
    private File getWorldFile(World world) {
        if (world == null) return null;
        return new File(Bukkit.getWorldContainer(), world.getName());
    }

    @Override
    public void onDisable() {
        //restoring means loading the map from our backup folder and copying them back into the Minecraft worlds folder.
        int reset = 0;
        for (String worldName : this.fileManager.getStringList("worlds")) {
            if (Bukkit.getWorld(worldName) != null) {
                File backupWorld = new File(this.mapsBackupFolder, worldName);
                if (backupWorld.exists()) {
                    try {
                        FileUtils.copyDirectoryToDirectory(backupWorld, Bukkit.getWorldContainer());
                        reset++;
                    } catch (IOException e) {
                        console.sendMessage(PREFIX + "World §b" + worldName + " §7couldn't be reset.");
                    }
                } else {
                    console.sendMessage(PREFIX + "No save found for world §b" + worldName + "§7! Please restart the server once.");
                }
                this.worldUtils.removePlayers(worldName);
            } else {
                console.sendMessage(PREFIX + "World §b" + worldName + " §7does not exist.");
            }
        }
        if (reset > 0)
            console.sendMessage(PREFIX + "Successfully reset §b" + reset + " §7world" + (reset > 1 ? "s" : "") + "!");
    }

}
