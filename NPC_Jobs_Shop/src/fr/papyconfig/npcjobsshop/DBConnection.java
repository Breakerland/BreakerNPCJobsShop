package fr.papyconfig.npcjobsshop;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {
	
	private String db_username = "plugin";
	private String db_password = "minecraft";
	private String db_name = "npcjobsshop";
	private String db_host = "localhost";
	private String db_urlbase = "jdbc:mysql://";

	private String insert1 = "INSERT INTO `";
	private String insert2 = "` (`item`,`price`,`quantity`) VALUES(?,?,?)";
	
	private String select1 = "SELECT * FROM `";
	private String select2 = "` WHERE ";
	private String select3 = "=?";
	
	private String update1 = "UPDATE `";
	private String update2 = "` SET ";
	private String update3 = "=? WHERE ";
	private String update4 = "=?";
	
	private String delete1 = "DELETE FROM `";
	private String delete2 = "` WHERE ";
	private String delete3 = "=?";
	
	private String create_table1 = "CREATE TABLE `";
	private String create_table2 = "` ( `item` TINYTEXT NOT NULL , `price` DOUBLE(12,2) NOT NULL , `quantity` INT NOT NULL DEFAULT '-1' )";
	
	private String drop_table = "DROP TABLE `";
	
	private Connection connection;
	
	public void connect() {
		if (connection == null) {
			try {
				connection = DriverManager.getConnection(db_urlbase + db_host + "/" + db_name, db_username, db_password);
				System.out.println("NPCJobsShop: Connection to Database successful");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isThere(String npc_name, String item) {
		//SELECT
		
		try {
			PreparedStatement state = connection.prepareStatement(select1 + npc_name + select2 + "item" + select3);
			state.setString(1, item);
			ResultSet result = state.executeQuery();
			boolean output = result.next();
			result.close();
			state.close();
			return output;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean addItem(String npc_name, String item_name, String price, String quantity) {
		//INSERT
		
		try {
			if(!isThere(npc_name, item_name)) {
				PreparedStatement state = connection.prepareStatement(insert1 + npc_name + insert2);
				state.setString(1, item_name);
				state.setString(2, price);
				state.setString(3, quantity);
				state.execute();
				state.close();
				return true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean removeItem(String npc_name, String item) {
		//DELETE
		
		if(isThere(npc_name, item)) {
			try {
				PreparedStatement state = connection.prepareStatement(delete1 + npc_name + delete2 + "item" + delete3);
				state.setString(1, item);
				state.execute();
				state.close();
				return true;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		return false;		
	}
	
	public boolean setPrice(String npc_name, String item, String price) {
		//UPDATE
		
		try {
			PreparedStatement state = connection.prepareStatement(update1 + npc_name + update2 + "price" + update3 + "item" + update4);
			state.setString(1, price);
			state.setString(2, item);
			state.execute();
			state.close();
			return isThere(npc_name, item);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean setQuantity(String npc_name, String item, String quantity) {
		//UPDATE
		
		try {
			PreparedStatement state = connection.prepareStatement(update1 + npc_name + update2 + "quantity" + update3 + "item" + update4);
			state.setString(1, quantity);
			state.setString(2, item);
			state.execute();
			state.close();
			return isThere(npc_name, item);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String getItemList(String npc_name) {
		//SELECT
		String output = "";
		
		try {
			PreparedStatement state = connection.prepareStatement(select1 + npc_name + "`");
			ResultSet result = state.executeQuery();
			
			while(result.next()) {
				output = output + result.getString(1) + " " + result.getString(2) + " " + result.getString(3) + " ";
			}
			
			result.close();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public boolean addNPC(String npc_name, String jobs) {
		//CREATE TABLE
		
		boolean output = false;
		try {
			PreparedStatement state = connection.prepareStatement(create_table1 + npc_name + create_table2);
			state.execute();
			state.close();
			output = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			PreparedStatement state = connection.prepareStatement(insert1 + "npc_jobs` (`name`, `jobs`) VALUES (?,?)");
			state.setString(1, npc_name);
			state.setString(2, jobs);
			state.execute();
			state.close();
			output = true;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public boolean removeNPC(String npc_name) {
		//DROP TABLE
		
		boolean output = false;
		try {
			PreparedStatement state = connection.prepareStatement(drop_table + npc_name + "`");
			state.execute();
			state.close();
			output = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			PreparedStatement state = connection.prepareStatement(delete1 + "npc_jobs" + delete2 + "name" + delete3);
			state.setString(1, npc_name);
			state.execute();
			state.close();
			output = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public boolean isTableDefined(String npc_name) {
		try {
			DatabaseMetaData dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, npc_name, null);
			return tables.next();
		} catch(SQLException e) {}
		return false;
	}
	
	public String jobsForNPC(String npc_name ) {
		//SELECT
		String output = "";
		
		try {
			PreparedStatement state = connection.prepareStatement(select1 + "npc_jobs" + select2 + "name" + select3);
			ResultSet result = state.executeQuery();
			
			while(result.next()) {
				output = result.getString(1) + " " + result.getString(2);
			}
			
			result.close();
			state.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return output;
	}
	
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				System.out.println("NPCJobsShop: Deconnection to Database successful");
			}
			catch (SQLException e){
				e.printStackTrace();
			}
		}
	}

}