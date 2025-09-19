package hjsonpp.expand;

import arc.util.Time;
import hjsonpp.expand.meta.AdditionalStats;
import mindustry.world.blocks.defense.Wall;

public class RestorableWall extends Wall{
    // reload between healing
    public float healReload = 1f;
    // how much heal does wall recieve
    public float healPercent = 7f;

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
        boolean canHeal = true;

        @Override
        public void updateTile() {
            canHeal = true;
            charge += Time.delta;
            if(charge >= healReload && canHeal && health() < maxHealth()) {
                charge = 0f;
                if(health() >= maxHealth()) canHeal = false;

                heal((maxHealth() / 5) * (healPercent) / 100f);
                recentlyHealed();
            }
        }
    }
}
