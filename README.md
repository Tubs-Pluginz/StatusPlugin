# Tub's Status Plugin
![Static Badge](https://img.shields.io/badge/MC-1.20.1-green)


This is a Minecraft plugin for Spigot/Paper servers that allows players to set their own status, which is displayed in the tab list and above their heads in-game.
## Features

- Set your own status with `/status <status>`
- Remove your status with `/status remove`
- Set status with formatting codes (for example to color them) with `/status &3<status>`
- Set other players' statuses with `/status <player> <status>` (requires `StatusPlugin.admin.setStatus` permission)
- Remove other players' statuses with `/status remove <player>` (requires `StatusPlugin.admin.setStatus` permission)
- Reload all statuses from file with `/status reload`
- View a list of all available color codes with `/status help colors`
- View info about the plugin with `/status info`

- The status of every player is saved to a file, so they will keep their status when they rejoin the server.

## Permissions

- `StatusPlugin.setStatus`: Allows a player to set their own status and remove it. (default: `true`)
- `StatusPlugin.formatting:`: Allows a player to use formatting codes in their status (for example to color them). (default: `false`)
- `StatusPlugin.admin.setStatus`: Allows a player to set and remove other players' statuses. (default: `false`)
- `StatusPlugin.admin.reload`: Allows a player to reload all statuses.(default: `false`)

## Installation

To install the plugin, simply download the .jar file and place it in your server's plugins folder. Then, restart your server.

## Support

If you have any issues, please report. And if u have any suggestions, feel free to open an issue.
<br>
If you have any further plugin suggestions you can contact me via discord: `tubyoub`

## Contributing

I currently have no plans to get co-contributer's on this project, but if you have any suggestions, feel free to open an issue

## License

This project is licensed under the [AGPL-3.0 License](LICENSE).
