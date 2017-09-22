package randomizedEmotes;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLogoutListener implements Listener{

	@EventHandler
	public void onPlayerLogout(PlayerQuitEvent event){
			main._chests.remove(event.getPlayer().getUniqueId());
			main._firstItems.remove(event.getPlayer().getUniqueId());
			main._availables.remove(event.getPlayer().getUniqueId());
			if (main.config.getBoolean("cooldowns")){
				main.cooldowns.remove(event.getPlayer().getUniqueId());
			}
			Bukkit.getLogger().info("[RandEmotes] Removed player from the list of GUIs and cooldowns");
	}
}