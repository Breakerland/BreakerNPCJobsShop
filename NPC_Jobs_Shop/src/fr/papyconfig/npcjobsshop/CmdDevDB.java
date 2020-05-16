package fr.papyconfig.npcjobsshop;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.Entity;
//import net.minecraft.server.v1_15_R1.NBTTagCompound;

public class CmdDevDB implements CommandExecutor {
	
	private DBConnection sql;

	public CmdDevDB(DBConnection sql) {
		this.sql = sql;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		boolean test = false;
		switch (args[0]) {
		case "additem":
			String quantity = "-1";
			if (args.length == 4) {
				test = true;
			}
			if (args.length == 5) {
				quantity = args[4];
				test = true;
			}
			
			if (test) {
				test = sql.addItem(args[1], args[2], args[3], quantity);
			}
			if(!test) {
				sender.sendMessage("/npcjobs additem <nomNPC> <nomItem> <prix> (<quantité>)");
			}
			
			break;

		case "removeitem":
			if(args.length == 3) {
				test = sql.removeItem(args[1], args[2]);
			}
			if (!test) {
				sender.sendMessage("/npcjobs removeitem <nomNPC> <nomItem>");
			}
			break;
			
		case "setprice":
			if(args.length == 4) {
				test = sql.setPrice(args[1], args[2], args[3]);
			}
			if(!test) {
				sender.sendMessage("/npcjobs setprice <nomNPC> <nomItem> <prix>");
			}
			break;
			
		case "setquantity":
			if(args.length == 4) {
				test = sql.setQuantity(args[1], args[2], args[3]);
			}
			if(!test) {
				sender.sendMessage("/npcjobs setquantity <nomNPC> <nomItem> <quantity>");
			}
			break;
	
		case "getlist":
			String output = "";
			if(args.length == 2) {
				output = sql.getItemList(args[1]);
			}
			sender.sendMessage(output);
			break;
		
		case "addnpc":
			if(args.length == 3) {
				test = sql.addNPC(args[1], args[2]);
			}
						
			if(sender instanceof Player && args.length >= 2) {
				Player player = (Player) sender;
				Location loc = player.getLocation();
				Villager npc = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
				npc.setProfession(Villager.Profession.CARTOGRAPHER);
				npc.setAI(false);
				Entity nmsVillager = ((CraftEntity) npc).getHandle();
				
				nmsVillager.setCustomName(new ChatComponentText(args[1]));
				nmsVillager.setCustomNameVisible(true);
				nmsVillager.setSilent(true);
				nmsVillager.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			}
			else if(!test) {
				sender.sendMessage("/npcjobs addnpc <nomNPC> <jobs>");
			}
			break;
			
		case "removenpc":
			if(args.length == 2) {
				test = sql.removeNPC(args[1]);
			}
			if(!test) {
				sender.sendMessage("/npcjobs removenpc <nomNPC>");
			}
			break;
		default:
			//HELP
			sender.sendMessage("/npcjobs additem <nomNPC> <nomItem> <prix> (<quantité>)");
			sender.sendMessage("/npcjobs removeitem <nomNPC> <nomItem>");
			sender.sendMessage("/npcjobs setprice <nomNPC> <nomItem> <prix>");
			sender.sendMessage("/npcjobs setquantity <nomNPC> <nomItem> <quantity>");
			sender.sendMessage("/npcjobs getlist <nomNPC> (<page>)");
			sender.sendMessage("/npcjobs addnpc <nomNPC>");
			sender.sendMessage("/npcjobs removenpc <nomNPC>");
			break;
		}
		
		return false;
	}

}
