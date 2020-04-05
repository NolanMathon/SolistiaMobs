package fr.hegsis.solistiamobs;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Utils {

    public static String convertLocationToString(Location loc) {
        return loc.getWorld().getName()+";"+loc.getBlockX()+";"+loc.getBlockY()+";"+loc.getBlockZ();
    }

    public static Location convertStringToLocation(String s) {
        String[] location = s.split(";");
        return new Location(Bukkit.getWorld(location[0]), isDouble(location[1]), isDouble(location[2]), isDouble(location[3]));
    }

    public static int isNumber(String entier) {
        try {
            return Integer.parseInt(entier);
        } catch (Exception e) {
            return -1;
        }
    }

    public static double isDouble(String doubleNumber) {
        try {
            return Double.parseDouble(doubleNumber);
        } catch (Exception e) {
            return -1;
        }
    }
}
