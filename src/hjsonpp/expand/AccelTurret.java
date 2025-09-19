package hjsonpp.expand;

import arc.Core;
import arc.graphics.Color;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import hjsonpp.expand.meta.AdditionalStats;
import hjsonpp.util.Utilites;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.bullet.BulletType;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.consumers.ConsumeLiquidFilter;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;

import static mindustry.Vars.tilesize;

public class AccelTurret extends ItemTurret {
    public float speedUpPerShoot = 2;
    public float maxAccel = 0.5f;
    public float cooldownSpeed = 1;
    public boolean canOverheat = false;
    public float overheatMultiplier = 1f;
    public float maxOverheatThreshold = 800f;
    public int overheatTime = 60;
    public float overheatEffectChance = 0.05f;
    public Effect overheatEffect = Fx.none;

    public AccelTurret(String name){
        super(name);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("speedingUp", (AccelTurretBuild entity) ->
                new Bar(
                        () -> Core.bundle.format("bar.speedingUp", Strings.autoFixed((entity.speedUp/maxAccel) * 100, 0)),
                        () -> Pal.powerBar,
                        () -> entity.speedUp / maxAccel
                )
        );
        if(canOverheat) addBar("overheat", (AccelTurretBuild entity) ->
                new Bar(
                        () -> Core.bundle.format("bar.overheat", Strings.autoFixed((entity.overheat/maxOverheatThreshold) * 100, 0)),
                        () -> entity.overheated ? Color.valueOf("f54c4c") : Utilites.lerpColor(Color.valueOf("f0f7c3"), Color.valueOf("eb8e4b"), entity.overheat / maxOverheatThreshold),
                        () -> entity.overheat / maxOverheatThreshold
                )
        );
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(Stat.reload);
        stats.add(AdditionalStats.reloadFrom, reload / 60f, StatUnit.seconds);
        stats.add(AdditionalStats.reloadTo, (reload / (maxAccel + 1.0f)) / 60f, StatUnit.seconds);
    }

    public  class AccelTurretBuild extends ItemTurretBuild {
        protected float overheat = 0;
        protected float speedUp = 0;
        protected float coolantSpeedMultiplier;
        protected float overheatCoolantSpdMultiplier;
        protected  boolean overheated = false;
        @Override
        public void updateTile() {
            //cooldown progress
            if (!isShooting() || !hasAmmo() || !isActive() || overheated && canOverheat){
                if(speedUp > 0) {
                    speedUp -= delta() * cooldownSpeed;

                }else {
                    speedUp = 0;
                }

                if (overheat > 0) {
                    overheat -= delta() * 0.1f * cooldownSpeed;
                    overheat -= coolantSpeedMultiplier;
                }

                if(overheated && Mathf.chance(overheatEffectChance)) overheatEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));

            }
            checkOverheat();
            if(!overheated) {
                super.updateTile();
            }else {
                if(linearWarmup){
                    shootWarmup = Mathf.approachDelta(shootWarmup, 0, shootWarmupSpeed);
                }else{
                    shootWarmup = Mathf.lerpDelta(shootWarmup, 0, shootWarmupSpeed);
                }

                unit.tile(this);
                unit.rotation(rotation);
                unit.team(team);
                speedUp = Mathf.approachDelta(speedUp, 0, cooldownSpeed * 2);
                curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / recoilTime);
                recoilOffset.trns(rotation, -Mathf.pow(curRecoil, recoilPow) * recoil);
                reloadCounter = Mathf.lerpDelta(reloadCounter, 0, 0.1f);
                if(logicControlTime > 0){
                    logicControlTime -= Time.delta;
                }
            }
        }

        protected void updOverheatCooling(){
            if(overheated){
                if (coolant != null && coolant.efficiency(this) > 0 && efficiency > 0) {
                    coolant.update(this);
                    overheatCoolantSpdMultiplier = liquids.current() == null ? 0 : liquids.current().heatCapacity  * coolantMultiplier;
                } else overheatCoolantSpdMultiplier = 0;
            }
        }

        @Override
        protected void updateCooling(){
            if(reloadCounter < reload && coolant != null && coolant.efficiency(this) > 0 && efficiency > 0){
                float capacity = coolant instanceof ConsumeLiquidFilter filter ? filter.getConsumed(this).heatCapacity : (coolant.consumes(liquids.current()) ? liquids.current().heatCapacity : 0.4f);
                float amount = coolant.amount * coolant.efficiency(this);
                coolant.update(this);
                coolantSpeedMultiplier = amount * capacity * coolantMultiplier * ammoReloadMultiplier();
                if(Mathf.chance(0.06 * amount)){
                    coolEffect.at(x + Mathf.range(size * tilesize / 2f), y + Mathf.range(size * tilesize / 2f));
                }
            }
        }

        public void checkOverheat(){
            if(overheat >= maxOverheatThreshold && !overheated && canOverheat) overheated = true;
            if(overheated) {
                overheat -= (delta() / overheatTime);
                updOverheatCooling();
                overheat -= overheatCoolantSpdMultiplier;
                if(overheat <= 0){
                    overheat = 0;
                    overheated = false;
                }
            }
            if (overheat < 0) overheat = 0;
        }
        @Override
        public void updateShooting(){
            //override shooting method
            if (reloadCounter >= reload) {
                if (!overheated || !canOverheat) {
                    BulletType type = peekAmmo();

                    shoot(type);
                    reloadCounter = 0;
                }
            }
            else
            {
                reloadCounter += (1 + speedUp) * edelta() * baseReloadSpeed();
            }
        }
        @Override
        public void shoot(BulletType type){
            //speedUp per shoot
            super.shoot(type);
            if (speedUp < maxAccel){
                speedUp += speedUpPerShoot * edelta();
                speedUp += coolantSpeedMultiplier * delta();
                if(speedUp>maxAccel) speedUp = maxAccel;
            }else {
                speedUp = maxAccel;
            }
            if(canOverheat) overheat += delta() * speedUpPerShoot * overheatMultiplier;
        }
        @Override
        public void write(Writes write){
            super.write(write);
            write.f(speedUp);
            write.f(overheat);
            write.bool(overheated);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            speedUp = read.f();
            overheat = read.f();
            overheated = read.bool();
        }
    }
}

