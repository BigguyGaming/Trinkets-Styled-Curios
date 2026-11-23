
# Trinkets Styled Curios

Trinkets Styled Curios is a NeoForge 1.21.1 Curios add-on. It presents the player's real Curios
slots around related vanilla equipment slots in the normal inventory screen. It does not create or
copy an accessory inventory: every displayed slot is backed directly by the Curios handler.

## Datapack slot assignments

Assignment files are loaded from
`data/<namespace>/trinkets_styled_curios/slot_assignments/*.json`. Each Curios slot identifier can
be assigned to one of `head`, `chest`, `legs`, `feet`, or `offhand`:

```json
{
  "replace": false,
  "chest": ["back", "charm"],
  "offhand": ["hand", "ring"]
}
```

Files are merged in resource order. Set `replace` to `true` to clear assignments loaded before that
file. Any identifier not assigned by a datapack always falls back to `offhand`; hand-related slots
are assigned there by the bundled default datapack. Server assignments are synchronized to clients.

## Development

Use Java 21. Import the project as a Gradle project in IntelliJ IDEA, or run
`./gradlew build` to build the JAR and `./gradlew runClient` to launch the development client.
Gradle downloads the compile dependencies. Curios is required at runtime.
EMI is optional. To test EMI compatibility in the development client, put its NeoForge
1.21.1 JAR in `run/mods/`.
