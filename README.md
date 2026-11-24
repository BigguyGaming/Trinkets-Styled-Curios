This was entirely inspired by the [Trinkets](https://www.curseforge.com/minecraft/mc-mods/trinkets) mod made by [EmilyPloszaj](https://www.curseforge.com/members/emilyploszaj/projects), made to work under Curios on NeoForge. Additionally it adds some QOL additions to make it fit into vanilla better, as well as create parity with Curios and vanilla as a whole. It supports datapack configuration of layouts. Most features of this mod are configurable!

Assignment files are loaded from `data//trinkets_styled_curios/slot_assignments/*.json`. Each Curios slot identifier can be assigned to one of `head`, `chest`, `legs`, `feet`, or `offhand`:

```
{
  "replace": false,
  "chest": ["back", "charm"],
  "offhand": ["hand", "ring"]
}
```

This mod also sets the `enableButton` setting in `config/curios-client.toml` to `False` by default, if you want to see the default button again you have to change it yourself in the Curios config!!

## 🤖<span style="color:#f1c40f">AI Disclamer</span>

<span style="color:#f1c40f">AI was used for the coding aspect of this mod, the textures for the slots are reused from Trinkets under MIT License, and the mod icon was created by me, using the Trinkets icon and the Curios icon as a basis. The original copyright notice and license are included in the JAR.</span>
