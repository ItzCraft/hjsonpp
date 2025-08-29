package hjsonpp.expand;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import arc.util.io.*;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.graphics.Drawf;
import mindustry.ui.Styles;
import mindustry.world.blocks.defense.Wall;
import mindustry.world.meta.Stat;

public class AdjustableShieldWall extends Wall{
    // radius of shield. 1 = default shield wall radius
    public float radius = 2f;
    // shield health
    public float shieldHealth = 400.0F;
    // break cooldown
    public float breakCooldown = 850.0F;
    public float regenSpeed = 2.0F;
    public Color glowColor = Color.valueOf("ff7531").a(0.5F);
    public float glowMag = 0.6F;
    public float glowScl = 8.0F;
    public TextureRegion glowRegion;

    public AdjustableShieldWall(String name){
        super(name);
        this.update = true;
        configurable = true;
        saveConfig = true;
        canOverdrive = false;
    }

    @Override
    public void load(){
        super.load();
        glowRegion = Core.atlas.find(name + "-glow");
    }

    @Override
    public void setStats(){
        super.setStats();
        this.stats.add(Stat.shieldHealth, this.shieldHealth);
    }

    public class AdjustableShieldWallBuild extends WallBuild{
        public boolean enabled = true;
        public float shield;
        public float shieldRadius;
        public float breakTimer;
        @Override
        public void draw(){
            Draw.rect(this.block.region, this.x, this.y);
            if(enabled){
                if (this.shieldRadius > 0.0F) {
                    float radiusN = this.shieldRadius * 8.0F * (float)AdjustableShieldWall.this.size / 2.0F * radius;
                    Draw.z(125.0F);
                    Draw.color(this.team.color, Color.white, Mathf.clamp(this.hit));
                    if (Vars.renderer.animateShields) {
                        Fill.square(this.x, this.y, radiusN);
                    } else {
                        Lines.stroke(1.5F);
                        Draw.alpha(0.09F + Mathf.clamp(0.08F * this.hit));
                        Fill.square(this.x, this.y, radiusN);
                        Draw.alpha(1.0F);
                        Lines.poly(this.x, this.y, 4, radiusN, 45.0F);
                        Draw.reset();
                    }

                    Draw.reset();
                    Drawf.additive(AdjustableShieldWall.this.glowRegion, AdjustableShieldWall.this.glowColor, (1.0F - AdjustableShieldWall.this.glowMag + Mathf.absin(AdjustableShieldWall.this.glowScl, AdjustableShieldWall.this.glowMag)) * this.shieldRadius, this.x, this.y, 0.0F, 31.0F);
                }
            }
        }
        @Override
        public void updateTile(){
            if(enabled){
                if (this.breakTimer > 0.0F) {
                    this.breakTimer -= Time.delta;
                } else {
                    this.shield = Mathf.clamp(this.shield + AdjustableShieldWall.this.regenSpeed * this.edelta(), 0.0F, AdjustableShieldWall.this.shieldHealth);
                }

                if (this.hit > 0.0F) {
                    this.hit -= Time.delta / 10.0F;
                    this.hit = Math.max(this.hit, 0.0F);
                }
                this.shieldRadius = Mathf.lerpDelta(this.shieldRadius, this.broken() ? 0.0F : 1.0F, 0.12F);
            }
        }

        public boolean broken() {
            return this.breakTimer > 0.0F || !this.canConsume();
        }

        @Override
        public void damage(float damage) {
            if(enabled){
                float shieldTaken = this.broken() ? 0.0F : Math.min(this.shield, damage);
                this.shield -= shieldTaken;
                if (shieldTaken > 0.0F) {
                    this.hit = 1.0F;
                }

                if (this.shield <= 1.0E-5F && shieldTaken > 0.0F) {
                    this.breakTimer = AdjustableShieldWall.this.breakCooldown;
                }

                if (damage - shieldTaken > 0.0F) {
                    super.damage(damage - shieldTaken);
                }
            }
            else{
                super.damage(damage);
            }

        }


        @Override
        public void buildConfiguration(Table table){
            table.button(Icon.defense, Styles.logici, ()->{
                enabled = true;
                deselect();
            }).size(40f);
            table.button(Icon.cancel, Styles.logici, ()->{
                enabled = false;
                deselect();
            }).size(40f);
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(this.shield);
            write.bool(enabled);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            this.shield = read.f();
            if (this.shield > 0.0F) {
                this.shieldRadius = 1.0F;
            }
            this.enabled = read.bool();
        }
    }
}