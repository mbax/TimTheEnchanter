package org.kitteh.enchant;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Tim extends JavaPlugin {

    private enum EnchantmentResult {
        INVALID_ID, CANNOT_ENCHANT, VICIOUS_STREAK_A_MILE_WIDE;
    }

    private HashMap<String, Enchantment> enchantmentNames;

    private void enchantAll(Player player, boolean dirty) {
        for (final Enchantment enchantment : Enchantment.values()) {
            int level;
            if (dirty) {
                level = 127;
            } else {
                level = enchantment.getMaxLevel();
            }
            this.enchantItem(player, enchantment, level);
        }
    }

    private EnchantmentResult enchantItem(Player player, Enchantment enchantment, int level) {
        if (enchantment == null) {
            return EnchantmentResult.INVALID_ID;
        }
        if (level > 127) {
            level = 127;
        }
        if ((level < 1) || (!player.hasPermission("enchanter.dirty") && (level > enchantment.getMaxLevel()))) {
            level = enchantment.getMaxLevel();
        }
        final ItemStack item = player.getInventory().getItemInHand();
        if (item == null) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        try {
            item.addUnsafeEnchantment(enchantment, level);
        } catch (final Exception e) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        return EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE;
    }

    private EnchantmentResult enchantItem(Player player, int enchantmentID, int level) {
        final Enchantment enchantment = Enchantment.getById(enchantmentID);
        if (enchantment == null) {
            return EnchantmentResult.INVALID_ID;
        }
        return this.enchantItem(player, enchantment, level);
    }

    private EnchantmentResult enchantItem(Player player, String enchantmentString, int level) {
        int enchantmentID;
        try {
            enchantmentID = Integer.valueOf(enchantmentString);
        } catch (final Exception e) {
            return this.enchantItem(player, this.getEnchantment(enchantmentString), level);
        }
        return this.enchantItem(player, enchantmentID, level);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("enchanter.reload")) {
                    this.loadEnchantments();
                    sender.sendMessage(ChatColor.YELLOW + "[Tim] Enchantment list reloaded");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[Tim] I don't think so.");
                    return true;
                }
            }
            if (sender instanceof Player) {
                final Player player = (Player) sender;
                if (player.hasPermission("enchanter.enchant")) {
                    if (args[0].equalsIgnoreCase("all")) {
                        if ((args.length > 1) && args[1].equalsIgnoreCase("natural")) {
                            this.enchantAll(player, false);
                        } else {
                            this.enchantAll(player, true);
                        }
                        sender.sendMessage(ChatColor.YELLOW + "[Tim] Enchanted to the best of my abilities");
                        return true;
                    } else {
                        int targetLevel = 1;
                        if (args.length > 1) {
                            try {
                                targetLevel = Integer.valueOf(args[1]);
                            } catch (final Exception e) {
                                if (args[1].equalsIgnoreCase("max")) {
                                    targetLevel = -1;
                                }
                            }
                        }
                        final EnchantmentResult result = this.enchantItem(player, args[0], targetLevel);
                        switch (result) {
                            case INVALID_ID:
                                sender.sendMessage(ChatColor.YELLOW + "[Tim] That's not an enchantment ID");
                                break;
                            case CANNOT_ENCHANT:
                                sender.sendMessage(ChatColor.YELLOW + "[Tim] Cannot enchant this item");
                                break;
                        }
                        if (!result.equals(EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE)) {
                            player.sendMessage(ChatColor.YELLOW + "Look, that rabbit's got a vicious streak a mile wide! It's a killer!");
                            return true;
                        }
                    }
                    sender.sendMessage(ChatColor.YELLOW + "[Tim] Item enchanted. I... am an enchanter.");
                    return true;
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[Tim] I don't think so.");
                    return true;
                }
            }
        }
        sender.sendMessage(ChatColor.YELLOW + "[Tim] Death awaits you all with nasty, big, pointy teeth.");
        return false;
    }

    @Override
    public void onDisable() {
        this.getServer().getLogger().info("[Tim] I *warned* you, but did you listen to me? Oh, no, you *knew*, didn't you? Oh, it's just a harmless little *bunny*, isn't it? ");
    }

    @Override
    public void onEnable() {
        this.loadEnchantments();
        this.getServer().getLogger().info("[Tim] There are some who call me... Tim?");
    }

    private Enchantment getEnchantment(String query) {
        return this.enchantmentNames.get(query.toLowerCase());
    }

    private void loadEnchantments() {
        this.reloadConfig();
        this.enchantmentNames = new HashMap<String, Enchantment>();
        final boolean noConfig = !(new File(this.getDataFolder(), "config.yml")).exists();
        if (noConfig) {
            this.getConfig().options().copyDefaults(true);
        }
        final Map<String, Object> map = this.getConfig().getValues(false);
        for (final Entry<String, Object> entry : map.entrySet()) {
            Enchantment enchantment = null;
            try {
                enchantment = Enchantment.getById((Integer) entry.getValue());
            } catch (final Exception e) {
                continue;
            }
            if (enchantment != null) {
                this.enchantmentNames.put(entry.getKey().toLowerCase(), enchantment);
            } else {
                this.getServer().getLogger().info("[Tim] Ignoring custom name \"" + entry.getKey() + "\". Bad enchantment ID");
            }
        }
        if (noConfig) {
            this.saveConfig();
        }
    }

}
