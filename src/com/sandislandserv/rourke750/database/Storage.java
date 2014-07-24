package com.sandislandserv.rourke750.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;

import com.sandislandserv.rourke750.LifeForce;

public class Storage {

	private LifeForce plugin;
	private DataBase db;
    private final String host;
    private final String dbname;
    private final String username;
    private final int port;
    private final String password;
    
	public Storage(FileConfiguration config_, LifeForce plugin){
		this.plugin = plugin;
		host = config_.getString("sql.hostname");
		port = config_.getInt("sql.port");
		dbname = config_.getString("sql.dbname");
		username = config_.getString("sql.username");
		password = config_.getString("sql.password");
		db = new DataBase(host, port, dbname, username, password, plugin.getLogger());
		boolean connected = db.connect();
		if (connected){
			createTables();
			initializeStatements();
		}
	}
	
	public void createTables(){
		db.execute("create table if not exists times("
				+ "player varchar(40) not null,"
				+ "play bigint not null,"
				+ "points int not null,"
				+ "primary key (player));");
	}
	
	
	public void initializeStatements(){
		getPlayerTime = db.prepareStatement("select play from times where player=?");
		addPlayer = db.prepareStatement("insert ignore into times(player, play, points) values(?,?,?)");
		updateTime = db.prepareStatement("update times set play=? where player=?");
		addStamina = db.prepareStatement("update times set points=? where player=?");
		getStamina = db.prepareStatement("select points from times where player=?");
	}
	
	private PreparedStatement getPlayerTime;
	private PreparedStatement addPlayer;
	public long getPlayerTime(UUID player){
		try {
			getPlayerTime.setString(1, player.toString());
			ResultSet set = getPlayerTime.executeQuery();
			if (!set.next()){
				addPlayer.setString(1, player.toString());
				addPlayer.setLong(2, 0);
				addPlayer.setInt(3, 0);
				addPlayer.execute();
				return 1;
			}
			return set.getLong("play");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	private PreparedStatement updateTime;
	public void updateTime(UUID player, long time){
		try {
			updateTime.setLong(1, time);
			updateTime.setString(2, player.toString());
			updateTime.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private PreparedStatement addStamina;
	public void addStamina(UUID player, int stamina){
		try {
			addStamina.setInt(1, stamina);
			addStamina.setString(2, player.toString());
			addStamina.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private PreparedStatement getStamina;
	public int getStamina(UUID player){
		try {
			getStamina.setString(1, player.toString());
			ResultSet set = getStamina.executeQuery();
			if (!set.next()) return -1;
			return set.getInt("points");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
}
