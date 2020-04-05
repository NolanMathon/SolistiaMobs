package fr.hegsis.solistiamobs.commands;

import fr.hegsis.solistiamobs.Main;
import fr.hegsis.solistiamobs.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class SpawnerCommand implements CommandExecutor {

    private Main main;
    public SpawnerCommand(Main main) {
        this.main = main;
    }

    public boolean onCommand(CommandSender p, Command cmd, String label, String[] args) {

        if (args.length >= 1 && args.length <= 3) {
            try {
                EntityType.valueOf(args[0].toUpperCase());
            } catch (Exception e) {
                p.sendMessage("§aL'entité §7" + args[0].toUpperCase() + " §an'existe pas !");
                return false;
            }
            if (!main.entityList.contains(EntityType.valueOf(args[0].toUpperCase()))) {
                p.sendMessage("§aL'entité §7" + args[0].toUpperCase() + " §an'est pas disponible !");
                return false;
            }

            // Si il y deux arguments | /spawner <entité> [joueur]
            if (args.length >= 2) {
                // Si le second argument (le joueur) existe | /spawner <entité> [joueur]
                if (Bukkit.getPlayer(args[1]) != null) {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (args.length == 3) {
                        int amount = Utils.isNumber(args[2]);
                        if (amount >= 1) {
                            giveSpawner(target, EntityType.valueOf(args[0].toUpperCase()), amount);
                            return true;
                        }

                        // Si le nombre est erroné
                        p.sendMessage("§aLe nombre §7" + args[2] + " §aest erroné !");
                        return false;
                    }

                    // Si il n'y a pas de nombre de spawners
                    giveSpawner(target, EntityType.valueOf(args[0].toUpperCase()), 1);
                    return true;
                }

                // Si le joueur appelé n'existe pas ou n'est pas connecté
                p.sendMessage("§aLe joueur §7" + args[1] + " §an'existe pas ou n'est pas connecté !");
                return false;
            }


            if (p instanceof Player) {
                // Si il y a qu'un seul argument on donne le spawner à l'utilisateur qui a fait la commande
                giveSpawner((Player) p, EntityType.valueOf(args[0].toUpperCase()), 1);
                return true;
            } else {
                p.sendMessage("§cSeul un joueur peut faire cette commande !");
                return false;
            }
        }

        // Si il y a pas d'argument ou s'il y en a plus que 3
        p.sendMessage("§a/spawner [entité] §7(joueur) (nombre)\n" + main.entityListString);
        return false;
    }

    // Fonction qui permet de give le spawner
    private void giveSpawner(Player p, EntityType entityType, int amount) {
        ItemStack itemStack = new ItemStack(Material.MOB_SPAWNER, amount);
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§d" + entityType.toString());
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);

        // Si l'inventaire du joueur est full, on lui drop le spawner au sol
        if (p.getInventory().firstEmpty() == -1) {
            p.getWorld().dropItemNaturally(p.getLocation(), itemStack);
            p.sendMessage("§aVous venez de recevoir §7" + amount + "§a spawner à §7" + entityType + " §a! §7(au sol)");
            return;
        }
        // Sinon on lui met dans son inventaire
        p.getInventory().addItem(itemStack);
        p.sendMessage("§aVous venez de recevoir §7" + amount + "§a spawner à §7" + entityType + " §a!");
    }
}
