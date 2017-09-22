package randomizedEmotes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main extends JavaPlugin{
	public static ConfigurationSection emotes = null;
	public static Random rndGen = new Random();
	public static Configuration config = null;
	public static String defaultPermissionMessage = "You don't have permissions to use this emote";
	public static ItemStack nextPage = new ItemStack(Material.ARROW);
	public static ItemStack prevPage = new ItemStack(Material.ARROW);
	public static HashMap<UUID, Integer> _firstItems = new HashMap<UUID, Integer>();
	public static HashMap<UUID,Inventory> _chests = new HashMap<UUID, Inventory>();
	public static HashMap<UUID, ArrayList<String>> _availables = new HashMap<UUID, ArrayList<String>>();
	public static HashMap<String, ItemStack> items = new HashMap<String, ItemStack>();
	public static String prefix; 
	public static HashMap<UUID, Long> cooldowns = null;
	public static long cooldownTime = 0;
	@Override
	public void onEnable(){
		/*
		 * Creates the configuration folder is it doesnt exist
		 * v--------------------------------------------------v
		 */
		File f = getDataFolder();
		if (!f.exists()){
			f.mkdir();
			saveResource("config.yml", false);
		}
		/*
		 * ^--------------------------------------------------^
		 */
		config = this.getConfig(); //loads the config
		emotes = config.getConfigurationSection("emotes"); //loads the emote list as ConfigurationSection
		prefix = config.getString("GUIPrefix");
		getServer().getPluginManager().registerEvents(new GUIListener(), this);
		getServer().getPluginManager().registerEvents(new PlayerLogoutListener(), this);
		if (config.getBoolean("cooldowns")){
			cooldowns = new HashMap<UUID, Long>();
			cooldownTime = config.getLong("cooldownTime");
		}
		generateItems();
	}
	
	public boolean reloadConfigCommand(){
		reloadConfig();
		config = getConfig();
		emotes = config.getConfigurationSection("emotes"); //loads the emote list as ConfigurationSection
		prefix = config.getString("GUIPrefix");
		if (config.getBoolean("cooldowns")){
			cooldowns = new HashMap<UUID, Long>();
			cooldownTime = config.getLong("cooldownTime");
		}
		generateItems();
		return true;
	}

	public static void generateItems(){
		for (String item: emotes.getKeys(false)){
			ItemStack book = new ItemStack(Material.BOOK);
			ItemMeta data = book.getItemMeta();
			String pl = "Player1";
			String target = "Player2";
			String displayName = item.substring(0, 1).toUpperCase() + item.substring(1, item.length());
			data.setDisplayName(colorize(prefix+displayName));
			List<String> lore = new ArrayList<String>();
			lore.add("Alone:");
			List<String> section = emotes.getConfigurationSection(item).getStringList("alone");
			String phrase = section.get(rndGen.nextInt(section.size()));
			phrase = randomizedEmotes.main.colorize(phrase);
			phrase = phrase.replaceAll("\\$player\\$", pl);
			while (phrase.contains("$random")){
				Pattern p = Pattern.compile("\\$random\\|\\d+\\|\\d+\\$");
				Matcher m = p.matcher(phrase);
				if (m.find()){
					int beginning = m.start();
					int end = m.end();
					/*
					 * Here I extract the information from the $random$ text variable, by splitting out the | symbols in an array
					 * splitted [0] = "random" in any case
					 * splitted [1] = lower integer
					 * splitted [2] = higher integer
					 */
					String data1 = phrase.substring(beginning, end);
					data1 = data1.substring(1, data1.length() - 1);
					String [] splitted = data1.split("\\|");
					Integer number = 0;
					if (splitted.length>0){
						int beg = Integer.parseInt(splitted[1]);
						int end1 = Integer.parseInt(splitted[2]);
						number = rndGen.nextInt(end1 - beg + 1) + beg;
					}
					/* I have very little importance of what's replaced, but since in normal cases the interpretation of instructions
					 * goes Left-to-right, i'll replace all the random numbers left-to-right
					 */
					phrase = phrase.replaceFirst("\\$random\\|\\d+\\|\\d+\\$", number.toString());
				}
			}
			lore.add(phrase);
			lore.add("With a target:");
			section = emotes.getConfigurationSection(item).getStringList("targeted");
			while (phrase.contains("$random")){
				Pattern p = Pattern.compile("\\$random\\|\\d+\\|\\d+\\$");
				Matcher m = p.matcher(phrase);
				if (m.find()){
					int beginning = m.start();
					int end = m.end();
					/*
					 * Here I extract the information from the $random$ text variable, by splitting out the | symbols in an array
					 * splitted [0] = "random" in any case
					 * splitted [1] = lower integer
					 * splitted [2] = higher integer
					 */
					String data1 = phrase.substring(beginning, end);
					data1 = data1.substring(1, data1.length() - 1);
					String [] splitted = data1.split("\\|");
					Integer number = 0;
					if (splitted.length>0){
						int beg = Integer.parseInt(splitted[1]);
						int end1 = Integer.parseInt(splitted[2]);
						number = rndGen.nextInt(end1 - beg + 1) + beg;
					}
					/* I have very little importance of what's replaced, but since in normal cases the interpretation of instructions
					 * goes Left-to-right, i'll replace all the random numbers left-to-right
					 */
					phrase = phrase.replaceFirst("\\$random\\|\\d+\\|\\d+\\$", number.toString());
				}
			}
			if (section.size() != 0){
				phrase = section.get(rndGen.nextInt(section.size()));
				phrase = colorize(phrase);
				phrase = phrase.replaceAll("\\$player\\$", pl);
				phrase = phrase.replaceAll("\\$target\\$", target);
			}else{
				phrase = "No Targeted Variant available";
			}
			lore.add(phrase);
			data.setLore(lore);
			book.setItemMeta(data);
			items.put(item, book);
		}
	}
	public static void updateGUI(Player pl){
		Inventory GUI = _chests.get(pl.getUniqueId());
		int firstItem = _firstItems.get(pl.getUniqueId());
		ArrayList<String> available = _availables.get(pl.getUniqueId());
		GUI.clear();
		//Build the GUI and Divide in pages
		int j=0;
		for (int i=firstItem; i < firstItem+45 && available.size() > i; i++){
			GUI.setItem(j, items.get(available.get(i)));
			j++;
		}
		ItemMeta arrowData = prevPage.getItemMeta();
		arrowData.setDisplayName("Previous Page");
		prevPage.setItemMeta(arrowData);
		arrowData = prevPage.getItemMeta();
		arrowData.setDisplayName("Next Page");
		nextPage.setItemMeta(arrowData);
		GUI.setItem(45, prevPage);
		GUI.setItem(53, nextPage);
	}
	public static void createAndShowGUI(Player pl){
		int firstItem = 0;
		Inventory GUI = null;
		ArrayList<String> available = null;
		if (_chests.containsKey(pl.getUniqueId())){
			GUI = _chests.get(pl.getUniqueId());
			firstItem = _firstItems.get(pl.getUniqueId());
			available = _availables.get(pl.getUniqueId());
		}else{
			GUI =  Bukkit.createInventory(null, 54, "Emotes List");
			_chests.put(pl.getUniqueId(), GUI);
			available = new ArrayList<String>();
			_availables.put(pl.getUniqueId(), available);
			_firstItems.put(pl.getUniqueId(), firstItem);
		}
		//Get the emote list and filter by permission
		available.clear();
		Set<String> emotelist=emotes.getKeys(false);
		for (String item : emotelist){
			if (pl.hasPermission("randemotes.emote."+item)){
				available.add(item);
			}
		}
		//Build the GUI and Divide in pages
		int j=0;
		for (int i=firstItem; i < firstItem+45 && available.size() > i; i++){
			GUI.setItem(j, items.get(available.get(i)));
			j++;
		}
		ItemMeta arrowData = prevPage.getItemMeta();
		arrowData.setDisplayName("Previous Page");
		prevPage.setItemMeta(arrowData);
		arrowData = prevPage.getItemMeta();
		arrowData.setDisplayName("Next Page");
		nextPage.setItemMeta(arrowData);
		GUI.setItem(45, prevPage);
		GUI.setItem(53, nextPage);
		//Show the GUI
		pl.openInventory(GUI);
	}
	
	@Override
	public void onDisable(){
		HandlerList.unregisterAll(this);
		getLogger().info("[RandEmotes] Plugin Disabled");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("emotegui")){
			createAndShowGUI((Player) sender);
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("randemotereload")){
			reloadConfigCommand();
			sender.sendMessage("Config Reloaded");
			getLogger().info("[RandEmote] Config Reloaded");
		}
		if (cmd.getName().equalsIgnoreCase("randemote")){
			// If called with no arguments it just shows Plugin infos and emote list
			if (args.length == 0){
				sender.sendMessage(ChatColor.GOLD + "------<->-----RandomizedEmotes-----<->------");
				sender.sendMessage(ChatColor.GOLD + "A plugin by: " + ChatColor.DARK_RED + "Penaz");
				sender.sendMessage(ChatColor.GOLD + "--------------------<->--------------------");
				sender.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.DARK_PURPLE + "/randemote" + ChatColor.GRAY + " <emote name>");
				sender.sendMessage(ChatColor.GOLD + "--------------------<->--------------------");
				Set<String> list = emotes.getKeys(false);
				sender.sendMessage(ChatColor.GOLD + "Available emote names with your current permissions:");
				//Using a stringbuilder to make it easier to have a decent-ish list of emotes to show.
				StringBuilder emotelist = new StringBuilder();
				for (String s: list){
					if (sender.hasPermission("randemote.emote."+s)){
						emotelist.append(ChatColor.GOLD + s);
						emotelist.append(ChatColor.DARK_PURPLE + ", ");
					}
				}
				sender.sendMessage(emotelist.toString());
				return true;
			}
			// If calls with an argument I look it it's inside the ConfigurationSection that contains all the emotes
			if (args.length >= 1){
				//Emote things here
				if (emotes.contains(args[0])){
					if (sender.hasPermission("randEmotes.emote."+args[0])){
						if (config.getBoolean("cooldowns")){
							if (!cooldowns.containsKey(((Player) sender).getUniqueId())){
								cooldowns.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
							}else{
								if (System.currentTimeMillis() - cooldowns.get(((Player) sender).getUniqueId()) < cooldownTime){
									sender.sendMessage("Please wait at least " + ((int) cooldownTime/1000) + "s before using another emote");
									return true;
								}
							}
							cooldowns.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
						}
						List<String> section = null;
						String selfSound = "";
						String targetSound = "";
						String everybodySound = "";
						String playerParticle = "";
						String targetParticle = "";
						if (args.length==1){
							//alone
							section = emotes.getConfigurationSection(args[0]).getStringList("alone");
						}else if (args.length==2){
							//targeted
							section = emotes.getConfigurationSection(args[0]).getStringList("targeted");
						}
						selfSound = emotes.getConfigurationSection(args[0]).getString("soundPlayer");
						playerParticle = emotes.getConfigurationSection(args[0]).getString("particlePlayer");
						Player pl = (Player) sender;
						Player target = null;
						if (args.length==2){
							if (!section.isEmpty()){
								Collection<? extends Player> onlinePlayers = getServer().getOnlinePlayers();
								for (Player item : onlinePlayers){
									if (item.getName().equalsIgnoreCase(args[1])){
										target = item;
									}
								}
							}else{
								sender.sendMessage("This emote doesn't have a 'targeted' section");
								return true;
							}
						}
						targetParticle = emotes.getConfigurationSection(args[0]).getString("particleTarget");
						targetSound = emotes.getConfigurationSection(args[0]).getString("soundTarget"); 
						String phrase = section.get(rndGen.nextInt(section.size()));
						phrase = colorize(phrase);
						phrase = phrase.replaceAll("\\$player\\$", pl.getDisplayName());
						if (args.length==2){
							phrase = phrase.replaceAll("\\$target\\$", target.getDisplayName());
						}
						if (target != null){
							if (args.length == 2){
								if (!targetSound.isEmpty()){
									target.playSound(target.getLocation(), Sound.valueOf(targetSound), 50.0F, 50.0F);
								}
								if (!targetParticle.isEmpty()){
									Location l = target.getLocation();
									int count = emotes.getConfigurationSection(args[0]).getInt("particleTargetCount");
									int Yoffset = emotes.getConfigurationSection(args[0]).getInt("particleTargetYOffset");
									target.spawnParticle(Particle.valueOf(playerParticle), l.getX(), l.getY() + Yoffset, l.getZ(), count);
								}	
							}else{
								sender.sendMessage("The player selected is not online!");
								return true;
							}
						}
						if (!selfSound.isEmpty()){
							pl.playSound(pl.getLocation(), Sound.valueOf(selfSound), 50.0F, 50.0F);
						}
						if (!playerParticle.isEmpty()){
							Location l = pl.getLocation();
							int count = emotes.getConfigurationSection(args[0]).getInt("particlePlayerCount");
							int Yoffset = emotes.getConfigurationSection(args[0]).getInt("particlePlayerYOffset");
							pl.spawnParticle(Particle.valueOf(playerParticle), l.getX(), l.getY() + Yoffset, l.getZ(), count);
						}
						@SuppressWarnings("deprecation")
						Player snd = Bukkit.getPlayer(sender.getName());
						// I get all the entities in a cubic radius defined in the config
						List<Entity> lst = snd.getNearbyEntities(config.getInt("radius"), config.getInt("radius"), config.getInt("radius"));
						if (!lst.contains(target) && args.length == 2){
							sender.sendMessage("Your target is too far!");
							return true;
						}
						/* Here I filter out $random|number|number$ and make it so it generates a random Integer, the regex is what allows me to
						 * filter it out, \d means "A number digit" \d+ instead is "One or more number digits" 
						 */
						while (phrase.contains("$random")){
							Pattern p = Pattern.compile("\\$random\\|\\d+\\|\\d+\\$");
							Matcher m = p.matcher(phrase);
							if (m.find()){
								int beginning = m.start();
								int end = m.end();
								/*
								 * Here I extract the information from the $random$ text variable, by splitting out the | symbols in an array
								 * splitted [0] = "random" in any case
								 * splitted [1] = lower integer
								 * splitted [2] = higher integer
								 */
								String data = phrase.substring(beginning, end);
								data = data.substring(1, data.length() - 1);
								String [] splitted = data.split("\\|");
								Integer number = 0;
								if (splitted.length>0){
									int beg = Integer.parseInt(splitted[1]);
									int end1 = Integer.parseInt(splitted[2]);
									number = rndGen.nextInt(end1 - beg + 1) + beg;
								}
								/* I have very little importance of what's replaced, but since in normal cases the interpretation of instructions
								 * goes Left-to-right, i'll replace all the random numbers left-to-right
								 */
								phrase = phrase.replaceFirst("\\$random\\|\\d+\\|\\d+\\$", number.toString());
							}
						}
						// Send the emote to the commandsender so they know the emote worked
						sender.sendMessage(phrase);
						// Send the emote to all the players (that's why instanceof) that are in the list i created at the beginning
						everybodySound = emotes.getConfigurationSection(args[0]).getString("soundEverybody");
						boolean playEverybody = (!everybodySound.isEmpty());
						for (Entity e: lst){
							if (e instanceof Player){
								e.sendMessage(phrase);
								if (playEverybody){
									((Player) e).playSound(e.getLocation(), Sound.valueOf(everybodySound), 50.0F, 50.0F);
								}
							}
						}
					}else{
						/*No permission*/
						sender.sendMessage(defaultPermissionMessage);
					}
				}else{
					//The emote doesn't exist, Cut it all.
					sender.sendMessage("This emote doesn't exist");
				}
				return true;
			}
			return true;
		}
		return true;
	}

	private static String colorize(String phrase) {
		/*
		 * Simple method to replace the & color codes with the respective colors.
		 */
		phrase = phrase.replaceAll("&0", ChatColor.BLACK + "");
		phrase = phrase.replaceAll("&1", ChatColor.DARK_BLUE + "");
		phrase = phrase.replaceAll("&2", ChatColor.DARK_GREEN + "");
		phrase = phrase.replaceAll("&3", ChatColor.DARK_AQUA + "");
		phrase = phrase.replaceAll("&4", ChatColor.DARK_RED + "");
		phrase = phrase.replaceAll("&5", ChatColor.DARK_PURPLE + "");
		phrase = phrase.replaceAll("&6", ChatColor.GOLD + "");
		phrase = phrase.replaceAll("&7", ChatColor.GRAY + "");
		phrase = phrase.replaceAll("&8", ChatColor.DARK_GRAY+ "");
		phrase = phrase.replaceAll("&9", ChatColor.BLUE + "");
		phrase = phrase.replaceAll("&a", ChatColor.GREEN + "");
		phrase = phrase.replaceAll("&b", ChatColor.AQUA + "");
		phrase = phrase.replaceAll("&c", ChatColor.RED + "");
		phrase = phrase.replaceAll("&d", ChatColor.LIGHT_PURPLE + "");
		phrase = phrase.replaceAll("&e", ChatColor.YELLOW + "");
		phrase = phrase.replaceAll("&f", ChatColor.WHITE + "");
		phrase = phrase.replaceAll("&k", ChatColor.MAGIC + "");
		phrase = phrase.replaceAll("&l", ChatColor.BOLD + "");
		phrase = phrase.replaceAll("&o", ChatColor.ITALIC + "");
		phrase = phrase.replaceAll("&n", ChatColor.UNDERLINE + "");
		phrase = phrase.replaceAll("&m", ChatColor.STRIKETHROUGH + "");
		phrase = phrase.replaceAll("&r", ChatColor.RESET + "");
		return phrase;
	}
}
