# Lidless

See inside containers without opening them. Look at a chest and a panel shows what is inside. Shulker boxes show their contents in the tooltip, and storage screens get a search box and sort buttons.

Lidless is a small client-side mod for Minecraft on Fabric or NeoForge, compatible with Mod Menu (Fabric) / the built-in config screen (NeoForge).

Supported versions: 26.1 to 26.3, plus the 26.4 snapshot on Fabric.

## Features

- **Peek panel** - look at a chest, double chest, barrel, shulker box, hopper, dispenser or ender chest to see its contents. It also works on donkeys, mules and llamas carrying chests, and on chest boats and chest minecarts.
- **Compact mode** - groups identical items and shows totals instead of the exact slot layout.
- **Loot chests** - chests that still hold unrolled loot are marked as unopened loot.
- **Tooltip previews** - shulker boxes show their contents as a grid tinted in the box's colour, and the ender chest item shows your ender chest.
- **Search** - a search box on storage screens. Ctrl+F focuses it, `@name` searches by mod, and it also finds items inside shulker boxes and bundles.
- **Sorting** - a sort button for the container and one for your inventory, with four orders: category, name, count and mod. Middle-click a slot to sort that part of the screen.
- **Deposit and take all** - move the items the container already holds out of your inventory, or take everything out in one click. Your hotbar is left alone.
- **Placement** - two sliders move the panel anywhere on screen, and the panel can be limited to while you sneak.
- **Client-side only** - there is nothing to install on a server.

Some information depends on where you play:

- In singleplayer and on a LAN host the panel reads the contents live.
- Servers do not send container contents until you open them, so on a server the panel shows what was inside the last time you opened that container, and how long ago that was. The ender chest tooltip works the same way.
- Middle-click sorting is off in creative mode, where middle-click copies items.

## Install

**Fabric**

1. Install [Fabric Loader](https://fabricmc.net/use/) for your version of Minecraft.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and the Fabric Lidless jar for your version in your `mods` folder.
3. Start the game.

Optional: add [Mod Menu](https://modrinth.com/mod/modmenu) to get a settings screen (*Mods > Lidless > Configure*).

**NeoForge**

1. Install [NeoForge](https://neoforged.net/) for your version of Minecraft.
2. Put the NeoForge Lidless jar for your version in your `mods` folder.
3. Start the game. The settings screen is available from the mod list (*Mods > Lidless > Config*).

## Building

`./gradlew build -Pminecraft_version=26.3` builds the Fabric jar for one version; add `-Ploader=neoforge` for NeoForge. `./gradlew runClientGameTest -Pminecraft_version=26.3` runs the in-game test on Fabric.
