package fr.papyconfig.npcjobsshop;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.milkbowl.vault.economy.Economy;

public class NPCListeners implements Listener {
	private DBConnection sql;
	private Economy economy;
	
	public NPCListeners(DBConnection sql, Economy economy) {
		this.sql = sql;
		this.economy = economy;
	}

	@EventHandler
	public void onInteractWithNPC(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		Entity entity = e.getRightClicked();
		
		if(isCustomNPC(entity)) {
			e.setCancelled(true);
			
			String str_inv = sql.getItemList(entity.getCustomName());
			String[] array_inv = str_inv.split(" ");
			int page = 0;
			
			Inventory shop_inv = setMenu(entity.getCustomName(), array_inv, page);
			
			player.openInventory(shop_inv);
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onClickNPCShop(InventoryClickEvent e) {
		Player player = (Player) e.getWhoClicked();
		Inventory inv = e.getInventory();
		ItemStack iS = e.getCurrentItem();
		String npc_name = e.getView().getTitle();
		
		if (inv != null && sql.isTableDefined(npc_name)) {
			if(iS == null || iS.getType() == null)return;
			e.setCancelled(true);
			player.closeInventory();
			String str_inv = sql.getItemList(npc_name);
			String[] array_inv = str_inv.split(" ");
			
			Inventory inv_shop;
			int page = 0;
			if(iS.equals(inv.getItem(30))) {
				try {
					page = Integer.parseInt(inv.getItem(31).getItemMeta().getDisplayName()) - 1;
				} catch (NumberFormatException nfe) {}
				if (page < 0) {
					page = 0;
				}
				inv_shop = setMenu(npc_name, array_inv, page);
			}
			else if (iS.equals(inv.getItem(32))) {
				try {
					page = Integer.parseInt(inv.getItem(31).getItemMeta().getDisplayName()) + 1;
				} catch (NumberFormatException nfe) {}
				inv_shop = setMenu(npc_name, array_inv, page);
			}
			else if (iS.equals(inv.getItem(31))) {
				inv_shop = setMenu(npc_name, array_inv, page);
			}
			else {
				// Ouverture de la confirmation
				int index_quantity = findQuantity(array_inv, iS.getItemMeta().getDisplayName());
				inv_shop = confirmationMenu(iS, "Farmer", 200, 0.0, array_inv[index_quantity], 0);
			}
			
			player.openInventory(inv_shop);
		}
		else if (inv != null && npc_name.equalsIgnoreCase("Menu de confirmation")) {
			if(iS == null || iS.getType() == null)return;
			e.setCancelled(true);
			double balance_player = economy.getBalance(player.getName());
			String prix = inv.getItem(13).getItemMeta().getLore().get(0).substring(7);
			
			int bal = howManyCanBuy(balance_player, prix);
			int qua = 0;
			try {
				qua = Integer.parseInt(inv.getItem(7).getItemMeta().getLore().get(0).substring(7));
			} catch (NumberFormatException nfe) {}
			int pla = placeLeftInventory(player, inv.getItem(13));
			
			int maximum_buyable = Math.min(Math.min(qua, pla), bal);
			boolean bobol = false;
			
			int previous_quantity = 0;
			try {
				previous_quantity = Integer.parseInt(inv.getItem(22).getItemMeta().getLore().get(1).substring(11));
			} catch (NumberFormatException nfe) {}
			double previous_prix = 0.0;
			try {
				previous_prix = Double.parseDouble(inv.getItem(22).getItemMeta().getLore().get(0).substring(7));
			} catch (NumberFormatException nfe) {}
			
			int noi = numberOfItems(player, inv.getItem(13));
			
			if(iS.equals(inv.getItem(10))) {
				previous_quantity = 0;
			} else if(iS.equals(inv.getItem(11))) {
				previous_quantity -= 64;
				if(previous_quantity < 0) {
					previous_quantity = 0;
				}
			} else if(iS.equals(inv.getItem(12))) {
				previous_quantity--;
				if(previous_quantity < 0) {
					previous_quantity = 0;
				}
			} else if(iS.equals(inv.getItem(14))) {
				previous_quantity++;
				if(qua != -1 && previous_quantity > maximum_buyable) {
					previous_quantity = maximum_buyable;
				}
				else if (qua == -1 && previous_quantity > noi){
					previous_quantity = noi;
				}
			} else if(iS.equals(inv.getItem(15))) {
				previous_quantity += 64;
				if(qua != -1 && previous_quantity > maximum_buyable) {
					previous_quantity = maximum_buyable;
				}
				else if (qua == -1 && previous_quantity > noi){
					previous_quantity = noi;
				}
			} else if(iS.equals(inv.getItem(16))) {
				if (qua != -1) {
					previous_quantity = maximum_buyable;
				}
				else {
					previous_quantity = noi;
				}
				
			} else if(iS.equals(inv.getItem(21))) {
				bobol = true;
				player.closeInventory();
			} else if(iS.equals(inv.getItem(23))) {
				if(inv.getItem(4).getItemMeta().getDisplayName().equalsIgnoreCase("Vendre")) {
					emptyInventory(player, inv.getItem(13), previous_quantity);
					economy.depositPlayer(player, previous_prix);
				}
				else {
					fillInventory(player, inv.getItem(13), previous_quantity);
					economy.withdrawPlayer(player, previous_prix);
				}
				bobol = true;
				player.closeInventory();
			}
			
			if (!bobol) {
				Inventory inv_shop;
				double prix_unitaire = 1.00;
				try {
					prix_unitaire = Double.parseDouble(inv.getItem(13).getItemMeta().getLore().get(0).substring(7));
				} catch (NumberFormatException nfe) {}
				previous_prix = previous_quantity * prix_unitaire;
				inv_shop = confirmationMenu(inv.getItem(13), "Farmer", 200, previous_prix, qua+"", previous_quantity);
				player.openInventory(inv_shop);
			}
			
		}
	}
	
	
	private int placeLeftInventory(Player player, ItemStack iS) {
		Inventory inv = player.getInventory();
		ItemStack[] items = inv.getContents();
		int place = 0;
		int size = 0;
		
		for (ItemStack stack : items) {
			if(stack != null) {
				if(stack.getData() == iS.getData()) {
					place += 64 - stack.getAmount();
				}
			}
			else {
				size++;
			}
		}
		place += (size - 5) * 64;
		
		return place;
	}
	
	private int numberOfItems(Player player, ItemStack iS) {
		Inventory inv = player.getInventory();
		ItemStack[] items = inv.getContents();
		int place = 0;
		
		for (ItemStack stack : items) {
			if(stack != null && stack.getData() == iS.getData()) {
				place += stack.getAmount();
			}
		}
				
		return place;
	}
	
	private int howManyCanBuy(double balance, String prix) {
		double prix_unitaire = 1;
		
		try {
			prix_unitaire = Double.parseDouble(prix);
		} catch (NumberFormatException nfe) {}
		
		return (int) (balance/prix_unitaire);
	}
	
	private void fillInventory(Player player, ItemStack items, int number) {
		Inventory inv = player.getInventory();
		int jus = number/64;
		ItemStack stack;
		System.out.println(jus);
		for (int i = 0; i < jus; i++) {
			stack = new ItemStack(items.getType());
			stack.setAmount(64);
			inv.addItem(stack);
		}
		System.out.println(number - jus * 64);
		stack = new ItemStack(items.getType());
		stack.setAmount(number - (int)(number/64) * 64);
		inv.addItem(stack);
	}
	
	private void emptyInventory(Player player, ItemStack items, int number) {
		Inventory inv = player.getInventory();
		int jus = number/64;
		ItemStack stack;
		System.out.println(jus);
		for (int i = 0; i < jus; i++) {
			stack = new ItemStack(items.getType());
			stack.setAmount(64);
			inv.removeItem(stack);
		}
		System.out.println(number - jus * 64);
		stack = new ItemStack(items.getType());
		stack.setAmount(number - (int)(number/64) * 64);
		inv.removeItem(stack);
	}
	
	private int findQuantity(String[] list, String item) {
		int index = 0;
		while (index < list.length && !list[index].equalsIgnoreCase(item)) {
			index++;
		}
		return index + 2;
	}
	
	
	private Inventory confirmationMenu(ItemStack item, String jobs, int coef_metier, double total, String quantity_left, int total_quantity) {
		Inventory conf = Bukkit.createInventory(null, 27, "Menu de confirmation");
		
		ItemStack avantage = new ItemStack(Material.STONE_PICKAXE);
		ItemMeta av = avantage.getItemMeta();
		av.setDisplayName("Avantage Métier");
		av.setLore(Arrays.asList("Métier :", jobs, coef_metier + "%"));
		avantage.setItemMeta(av);
		ItemStack mode_vente = new ItemStack(Material.EMERALD);
		ItemMeta mv = mode_vente.getItemMeta();
		if(quantity_left.equalsIgnoreCase("-1")) {
			mv.setDisplayName("Vendre");
			mv.setLore(Arrays.asList("Le png achète"));
		}
		else {
			mv.setDisplayName("Achat");
			mv.setLore(Arrays.asList("Le png vend"));
		}
		mode_vente.setItemMeta(mv);
		
		ItemStack m_all = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta ma = m_all.getItemMeta();
		ma.setDisplayName("- all");
		ma.setLore(Arrays.asList("Enlève tout"));
		m_all.setItemMeta(ma);
		ItemStack m_64 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta m6 = m_64.getItemMeta();
		m6.setDisplayName("- 64");
		m6.setLore(Arrays.asList("Enlève un Stack"));
		m_64.setItemMeta(m6);
		ItemStack m_1 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta m1 = m_1.getItemMeta();
		m1.setDisplayName("- 1");
		m1.setLore(Arrays.asList("Enlève un"));
		m_1.setItemMeta(m1);
		
		ItemStack p_all = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta pa = p_all.getItemMeta();
		pa.setDisplayName("+ all");
		pa.setLore(Arrays.asList("Ajoute tout"));
		p_all.setItemMeta(pa);
		ItemStack p_64 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta p6 = p_64.getItemMeta();
		p6.setDisplayName("+ 64");
		p6.setLore(Arrays.asList("Ajoute un Stack"));
		p_64.setItemMeta(p6);
		ItemStack p_1 = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta p1 = p_1.getItemMeta();
		p1.setDisplayName("+ 1");
		p1.setLore(Arrays.asList("Ajoute un"));
		p_1.setItemMeta(p1);
		
		ItemStack cancel = new ItemStack(Material.RED_CONCRETE);
		ItemMeta ca = cancel.getItemMeta();
		ca.setDisplayName("Annulé");
		ca.setLore(Arrays.asList("Revenir en Arrière"));
		cancel.setItemMeta(ca);
		ItemStack finish = new ItemStack(Material.GOLDEN_APPLE);
		ItemMeta fi = finish.getItemMeta();
		fi.setDisplayName("Total");
		fi.setLore(Arrays.asList("Prix : " + total, "Quantité : " + total_quantity));
		finish.setItemMeta(fi);
		ItemStack agree = new ItemStack(Material.LIME_CONCRETE);
		ItemMeta ag = agree.getItemMeta();
		ag.setDisplayName("Confirmé");
		ag.setLore(Arrays.asList("Conclu le marché"));
		agree.setItemMeta(ag);
		
		ItemStack quantitee = new ItemStack(Material.CHEST);
		ItemMeta qu = quantitee.getItemMeta();
		qu.setDisplayName("Quantité");
		qu.setLore(Arrays.asList("Reste :" + quantity_left));
		quantitee.setItemMeta(qu);
		conf.setItem(7, quantitee);
		
		conf.setItem(1, avantage);
		conf.setItem(4, mode_vente);
		
		conf.setItem(10, m_all);
		conf.setItem(11, m_64);
		conf.setItem(12, m_1);
		conf.setItem(13, item);
		conf.setItem(14, p_1);
		conf.setItem(15, p_64);
		conf.setItem(16, p_all);
		
		conf.setItem(21, cancel);
		conf.setItem(22, finish);
		conf.setItem(23, agree);
		
		return conf;
	}
	
	
	private boolean isCustomNPC(Entity entity) {
		if (entity instanceof Villager) {
			Villager npc = (Villager) entity;
			return (npc.isCustomNameVisible() && npc.getCustomName() != null && sql.isTableDefined(npc.getCustomName()));
		}
		return false;
	}
	
	private Inventory setMenu(String npc_name, String[] items, int page) {
		Inventory shop = Bukkit.createInventory(null, 36, npc_name);
		
		int index = page*27;
		while(index < (page+1)*27 && index < items.length/3) {
			ItemStack iS = new ItemStack(Material.matchMaterial(items[index*3]));
			if(iS != null) {
				ItemMeta iM = iS.getItemMeta();
				iM.setDisplayName(items[index*3]);
				if(items[index*3+2] == "-1") {
					iM.setLore(Arrays.asList("Prix : " + items[index*3+1], "Quantité restante : " + items[index*3+2]));
				}
				else {
					iM.setLore(Arrays.asList("Prix : " + items[index*3+1]));
				}
				iS.setItemMeta(iM);
				shop.addItem(iS);
			}
			index++;
		}
		
		ItemStack prevPage = new ItemStack(Material.PAPER);
		ItemStack center = new ItemStack(Material.SUNFLOWER);
		ItemStack nextPage = new ItemStack(Material.PAPER);
		
		ItemMeta pP = prevPage.getItemMeta();
		ItemMeta nP = nextPage.getItemMeta();
		ItemMeta c = center.getItemMeta();
		pP.setDisplayName("Précédente Page");
		nP.setDisplayName("Prochaine Page");
		c.setDisplayName("" + page);
		
		prevPage.setItemMeta(pP);
		nextPage.setItemMeta(nP);
		center.setItemMeta(c);
		
		shop.setItem(30, prevPage);
		shop.setItem(31, center);
		shop.setItem(32, nextPage);
		
		return shop;
	}
	
}