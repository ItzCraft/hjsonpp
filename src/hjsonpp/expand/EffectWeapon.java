package hjsonpp.expand;


import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.entities.units.WeaponMount;
import mindustry.gen.Unit;
import mindustry.type.Weapon;

public class EffectWeapon extends Weapon {
    // the list of effects
    public Effect effect;
    // interval between showup of effects
    public float effectInterval = 60;
    // X of displayed effects
    public float effectX = 0;
    // Y of displayed effects
    public float effectY = 0;

    public float effectChance = 1f;

    private float effectTimer = 0f;


    public EffectWeapon(String name){
        super(name);
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        super.update(unit, mount);
        effectTimer += Time.delta;
        float mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y);
        float mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);
        float weaponRotation = unit.rotation - 90 + (rotate ? mount.rotation : baseRotation);
        float wX = mountX + Angles.trnsx(weaponRotation, this.effectX, this.effectY);
        float wY = mountY + Angles.trnsy(weaponRotation, this.effectX, this.effectY);
        if(effectTimer >= effectInterval){
            effectTimer = 0;
            if(Mathf.chance(effectChance)) effect.at(wX, wY, mount.rotation + unit.rotation());
        }
    }
}
