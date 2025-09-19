package hjsonpp.expand.meta;

import mindustry.world.meta.Stat;
import mindustry.world.meta.StatCat;

public class AdditionalStats{
    public static Stat
            healPercent = new Stat("heal-percent", StatCat.general),
            produceChance = new Stat("produce-chance", StatCat.crafting),
            reloadFrom = new Stat("expansion-reload-at-start", Stat.reload.category),
            reloadTo = new Stat("expansion-reload-at-end", Stat.reload.category);
}
