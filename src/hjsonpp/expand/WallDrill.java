package hjsonpp.expand;

import arc.*;
import arc.func.Cons;
import arc.func.Intc2;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.entities.Effect;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.*;
import mindustry.world.blocks.environment.StaticWall;
import mindustry.world.meta.*;
import mindustry.world.consumers.*;

import static mindustry.Vars.*;

public class WallDrill extends Block {
    public TextureRegion rotatorBottomRegion;
    public TextureRegion rotatorRegion;
    public TextureRegion topRegion;

    @Override
    public void load(){
        super.load();
        rotatorBottomRegion = Core.atlas.find(name + "-rotator-bottom");
        rotatorRegion = Core.atlas.find(name + "-rotator");
        topRegion = Core.atlas.find(name + "-top");
    }

    public float drillTime = 200f;
    public int tier = 1;
    public float rotateSpeed = 2f;
    public float liquidBoostIntensity = 1.6f;
    public @Nullable Item blockedItem;
    public @Nullable Seq<Item> blockedItems;
    public Effect drillEffect;
    public float drillEffectChance = 0.01f;

    public WallDrill(String name) {
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        rotate = true;
        regionRotated1 = 1;
        ignoreLineRotation = true;
        ambientSound = Sounds.loopDrill;
        ambientSoundVolume = 0.035f;
        flags = EnumSet.of(BlockFlag.drill);
        envEnabled |= Env.space;
    }

    @Override
    public void init(){
        super.init();
        if(blockedItems == null && blockedItem != null){
            blockedItems = Seq.with(blockedItem);
        }
    }

    @Override
    public void setBars(){
        super.setBars();

        addBar("drillspeed", (WallDrillBuild e) ->
                new Bar(() -> Core.bundle.format("bar.drillspeed", Strings.fixed(e.lastDrillSpeed * 60, 2)), () -> Pal.ammo, () -> e.warmup));
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(Stat.drillTier, StatValues.drillables(drillTime, 0f, size, new ObjectFloatMap<>(), b ->
                (b instanceof StaticWall w && w.itemDrop != null && w.itemDrop.hardness <= tier && (blockedItems == null || !blockedItems.contains(w.itemDrop)))
        ));

        stats.add(Stat.drillSpeed, 60f / drillTime * size, StatUnit.itemsSecond);

        if(liquidBoostIntensity != 1 && findConsumer(f -> f instanceof ConsumeLiquidBase && f.booster) instanceof ConsumeLiquidBase consBase){
            stats.remove(Stat.booster);
            stats.add(Stat.booster,
                    StatValues.speedBoosters("{0}" + StatUnit.timesSpeed.localized(),
                            consBase.amount, liquidBoostIntensity, false,
                            l -> (consumesLiquid(l) && (findConsumer(f -> f instanceof ConsumeLiquid).booster || ((ConsumeLiquid)findConsumer(f -> f instanceof ConsumeLiquid)).liquid != l)))
            );
        }
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region, topRegion};
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(region, plan.drawx(), plan.drawy());
        Draw.rect(topRegion, plan.drawx(), plan.drawy(), plan.rotation * 90);
    }

    @Override
    public void drawPlace(int x, int y, int rotation, boolean valid){
        float eff = getEfficiency(x, y, rotation, null, null);
        drawPlaceText(Core.bundle.formatFloat("bar.drillspeed", 60f / drillTime * eff, 2), x, y, valid);
    }

    @Override
    public boolean canPlaceOn(Tile tile, Team team, int rotation){
        return getEfficiency(tile.x, tile.y, rotation, null, null) > 0;
    }

    float getEfficiency(int tx, int ty, int rotation, @Nullable Cons<Tile> ctile, @Nullable Intc2 cpos){
        float eff = 0f;
        int cornerX = tx - (size-1)/2, cornerY = ty - (size-1)/2, s = size;

        for(int i = 0; i < size; i++){
            int rx = 0, ry = 0;

            switch(rotation){
                case 0 -> {
                    rx = cornerX + s;
                    ry = cornerY + i;
                }
                case 1 -> {
                    rx = cornerX + i;
                    ry = cornerY + s;
                }
                case 2 -> {
                    rx = cornerX - 1;
                    ry = cornerY + i;
                }
                case 3 -> {
                    rx = cornerX + i;
                    ry = cornerY - 1;
                }
            }

            if(cpos != null){
                cpos.get(rx, ry);
            }

            Tile other = world.tile(rx, ry);
            if(other != null && other.solid()){
                Item drop = other.wallDrop();
                if(drop != null && drop.hardness <= tier && (blockedItems == null || !blockedItems.contains(drop))){
                    eff += 1f;
                    if(ctile != null){
                        ctile.get(other);
                    }
                }
            }
        }
        return eff;
    }

    public class WallDrillBuild extends Building {
        public float lastDrillSpeed;
        public float time;
        public float warmup;
        public float lastEfficiency;
        public Tile[] facing = new Tile[size];
        public Point2[] laserPositions = new Point2[size];
        public @Nullable Item lastItem;
        public float totalTime;

        @Override
        public void created(){
            super.created();
            updateFacing();
        }

        @Override
        public void updateTile(){
            super.updateTile();

            if(laserPositions[0] == null){
                for(int i = 0; i < size; i++){
                    laserPositions[i] = new Point2();
                    nearbySide(tileX(), tileY(), rotation, i, laserPositions[i]);
                }
            }

            warmup = Mathf.approachDelta(warmup, Mathf.num(efficiency > 0), 1f / 60f);

            updateFacing();

            float eff = getEfficiency(tile.x, tile.y, rotation, null, null);
            float multiplier = Mathf.lerp(1f, liquidBoostIntensity, optionalEfficiency);
            lastEfficiency = eff * multiplier * timeScale * efficiency;

            lastDrillSpeed = lastEfficiency / drillTime;

            if(lastEfficiency > 0){
                time += edelta() * lastEfficiency;

                if(wasVisible && warmup > 0.5f){
                    for(int i = 0; i < size; i++){
                        Tile targetTile = facing[i];
                        if(targetTile != null && Mathf.chanceDelta(drillEffectChance * warmup)){
                            float dx = Geometry.d4x(rotation) * 0.5f;
                            float dy = Geometry.d4y(rotation) * 0.5f;
                            float effectX = targetTile.worldx() + Mathf.range(3f) - dx * tilesize;
                            float effectY = targetTile.worldy() + Mathf.range(3f) - dy * tilesize;

                            drillEffect.at(
                                    effectX,
                                    effectY,
                                    targetTile.block().mapColor
                            );
                        }
                    }
                }

                if(time >= drillTime){
                    for(Tile targetTile : facing){
                        if(targetTile == null) continue;
                        Item drop = targetTile.wallDrop();
                        if(drop != null && items.total() < itemCapacity){
                            items.add(drop, 1);
                            produced(drop);
                        }
                    }
                    time %= drillTime;
                }
            }

            totalTime += edelta() * warmup * (eff <= 0f ? 0f : 1f);

            if(timer(timerDump, dumpTime / timeScale)){
                dump();
            }
        }

        protected void updateFacing(){
            for(int i = 0; i < size; i++){
                facing[i] = null;
            }

            lastItem = null;
            boolean multiple = false;
            int found = 0;

            int cornerX = tile.x - (size-1)/2, cornerY = tile.y - (size-1)/2;
            int s = size;

            for(int i = 0; i < size; i++){
                int rx = 0, ry = 0;

                switch(rotation){
                    case 0 -> {
                        rx = cornerX + s;
                        ry = cornerY + i;
                    }
                    case 1 -> {
                        rx = cornerX + i;
                        ry = cornerY + s;
                    }
                    case 2 -> {
                        rx = cornerX - 1;
                        ry = cornerY + i;
                    }
                    case 3 -> {
                        rx = cornerX + i;
                        ry = cornerY - 1;
                    }
                }

                Tile other = world.tile(rx, ry);
                if(other != null && other.solid()){
                    Item drop = other.wallDrop();
                    if(drop != null && drop.hardness <= tier && (blockedItems == null || !blockedItems.contains(drop))){
                        facing[found] = other;

                        if(lastItem != null && lastItem != drop){
                            multiple = true;
                        }
                        lastItem = drop;
                        found++;
                    }
                }
            }

            if(multiple){
                lastItem = null;
            }
        }

        @Override
        public boolean shouldConsume(){
            boolean hasTarget = false;
            for(Tile t : facing){
                if(t != null){
                    hasTarget = true;
                    break;
                }
            }
            return hasTarget && items.total() < itemCapacity && enabled;
        }

        @Override
        public void draw(){
            Draw.rect(block.region, x, y);
            Draw.rect(topRegion, x, y, rotdeg());

            int[] idxHolder = {0};
            getEfficiency(tile.x, tile.y, rotation, null, (cx, cy) -> {
                int sign = idxHolder[0] >= size/2 && size % 2 == 0 ? -1 : 1;
                float dx = Geometry.d4x(rotation) * 0.5f;
                float dy = Geometry.d4y(rotation) * 0.5f;
                float vx = (cx - dx) * tilesize;
                float vy = (cy - dy) * tilesize;

                Draw.z(Layer.blockOver);
                Draw.rect(rotatorBottomRegion, vx, vy, totalTime * rotateSpeed * sign);
                Draw.rect(rotatorRegion, vx, vy);
                idxHolder[0]++;
            });

            Draw.reset();
        }

        @Override
        public void onProximityUpdate(){
            updateFacing();
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(time);
            write.f(warmup);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(revision >= 1){
                time = read.f();
                warmup = read.f();
            }
        }
    }
}