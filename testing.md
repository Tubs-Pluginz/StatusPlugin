# Tubs Status Plugin Testing Flow

This document outlines a testing flow for the Tubs Status Plugin after the refactoring, covering various functionalities including custom statuses, group mode, admin commands, and permissions.

## Prerequisites

1.  Install the latest build of the Tubs Status Plugin on a Spigot/Paper server (version 1.13+).
2.  Ensure PlaceholderAPI and LuckPerms are installed if you want to test those integrations.
3.  Start the server to generate the default `config.yml` and `statuses.yml` files.
4.  Shut down the server.
5.  Replace the default `config.yml` with the example configuration provided below.
6.  Start the server again.

## Example `config.yml`

```yaml
################################
#      Tub's StatusPlugin      #
#          by TubYoub          #
################################
# Don't change this value, it's changed by the plugin if needed
fileversion: 5

# Check for updates on startup
# default: true
checkUpdate: true

# Choose which logging level should be used
# It depends on how much you want you're console to be filled with information from TSP
# For production you can safely set it to a higher level but for development/testing it is recommended to use a lower level
# 10 = Debug
# 20 = Info
# 30 = Warning
# 40 = Error
# default: 20
loggerLevel: 10 # Set to DEBUG for detailed testing logs

# maximum Character length a Status should be allowed to have.
# default: 15
maxStatusLength: 15

# If the Chat formatter should be enabled (so the Plugin sends Messages with the Status in front of the Player name and formats colors).
# default: true
chatFormatter: true

# If the Tablist name should be changed by the plugin or not. (restart your server so the changes will work correctly)
# default: true
changeTablistNames: true

# Enable group mode for statuses
# When enabled, players can only choose from predefined status groups
# default: false
groupMode: false # Start with group mode off

# Opening and closing characters for the status
# default: '[' & ']'
openingCharacter: '<'
closingCharacter: '>'

# Define status groups
# Each group has a name and a status
# You can also define specific permissions required to use a group.
# If 'permissions' is empty or not present, the general 'StatusPlugin.group.set' permission will be checked.
statusGroups:
  admin_group:
    status: '&cAdmin'
    permissions:
      - 'StatusPlugin.group.set.admin_group'
  vip_group:
    status: '&eVIP'
    permissions:
      - 'StatusPlugin.group.set.vip_group'
  default_group:
    status: '&aOnline'
    # No specific permissions defined, will use StatusPlugin.group.set
```

## Test Players

*   **Tester (Non-OP):** A regular player account with default permissions.
*   **Admin Tester (OP):** A player account with operator privileges or the `StatusPlugin.admin` permission node.
* 
## Testing Flow - Step-by-Step

Perform the following steps with your `Tester` and `Admin Tester` accounts logged into the server. Observe the console logs (set to DEBUG level in the config) for detailed plugin behavior.

### Phase 1: Group Mode Off (Default `config.yml`)

1.  **Connect as Tester:**
    *   Check your current status: `/status` (Should be empty).
    *   Set a custom status: `/status &bHello!`
        *   *Expected:* Your tab list name and chat status should update to reflect `<&bHello!> [YourName]`.
        *   *Verify:* Check tab list and send a chat message.
    *   Set a longer custom status: `/status ThisIsAveryLongStatus.`
        *   *Expected:* You should receive a message saying the status is too long.
        *   *Verify:* Check chat.
    *   Set a status with formatting codes: `/status &l&nBoldUnderlined`
        *   *Expected:* If you have `StatusPlugin.formatting.bold` and `StatusPlugin.formatting.underline` permissions (default for OP, may need to grant to Tester), the status should show bold and underlined. If not, the formatting should be stripped.
        *   *Verify:* Check tab list and chat.
    *   Try to set another player's status: `/status AdminTester SomeStatus`
        *   *Expected:* You should receive a "no permission" message.
        *   *Verify:* Check chat.
    *   Remove your status: `/status remove`
        *   *Expected:* Your status should be removed, and your tab list name and chat format should revert.
        *   *Verify:* Check tab list and chat.
    *   Try to remove another player's status: `/status remove AdminTester`
        *   *Expected:* You should receive a "no permission" message.
        *   *Verify:* Check chat.
    *   Try to use group commands: `/status admin_group`
        *   *Expected:* You should receive a message indicating group mode is not enabled.
        *   *Verify:* Check chat.

2.  **Connect as Admin Tester:**
    *   Check your current status: `/status` (Should be empty initially, unless you set one).
    *   Set a custom status: `/status &aAdmin Status`
        *   *Expected:* Your status should update.
        *   *Verify:* Check tab list and chat.
    *   Set a longer custom status: `/status This status should be too long for the default limit.`
        *   *Expected:* You should receive a "too long" message.
        *   *Verify:* Check chat.
    *   Set another player's status: `/tsp setstatus Tester &dTesting`
        *   *Expected:* Tester's status should be set to `<&dTesting> Tester` (check Tester's client and the console log). You should receive a confirmation message.
        *   *Verify:* Check chat and console logs.
    *   Remove another player's status: `/tsp remove Tester`
        *   *Expected:* Tester's status should be removed (check Tester's client and console). You should receive a confirmation message.
        *   *Verify:* Check chat and console logs.
    *   Reload the plugin: `/tsp reload`
        *   *Expected:* Console should show reload messages. Statuses should persist.
        *   *Verify:* Check console.
    *   Check plugin info: `/tsp info`
        *   *Expected:* Plugin version and author information should be displayed.
        *   *Verify:* Check chat.
    *   Check help: `/tsp help` and `/tsp help colorcodes`
        *   *Expected:* List of commands and color codes should be displayed.
        *   *Verify:* Check chat.
    *   Set max status length: `/tsp setmaxlength 30`
        *   *Expected:* Max length should be updated. You should receive a confirmation message.
        *   *Verify:* Check chat and console logs.
    *   As Tester (still connected), try the long status again: `/status This is a very long status that should now fit.`
        *   *Expected:* The status should now be set.
        *   *Verify:* Check tab list and chat.
    *   Reset max status length: `/tsp resetmaxlength`
        *   *Expected:* Max length should revert to 15. You should receive a confirmation message.
        *   *Verify:* Check chat and console logs.
    *   As Tester (still connected), try the long status again: `/status This is a very long status that should now be too long again.`
        *   *Expected:* You should receive a "too long" message.
        *   *Verify:* Check chat.
    *   Try to use group commands: `/status admin_group`
        *   *Expected:* You should receive a message indicating group mode is not enabled.
        *   *Verify:* Check chat.

### Phase 2: Group Mode On

1.  **Shut down the server.**
2.  **Edit `config.yml`:** Change `groupMode: false` to `groupMode: true`.
3.  **Start the server.**
4.  **Connect as Tester:**
    *   Check your current status: `/status` (Should be empty).
    *   Try setting a custom status: `/status My Custom Status`
        *   *Expected:* You should receive a message indicating group mode is enabled and you need to use a group.
        *   *Verify:* Check chat.
    *   Try removing your status: `/status remove`
        *   *Expected:* You should receive a message indicating this command is not usable in group mode or a similar message.
        *   *Verify:* Check chat.
    *   Try to set a group status you don't have permission for (e.g., `admin_group`): `/status admin_group`
        *   *Expected:* You should receive a "no permission" message.
        *   *Verify:* Check chat.
    *   Set a group status you have general permission for (`default_group` - assuming `StatusPlugin.group.set` is granted): `/status default_group`
        *   *Expected:* Your status should be set to `<&aOnline> [YourName]`.
        *   *Verify:* Check tab list and chat.
    *   Try to set a group status you have specific permission for (`vip_group` - grant `StatusPlugin.group.set.vip_group` to Tester if needed): `/status vip_group`
        *   *Expected:* Your status should be set to `<&eVIP> [YourName]`.
        *   *Verify:* Check tab list and chat.
    *   As Tester, try using `/tsp` commands you don't have permission for (e.g., `/tsp reload`, `/tsp setmaxlength`).
        *   *Expected:* You should receive "no permission" messages.
        *   *Verify:* Check chat.

5.  **Connect as Admin Tester:**
    *   Check your current status: `/status` (Should be empty initially).
    *   Try setting a custom status: `/status Admin Custom Status`
        *   *Expected:* You should receive a message indicating group mode is enabled and you need to use a group (unless you use the `/tsp setstatus` command).
        *   *Verify:* Check chat.
    *   Set a group status using the `/status` command: `/status admin_group`
        *   *Expected:* Your status should be set to `<&cAdmin> [YourName]` (assuming you have `StatusPlugin.group.set.admin_group` or `StatusPlugin.admin.setStatus`).
        *   *Verify:* Check tab list and chat.
    *   Try removing your status using `/status remove`: `/status remove`
        *   *Expected:* You should receive a message that this command is not usable in group mode.
        *   *Verify:* Check chat.
    *   Remove your status using the admin command: `/tsp remove AdminTester`
        *   *Expected:* Your status should be removed. You should receive a confirmation message.
        *   *Verify:* Check tab list, chat, and console.
    *   Set another player's group status using the admin command: `/tsp setstatus Tester vip_group`
        *   *Expected:* Tester's status should be set to `<&eVIP> Tester`. You should receive a confirmation message.
        *   *Verify:* Check Tester's client, your chat, and console logs.
    *   Set another player's custom status using the admin command (even in group mode): `/tsp setstatus Tester &6ForcedCustom`
        *   *Expected:* Tester's status should be set to `<&6ForcedCustom> Tester`. You should receive a confirmation message. This bypasses group mode for admins.
        *   *Verify:* Check Tester's client, your chat, and console logs.
    *   As Admin Tester, test all `/tsp` commands again (`/tsp reload`, `/tsp setmaxlength`, `/tsp resetmaxlength`, `/tsp info`, `/tsp help`).
        *   *Expected:* All commands should work as expected with admin permissions.
        *   *Verify:* Check chat and console.

### Phase 3: Persistence

1.  **With both players having statuses set (e.g., Tester with a group status, Admin Tester with a custom status via `/tsp setstatus`), shut down and restart the server.**
    *   *Expected:* Upon joining, both players should retain the statuses they had before the shutdown.
    *   *Verify:* Log back in as both players and check tab list and chat.

### Phase 4: Console Commands

1.  **Run the following commands from the server console:**
    *   `tsp help`
        *   *Expected:* The general help message should be displayed in the console.
    *   `tsp info`
        *   *Expected:* Plugin info should be displayed in the console.
    *   `tsp reload`
        *   *Expected:* Reload messages should appear in the console.
    *   `tsp setmaxlength 50`
        *   *Expected:* Max length should be set to 50. Confirmation message in console.
    *   `tsp resetmaxlength`
        *   *Expected:* Max length should revert to 15. Confirmation message in console.
    *   `tsp setstatus Tester ConsoleStatus`
        *   *Expected:* Tester's status should be set to `<ConsoleStatus> Tester`. Confirmation message in console. Check Tester's client.
    *   `tsp remove Tester`
        *   *Expected:* Tester's status should be removed. Confirmation message in console. Check Tester's client.
    *   `status Tester SomeStatus`
        *   *Expected:* Console should receive a message that `/status` is for players only.
    *   `status remove Tester`
        *   *Expected:* Console should receive a message that `/status remove` is for players only.

## Tab Completion Testing

While in-game as both Tester and Admin Tester, test tab completion for the `/status` and `/tsp` commands at various argument levels to ensure only relevant and permitted suggestions appear.

*   **`/status <TAB>`:**
    *   *Group Mode Off:* Should suggest `remove`.
    *   *Group Mode On (Tester):* Should suggest groups Tester has permission for (`default_group`, `vip_group`).
    *   *Group Mode On (Admin Tester):* Should suggest all groups (`admin_group`, `vip_group`, `default_group`).
*   **`/tsp <TAB>`:**
    *   *Tester:* Should suggest `help`, `info`.
    *   *Admin Tester:* Should suggest `help`, `info`, `reload`, `remove`, `setstatus`, `setmaxlength`, `resetmaxlength`.
*   **`/tsp remove <TAB>`:**
    *   *Tester:* No suggestions or command not found.
    *   *Admin Tester:* Should suggest online player names.
*   **`/tsp setstatus <TAB>`:**
    *   *Tester:* No suggestions or command not found.
    *   *Admin Tester:* Should suggest online player names.
*   **`/tsp setstatus <PlayerName> <TAB>`:**
    *   *Tester:* No suggestions or command not found.
    *   *Admin Tester:*
        *   *Group Mode Off:* No suggestions (or just general text completion if the client does it).
        *   *Group Mode On:* Should suggest group names.
*   **`/tsp setmaxlength <TAB>`:**
    *   *Tester:* No suggestions or command not found.
    *   *Admin Tester:* Should suggest lengths like `10`, `20`, `30`.
*   **`/tsp help <TAB>`:** Should suggest `colorcodes`.
