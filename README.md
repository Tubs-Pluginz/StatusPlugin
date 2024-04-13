# Tub's Status Plugin
![Static Badge](https://img.shields.io/badge/MC-1.13-green)
![Static Badge](https://img.shields.io/badge/MC-1.14-green)
![Static Badge](https://img.shields.io/badge/MC-1.15-green)
![Static Badge](https://img.shields.io/badge/MC-1.16-green)
![Static Badge](https://img.shields.io/badge/MC-1.17-green)
![Static Badge](https://img.shields.io/badge/MC-1.18-green)
![Static Badge](https://img.shields.io/badge/MC-1.19-green)
![Static Badge](https://img.shields.io/badge/MC-1.20-green)

[![forthebadge](https://forthebadge.com/images/badges/works-on-my-machine.svg)](https://forthebadge.com)


This is a Minecraft plugin for Spigot/Paper servers that allows players to set their own status, which is displayed in the tab list and above their heads in-game.
## Features

- Set your own status with `/status <status>`
- Remove your status with `/status remove`
- Set status with formatting codes (for example to color them) with `/status &3<status> (/status help colorcodes)`
- Set other players' statuses with `/status <player> <status>` (requires `StatusPlugin.admin.setStatus` permission)
- Remove other players' statuses with `/status remove <player>` (requires `StatusPlugin.admin.setStatus` permission)
- Reload all statuses from file with `/status reload` (requires `StatusPlugin.admin.reload` permission) (Can be executed by console)
- View a list of all available color codes with `/status help colors`
- View info about the plugin with `/status info`
- Set a maximum length for statuses with `/status setmaxlength <length>` (requires StatusPlugin.admin.setMaxlength permission)
- Reset the maximum length of statuses to default (`15`) with `/status resetmaxlength` (requires StatusPlugin.admin.resetMaxlength permission)
  (Color codes in statuses are not counted towards the character limit)
- The status of every player is saved to a file, so they will keep their status when they rejoin the server.
- The Plugin supports `PlaceholderAPI v2.11.5`
  - The Plugin reloads statuses every 600 Game Ticks (30seconds) so the Placeholders can update themselves.

## Permissions

- `StatusPlugin.setStatus`: Allows a player to set their own status and remove it. (default: `true`)
- `StatusPlugin.admin.setStatus`: Allows a player to set and remove other players' statuses. (default: `false`)
- `StatusPlugin.admin.reload`: Allows a player to reload all statuses.(default: `false`)
- `StatusPlugin.admin.setMaxlength:` Allows a player to set the maximum length of statuses. (default: `false`)
- `StatusPlugin.admin.resetMaxlength:` Allows a player to reset the maximum length of statuses to default. (default: `false`)
- `StatusPlugin.formatting.color`: Allows a player to use color codes in their status. (default: `false`)
- `StatusPlugin.formatting.bold`: Allows a player to use bold formatting in their status. (default: `false`)
- `StatusPlugin.formatting.italic`: Allows a player to use italic formatting in their status. (default: `false`)
- `StatusPlugin.formatting.underlined`: Allows a player to use underlined formatting in their status. (default: `false`)
- `StatusPlugin.formatting.strikethrough`: Allows a player to use strikethrough formatting in their status. (default: `false`)
- `StatusPlugin.formatting.magic`: Allows a player to use obfuscated formatting in their status. (default: `false`)

## Installation

To install the plugin, simply download the .jar file and place it in your server's plugins folder. Then, restart your server.

## Support

If you have any issues, please report. And if u have any suggestions, feel free to open an issue.
<br>
If you have any further plugin suggestions you can contact me via discord: `tubyoub`

## Contributing

I currently have no plans to get co-contributer's on this project, but if you have any suggestions, feel free to open an issue

## License

This project is licensed under the [MIT License](LICENSE).

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)
