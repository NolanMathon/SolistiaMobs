package fr.hegsis.solistiamobs.listeners;

import fr.hegsis.solistiamobs.Main;
import fr.hegsis.solistiamobs.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListeners implements Listener {

    private Main main;
    public InventoryListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return; // Si l'action n'est pas un click droit

        Block block = e.getClickedBlock();
        if (block.getType() != Material.MOB_SPAWNER) return; // Si le block n'est pas un spawner

        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        EntityType entityType = spawner.getSpawnedType();

        if (entityType != EntityType.IRON_GOLEM && entityType != EntityType.PIG_ZOMBIE) return; // Si le spawn est un spawner à pig zombie ou golem

        Player p = e.getPlayer();
        main.openInv(p, block.getLocation(), entityType);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;

        if (e.getInventory().getTitle().contains("§2Spawner à §8")) {
            e.setCancelled(true);

            if (e.getCurrentItem() == null) return;
            ItemStack item = e.getCurrentItem();

            if (item.getType() != Material.NETHER_STAR) return;

            if (!item.hasItemMeta()) return;

            if (item.getItemMeta().getDisplayName().equalsIgnoreCase("§2Votre spawner est déjà au niveau max !")) {
                for(HumanEntity he:e.getViewers()) {
                    if(he instanceof Player) {
                        Player p = (Player) he;
                        p.closeInventory();
                        return;
                    }
                }
            }

            for(HumanEntity he:e.getViewers()) {
                if(he instanceof Player) {
                    Player p = (Player) he;

                    String locMsg = ChatColor.stripColor(item.getItemMeta().getLore().get(3));
                    locMsg = locMsg.replaceAll("X ", "");
                    locMsg = locMsg.replaceAll("Y ", "");
                    locMsg = locMsg.replaceAll("Z ", "");
                    locMsg = locMsg.replaceAll("→ ", "");

                    String[] coord = locMsg.split(" ");
                    double x = Utils.isDouble(coord[0]);
                    double y = Utils.isDouble(coord[2]);
                    double z = Utils.isDouble(coord[4]);

                    Location loc = new Location(p.getWorld(), x, y, z);
                    Block block = loc.getBlock();
                    if (block.getType() != Material.MOB_SPAWNER) return;

                    CreatureSpawner spawner = (CreatureSpawner) block.getState();
                    EntityType entityType = spawner.getSpawnedType();

                    int actualLevel = 1;
                    double price = 0;
                    if (main.upgradedSpawners.containsKey(loc)) {
                        actualLevel = main.upgradedSpawners.get(loc)+1;
                    }

                    price = main.getConfig().getDouble("price."+entityType.toString().toLowerCase()+""+(actualLevel+1));
                    if (price == 0) {
                        p.closeInventory();
                        p.sendMessage("§cUne erreur est survenu !");
                        return;
                    }

                    OfflinePlayer of = Bukkit.getOfflinePlayer(p.getUniqueId());
                    double playerBalance = main.economy.getBalance(of);

                    if (playerBalance < price) {
                        p.closeInventory();
                        p.sendMessage("§cVous n'avez pas assez d'argent pour améliorer votre spawner !");
                        return;
                    } else {
                        p.closeInventory();
                        main.economy.withdrawPlayer(of, price);
                        main.upgradeSpawner(loc, actualLevel);
                        p.sendMessage("§aAmélioration du spawner à §7" + entityType.toString() + "§a au niveau §7" + (actualLevel+1) + " §a!");
                        return;
                    }
                }
            }


        }
    }
}
