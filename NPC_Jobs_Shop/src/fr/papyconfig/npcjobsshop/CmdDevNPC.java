package fr.papyconfig.npcjobsshop;

import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Entity;
//import net.minecraft.server.v1_15_R1.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class CmdDevNPC implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		
		if(sender instanceof Player && args.length == 1) {
			
			Player player = (Player) sender;
			Location loc = player.getLocation();
			Villager npc = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
			npc.setProfession(Villager.Profession.CARTOGRAPHER);
			npc.setAI(false);
			Entity nmsVillager = ((CraftEntity) npc).getHandle();
			
			nmsVillager.setCustomName(new ChatComponentText(args[0]));
			nmsVillager.setCustomNameVisible(true);
			nmsVillager.setSilent(true);
			nmsVillager.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			
			return true;
		}
		else {
			sender.sendMessage("/npcshop <nomNPC>");
		}
		
		return false;
	}

}
