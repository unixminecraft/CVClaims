package org.cubeville.cvclaims;

import java.io.File;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import org.cubeville.commons.commands.CommandParser;

import org.cubeville.cvclaims.commands.*;

public class CVClaims extends JavaPlugin {
    private CommandParser claimCommandParser;
    private CommandParser claimhereCommandParser;
    private CommandParser subzoneCommandParser;

    private ClaimManager claimManager;
    
    public void onEnable() {
        File dataFolder = getDataFolder();
        if(!dataFolder.exists()) dataFolder.mkdirs();
        claimManager = new ClaimManager(dataFolder);
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for(Player p: players) claimManager.load(p.getUniqueId());
        
        claimCommandParser = new CommandParser();
        claimCommandParser.addCommand(new ClaimCommand(claimManager));

        claimhereCommandParser = new CommandParser();
        claimhereCommandParser.addCommand(new ClaimhereCommand(claimManager));

        subzoneCommandParser = new CommandParser();
        subzoneCommandParser.addCommand(new SubzoneCommand(claimManager));
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("claimrg")) {
            return claimCommandParser.execute(sender, args);
        }
        else if(command.getName().equals("claimhere")) {
            return claimhereCommandParser.execute(sender, args);
        }
        else if(command.getName().equals("subzone")) {
            return subzoneCommandParser.execute(sender, args);
        }
        return false;
    }
}

