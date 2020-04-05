package fr.hegsis.solistiamobs;

import fr.hegsis.solistiamobs.commands.SpawnerCommand;
import fr.hegsis.solistiamobs.listeners.InventoryListeners;
import fr.hegsis.solistiamobs.listeners.MobListeners;
import fr.hegsis.solistiamobs.listeners.SpawnerListeners;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin {

    public static Economy economy = null;

    public Map<Location, Integer> upgradedSpawners = new HashMap<>(); // Map contenant tous les emplacements des spawners améliorés
    public Map<UUID, Integer> upgradedEntitesUUID = new HashMap<>(); // Liste des entitées améliorées
    public List<EntityType> entityList = new ArrayList<>(); // Liste des entités vivantes
    public String entityListString = new String(); // Chaîne de caractère qui contient la liste des entités vivantes
    public Inventory onClickSpawner = Bukkit.createInventory(null, InventoryType.HOPPER);

    private SpawnerListeners spawnerListeners;

    @Override
    public void onEnable() {
        this.getServer().getConsoleSender().sendMessage("§7SolistiaMobs §5→ §aON §f§l(By HegSiS)");

        // Dossier du plugin
        if (!getDataFolder().exists()) { getDataFolder().mkdir(); }

        // Fichier des spawners
        createFile();
        FileConfiguration playerJobsLevelConfiguration = getFileConfiguration();
        ConfigurationSection cs = playerJobsLevelConfiguration.getConfigurationSection("spawners");
        if (cs==null) {
            playerJobsLevelConfiguration.createSection("spawners");
            saveFile(playerJobsLevelConfiguration);
        }
        setUpgradedSpawners();
        saveDefaultConfig();

        setDefaultOnClickSpawnerMenu();

        spawnerListeners = new SpawnerListeners(this);

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new MobListeners(this), this);
        pm.registerEvents(spawnerListeners, this);
        pm.registerEvents(new InventoryListeners(this), this);

        entityList = spawnerListeners.setEntityList(entityList);
        entityListString = spawnerListeners.setEntityListString(entityList, entityListString);

        getCommand("spawner").setExecutor(new SpawnerCommand(this));

        if(!setupEconomy()) {
            this.getServer().getConsoleSender().sendMessage("§4Plugin d'économie nécessaire !");
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = (Economy) economyProvider.getProvider();
        }
        return economy != null;
    }

    public void removeSpawner(Location loc) {
        if (upgradedSpawners.containsKey(loc)) {
            upgradedSpawners.remove(loc);
            FileConfiguration upgradeSpawnerConfiguration = getFileConfiguration();
            upgradeSpawnerConfiguration.set("spawners." + Utils.convertLocationToString(loc), null);
            saveFile(upgradeSpawnerConfiguration);
        }
    }

    public void upgradeSpawner(Location loc, int level) {
        upgradedSpawners.put(loc, level);
        FileConfiguration upgradeSpawnerConfiguration = getFileConfiguration();
        upgradeSpawnerConfiguration.set("spawners." + Utils.convertLocationToString(loc), level);
        saveFile(upgradeSpawnerConfiguration);
    }

    public void setUpgradedSpawners() {
        FileConfiguration upgradeSpawnerConfiguration = getFileConfiguration();
        ConfigurationSection cs = upgradeSpawnerConfiguration.getConfigurationSection("spawners");
        for (String s : cs.getKeys(false)) {
            upgradedSpawners.put(Utils.convertStringToLocation(s), cs.getInt(s));
        }
    }

    private void setDefaultOnClickSpawnerMenu() {
        ItemStack green = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 13);
        ItemMeta im = green.getItemMeta();
        im.setDisplayName(" ");
        green.setItemMeta(im);
        ItemStack black = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 7);
        im = black.getItemMeta();
        im.setDisplayName(" ");
        black.setItemMeta(im);
        onClickSpawner.setItem(0, green);
        onClickSpawner.setItem(1, black);
        onClickSpawner.setItem(3, black);
        onClickSpawner.setItem(4, green);
    }

    public void openInv(Player p, Location loc, EntityType ent) {

        int actualLevel = 1, maxLevel = 3, price = 0;
        if (upgradedSpawners.containsKey(loc)) {
            actualLevel = upgradedSpawners.get(loc)+1;
        }

        String starName = "§2Amélioration vers le niveau §7" + (actualLevel+1) + " §2!";

        if (ent == EntityType.IRON_GOLEM) {
            maxLevel = 2;
        }

        if (actualLevel == maxLevel) {
            starName = "§2Votre spawner est déjà au niveau max !";
        } else {
            price = getConfig().getInt("price."+ent.toString().toLowerCase()+""+(actualLevel+1));
        }

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = star.getItemMeta();
        im.setDisplayName(starName);
        List<String> lore = new ArrayList<>();
        lore.add("§aNiveau actuel §f→ §7" + actualLevel);
        if (price != 0) {
            lore.add("§aPrix §f→ §7" + price + "§a$");
            lore.add("");
            lore.add("§aX §f→ §7" + loc.getBlockX() + " §f| §aY §f→ §7" + loc.getBlockY() + " §f| §aZ §f→ §7" + loc.getBlockZ());
        }
        im.setLore(lore);
        star.setItemMeta(im);
        Inventory spawnerMenu = Bukkit.createInventory(null, InventoryType.HOPPER, "§2Spawner à §8" + ent.toString());
        spawnerMenu.setContents(onClickSpawner.getContents());
        spawnerMenu.setItem(2, star);
        p.openInventory(spawnerMenu);
    }

    /******************************************************
     *
     *          Gestion du fichier des spawners
     *
     ******************************************************/

    public void createFile() {
        File file = getFile();
        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public File getFile() {
        return new File( getDataFolder() + File.separator + "upgraded_spawners.yml");
    }

    public FileConfiguration getFileConfiguration() {
        return YamlConfiguration.loadConfiguration(getFile());
    }

    public void saveFile(FileConfiguration fileConfiguration) {
        try {
            fileConfiguration.save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
