package fr.hegsis.solistiamobs.listeners;

import fr.hegsis.solistiamobs.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SpawnerListeners implements Listener {

    private Main main;
    public SpawnerListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onSpawnerPlace(BlockPlaceEvent e) {
        if (e.getBlockPlaced().getType() != Material.MOB_SPAWNER) return; // Si ce n'est pas un spawner on fait rien

        Player p = e.getPlayer();
        if (p.getItemInHand().getType() != Material.MOB_SPAWNER) return; // Si l'item dans la main du joueur n'est pas un spawner on fait rien

        ItemStack spawner = p.getItemInHand();
        if (!(spawner.getItemMeta().hasLore())) return; // Si le spawner n'a pas de description on fait rien

        List<String> lore = spawner.getItemMeta().getLore();
        EntityType spawner_entity = EntityType.valueOf(ChatColor.stripColor(lore.get(0)));
        CreatureSpawner spawner_block = (CreatureSpawner) e.getBlockPlaced().getState();
        spawner_block.setSpawnedType(spawner_entity);
        p.sendMessage("§aVotre spawner à §7" + spawner_entity + " §avient d'être placé !");
    }

    @EventHandler
    public void onSpawnerBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() != Material.MOB_SPAWNER) return; // Si ce n'est pas un spawner on fait rien

        main.removeSpawner(e.getBlock().getLocation());
    }

    // Fonction qui permet de créer la liste des entités
    public List<EntityType> setEntityList(List<EntityType> entityList) {
        for (String s : main.getConfig().getStringList("allowed-entities")) {
            entityList.add(EntityType.valueOf(s.toUpperCase()));
        }
        return entityList;
    }

    // Fonction qui permet de créer la liste des entités
    public String setEntityListString(List<EntityType> entityList, String entityListString) {
        for (int i = 0; i < entityList.size(); i++) {
            entityListString += "§7, §a" + entityList.get(i);
            // Pour le premier on ne met pas d'espace avant
            if (i==0) {
                entityListString = "§a" + entityList.get(i);
            }
        }

        return entityListString;
    }
}
