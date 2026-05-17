package hjsonpp.expand;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.part.DrawPart;
import mindustry.gen.Bullet;

import static hjsonpp.HppUtilities.*;

public class BlackHoleBulletType extends BulletType {
    public int damageInterval = 10;
    public float layer = -1;
    public float shrinkTime = 60f;
    public float fadeTime = 20;
    public float growTime = 10f;
    public float eventHorizonRadius = 48;
    public float accretionDiskWidth = 8;
    public float inclinedDiskwidth = 0.8f;
    public float offset = 7;
    public Color accretionDiskColor = Color.valueOf("fffffe");
    public DrawPart.PartProgress progress = DrawPart.PartProgress.life;
    public boolean clampProgress = true;
    public float factorRadius = 170;
    public float pullStrength = 3;
    public float shake = 0;
    public Effect updateEffect;
    public float updateEffectChance = 0.75f;
    public float updateEffectTime = -1;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = false;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = Fx.none;
    }
    @Override
    public void init(Bullet b){
        super.init(b);
    }

    public BlackHoleBulletType(){
        this(0f, 1f);
    }

    @Override
    public float continuousDamage(){
        return damage * 100f / damageInterval * 3f;
    }

    @Override
    public void update(Bullet b){

        if(updateEffect != null && Mathf.chance(updateEffectChance)  && (updateEffectTime == -1 || b.time < updateEffectTime)) {
            updateEffect.at(b.x, b.y, b.rotation());
        }
        if(b.timer(1, damageInterval)){
            Effect.shake(shake, shake, b.x, b.y);
            blackHoleUpdate(b.team, b, factorRadius, pullStrength, damage, armorMultiplier, damage * damageMultiplier(b), buildingDamageMultiplier);
        }
    }

    @Override
    public void draw(Bullet b){
        float px = b.x, py = b.y;
        float prog =  fout(b);
        float diskRad = (eventHorizonRadius) * prog;
        float z = Draw.z();
        if(layer > 0) Draw.z(layer);
        Draw.color(accretionDiskColor);
        Lines.stroke(accretionDiskWidth);
        Lines.circle(px, py, diskRad);
        for(int s : Mathf.signs) {
            Fill.rect(px, py, eventHorizonRadius * prog, eventHorizonRadius * 1.5f * prog, (90 + offset) * s);
            Fill.rect(px, py, eventHorizonRadius / 2 * prog, eventHorizonRadius * 1.85f * prog, (90 + offset) * s);
        }

        Draw.z(120);
        Draw.color(Color.black);
        Fill.circle(px, py, eventHorizonRadius * prog);

    }

    public float fout(Bullet b){
        return Interp.sineOut.apply(
                Mathf.curve(b.time, 0f, growTime)
                        - Mathf.curve(b.time, b.lifetime - shrinkTime, b.lifetime)
        );
    }

    @Override
    public void drawLight(Bullet b){
        //none
    }

}
