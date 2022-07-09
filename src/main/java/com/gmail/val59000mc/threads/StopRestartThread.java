package com.gmail.val59000mc.threads;

import com.gmail.val59000mc.UhcCore;
import com.gmail.val59000mc.configuration.MainConfig;
import com.gmail.val59000mc.game.GameManager;
import com.gmail.val59000mc.languages.Lang;
import org.bukkit.Bukkit;

public class StopRestartThread implements Runnable{

	private long timeBeforeStop;
	
	public StopRestartThread(){
		this.timeBeforeStop = GameManager.getGameManager().getConfig().get(MainConfig.TIME_BEFORE_RESTART_AFTER_END);
	}
	
	@Override
	public void run() {
		if (timeBeforeStop < 0){
			return; // Stop thread
		}

		GameManager gm = GameManager.getGameManager();
			
		if(timeBeforeStop == 0){
			//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart");
			//Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload confirm");
		}else{
			if(timeBeforeStop<5 || timeBeforeStop%10 == 0){
				Bukkit.getLogger().info("[UhcCore] Server will shutdown in "+timeBeforeStop+"s");
				gm.broadcastInfoMessage(Lang.GAME_SHUTDOWN.replace("%time%", ""+timeBeforeStop));
			}

			timeBeforeStop--;
			Bukkit.getScheduler().scheduleSyncDelayedTask(UhcCore.getPlugin(), this,20);
		}
	}

}