# randomizedEmotes For Spigot 1.12

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT) ![Status:Archived](https://img.shields.io/badge/Status-Archived-inactive)

randomizedEmotes is a small Spigot plugin that takes a random emote from a
certain category defined in the configuration and gives it out to all the
players in a certain radius. This is great if paired with any CommandItems
plugin.

### Notes on branches

If you want to use real player names inside the messages instead of Displaynames (which show nicks), you can switch to the RealPlayerNames branch.

### Commands

- **/randemote|/re**: Shows the plugin information and the list of emotes available
- **/randemote [name] | /re [name]**: Performs the emote in the <name> category
- **/rer|/randemotereload**: Reloads the configuration files
- **/emotegui|/egui**: Shows the Emote List in a GUI

### Permissions

- **randomEmote.use**: Permission available to use any command
- **randomEmote.emote.[emotename]**: Single permission to use the [emotename] emote
- **randomEmote.reload** Permission to reload the plugin's configuration
- **randomEmote.gui** Permission to use the GUI

### Default emotes

- **coin**: Flips a coin
- **d20**: Throws a D20, useful for a minecraft session of DnD

##### License and other stuff

I'm fine with commercial usage of this plugin, as well as any kind of modification
to the source code, as long as I'm quoted as the original author of this plugin.
