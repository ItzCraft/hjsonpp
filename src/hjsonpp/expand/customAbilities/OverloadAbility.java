package hjsonpp.expand.customAbilities;

import arc.Core;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.content.StatusEffects;
import mindustry.entities.Effect;
import mindustry.entities.*;
import mindustry.entities.abilities.Ability;
import mindustry.entities.bullet.BulletType;
import mindustry.gen.Sounds;
import mindustry.gen.Unit;
import mindustry.type.StatusEffect;

public class OverloadAbility extends Ability {
    public float duration = 60, reload = 100;
    public StatusEffect applyStatus = StatusEffects.fast;
    public boolean onShoot = false;
    public Effect activeEffect = Fx.overdriveWave;
    public float x, y = 0;
    public boolean parentizeEffects = true;
    public Color color = Color.valueOf("e0ecff");

    public float shootInterval = 3;

    public Sound shootSound = Sounds.shootArc;

    public @Nullable BulletType bullet;

    public float bulletAngle = 0f, bulletSpread = 0f;

    public int lightningLength = 5;

    public float damage = 10;

    protected float shootTimer;

    protected float timer;

    protected boolean overloading = false;

    protected float overloadTimer;

    public OverloadAbility(){
    }

    @Override
    public void addStats(Table t){
        super.addStats(t);
        t.add(abilityStat("firingrate", Strings.autoFixed(60f / reload, 2)));
        t.row();
        t.add((applyStatus.hasEmoji() ? applyStatus.emoji() : "") + "[stat]" + applyStatus.localizedName);
        t.row();
        t.add(Core.bundle.format("bullet.damage", damage));
    }

    @Override
    public void update(Unit unit){
        if(!overloading) {
           if((!onShoot || unit.isShooting)) timer += Time.delta;
        } else {
            shootTimer += Time.delta;
            if(shootTimer >= shootInterval){
                shootTimer = 0;
                strikeL(unit);
            }
            overloadTimer -= Time.delta;
            if(overloadTimer <= 0){
                overloadTimer = 0;
                overloading = false;
            }
        }

        if(timer >= reload && (!onShoot || unit.isShooting)){
            overloading = true;
            unit.apply(applyStatus, duration);

            float ex = unit.x + Angles.trnsx(unit.rotation, this.x, this.y), ey = unit.y + Angles.trnsy(unit.rotation, this.x, this.y);
            activeEffect.at(ex, ey, unit.rotation, color, parentizeEffects ? unit : null);

            timer = 0f;
            overloadTimer = duration;
        }
    }

    public void strikeL(Unit unit){
        shootSound.at(unit.x, unit.y);
        if(lightningLength > 0){
            float lx = unit.x + Angles.trnsx(unit.rotation, this.y, this.x), ly = unit.y + Angles.trnsy(unit.rotation, this.y, this.x);

            Lightning.create(unit.team, color, damage, lx, ly, unit.rotation + Mathf.random(360f), lightningLength);
        }
        if(bullet != null) bullet.create(unit, unit.team, x, y, unit.rotation + bulletAngle + Mathf.range(bulletSpread));
    }
}
