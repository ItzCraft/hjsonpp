package hjsonpp.expand;

import arc.util.Time;
import hjsonpp.expand.meta.AdditionalStats;
import mindustry.world.blocks.defense.Wall;

public class RestorableWall extends Wall{
    // reload between healing
    public float healReload = 1f;
    // how much heal does wall recieve
    public float healPercent = 7f;

    public float healAmount = Float.NEGATIVE_INFINITY;

    public RestorableWall(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(AdditionalStats.healPercent, healPercent);
    }

    public class RestorableWallBuild extends WallBuild {
        public float charge = 0;

        @Override
        public void updateTile(){
            charge += Time.delta;
            if(charge >= healReload && health() < maxHealth() && canConsume()) {
                charge = 0f;
                if(healAmount == Float.NEGATIVE_INFINITY) {
                    heal(maxHealth() / 100 * healPercent);
                } else  {
                    heal(healAmount);
                }
                recentlyHealed();
            }
        }
    }
}
