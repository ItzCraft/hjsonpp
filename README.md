![image](assets/icon.png)

# Hjson++

a library that adds new classes to work with!
classes that are added:

- AdvancedConsumeGenerator - you can now output items/liquids and make craft progress bar. 
variables: outputItem/Items/Liquid/Liquids, progressBar: true/false
- AdvancedHeaterGenerator - basically the same version as HeaterGenerator but have same things as previous class
variables: outputItem/Items/Liquid/Liquids, progressBar (boolean)
- TileGenerator - placeable on only specific tiles
variables: filter: []
- AdvancedCoreBlock - basically you can use drawers
variables: drawers
- GeneratorCoreBlock - basic core block but it can generate power
variables: powerProduction: n
- AccelTurret - turret which have acceleration
variables: speedUpPerShoot: n, maxAccel: n, cooldownSpeed: n, canOverheat: false/true, overheatMultiplier: n, maxOverheatThreshold: n,overheatTime: n, overheatEffectChance: n, overheatEffect: effect
- DrawTeam - drawer class. Draws -team sprite
- ColliderCrafter - basic GenericCrafter but it outputs items/liquids with specific chance.
variables: produceChance: n (1=100%)
- OverHeatTurret - Turret which overheats after some shoots.
variables: overHeatAmount: n, timeToCooldown: n
- AdjustableShieldWall - Basic shield wall, but you can turn it on and off. and can justify shield radius
variables: radius: n
- TiledFloor - deprecated Anuken class. Can make big floors (64x64 etc)
variables: tilingVariants: n, tilingSize: n
- AdjustableBeamNode - you can create infirmity nodes from beam node and change it angle (highly recommended to check examples folder)
variables: beamDirections: [[n,n]]

- ## NEW!
- MultiCrafter - GenericCrafter with multiple recipes available. check examples folder for more information.
- DrawableBlock - Just Block type, but you can use drawers
variables: drawers: []
- RestorableWall - Wall which can heal itself. 
variables: healReload: n, healPercent: n
-  You can now make dialogues through world processors. check it by yourself in game for more information

check examples folder for more information



# WARNING
Report all errors to [IJT team mods discord server](https://discord.gg/btUe3rhGuQ)
