package hjsonpp.expand.meta;

import arc.func.Func;
import arc.scene.ui.layout.Table;
import arc.util.Nullable;
import hjsonpp.expand.MultiCrafter;
import mindustry.gen.Building;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;
import mindustry.ui.ReqImage;
import mindustry.world.consumers.Consume;
import mindustry.world.*;
import mindustry.world.meta.*;

public class ConsumeRecipe extends Consume {

    public final @Nullable Func<Building, Recipe> recipe;

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeRecipe(Func<T, Recipe> recipe, Func<T, Recipe> display) {
        this.recipe = (Func<Building, Recipe>) recipe;
    }

    @SuppressWarnings("unchecked")
    public <T extends Building> ConsumeRecipe(Func<T, Recipe> recipe) {
        this.recipe = (Func<Building, Recipe>) recipe;
    }

    @Override
    public void update(Building build) {
        if(build instanceof MultiCrafter.MultiCrafterBuild){
            Recipe curRecipe = ((MultiCrafter.MultiCrafterBuild) build).currentRecipe;
            if(curRecipe == null) return;

            LiquidStack[] liquids = curRecipe.inputLiquids;

            if(liquids == null && curRecipe.inputLiquid != null) {
                liquids = new LiquidStack[]{curRecipe.inputLiquid};
            }

            if (liquids != null){
                float mult = multiplier.get(build);
                for (var stack : liquids) {
                    if(stack != null && stack.liquid != null) {
                        build.liquids.remove(stack.liquid, stack.amount * build.edelta() * mult);
                    }
                }
            }
        }
    }

    @Override
    public void trigger(Building build){
        if(build instanceof MultiCrafter.MultiCrafterBuild) {
            Recipe curRecipe = ((MultiCrafter.MultiCrafterBuild) build).currentRecipe;
            if(curRecipe == null) return;

            ItemStack[] items = curRecipe.inputItems;

            if(items == null && curRecipe.inputItem != null) {
                items = new ItemStack[]{curRecipe.inputItem};
            }

            if (items != null) {
                for (var stack : items) {
                    if(stack != null && stack.item != null) {
                        build.items.remove(stack.item, Math.round(stack.amount * multiplier.get(build)));
                    }
                }
            }
        }
    }

    @Override
    public void build(Building build, Table table){
        if(build instanceof MultiCrafter.MultiCrafterBuild) {
            Recipe curRecipe = ((MultiCrafter.MultiCrafterBuild) build).currentRecipe;
            if(curRecipe == null) return;

            ItemStack[] items = curRecipe.inputItems;

            if(items == null && curRecipe.inputItem != null) {
                items = new ItemStack[]{curRecipe.inputItem};
            }

            if (items != null) {
                ItemStack[] finalItems = items;
                table.table(c -> {
                    int i = 0;
                    for (var stack : finalItems) {
                        if(stack != null && stack.item != null) {
                            c.add(new ReqImage(StatValues.stack(stack.item, Math.round(stack.amount * multiplier.get(build))),
                                    () -> build.items.has(stack.item, Math.round(stack.amount * multiplier.get(build))))).padRight(8);
                            if (++i % 4 == 0) c.row();
                        }
                    }
                }).left();
            }
        }
    }

    @Override
public float efficiency(Building build){
    if(build instanceof MultiCrafter.MultiCrafterBuild b){
        Recipe r = b.currentRecipe;
        if(r == null) return 0f;

        ItemStack[] items = r.inputItems;
        if(items == null && r.inputItem != null) items = new ItemStack[]{r.inputItem};

        LiquidStack[] liquids = r.inputLiquids;
        if(liquids == null && r.inputLiquid != null) liquids = new LiquidStack[]{r.inputLiquid};

        float first = 1f;
        if(items != null){
            for(var s : items){
                if(s != null && !b.items.has(s.item, Math.round(s.amount * multiplier.get(b)))){
                    first = 0f;
                    break;
                }
            }
        }

        float mult = multiplier.get(b);
        float ed = b.edelta() * b.efficiencyScale();
        float liquidMin = 1f;
        if(liquids != null){
            for(var s : liquids){
                liquidMin = Math.min(
                    b.liquids.get(s.liquid) / (s.amount * ed * mult),
                    liquidMin
                );
            }
        }

        float power = r.powerUse > 0 && b.power != null ? b.power.status : 1f;
        float heat = r.heatConsume > 0 ? Mathf.clamp(b.heat / r.heatConsume) : 1f;

        return Math.min(Math.min(first, liquidMin), Math.min(power, heat));
    }
    return 0f;
}
    @Override
    public void display(Stats stats){}


    @Override
    public boolean ignore(){
        return false;
    }
}