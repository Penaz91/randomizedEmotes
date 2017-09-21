package randomizedEmotes;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class main extends JavaPlugin{
	public static ConfigurationSection emotes = null;
	public static Random rndGen = new Random();
	public static Configuration config = null;
	public static String defaultPermissionMessage = "You don't have permissions to use this emote";
	@Override
	public void onEnable(){
		config = this.getConfig(); //loads the config
		emotes = config.getConfigurationSection("emotes"); //loads the emote list as ConfigurationSection
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
	}
	
	@Override
	public void onDisable(){}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (cmd.getName().equalsIgnoreCase("randemote")){
			// If called with no arguments it just shows Plugin infos and emote list
			if (args.length == 0){
				sender.sendMessage(ChatColor.GOLD + "------<->-----RandomizedEmotes-----<->------");
				sender.sendMessage(ChatColor.GOLD + "A plugin by: " + ChatColor.DARK_RED + "Penaz");
				sender.sendMessage(ChatColor.GOLD + "--------------------<->--------------------");
				sender.sendMessage(ChatColor.GOLD + "Usage: " + ChatColor.DARK_PURPLE + "/randemote" + ChatColor.GRAY + " <emote name>");
				sender.sendMessage(ChatColor.GOLD + "--------------------<->--------------------");
				Set<String> list = emotes.getKeys(false);
				sender.sendMessage(ChatColor.GOLD + "Available emote names:");
				//Using a stringbuilder to make it easier to have a decent-ish list of emotes to show.
				StringBuilder emotelist = new StringBuilder();
				for (String s: list){
					emotelist.append(ChatColor.GOLD + s);
					emotelist.append(ChatColor.DARK_PURPLE + ", ");
				}
				sender.sendMessage(emotelist.toString());
				return true;
			}
			// If calles with an argument I look it it's inside the ConfigurationSection that contains all the emotes
			if (args.length == 1){
				//Emote things here
				if (emotes.contains(args[0])){
					if (sender.hasPermission("randEmotes.emote."+args[0])){
						List<String> section = emotes.getStringList(args[0]);
						String phrase = section.get(rndGen.nextInt(section.size()));
						phrase = colorize(phrase);
						Player pl = (Player) sender;
						phrase = phrase.replaceAll("\\$player\\$", pl.getDisplayName());
						@SuppressWarnings("deprecation")
						Player snd = Bukkit.getPlayer(sender.getName());
						// I get all the entities in a cubic radius defined in the config
						List<Entity> lst = snd.getNearbyEntities(config.getInt("radius"), config.getInt("radius"), config.getInt("radius"));
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
						for (Entity e: lst){
							if (e instanceof Player){
								e.sendMessage(phrase);
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

	private String colorize(String phrase) {
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
