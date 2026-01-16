package hjsonpp.expand;

import arc.graphics.Color;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.*;
import arc.util.io.*;
import hjsonpp.expand.meta.*;
import mindustry.gen.Building;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.type.*;
import mindustry.ui.Styles;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.consumers.ConsumePower;
import mindustry.world.meta.Stat;

/**
 * @author Sputnuc & ItzCraft
 */
public class MultiCrafter extends GenericCrafter {

    public MultiCrafter(String name) {
        super(name);
        configurable = true;
        saveConfig = true;
        copyConfig = true;
        hasPower = true;
        update = true;
        sync = true;
        hasItems = true;
        hasLiquids = true;
        consume(new ConsumeRecipe(MultiCrafterBuild::getRecipe));
    }

    public Seq<Recipe> craftRecipes = new Seq<>();

    public void addRecipes(Recipe... recipes){
        craftRecipes.addAll(recipes);
    }



    @Override
    public void init(){
        super.init();
    }

    public class MultiCrafterBuild extends GenericCrafterBuild{
        public int curRecipeIndex = 0;
        public Recipe currentRecipe = craftRecipes.get(curRecipeIndex);

        public Recipe getRecipe() {
            if (curRecipeIndex < 0 || curRecipeIndex >= craftRecipes.size) return null;
            return craftRecipes.get(curRecipeIndex);
        }

        public void updateCurRecipe(int index, boolean clear){
            if(craftRecipes.size > 0){
                currentRecipe = craftRecipes.get(index);
                curRecipeIndex = index;
                if(!clear){
                    items.clear();
                    liquids.clear();
                }
                this.block.consPower = new ConsumePower(currentRecipe.powerUse, 0, false);
                this.block.hasPower = currentRecipe.powerUse > 0;
            }
        }

        @Override
        public boolean acceptItem(Building source, Item item){
            if(currentRecipe.inputItem != null){
                if(item == currentRecipe.inputItem.item){
                    return items.get(item) < itemCapacity;
                }
            }
            if(currentRecipe.inputItems != null){
                for(ItemStack input : currentRecipe.inputItems){
                    if(input != null && input.item == item){
                        return items.get(item) < itemCapacity;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            if(currentRecipe.inputLiquid != null){
                if(liquid == currentRecipe.inputLiquid.liquid){
                    return liquids.get(liquid) < liquidCapacity;
                }
            }
            if(currentRecipe.inputLiquids != null){
                for(LiquidStack input : currentRecipe.inputLiquids){
                    if(input != null && input.liquid == liquid){
                        return liquids.get(liquid) < liquidCapacity;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean shouldConsume(){
            if(currentRecipe.outputItems != null){
                for(ItemStack item : currentRecipe.outputItems){
                    if(items.get(item.item) + item.amount > itemCapacity){
                        return false;
                    }
                }
            } else if (currentRecipe.outputItem != null) {
                if(items.get(currentRecipe.outputItem.item) + currentRecipe.outputItem.amount > itemCapacity){
                    return false;
                }
            }

            if(currentRecipe.outputLiquids != null && !ignoreLiquidFullness){
                for(LiquidStack liquid : currentRecipe.outputLiquids){
                    if(liquids.get(liquid.liquid) >= liquidCapacity - 0.001f) return false;
                }
            }else if(currentRecipe.outputLiquid != null && !ignoreLiquidFullness){
                if(liquids.get(currentRecipe.outputLiquid.liquid) >= liquidCapacity - 0.001f) return false;
            }
            return enabled;
        }

        @Override
        public void updateTile(){
            if(efficiency > 0){

                if(shouldConsume()) progress += getProgressIncrease(currentRecipe.craftTime);
                warmup = Mathf.approachDelta(warmup, warmupTarget(), warmupSpeed);

                if(currentRecipe.outputLiquids != null){
                    float inc = getProgressIncrease(1f);
                    for(var output : currentRecipe.outputLiquids){
                        handleLiquid(this, output.liquid, Math.min(output.amount * inc, liquidCapacity - liquids.get(output.liquid)));
                    }
                }
                if(currentRecipe.outputLiquid != null){
                    float inc = getProgressIncrease(1f);
                    handleLiquid(this, currentRecipe.outputLiquid.liquid, Math.min(currentRecipe.outputLiquid.amount * inc, liquidCapacity - liquids.get(currentRecipe.outputLiquid.liquid)));
                }

                if(wasVisible && Mathf.chanceDelta(updateEffectChance)){
                    updateEffect.at(x + Mathf.range(size * updateEffectSpread), y + Mathf.range(size * updateEffectSpread));
                }
            }else{
                warmup = Mathf.approachDelta(warmup, 0f, warmupSpeed);
            }

            totalProgress += warmup * Time.delta;

            if(progress >= 1f){
                craft();
            }

            dumpOutputs();
        }

        @Override
        public float getProgressIncrease(float baseTime){
            if(ignoreLiquidFullness){
                return super.getProgressIncrease(baseTime);
            }
            float scaling = 1f, max = 1f;
            if(currentRecipe.outputLiquids != null){
                max = 0f;
                for(var s : currentRecipe.outputLiquids){
                    float value = (liquidCapacity - liquids.get(s.liquid)) / (s.amount * edelta());
                    scaling = Math.min(scaling, value);
                    max = Math.max(max, value);
                }
            }
            if(currentRecipe.outputLiquid != null){
                max = 0f;
                float value = (liquidCapacity - liquids.get(currentRecipe.outputLiquid.liquid)) / (currentRecipe.outputLiquid.amount * edelta());
                scaling = Math.min(scaling, value);
                max = Math.max(max, value);
            }

            return super.getProgressIncrease(baseTime) * (dumpExtraLiquid ? Math.min(max, 1f) : scaling);
        }

        @Override
        public void craft(){
            consume();

            if(currentRecipe.outputItems != null){
                for(var output : currentRecipe.outputItems){
                    for(int i = 0; i < output.amount; i++){
                        offload(output.item);
                    }
                }
            }

            if(currentRecipe.outputItem != null){
                for(int i = 0; i < currentRecipe.outputItem.amount; i++){
                    offload(currentRecipe.outputItem.item);
                }
            }

            if(wasVisible && currentRecipe.craftEffect != null){
                currentRecipe.craftEffect.at(x, y);
            }
            progress %= 1f;
        }

        public void dumpOutputs(){
            ItemStack[] items = CreateItemstack();
            LiquidStack[] liquids = CreateLiquidstack();

            if(items != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : items){
                    dump(output.item);
                }
            }

            if(liquids != null){
                for(int i = 0; i < liquids .length; i++){
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(liquids[i].liquid, 2f, dir);
                }
            }
        }

        public ItemStack[] CreateItemstack(){
            ItemStack[] out = null;
            if(currentRecipe.outputItems != null){
                out = currentRecipe.outputItems;
            }
            if(currentRecipe.outputItem != null){
                out = ItemStack.with(currentRecipe.outputItem);
            }
            return out;
        }

        public LiquidStack[] CreateLiquidstack(){
            LiquidStack[] out = null;
            if(currentRecipe.outputLiquids != null){
                out = currentRecipe.outputLiquids;
            }
            if(currentRecipe.outputLiquid != null){
                out = LiquidStack.with(currentRecipe.outputLiquid);
            }
            return out;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.i(curRecipeIndex);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            curRecipeIndex = read.i();
            updateCurRecipe(curRecipeIndex, true);
        }

        @Override
        public void buildConfiguration(Table table){
            table.table(Styles.grayPanel, t ->{
                t.image().color(Pal.gray).height(4).growX().row();
                t.table(buttons ->{
                    for(int i = 0; i < craftRecipes.size; i++){
                        final int recipeIndex = i;
                        Recipe recipe = craftRecipes.get(i);
                        if(recipe == null) continue;

                        boolean isCurrent = curRecipeIndex == recipeIndex;
                        float baseWidth = 350;

                        buttons.button(b ->{
                            b.center();

                            b.table(content ->{

                                content.stack(
                                        new Table(td -> {
                                            td.table(input -> addRecipeResources(input, recipe, false))
                                                    .left().growX();

                                            td.table(output -> addRecipeResources(output, recipe, true))
                                                    .right().growX();
                                        }),
                                        new Table(ta -> {
                                            ta.add(">").color(Pal.accent).center();
                                        })
                                ).growX().row();

                                content.table(info ->{
                                    info.defaults().padRight(8);
                                    info.center();

                                    info.add("[lightgray]Time:[] [white]" +
                                            Strings.fixed(recipe.craftTime / 60, 1) + "s");

                                    if(recipe.powerUse > 0){
                                        info.add("[lightgray]Power:[] [white]" +
                                                Strings.fixed(recipe.powerUse * 60, 1) + "/s");
                                    }

                                }).padTop(2);

                            }).grow();

                        }, Styles.flatBordert, () ->{
                            updateCurRecipe(recipeIndex, false);
                            deselect();
                        }).size(350, 50f).row();
                    }
                }).pad(4);
            });
        }

        protected float calculateRecipeWidth(Recipe recipe){
            float width = 90f; // Базовая ширина

            float iconSize = 24f;
            float textWidth = 25f;
            float padding = 2f;
            float arrowWidth = 20f;

            int elementCount = 0;

            if(recipe.inputItem != null) elementCount++;
            if(recipe.outputItem != null) elementCount++;
            if(recipe.inputLiquid != null) elementCount++;
            if(recipe.outputLiquid != null) elementCount++;

            if(recipe.inputItems != null) elementCount += recipe.inputItems.length;
            if(recipe.outputItems != null) elementCount += recipe.outputItems.length;
            if(recipe.inputLiquids != null) elementCount += recipe.inputLiquids.length;
            if(recipe.outputLiquids != null) elementCount += recipe.outputLiquids.length;

            if(hasInputs(recipe) && hasOutputs(recipe)){
                width += arrowWidth;
            }

            width += elementCount * (iconSize + textWidth + padding * 2);

            return Mathf.clamp(width, 180f, 400f);
        }

        private void addRecipeResources(Table table, Recipe recipe, boolean isOutput){
            if(!isOutput){
                // Входные ресурсы
                if(recipe.inputItem != null){
                    addResourceElement(table, recipe.inputItem.item.uiIcon,
                            Strings.fixed(recipe.inputItem.amount, 0) + "x", false);
                }
                if(recipe.inputLiquid != null){
                    addResourceElement(table, recipe.inputLiquid.liquid.uiIcon,
                            Strings.fixed(recipe.inputLiquid.amount * 60, 1) + "/s", false);
                }
                if(recipe.inputItems != null){
                    for(ItemStack input : recipe.inputItems){
                        if(input == null || input.item == null) continue;
                        addResourceElement(table, input.item.uiIcon,
                                Strings.fixed(input.amount, 0) + "x", false);
                    }
                }
                if(recipe.inputLiquids != null){
                    for(LiquidStack liquid : recipe.inputLiquids){
                        if(liquid == null || liquid.liquid == null) continue;
                        addResourceElement(table, liquid.liquid.uiIcon,
                                Strings.fixed(liquid.amount * 60, 1) + "/s", false);
                    }
                }
            } else {
                if(recipe.outputItem != null){
                    addResourceElement(table, recipe.outputItem.item.uiIcon,
                            Strings.fixed(recipe.outputItem.amount, 0) + "x", true);
                }
                if(recipe.outputLiquid != null){
                    addResourceElement(table, recipe.outputLiquid.liquid.uiIcon,
                            Strings.fixed(recipe.outputLiquid.amount * 60, 1) + "/s", true);
                }
                if(recipe.outputItems != null){
                    for(ItemStack output : recipe.outputItems){
                        if(output == null || output.item == null) continue;
                        addResourceElement(table, output.item.uiIcon,
                                Strings.fixed(output.amount, 0) + "x", true);
                    }
                }
                if(recipe.outputLiquids != null){
                    for(LiquidStack liquid : recipe.outputLiquids){
                        if(liquid == null || liquid.liquid == null) continue;
                        addResourceElement(table, liquid.liquid.uiIcon,
                                Strings.fixed(liquid.amount * 60, 1) + "/s", true);
                    }
                }
            }
        }

        private void addResourceElement(Table table, TextureRegion icon, String text, boolean isOutput){
            table.table(element -> {
                element.image(icon).size(24).padRight(2);
                element.add(text).color(Color.lightGray);
            }).padRight(4);
        }

        private boolean hasInputs(Recipe recipe){
            return recipe.inputItem != null || recipe.inputLiquid != null ||
                    recipe.inputItems != null || recipe.inputLiquids != null;
        }

        private boolean hasOutputs(Recipe recipe){
            return recipe.outputItem != null || recipe.outputLiquid != null ||
                    recipe.outputItems != null || recipe.outputLiquids != null;
        }

    }
}
