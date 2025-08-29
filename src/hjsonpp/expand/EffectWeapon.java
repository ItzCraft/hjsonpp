package hjsonpp.expand;


import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class EffectWeapon extends Weapon {
    // the list of effects
    public Effect[] effects = new Effect[]{};
    // interval between showup of effects
    public float effectInterval = 60;
    // X of displayed effects
    public float effectX = 0;
    // Y of displayed effects
    public float effectY = 0;


    private float effectTimer = 0f;

    public EffectWeapon(String name){
        super(name);
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);
        effectTimer += Time.delta;

        if(effectTimer >= effectInterval){
            effectTimer = 0f;
            for(Effect eff : effects){
                eff.at(unit.x + effectX, unit.y + effectY, unit.rotation + mount.rotation);
            }
        }
    }

    @Override
    public void draw(Unit unit, WeaponMount mount){
        super.draw(unit, mount);
    }
}
