# Trophy Manager

<a href="https://www.curseforge.com/minecraft/mc-mods/trophymanager/files"><img src="https://img.shields.io/badge/Available%20for-MC%201.16.5+-c70039" alt="Supported Versions"></a>
<a href="https://github.com/JDKDigital/trophymanager/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDKDigital/productive-bees?style=flat&color=900c3f" alt="License"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/trophymanager"><img src="http://cf.way2muchnoise.eu/short_trophymanager.svg" alt="Curseforge Downloads"></a><br><br>

This mod lets you define your own trophies and hand then out as quest rewards.



You can make a trophy for any entity and any item in the game including modded ones. The mod does not include a way to obtain trophies by default, you will need to either add your own recipes or use a mod like FTB Quests to give out custom trophy items.



A trophy is made with a trophymanager:trophy item and a number of NBT tags. In the creative menu you can find all the vanilla mobs as trophies, but you are encouraged to create your own as well.



Here's an example of the NBT required for a trophy with a wooden hoe

{ "TrophyType": "item", "TrophyItem": { "id": "minecraft:wooden_hoe" }, "Name": "Super Hoe Trophy" }
and an example of a big creeper

{ "TrophyType": "entity", "TrophyEntity": { "entityType": "minecraft:creeper" }, "Scale": 2.0, "Name": "Creeper Trophy" }


You can also pick the base block

{ "TrophyType": "entity", "TrophyEntity": { "entityType": "minecraft:wither" }, "OffsetY": 0.8, "BaseBlock": "minecraft:diamond_block", "Name": "Wither Trophy" }


"Scale" and "OffsetY" are optional and defaults to 0.5 for both.



By default, the trophy base can be changed by right clicking with any slab block. The list of blocks you can use as base blocks is defined in the block tag trophymanager:trophy_base



Example of a give command

/give <player> trophymanager:trophy[minecraft:custom_data={ "TrophyType": "entity", "TrophyEntity": { "entityType": "minecraft:wither" }, "Scale": 0.75, "OffsetY": 0.25, "BaseBlock": "minecraft:quartz_slab", "Name": "Wither Trophy" }]



Recipe examples can be found in the datapack file linked with each release.
