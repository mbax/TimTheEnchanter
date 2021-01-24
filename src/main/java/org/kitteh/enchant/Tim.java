/*
 * TimTheEnchanter
 * Copyright (C) 2012-2021 Matt Baxter
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kitteh.enchant;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Tim extends JavaPlugin {
    private static final int MAX_ENCHANT = 1000;
    private static final String MESSAGE_NAME = "[Tim] ";
    private static final String MESSAGE_COLOR = ChatColor.YELLOW + Tim.MESSAGE_NAME;

    private enum EnchantmentResult {
        INVALID_ID,
        CANNOT_ENCHANT,
        VICIOUS_STREAK_A_MILE_WIDE
    }

    private @NonNull EnchantmentResult enchantItem(@NonNull Player player, @Nullable Enchantment enchantment, int level) {
        if (enchantment == null) {
            return EnchantmentResult.INVALID_ID;
        }
        if ((level < 1) || ((!player.hasPermission("enchanter.dirty") && (level > enchantment.getMaxLevel())))) {
            level = enchantment.getMaxLevel();
        }
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        try {
            item.addUnsafeEnchantment(enchantment, Math.min(level, Tim.MAX_ENCHANT));
        } catch (final Exception e) {
            return EnchantmentResult.CANNOT_ENCHANT;
        }
        return EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE;
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length > 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Tim.MESSAGE_COLOR + "I don't think so.");
                return true;
            }
            final Player player = (Player) sender;
            if (sender.hasPermission("enchanter.enchant")) {
                if (args[0].equalsIgnoreCase("all")) {
                    boolean dirty = (!((args.length > 1) && args[1].equalsIgnoreCase("natural")));
                    for (final Enchantment enchantment : Enchantment.values()) {
                        this.enchantItem(player, enchantment, dirty ? Tim.MAX_ENCHANT : enchantment.getMaxLevel());
                    }
                    sender.sendMessage(Tim.MESSAGE_COLOR + "Enchanted to the best of my abilities");
                    return true;
                } else {
                    int targetLevel = 1;
                    if (args.length > 1) {
                        try {
                            targetLevel = Integer.parseInt(args[1]);
                        } catch (final NumberFormatException e) {
                            if (args[1].equalsIgnoreCase("max")) {
                                targetLevel = -1;
                            }
                        }
                    }
                    final EnchantmentResult result = this.enchantItem(player, Enchantment.getByKey(NamespacedKey.minecraft(args[0])), targetLevel);
                    switch (result) {
                        case INVALID_ID:
                            sender.sendMessage(Tim.MESSAGE_COLOR + "That's not an enchantment ID");
                            break;
                        case CANNOT_ENCHANT:
                            sender.sendMessage(Tim.MESSAGE_COLOR + "Cannot enchant this item");
                            break;
                    }
                    if (!result.equals(EnchantmentResult.VICIOUS_STREAK_A_MILE_WIDE)) {
                        sender.sendMessage(ChatColor.YELLOW + "Look, that rabbit's got a vicious streak a mile wide! It's a killer!");
                        return true;
                    }
                }
                sender.sendMessage(Tim.MESSAGE_COLOR + "Item enchanted. I... am an enchanter.");
                return true;
            }
        }
        sender.sendMessage(Tim.MESSAGE_COLOR + "Death awaits you all with nasty, big, pointy teeth.");
        return false;
    }

    @Override
    public void onDisable() {
        this.getLogger().info("I *warned* you, but did you listen to me? Oh, no, you *knew*, didn't you? Oh, it's just a harmless little *bunny*, isn't it? ");
    }

    @Override
    public void onEnable() {
        this.getLogger().info("There are some who call me... Tim?");
    }
}