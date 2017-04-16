package org.cubeville.cvclaims.commands;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

import org.cubeville.commons.utils.BlockUtils;
import org.cubeville.commons.commands.Command;
import org.cubeville.commons.commands.CommandExecutionException;
import org.cubeville.commons.commands.CommandParameterString;
import org.cubeville.commons.commands.CommandResponse;

import org.cubeville.cvclaims.ClaimManager;

public class ClaimCommand extends Command
{
    ClaimManager claimManager;
    
    public ClaimCommand(ClaimManager claimManager) {
        super("");
        addBaseParameter(new CommandParameterString()); // Region name
        addOptionalBaseParameter(new CommandParameterString());
        addOptionalBaseParameter(new CommandParameterString());
        addOptionalBaseParameter(new CommandParameterString());
        addFlag("flat"); // Force claiming of flat regions
        this.claimManager = claimManager;
    }

    public CommandResponse execute(Player player, Set<String> flags, Map<String, Object> parameters, List<Object> baseParameters)
        throws CommandExecutionException {

        Selection selection = BlockUtils.getWESelection(player);
        if(selection == null) throw new CommandExecutionException("Please make a selection first."); // TODO: Better documentation?
        if(!(selection instanceof CuboidSelection)) throw new CommandExecutionException("This command only works on cuboid selections.");
        if(selection.getWidth() > 30 || selection.getLength() > 30) throw new CommandExecutionException("Claimed regions can only be up to 30x30 blocks. Enter \"//size\" to check the size of your current selection (first and third values under \"Size:\", the middle number is the height).");
        if(selection.getWidth() < 5 || selection.getLength() < 5) throw new CommandExecutionException("Claimed region must be at least 5 blocks wide and long. Enter \"//size\" to check the size of your current selection (first and third values under \"Size:\", the middle number is the height).");
        if(selection.getHeight() < 30) {
            if(!flags.contains("flat")) throw new CommandExecutionException("Your region is less than 30 blocks high. If you want to claim from bedrock to sky, enter \"//expand vert\" first. Otherwise, if you really want to claim only this flat, add the keyword \"flat\" to you claim command.");
        }

        String regionName = (String) baseParameters.get(0);
        for(int i = 1; i < baseParameters.size(); i++) {
            regionName += "_" + baseParameters.get(i);
        }
        
        // Check name for invalid chars,
        claimManager.claimPlayerRegion(player, new BlockVector(selection.getNativeMinimumPoint()), new BlockVector(selection.getNativeMaximumPoint()), regionName);

        return new CommandResponse("&eSuccessfully claimed region " + regionName);
    }
}
