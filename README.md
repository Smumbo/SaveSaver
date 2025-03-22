# SaveSaver
SaveSaver is a Java program created to help automate the process of syncing your game saves to the cloud, as well as creating backups of them. I originally created it to be used with Minecraft, but it can theoretically be used with any standard game save setup. It requires no authentication directly, just a separate cloud sync client of your choice installed on your computer.

## Requirements
- [Java 8+](https://www.java.com/en/download/) must be installed and added to PATH
- A location to store your saves
  - This could be a Network-Attached Storage device, external hard drive, or a cloud storage provider with desktop sync client of your choice.
  - I use [OneDrive](https://www.microsoft.com/en-us/microsoft-365/onedrive/download), but [Google Drive](https://workspace.google.com/products/drive/#download) and [Dropbox](https://www.dropbox.com/install) should work as well. These programs create a directory on your file system where you can copy your save files to in order to effectively "upload" and "download" them.

## How to use
```
java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> [<backupCount>]...]

    arguments:
    <savePath>path to the game save folder

    options:
    -u, --upload      upload the save to the cloud. This will override the cloud save.
    -d, --download    download the save from the cloud. This will override the local save.
        <cloudPath>   path to the cloud save folder

    -b, --backup        create a backup of the save folder
        <backupPath>    where to save the backup to
        <backupNumber>  number of rotating backups to keep at path. Setting to 0 or omitting will keep all backups.
```

- Download the latest release from the [releases](https://github.com/Smumbo/SaveSaver/releases) page, and store in a safe location
- For each game you want to sync and/or backup:
  - Create a script to upload your game save. Run this every time after you close the game.
      - Example:  
    `java -jar SaveSaver.jar "C:\Users\<username>\AppData\Roaming\.minecraft\saves" -u "C:\OneDrive\Minecraft\saves"`  
  - Create a script to download your game save. Run this every time before you open the game.  
      - Example:  
    `java -jar SaveSaver.jar "C:\Users\<username>\AppData\Roaming\.minecraft\saves" -d "C:\OneDrive\Minecraft\saves"`  
  - Create a script to perform backups of your saves. Run this before you open the game, or after you close it.  
      - Example:  
    `java -jar SaveSaver.jar "C:\Users\<username>\AppData\Roaming\.minecraft\saves" -b "D:\Backups\Minecraft\saves" 3`
      - The backup number argument is optional, but I recommend setting it to some number (2-5 is a good balance) so you don't fill your storage.
      - Tip: You can perform as many backups as you want when running the program, as long as you specify another set of arguments (i.e. another `-b "<directory>" <number>` set)

Note: Please ensure that your cloud sync client has properly synced all files before running this program. If your client has not fully synced the files before you run this program, your save files could be corrupted. In case of this, creating regular backups is a good idea.

Tip: To automatically run these scripts, I would recommend using a launcher like [Playnite](https://playnite.link), which can run commands before a game launches and after it closes.

## More Usage Examples
I personally use this tool to sync and backup my Minecraft saves using [Prism Launcher](https://prismlauncher.org). By utilizing the launcher's built-in custom command functionality, you can set it up to download your saves before the game opens, and upload and backup your saves after the game closes. Here is how I set that up using Prism Launcher's environment variables:
- Open the settings for a specific instance that you want to backup and/or sync, or open Prism Launcher's settings
- Go to the Custom Commands tab
- For Pre-launch command, enter something like:  
  `java -jar SaveSaver.jar "$INST_MC_DIR\saves" -d "C:\OneDrive\PrismLauncher\$INST_ID\.minecraft\saves"`
  - You may have to specify the exact location where you stored the `SaveSaver.jar` file
- For Post-exit command, enter something like:  
  `java -jar SaveSaver.jar "$INST_MC_DIR\saves" -u "C:\OneDrive\PrismLauncher\$INST_ID\.minecraft\saves" -b "C:\OneDrive\Backups\PrismLauncher\$INST_ID\.minecraft\saves" 3 -b "E:\Backups\PrismLauncher\$INST_ID\.minecraft\saves" 5`
  - This command demonstrates creating backups to multiple locations, with different maximum amounts

Again, use whichever cloud storage provider you prefer, as long as it has a directory in your file system that it syncs to.

## Disclaimer
This tool was not designed to be used for mass game save sync/backup, and is intended to be setup on a per-game basis for games that don't already support cloud saves. For mass game backups, use [ludusavi](https://github.com/mtkennerly/ludusavi) or [GameSave Manager](https://www.gamesave-manager.com). For mass save cloud sync, [Open Cloud Sync](https://github.com/DavidDeSimone/OpenCloudSaves) may be a good option. This software is provided as-is. Use at your own risk.
