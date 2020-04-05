package fr.hegsis.solistiamobs.listeners;

import fr.hegsis.solistiamobs.Main;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class MobListeners implements Listener {

    private Main main;
    public MobListeners(Main main) {
        this.main = main;
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        EntityType entityType = e.getEntityType();
        if (entityType == EntityType.IRON_GOLEM || entityType == EntityType.PIG_ZOMBIE) {
            if (main.upgradedSpawners.containsKey(e.getSpawner().getLocation())) {
                main.upgradedEntitesUUID.put(e.getEntity().getUniqueId(), main.upgradedSpawners.get(e.getSpawner().getLocation()));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        EntityType entityType = e.getEntityType();

        switch (entityType) {
            case IRON_GOLEM: // Si l'entité qui meurt est un golem

                for (int i=0; i<e.getDrops().size(); i++) {
                    if (main.upgradedEntitesUUID.containsKey(e.getEntity().getUniqueId())) {
                        main.upgradedEntitesUUID.remove(e.getEntity().getUniqueId());
                        if (e.getDrops().get(i).getType() == Material.IRON_INGOT) {
                            e.getDrops().set(i, new ItemStack(Material.IRON_BLOCK));
                        }
                    }

                    if (e.getDrops().get(i).getType() == Material.RED_ROSE) {
                        e.getDrops().set(i, new ItemStack(Material.AIR));
                    }
                }
                break;
            case PIG_ZOMBIE: // Si l'entité qui meurt est un pig zombie
                for (int i=0; i<e.getDrops().size(); i++) {
                    if (main.upgradedEntitesUUID.containsKey(e.getEntity().getUniqueId())) {
                        int level = main.upgradedEntitesUUID.get(e.getEntity().getUniqueId());

                        if (e.getDrops().get(i).getType() == Material.GOLD_NUGGET) {
                            if (level == 1) {
                                e.getDrops().set(i, new ItemStack(Material.GOLD_INGOT));
                            } else if (level == 2) {
                                e.getDrops().set(i, new ItemStack(Material.GOLD_BLOCK));
                            }
                        }
                        main.upgradedEntitesUUID.remove(e.getEntity().getUniqueId());
                    }

                    if (e.getDrops().get(i).getType() == Material.GOLD_SWORD) {
                        e.getDrops().set(i, new ItemStack(Material.AIR));
                    }
                }
                break;
            case WITCH: // Si l'entité qui meurt est une sorcière
                e.getDrops().add(new ItemStack(Material.DIAMOND, randomNumber()));
                for (int i=0; i<e.getDrops().size(); i++) {
                    if (e.getDrops().get(i).getType() == Material.GLASS_BOTTLE || e.getDrops().get(i).getType() == Material.STICK || e.getDrops().get(i).getType() == Material.SUGAR || e.getDrops().get(i).getType() == Material.SULPHUR) {
                        e.getDrops().set(i, new ItemStack(Material.AIR));
                    }
                }
                break;
            case VILLAGER: // Si l'entité qui meurt est un villageois
                e.getDrops().add(new ItemStack(Material.EMERALD, randomNumber()));
                break;
        }
    }

    public int randomNumber() {
        Random rdm = new Random();
        int random = rdm.nextInt(99);
        if (random < 55) {
            return 3;
        } else if (random < 85) {
            return 4;
        } else {
            return 5;
        }
    }
}
