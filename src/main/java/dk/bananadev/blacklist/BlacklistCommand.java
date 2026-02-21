package dk.ditnavn.blacklist;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class BlacklistCommand implements SimpleCommand {

    private final VelocityBlacklist plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public BlacklistCommand(VelocityBlacklist plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 2) {
            source.sendMessage(mm.deserialize("<red>Brug: /blacklist <add/remove> <spiller></red>"));
            return;
        }

        String action = args[0].toLowerCase();
        String targetPlayer = args[1].toLowerCase();

        if (action.equals("add")) {
            if (plugin.configData.blacklistedPlayers.contains(targetPlayer)) {
                source.sendMessage(mm.deserialize("<red>" + targetPlayer + " er allerede blacklisted.</red>"));
                return;
            }
            plugin.configData.blacklistedPlayers.add(targetPlayer);
            plugin.saveConfig();
            source.sendMessage(mm.deserialize("<green>" + targetPlayer + " er nu blacklisted.</green>"));
            
        } else if (action.equals("remove")) {
            if (!plugin.configData.blacklistedPlayers.contains(targetPlayer)) {
                source.sendMessage(mm.deserialize("<red>" + targetPlayer + " er ikke blacklisted.</red>"));
                return;
            }
            plugin.configData.blacklistedPlayers.remove(targetPlayer);
            plugin.saveConfig();
            source.sendMessage(mm.deserialize("<green>" + targetPlayer + " er fjernet fra blacklisten.</green>"));
            
        } else {
            source.sendMessage(mm.deserialize("<red>Brug: /blacklist <add/remove> <spiller></red>"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocityblacklist.admin");
    }
}

