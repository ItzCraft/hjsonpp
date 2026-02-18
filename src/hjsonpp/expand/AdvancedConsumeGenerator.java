package hjsonpp.expand;

import arc.Core;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Bar;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.meta.*;

public class AdvancedConsumeGenerator extends ConsumeGenerator{
    // make them able to output multiple items and liquids
    @Nullable
    public ItemStack outputItem;
    @Nullable
    public ItemStack[] outputItems;
    @Nullable
    public LiquidStack outputLiquid;
    @Nullable
    public LiquidStack[] outputLiquids;
    public int[] liquidOutputDirections = new int[]{-1};
    public float warmupSpeed = 0.019f;

    // is production bar will be displayed
    public boolean progressBar = false;

    public AdvancedConsumeGenerator(String name){
        super(name);
    }

    @Override
    public void setStats(){
        super.setStats();
        if (outputItems != null) {
            stats.add(Stat.output, StatValues.items(this.itemDuration, outputItems));
        }
        if (outputLiquids != null) {
            stats.add(Stat.output, StatValues.liquids(1.0F, outputLiquids));
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        if (outputLiquids != null && outputLiquids.length > 0) {
            removeBar("liquid");

            for(LiquidStack stack : outputLiquids) {
                addLiquidBar(stack.liquid);
            }
        }
        if (progressBar) {
            addBar("hj-bar.progress", (AdvancedConsumeGeneratorBuild entity) ->
                    new Bar(
                            () -> Core.bundle.format("bar.production-progress", Strings.fixed(entity.totalProgress() / itemDuration * 100, 1)),
                            () -> Pal.accent,
                            () -> entity.totalProgress() / itemDuration
                    )
            );
        }
    }

    @Override
    public void init(){
        if (outputItems == null && outputItem != null) {
            outputItems = new ItemStack[]{outputItem};
        }

        if (outputLiquids == null && outputLiquid != null) {
            outputLiquids = new LiquidStack[]{outputLiquid};
        }

        if (outputLiquid == null && outputLiquids != null && outputLiquids.length > 0) {
            outputLiquid = outputLiquids[0];
        }

        outputsLiquid = outputLiquids != null;
        if (outputItems != null) {
            hasItems = true;
        }

        if (outputLiquids != null) {
            hasLiquids = true;
        }

        super.init();
    }

    public boolean outputsItems() {
        return outputItems != null;
    }

    public class AdvancedConsumeGeneratorBuild extends ConsumeGeneratorBuild{
        public float progress;
        public float totalProgress;
        public float warmup;
        @Override
     public void updateTile(){
            if(efficiency > 0){

                progress += getProgressIncrease(craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);
                if (AdvancedConsumeGenerator.outputLiquids != null) {
                    float inc = getProgressIncrease(1.0F);

                    for(LiquidStack output : AdvancedConsumeGenerator.outputLiquids) {
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, AdvancedConsumeGenerator.liquidCapacity - this.liquids.get(output.liquid)));
                    }
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }
            
            totalProgress += warmup * Time.delta;

            if(progress>=1f){
                craft();
            }
            dumpOutputs();
            super.updateTile();          
        }
        public void craft() {
            consume();
            if (AdvancedConsumeGenerator.outputItems != null) {
                for(ItemStack output : AdvancedConsumeGenerator.outputItems) {
                    for(int i = 0; i < output.amount; ++i) {
                        offload(output.item);
                    }
                }
            }
        }

        public void dumpOutputs() {
            if (AdvancedConsumeGenerator. outputItems != null && timer(AdvancedConsumeGenerator.timerDump, (float)AdvancedConsumeGenerator.dumpTime / timeScale)) {
                for(ItemStack output : AdvancedConsumeGenerator.outputItems) {
                    dump(output.item);
                }
            }

            if (AdvancedConsumeGenerator.outputLiquids != null) {
                for(int i = 0; i < AdvancedConsumeGenerator.outputLiquids.length; ++i) {
                    int dir = AdvancedConsumeGenerator.liquidOutputDirections.length > i ? AdvancedConsumeGenerator.liquidOutputDirections[i] : -1;
                    dumpLiquid(AdvancedConsumeGenerator.outputLiquids[i].liquid, 2.0F, dir);
                }
            }
        }
    }
}