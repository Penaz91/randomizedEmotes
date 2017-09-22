package randomizedEmotes;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import randomizedEmotes.main;

public class GUIListener implements Listener{
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		Player player = (Player) event.getWhoClicked();
		ItemStack clicked = event.getCurrentItem();
		Inventory inventory = event.getInventory();
		if (clicked == null){
			return;
		}
		int firstItem = main._firstItems.get(player.getUniqueId());
		if (inventory.getName().equals(main._chests.get(player.getUniqueId()).getName())) {
			event.setCancelled(true);
			if (clicked.equals(main.prevPage)){
				//goto previous
				if (main._firstItems.get(player.getUniqueId())<=0){
					return;
				}
				main._firstItems.put(player.getUniqueId(), firstItem - 45);
				main.updateGUI(player);
			}
			if (clicked.equals(main.nextPage)){
				//goto next
				if (firstItem + 45 < main._availables.get(player.getUniqueId()).size()){
					main._firstItems.put(player.getUniqueId(), firstItem+45);
					main.updateGUI(player);;
				}else{
					return;
				}
			}
		}
	}
}
