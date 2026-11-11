# Minecraft Kit Menu Plugin

KitMenu Pro is a Minecraft plugin for Paper servers that provides a graphical kit menu, kit creation, inventory kit management, kit permissions, pagination, and per-player kit layout saving. Players can open `/kit`, select a configured kit, receive its items in the correct inventory slots, and save their preferred item organization for future selections.

## Install

1. Build or obtain the plugin JAR.
2. Stop your Paper Minecraft server.
3. Place the plugin JAR in the server's `plugins` directory.
4. Start or restart the server.
5. The plugin creates its data files in `plugins/KitMenu/`:
   - `kits.yml` stores kits and their items.
   - `layouts.yml` stores player-specific kit layouts.

Requirements:

- Paper 1.21.11 or a compatible Paper server.
- Java 21 or newer.
- The `kit.admin` permission for kit creation and deletion.

## Use

Open the kit menu:

```text
/kit
```

Click a kit in the graphical menu to receive its items. Kits are displayed over multiple pages when more than 45 kits exist.

Create a kit:

1. Place the desired items in your

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/0d9a4d86-b90c-401b-bd79-a6f36ec80ffc?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=0d9a4d86-b90c-401b-bd79-a6f36ec80ffc
