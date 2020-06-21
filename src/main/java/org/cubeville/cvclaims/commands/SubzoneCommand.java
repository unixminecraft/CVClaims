package org.cubeville.cvclaims.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import org.cubeville.commons.utils.BlockUtils;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.cvclaims.ClaimManager;

public class SubzoneCommand extends Command
{
    ClaimManager claimManager;

    public SubzoneCommand(ClaimManager claimManager) {
        super("");
        addBaseParameter(new CommandParameterString()); // Parent region
        addBaseParameter(new CommandParameterString()); // Subzone region
        this.claimManager = claimManager;
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Region selection = BlockUtils.getWESelection(player);
        if(selection == null) throw new CommandExecutionException("Please make a selection first."); // TODO: Better documentation?
        if(!(selection instanceof CuboidRegion)) throw new CommandExecutionException("This command only works on cuboid selections.");

        String parentRegionName = (String) baseParameters.get(0);
        String childRegionName = (String) baseParameters.get(1);

        claimManager.createSubzone(player, parentRegionName, selection.getMinimumPoint(), selection.getMaximumPoint(), childRegionName);

        return new CommandResponse("&eSuccessfully created subzone " + childRegionName);
    }
}
