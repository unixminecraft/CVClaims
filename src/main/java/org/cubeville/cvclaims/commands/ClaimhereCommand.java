package org.cubeville.cvclaims.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.math.BlockVector3;

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
        addOptionalBaseParameter(new CommandParameterString()); // Optional region name.
        addOptionalBaseParameter(new CommandParameterString());
        addOptionalBaseParameter(new CommandParameterString());
        addOptionalBaseParameter(new CommandParameterString());
        this.claimManager = claimManager;
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Location loc = player.getLocation();
        BlockVector3 min = BlockVector3.at(loc.getBlockX() - 14, 0, loc.getBlockZ() - 14);
        BlockVector3 max = BlockVector3.at(loc.getBlockX() + 14, 255, loc.getBlockZ() + 14);
        if(baseParameters.size() == 0) {
            claimManager.claimPlayerRegion(player, min, max, null);
        }
        else {
            String regionName = (String) baseParameters.get(0);
            for(int i = 1; i < baseParameters.size(); i++) {
                regionName += "_" + baseParameters.get(i);
            }
            claimManager.claimPlayerRegion(player, min, max, regionName);
        }

        return new CommandResponse("&eSuccessfully claimed region.");
    }
}
