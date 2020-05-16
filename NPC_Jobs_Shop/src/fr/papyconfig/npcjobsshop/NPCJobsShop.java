package fr.papyconfig.npcjobsshop;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class NPCJobsShop extends JavaPlugin {
	
	public DBConnection sql;
	public static Economy economy = null;
	
	@Override
	public void onEnable() {
		System.out.println("NPCJobsShop enable");
		sql = new DBConnection();
		sql.connect();
		setupEconomy();
		getCommand("npcjobs").setExecutor(new CmdDevDB(sql));
		getCommand("npcshop").setExecutor(new CmdDevNPC());
		getServer().getPluginManager().registerEvents(new NPCListeners(sql, economy), this);
	}
	
	public boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	
	
	@Override
	public void onDisable() {
		System.out.println("NPCJobsShop disable");
		sql.disconnect();
	}
}
