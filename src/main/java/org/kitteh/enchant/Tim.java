/*
 * Tim the Enchanter
 * Copyright 2012-2014 Matt Baxter
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kitteh.enchant;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class Tim extends JavaPlugin {
    private static final int MAX_ENCHANT = 1000;
    private static final String MESSAGE_NAME = "[Tim] ";
    private static final String MESSAGE_COLOR = ChatColor.YELLOW + MESSAGE_NAME;

    private enum EnchantmentResult {
        INVALID_ID,
        CANNOT_ENCHANT,
        VICIOUS_STREAK_A_MILE_WIDE
    }

    private final Map<String, Enchantment> enchantmentNames = new HashMap<String, Enchantment>() {
        @Override
        public Enchantment get(Object key) {
            return super.get(key.toString().toLowerCase());
        }

        @Override
        public Enchantment put(String key, Enchantment enchantment) {
            return super.put(key.toLowerCase(), enchantment);
        }
    };

    private EnchantmentResult enchantItem(Player player, Enchantment enchantment, int level) {
        if (enchantment == null) {
            return EnchantmentResult.INVALID_ID;
        }
        if ((level < 1) || ((!player.hasPermission("enchanter.dirty") && (level > enchantment.getMaxLevel())))) {
            level = enchantment.getMaxLevel();
        }
        final ItemStack item = player.getInventory().getItemInHand();
        if (item == null) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        try {
            item.addUnsafeEnchantment(enchantment, level > MAX_ENCHANT ? MAX_ENCHANT : level);
        } catch (final Exception e) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        return EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE;
    }

    private EnchantmentResult enchantItem(Player player, String enchantmentString, int level) {
        try {
            return this.enchantItem(player, Enchantment.getById(Integer.valueOf(enchantmentString)), level);
        } catch (final NumberFormatException e) {
            return this.enchantItem(player, this.getEnchantment(enchantmentString), level);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("enchanter.reload")) {
                    this.loadEnchantments(sender);
                    sender.sendMessage(ChatColor.GREEN + MESSAGE_NAME + "Enchantment list reloaded");
                    return true;
                } else {
                    sender.sendMessage(MESSAGE_COLOR + "I don't think so.");
                    return true;
                }
            }
            if (!(sender instanceof Player)) {
                sender.sendMessage(MESSAGE_COLOR + "I don't think so.");
                return true;
            }
            final Player player = (Player) sender;
            if (sender.hasPermission("enchanter.enchant")) {
                if (args[0].equalsIgnoreCase("all")) {
                    boolean dirty = (!((args.length > 1) && args[1].equalsIgnoreCase("natural")));
                    for (final Enchantment enchantment : Enchantment.values()) {
                        this.enchantItem(player, enchantment, dirty ? MAX_ENCHANT : enchantment.getMaxLevel());
                    }
                    sender.sendMessage(MESSAGE_COLOR + "Enchanted to the best of my abilities");
                    return true;
                } else {
                    int targetLevel = 1;
                    if (args.length > 1) {
                        try {
                            targetLevel = Integer.valueOf(args[1]);
                        } catch (final NumberFormatException e) {
                            if (args[1].equalsIgnoreCase("max")) {
                                targetLevel = -1;
                            }
                        }
                    }
                    final EnchantmentResult result = this.enchantItem(player, args[0], targetLevel);
                    switch (result) {
                        case INVALID_ID:
                            sender.sendMessage(MESSAGE_COLOR + "That's not an enchantment ID");
                            break;
                        case CANNOT_ENCHANT:
                            sender.sendMessage(MESSAGE_COLOR + "Cannot enchant this item");
                            break;
                    }
                    if (!result.equals(EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE)) {
                        sender.sendMessage(ChatColor.YELLOW + "Look, that rabbit's got a vicious streak a mile wide! It's a killer!");
                        return true;
                    }
                }
                sender.sendMessage(MESSAGE_COLOR + "Item enchanted. I... am an enchanter.");
                return true;
            }
        }
        sender.sendMessage(MESSAGE_COLOR + "Death awaits you all with nasty, big, pointy teeth.");
        return false;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("I *warned* you, but did you listen to me? Oh, no, you *knew*, didn't you? Oh, it's just a harmless little *bunny*, isn't it? ");
    }

    @Override
    public void onEnable() {
        this.loadEnchantments(this.getServer().getConsoleSender());
        this.getLogger().info("There are some who call me... Tim?");
    }

    private Enchantment getEnchantment(String query) {
        return this.enchantmentNames.get(query);
    }

    private void loadEnchantments(CommandSender sender) {
        this.enchantmentNames.clear();
        this.saveDefaultConfig();
        this.reloadConfig();
        final Map<String, Object> map = this.getConfig().getValues(false);
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            Enchantment enchantment;
            try {
                enchantment = Enchantment.getById((Integer) entry.getValue());
            } catch (final Exception e) {
                enchantment = null;
            }
            if (enchantment != null) {
                this.enchantmentNames.put(entry.getKey(), enchantment);
            } else {
                sender.sendMessage(ChatColor.RED + MESSAGE_NAME + "Ignoring name \"" + entry.getKey() + "\". Bad enchantment ID (" + entry.getValue() + ")");
            }
        }
        StringBuilder builder = new StringBuilder();
        for (Enchantment enchantment : Enchantment.values()) {
            if (!this.enchantmentNames.containsValue(enchantment)) {
                builder.append(enchantment.getName()).append('(').append(enchantment.getId()).append(") ");
            }
        }
        if (builder.length() > 0) {
            builder.insert(0, MESSAGE_COLOR + "Unused enchantments: ");
            sender.sendMessage(builder.toString());
        }
    }
}