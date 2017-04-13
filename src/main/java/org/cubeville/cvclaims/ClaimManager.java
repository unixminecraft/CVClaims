package org.cubeville.cvclaims;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sk89q.worldedit.BlockVector;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ClaimManager
{
    WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

    Map<UUID, Map<String, Set<String>>> playerRegionList;
    File dataFolder;

    
    public ClaimManager(File dataFolder) {
        this.dataFolder = dataFolder;
        playerRegionList = new HashMap<>();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        load(player.getUniqueId());
    }

    public void load(UUID playerId) {
        File configFile = new File(dataFolder, playerId.toString());
        playerRegionList.remove(playerId);
        if(configFile.exists()) {
            YamlConfiguration config = new YamlConfiguration();
            try { config.load(configFile); } catch (Exception e) {}
            for(String worldName: config.getKeys(false)) {
                for(String regionId: config.getStringList(worldName)) {
                    addPlayerRegion(playerId, worldName, regionId);
                }
            }
        }
    }
    
    public void save(UUID playerId) {
        YamlConfiguration config = new YamlConfiguration();
        Map<String, Set<String>> regionList = playerRegionList.get(playerId);
        if(regionList != null) {
            for(String worldName: regionList.keySet()) {
                config.set(worldName, new ArrayList(regionList.get(worldName)));
            }
        }
        try {config.save(new File(dataFolder, playerId.toString())); } catch (IOException e) {}
    }
    
    public void addPlayerRegion(UUID playerId, String worldName, String regionName) {
        if(!playerRegionList.containsKey(playerId)) playerRegionList.put(playerId, new HashMap<>());
        if(!playerRegionList.get(playerId).containsKey(worldName)) playerRegionList.get(playerId).put(worldName, new HashSet<>());
        playerRegionList.get(playerId).get(worldName).add(regionName);
    }

    public void removePlayerRegion(UUID playerId, String worldName, String regionName) {
        playerRegionList.get(playerId).get(worldName).remove(regionName);
    }
    
    Set<String> getPlayerRegions(UUID playerId, String worldName) {
        if(!playerRegionList.containsKey(playerId)) return new HashSet<>();
        if(!playerRegionList.get(playerId).containsKey(worldName)) return new HashSet<>();
        return playerRegionList.get(playerId).get(worldName);
    }

    int getMaximumPlayerRegionCount(Player player) {
        int ret = 3;
        if(player.hasPermission("cvclaims.max.4")) ret = 4;
        if(player.hasPermission("cvclaims.max.5")) ret = 5;
        return ret;
    }
    
    public void claimPlayerRegion(Player player, BlockVector min, BlockVector max, String regionName) {
        RegionManager regionManager = worldGuard.getRegionManager(player.getLocation().getWorld());

        if(regionName == null) {
            for(int i = 1; i < 100; i++) {
                String prop = player.getName() + "s_claim_" + i;
                if(regionManager.getRegion(prop) == null) {
                    regionName = prop;
                    break;
                }
            }
        }
        if(regionName == null) throw new IllegalArgumentException("Unable to assign a region name. Please use the \"/claimrg\" command.");
        
        if(regionManager.getRegion(regionName) != null) throw new IllegalArgumentException("A region with that name already exists. If it belongs to you and you want to replace it, remove it first with \"/rg remove " + regionName + "\".");

        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);

        Set<String> existingRegions = getPlayerRegions(player.getUniqueId(), player.getLocation().getWorld().getName());
        for(Iterator<String> it = existingRegions.iterator(); it.hasNext();) {
            String rg = it.next();
            if(regionManager.getRegion(rg) == null || regionManager.getRegion(rg).isOwner(localPlayer) == false) {
                it.remove();
            }
        }
        int maxRg = getMaximumPlayerRegionCount(player);
        if(existingRegions.size() >= maxRg) throw new IllegalArgumentException("You're exceeding your limit of " + maxRg + " claims. Please delete an existing claim before getting a new one.");

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, min, max);

        if(regionManager.overlapsUnownedRegion(region, localPlayer)) throw new IllegalArgumentException("Can't claim here, the selection overlaps with an existing protection."); // TODO: With which?

        region.getOwners().addPlayer(localPlayer);
        regionManager.addRegion(region);

        addPlayerRegion(player.getUniqueId(), player.getLocation().getWorld().getName(), regionName);
        save(player.getUniqueId());
    }

    public void createSubzone(Player player, String parentRegionName, BlockVector min, BlockVector max, String childRegionName) {

        RegionManager regionManager = worldGuard.getRegionManager(player.getLocation().getWorld());
        
        ProtectedRegion parentRegion = regionManager.getRegion(parentRegionName);
        if(!(parentRegion instanceof ProtectedCuboidRegion)) throw new IllegalArgumentException("Subzoning is only possible within cuboid regions.");
        if(parentRegion == null) throw new IllegalArgumentException("Region " + parentRegionName + " does not exist.");
        if(regionManager.getRegion(childRegionName) != null) throw new IllegalArgumentException("Region " + childRegionName + " already exists.");
        if(!(parentRegion.contains(min) && parentRegion.contains(max))) throw new IllegalArgumentException("The subzone must be completely in the parent region!");

        LocalPlayer localPlayer = worldGuard.wrapPlayer(player);
        if(!parentRegion.isOwner(localPlayer)) throw new IllegalArgumentException("To create subzones you must be owner of the parent region.");

        ProtectedCuboidRegion childRegion = new ProtectedCuboidRegion(childRegionName, min, max);
        try {
            childRegion.setParent(parentRegion);
        }
        catch(ProtectedRegion.CircularInheritanceException e) {
            return;
        }
        regionManager.addRegion(childRegion);
    }
}
