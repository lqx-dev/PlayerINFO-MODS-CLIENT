package de.clientinfo;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

/**
 * Main class of the ClientInfo plugin.
 *
 * Responsibilities:
 * - load config and player settings
 * - register the /mods command
 * - register listeners (join/quit)
 * - register the plugin messaging channel used by compatible client mods to send their mod list
 */
public final class ClientInfoPlugin extends JavaPlugin {

    /** Channel a compatible client mod uses to send its mod list. See PROTOCOL.md. */
    public static final String MODLIST_CHANNEL = "clientinfo:modlist";

    private MessageUtil messages;
    private PlayerSettings playerSettings;
    private ClientDetector clientDetector;
    private ModListManager modListManager;
    private ChannelDetector channelDetector;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        // Add any new config keys from the bundled default config (keeps user changes).
        getConfig().options().copyDefaults(true);
        saveConfig();

        this.messages = new MessageUtil(this);
        this.playerSettings = new PlayerSettings(this);
        this.playerSettings.load();
        this.clientDetector = new ClientDetector(this);
        this.modListManager = new ModListManager();
        this.channelDetector = new ChannelDetector();

        // Command
        PluginCommand command = getCommand("mods");
        if (command == null) {
            getLogger().severe("Command 'mods' is missing in plugin.yml! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ModsCommand modsCommand = new ModsCommand(this);
        command.setExecutor(modsCommand);
        command.setTabCompleter(new ModsTabCompleter());

        // Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);

        // Plugin messaging channel for mod lists.
        // Registering the outgoing side as well makes the server announce the channel to clients
        // via minecraft:register, so a client mod can detect that this plugin is installed.
        Messenger messenger = getServer().getMessenger();
        messenger.registerIncomingPluginChannel(this, MODLIST_CHANNEL, new ModListMessageListener(this));
        messenger.registerOutgoingPluginChannel(this, MODLIST_CHANNEL);

        getLogger().info("ClientInfo enabled.");
    }

    @Override
    public void onDisable() {
        if (playerSettings != null) {
            playerSettings.save();
        }
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        getLogger().info("ClientInfo disabled.");
    }

    /** Reloads config.yml and players.yml from disk. */
    public void reloadAll() {
        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        messages.reload();
        playerSettings.load();
    }

    public MessageUtil getMessages() {
        return messages;
    }

    public PlayerSettings getPlayerSettings() {
        return playerSettings;
    }

    public ClientDetector getClientDetector() {
        return clientDetector;
    }

    public ModListManager getModListManager() {
        return modListManager;
    }

    public ChannelDetector getChannelDetector() {
        return channelDetector;
    }
}
