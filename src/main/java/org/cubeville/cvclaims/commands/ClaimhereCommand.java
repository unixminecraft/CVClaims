package org.cubeville.cvclaims.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;

import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.cvclaims.ClaimManager;

public class ClaimhereCommand extends Command
{
    ClaimManager claimManager;

    public ClaimhereCommand(ClaimManager claimManager) {
        super("");
        this.claimManager = claimManager;
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Location loc = player.getLocation();
        BlockVector min = new BlockVector(loc.getBlockX() - 14, 0, loc.getBlockZ() - 14);
        BlockVector max = new BlockVector(loc.getBlockX() + 14, 255, loc.getBlockZ() + 14);
        claimManager.claimPlayerRegion(player, min, max, null);

        return new CommandResponse("&eSuccessfully claimed region.");
    }
}
