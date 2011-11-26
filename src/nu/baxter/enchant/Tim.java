package nu.baxter.enchant;

import java.lang.reflect.Field;

import net.minecraft.server.Enchantment;
import net.minecraft.server.ItemStack;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Tim extends JavaPlugin{

    @Override
    public void onDisable() {
        this.getServer().getLogger().info("[Tim] I *warned* you, but did you listen to me? Oh, no, you *knew*, didn't you? Oh, it's just a harmless little *bunny*, isn't it? ");
    }

    @Override
    public void onEnable() {
        //this.enchantmentNames=new HashMap<String, Integer>();
        
        this.getServer().getLogger().info("[Tim] There are some who call me... Tim?");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length>0 && sender instanceof Player){
            Player player=(Player) sender;
            if(player.hasPermission("enchanter.enchant")){
                CraftItemStack craftItemStack=(CraftItemStack)player.getInventory().getItemInHand();
                Field itemField = null;
                try {
                    itemField=CraftItemStack.class.getDeclaredField("item");
                } catch (Exception e) {
                    this.getServer().getLogger().warning("[Tim] Failure in reflection, level one.");
                    e.printStackTrace();
                }
                if(itemField==null){
                    sender.sendMessage(ChatColor.YELLOW+"Look, that rabbit's got a vicious streak a mile wide! It's a killer!");
                    sender.sendMessage("[Tim] Enchantment failed due to error :(");
                    return true;
                }
                itemField.setAccessible(true);
                ItemStack itemTarget = null;
                try {
                    itemTarget=(ItemStack) itemField.get(craftItemStack);
                } catch (Exception e) {
                    this.getServer().getLogger().warning("[TIM] Failure in reflection, level two.");
                    e.printStackTrace();
                }
                if(itemTarget==null){
                    sender.sendMessage(ChatColor.YELLOW+"Look, that rabbit's got a vicious streak a mile wide! It's a killer!");
                    sender.sendMessage("[Tim] Enchantment failed due to error :(");
                    return true;
                }
                if(args[0].equalsIgnoreCase("all")){
                    this.enchantAll(itemTarget);
                    sender.sendMessage(ChatColor.YELLOW+"[Tim] Enchantment attemped.");
                    return true;
                }
                else{
                    int targetLevel=1;
                    if(args.length>1){
                        Integer.valueOf(args[1]);
                    }
                    enchantResult code=this.enchantDat(itemTarget, Integer.valueOf(args[0]), targetLevel);
                    switch (code){
                        case INVALID_ID:sender.sendMessage(ChatColor.YELLOW+"[Tim] That's not an enchantment ID");break;
                        case CANNOT_ENCHANT:sender.sendMessage(ChatColor.YELLOW+"[Tim] Cannot enchant this item");break;
                    }
                }
                sender.sendMessage(ChatColor.YELLOW+"[Tim] Item enchanted. I... am an enchanter.");
                return true;
            }
        }
        sender.sendMessage(ChatColor.YELLOW+"[Tim] Death awaits you all with nasty, big, pointy teeth.");

        return true;
    }

    public void enchantAll(ItemStack sargeantStackums){
        this.enchantDat(sargeantStackums, 0, 127);
        this.enchantDat(sargeantStackums, 1, 127);
        this.enchantDat(sargeantStackums, 2, 127);
        this.enchantDat(sargeantStackums, 3, 127);
        this.enchantDat(sargeantStackums, 4, 127);
        this.enchantDat(sargeantStackums, 5, 127);
        this.enchantDat(sargeantStackums, 6, 127);
        this.enchantDat(sargeantStackums, 16, 127);
        this.enchantDat(sargeantStackums, 17, 127);
        this.enchantDat(sargeantStackums, 18, 127);
        this.enchantDat(sargeantStackums, 19, 127);
        this.enchantDat(sargeantStackums, 20, 127);
        this.enchantDat(sargeantStackums, 21, 127);
        this.enchantDat(sargeantStackums, 32, 127);
        this.enchantDat(sargeantStackums, 33, 127);
        this.enchantDat(sargeantStackums, 34, 127);
        this.enchantDat(sargeantStackums, 35, 127);
    }
    
    public enchantResult enchantDat(ItemStack stacky, int enchantmentID, int level){
        Enchantment enchantment=Enchantment.byId[enchantmentID];
        if(enchantment==null){
            return enchantResult.INVALID_ID;
        }
            /*if(level>enchantment.getMaxLevel()){
                level=enchantment.getMaxLevel();
            }*/
        if(level>127){
            level=127;
        }
        else if(level <1){
            level=1;
        }
        try{
            stacky.a(enchantment, level);
        }
        catch(Exception e){
            return enchantResult.CANNOT_ENCHANT;
        }
        return enchantResult.VICIOUS_STREAK_A_MILE_WIDE;
    }
    
    private enum enchantResult{
        INVALID_ID,CANNOT_ENCHANT,VICIOUS_STREAK_A_MILE_WIDE;
    }
    
    /*private HashMap<String, Integer> enchantmentNames;
    
    private Enchantment matchEnchantment(String query){
        
    }*/

}
