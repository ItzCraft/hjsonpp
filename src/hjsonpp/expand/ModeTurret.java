package hjsonpp.expand;

import arc.Core;
import arc.Graphics;
import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Time;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.entities.Mover;
import mindustry.entities.Sized;
import mindustry.entities.bullet.BulletType;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Building;
import mindustry.gen.Sounds;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.ItemSelection;
import mindustry.world.blocks.defense.turrets.ItemTurret;

import java.util.Objects;

import static mindustry.Vars.fogControl;
import static mindustry.Vars.player;

//Extendable class for ItemTurret with different fire modes.
public class ModeTurret extends ItemTurret{
    public static TextureRegion defaultIcon;

    public TurretMode defaultMode = new TurretMode("default");

    //Can be null. In constructor, it's just adding DefaultMode

    public Seq<TurretMode> turModes = new Seq<>();

    public void addModes(TurretMode...tmodes){
        turModes.add(tmodes);
    }

    @Override
    public void setBars(){
        super.setBars();
        addBar("currentMode", (ModeTurretBuild entity) ->
                new Bar(
                        () -> Core.bundle.format("turret.mode." + entity.getMode().name),
                        () -> entity.getMode().barColor,
                        () -> 1f
                )
        );
    }

    @Override
    public void load(){
        super.load();
        defaultIcon = Core.atlas.find("vnrwk-turret-mode-default");
        for(TurretMode t : turModes){
            t.load();
        }
    }

    public ModeTurret(String name) {
        super(name);
        turModes.insert(0, defaultMode);
        sync = true;
        saveConfig = true;
        copyConfig = true;
        configurable = true;
        config(Integer.class, (ModeTurretBuild e, Integer idx) -> {
            e.currentTurretMode = idx;
        });
    }

    public static class TurretMode{
        public String name = "";
        public Color barColor = Color.white;
        public float accuracyMultiplier = 1;
        public float reloadMultiplier = 1;
        public float rotateSpeedMultiplier = 1;
        public float rangeChange = 0;
        public float targetIntervalMultiplier = 1;
        @Nullable
        public ShootPattern modePattern;
        public TextureRegion icon;

        public void load(){
            icon = Core.atlas.find("vnrwk-turret-mode-" + this.name);
        }

        public TextureRegion icon(){
            return Objects.equals(this.name, "default") ? defaultIcon : !icon.found() ? defaultIcon  : icon;
        }

        public TurretMode(String name){
            this.name = name;
        }

        public TurretMode(String name, float accuracyM, float reloadM, float rotateSpdM){
            this.name = name;
            this.accuracyMultiplier = accuracyM;
            this.reloadMultiplier = reloadM;
            this.rotateSpeedMultiplier = rotateSpdM;
        }

        public TurretMode(String name, float accuracyM, float reloadM, float rotateSpdM, Color barColor){
            this.barColor = barColor;
            this.name = name;
            this.accuracyMultiplier = accuracyM;
            this.reloadMultiplier = reloadM;
            this.rotateSpeedMultiplier = rotateSpdM;
        }
    }

    public class ModeTurretBuild extends ItemTurretBuild{

        public int currentTurretMode;

        public TurretMode getMode(){
            return turModes.get(currentTurretMode);
        }

        float lastRangeChange;

        @Override
        public float range(){
            if(peekAmmo() != null){
                return range + peekAmmo().rangeChange + getMode().rangeChange;
            }
            return range;
        }

        @Override
        public void updateTile(){
            if(!validateTarget()) target = null;

            if(soundLoop != null){
                soundLoop.update(x, y, shouldActiveSound(), activeSoundVolume());
            }

            float warmupTarget = (isShooting() && canConsume()) || charging() ? 1f : 0f;
            if(warmupTarget > 0 && !isControlled()){
                warmupHold = 1f;
            }
            if(warmupHold > 0f){
                warmupHold -= Time.delta / warmupMaintainTime;
                warmupTarget = 1f;
            }

            if(linearWarmup){
                shootWarmup = Mathf.approachDelta(shootWarmup, warmupTarget, shootWarmupSpeed * (warmupTarget > 0 ? efficiency : 1f));
            }else{
                shootWarmup = Mathf.lerpDelta(shootWarmup, warmupTarget, shootWarmupSpeed * (warmupTarget > 0 ? efficiency : 1f));
            }

            wasShooting = false;

            curRecoil = Mathf.approachDelta(curRecoil, 0, 1 / recoilTime);
            if(recoils > 0){
                if(curRecoils == null) curRecoils = new float[recoils];
                for(int i = 0; i < recoils; i++){
                    curRecoils[i] = Mathf.approachDelta(curRecoils[i], 0, 1 / recoilTime);
                }
            }
            heat = Mathf.approachDelta(heat, 0, 1 / cooldownTime);
            charge = charging() ? Mathf.approachDelta(charge, 1, 1 / shoot.firstShotDelay) : 0;

            unit.tile(this);
            unit.rotation(rotation);
            unit.team(team);
            recoilOffset.trns(rotation, -Mathf.pow(curRecoil, recoilPow) * recoil);

            if(logicControlTime > 0){
                logicControlTime -= Time.delta;
            }

            if(heatRequirement > 0){
                heatReq = calculateHeat(sideHeat);
            }

            if(rotate){
                //sync underlying rotation; 0-3 rotation is a shadowed field
                ((Building)this).rotation = Mathf.mod(Mathf.round(rotation / 90f), 4);
            }

            //turret always reloads regardless of whether it's targeting something
            if(reloadWhileCharging || !charging()){
                updateReload();
                updateCooling();
            }

            if(Vars.state.rules.fog){
                float newRange = hasAmmo() ? peekAmmo().rangeChange : 0f;
                if(newRange != lastRangeChange){
                    lastRangeChange = newRange;
                    fogControl.forceUpdate(team, this);
                }
            }

            if(activationTimer > 0){
                activationTimer -= Time.delta;
                return;
            }

            if(hasAmmo()){
                if(Float.isNaN(reloadCounter)) reloadCounter = 0;

                if(timer(timerTarget, (target != null ? newTargetInterval : targetInterval) * getMode().targetIntervalMultiplier)){
                    findTarget();
                }

                if(validateTarget()){
                    boolean canShoot;

                    if(isControlled()){ //player behavior
                        targetPos.set(unit.aimX(), unit.aimY());
                        canShoot = unit.isShooting();
                    }else if(logicControlled()){ //logic behavior
                        canShoot = logicShooting;
                    }else{ //default AI behavior
                        targetPosition(target);

                        if(Float.isNaN(rotation)) rotation = 0;
                        canShoot = within(target, range() + (target instanceof Sized hb ? hb.hitSize()/1.9f : 0f));
                    }

                    if(!isControlled()){
                        unit.aimX(targetPos.x);
                        unit.aimY(targetPos.y);
                    }

                    float targetRot = angleTo(targetPos);

                    if(shouldTurn()){
                        turnToTarget(targetRot);
                    }

                    if(!alwaysShooting && Angles.angleDist(rotation, targetRot) < shootCone && canShoot){
                        wasShooting = true;
                        updateShooting();
                    }
                }else{
                    target = null;
                }

                if(alwaysShooting){
                    wasShooting = true;
                    updateShooting();
                }
            }
        }

        @Override
        protected void turnToTarget(float targetRot){
            rotation = Angles.moveToward(rotation, targetRot, rotateSpeed * getMode().rotateSpeedMultiplier * delta() * potentialEfficiency);
        }

        @Override
        protected void updateShooting(){

            if(reloadCounter >= reload && !charging() && shootWarmup >= minWarmup){
                BulletType type = peekAmmo();

                shoot(type);

                reloadCounter %= reload;
            }
        }

        @Override
        protected void updateReload(){
            reloadCounter += delta() * ammoReloadMultiplier() * baseReloadSpeed() * getMode().reloadMultiplier;

            //cap reload for visual reasons
            reloadCounter = Math.min(reloadCounter, reload);
        }

        @Override
        protected void bullet(BulletType type, float xOffset, float yOffset, float angleOffset, Mover mover){
            queuedBullets --;

            if(dead || (!consumeAmmoOnce && !hasAmmo())) return;

            float
                    xSpread = Mathf.range(xRand),
                    bulletX = x + Angles.trnsx(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                    bulletY = y + Angles.trnsy(rotation - 90, shootX + xOffset + xSpread, shootY + yOffset),
                    shootAngle = rotation + angleOffset + Mathf.range(inaccuracy / getMode().accuracyMultiplier + type.inaccuracy);

            float lifeScl = type.scaleLife ? Mathf.clamp((1 + scaleLifetimeOffset) * Mathf.dst(bulletX, bulletY, targetPos.x, targetPos.y) / type.range, minRange() / type.range, range() / type.range) : range() / type.range;


            //TODO aimX / aimY for multi shot turrets?
            handleBullet(type.create(this, team, bulletX, bulletY, shootAngle, -1f, (1f - velocityRnd) + Mathf.random(velocityRnd), lifeScl, null, mover, targetPos.x, targetPos.y), xOffset, yOffset, shootAngle - rotation);

            (shootEffect == null ? type.shootEffect : shootEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            (smokeEffect == null ? type.smokeEffect : smokeEffect).at(bulletX, bulletY, rotation + angleOffset, type.hitColor);
            (type.shootSound != Sounds.none ? type.shootSound : shootSound).at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax), shootSoundVolume);

            ammoUseEffect.at(
                    x - Angles.trnsx(rotation, ammoEjectBack),
                    y - Angles.trnsy(rotation, ammoEjectBack),
                    rotation * Mathf.sign(xOffset)
            );

            if(shake > 0){
                Effect.shake(shake, shake, this);
            }

            curRecoil = 1f;
            if(recoils > 0){
                curRecoils[barrelCounter % recoils] = 1f;
            }
            heat = 1f;
            totalShots++;

            if(!consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        protected void shoot(BulletType type){
            float
                    bulletX = x + Angles.trnsx(rotation - 90, shootX, shootY),
                    bulletY = y + Angles.trnsy(rotation - 90, shootX, shootY);

            if(shoot.firstShotDelay > 0){
                chargeSound.at(bulletX, bulletY, Mathf.random(soundPitchMin, soundPitchMax));
                type.chargeEffect.at(bulletX, bulletY, rotation);
            }

            TurretMode mode = getMode();

            ShootPattern pattern = mode.modePattern != null ? mode.modePattern : shoot;

            pattern.shoot(barrelCounter, (xOffset, yOffset, angle, delay, mover) -> {
                queuedBullets++;
                int barrel = barrelCounter;

                if(delay > 0f){
                    Time.run(delay, () -> {
                        //hack: make sure the barrel is the same as what it was when the bullet was queued to fire
                        int prev = barrelCounter;
                        barrelCounter = barrel;
                        bullet(type, xOffset, yOffset, angle, mover);
                        barrelCounter = prev;
                    });
                }else{
                    bullet(type, xOffset, yOffset, angle, mover);
                }
            }, () -> barrelCounter++);

            if(consumeAmmoOnce){
                useAmmo();
            }
        }

        @Override
        public void buildConfiguration(Table table){
            table.background(Styles.black6);
            table.image(Tex.whiteui, Pal.gray).height(4f).growX().row();
            table.table(buttons ->{
                for(int i = 0; i < turModes.size; i++){
                    TurretMode tm = turModes.get(i);
                    int bid = i;
                    boolean isCurrent = currentTurretMode == i;
                    if(i % 5 == 0) table.row();
                    table.button(b ->{
                        b.center();
                        b.image(tm.icon());
                        b.update(() -> b.setChecked(isCurrent));
                    }, Styles.clearNoneTogglei, ()->{
                        configure(bid);
                        deselect();
                    }).growX().size(40).tooltip(Core.bundle.format("fire.turret.mode.") + tm.name);
                }}).grow();
        }

        @Override
        public void write(Writes write) {
            super.write(write);

            write.i(currentTurretMode);

        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);

            currentTurretMode = read.i();
        }

    }
}