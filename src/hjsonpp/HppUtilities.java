package hjsonpp;

import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.struct.IntSet;
import arc.util.Tmp;
import mindustry.core.World;
import mindustry.entities.Units;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Posc;

import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

public class HppUtilities {
    private static final IntSet collidedBlocks = new IntSet();

    public static void blackHoleUpdate(Team t, Posc owner, float damageRadius, float pullStrength, float damage, float armorMultiplier, float damageMultiplier, float buildingDamageMp){
        float x = owner.x(), y = owner.y();
        completeDamage(t,owner.x(), owner.y(), damageRadius, damage * damageMultiplier, buildingDamageMp, armorMultiplier == -1, armorMultiplier);
        Units.nearbyEnemies(t, owner.x() - damageRadius, owner.y() - damageRadius, damageRadius * 2, damageRadius * 2, u ->{
            float pullingRadius = damageRadius + u.hitSize / 2;
            if(!u.type.internal && u.hittable() && u.within(x, y, pullingRadius) && owner != u){
                Vec2 impulse = Tmp.v1.trns(u.angleTo(x, y), pullStrength + (1f - u.dst(x, y) / pullingRadius) * pullStrength);
                u.impulseNet(impulse);
            }
        });
    }

    public static void completeDamage(Team team, float x, float y, float radius, float damage, float buildingDamageMultiplier, boolean pierceArmor, float armorMultiplier){
        Units.nearbyEnemies(team, x - radius, y - radius, radius * 2f, radius * 2f, unit -> {
            if(!unit.dead && unit.hittable() && unit.within(x, y, radius + unit.hitSize / 2f)){
                if(pierceArmor){
                    unit.damagePierce(damage * (1f - unit.dst(x, y) / radius));
                }else if(armorMultiplier != 1){
                    unit.damage(damage * (1f - unit.dst(x, y) / radius));
                } else unit.damageArmorMult(damage * (1f - unit.dst(x, y) / radius), armorMultiplier);
            }
        });

        trueEachBlock(x, y, radius, build -> {
            if(build.team != team && !build.dead && build.block != null){
                if(pierceArmor){
                    build.damagePierce(damage * buildingDamageMultiplier * (1f - build.dst(x, y) / radius));
                }else if(armorMultiplier != 1){
                    build.damage(damage * buildingDamageMultiplier * (1f - build.dst(x, y) / radius));
                } else build.damageArmorMult(damage * buildingDamageMultiplier, armorMultiplier * (1f - build.dst(x, y) / radius));
            }
        });
    }

    public static void trueEachBlock(float wx, float wy, float range, Cons<Building> cons){
        collidedBlocks.clear();
        int tx = World.toTile(wx);
        int ty = World.toTile(wy);

        int tileRange = Mathf.floorPositive(range / tilesize);

        for(int x = tx - tileRange - 2; x <= tx + tileRange + 2; x++){
            for(int y = ty - tileRange - 2; y <= ty + tileRange + 2; y++){
                if(Mathf.within(x * tilesize, y * tilesize, wx, wy, range)){
                    Building other = world.build(x, y);
                    if(other != null && !collidedBlocks.contains(other.pos())){
                        cons.get(other);
                        collidedBlocks.add(other.pos());
                    }
                }
            }
        }
    }
}
