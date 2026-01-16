package hjsonpp.expand.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class AdditionalStats{
    public static Stat
            healPercent = new Stat("heal-percent", StatCat.general),
            produceChance = new Stat("produce-chance", StatCat.crafting),
            recipes = new Stat("recipes", StatCat.crafting);
}
