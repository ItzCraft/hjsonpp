package hjsonpp.expand.modules;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Nullable;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItems;
import mindustry.world.consumers.ConsumeLiquids;
import mindustry.world.draw.DrawBlock;
import mindustry.world.meta.*;

public class RecipeIO {
    @Nullable
    public ItemStack[] input;
    @Nullable
    public ItemStack[] output;
    public float powerUse = 0;
    @Nullable
    public LiquidStack[] liquidIn;
    @Nullable
    public LiquidStack[] liquidOut;
    public float recipeTime = 60;
    public boolean needDoUnlock = true;
    public Seq<Consume> consumers = new Seq<>();
    public float totalProgress = 0;
    public float warmup = 0;

    @Nullable
    public DrawBlock uniqueDrawer;

    @Nullable
    public boolean[] itemFilter, liquidFilter;

    @Nullable
    public Seq<UnlockableContent> recipeReq = new Seq<>();

    public void dumpOutputs(Building build) {
        if(output != null){
            for (ItemStack i : output) {
                for (int j = 0; j < i.amount; j++) {
                    build.dump(i.item);
                }
            }
        }
    }

    public void dumpTimedOutputs(Building build) {
        if(output != null){
            for (ItemStack i : output) {
                for (int j = 0; j < i.amount; j++) {
                    build.dump(i.item);
                }
            }
        }
    }

    public void craft(Building build){
        if(output != null){
            for (ItemStack stack : output) {
                for (int i = 0; i < stack.amount; i++) {
                    build.offload(stack.item);
                }
            }
        }
    }

    public void update(Building build) {
        for (Consume consume : consumers) {
            consume.update(build);
        }

        if(liquidOut != null){
            float inc = build.getProgressIncrease(1f);
            for (LiquidStack stack : liquidOut) {
                build.handleLiquid(build, stack.liquid, Math.min(stack.amount * inc, build.block.liquidCapacity - build.liquids.get(stack.liquid)));
            }
        }
    }

    public RecipeIO(){
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output){
        this.input = input;
        this.output = output;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, float recipeTime){
        this.input = input;
        this.output = output;
        this.recipeTime = recipeTime;

    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, float recipeTime, float powerUse){
        this.input = input;
        this.output = output;
        this.recipeTime = recipeTime;
        this.powerUse = powerUse;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn, float recipeTime){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
        this.recipeTime = recipeTime;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn, float recipeTime, float powerUse){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
        this.recipeTime = recipeTime;
        this.powerUse = powerUse;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn, LiquidStack[] liquidOut){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
        this.liquidOut = liquidOut;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn, LiquidStack[] liquidOut, float recipeTime){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
        this.liquidOut = liquidOut;
        this.recipeTime = recipeTime;
    }

    public RecipeIO(ItemStack[] input, ItemStack[] output, LiquidStack[] liquidIn, LiquidStack[] liquidOut, float recipeTime, float powerUse){
        this.input = input;
        this.output = output;
        this.liquidIn = liquidIn;
        this.liquidOut = liquidOut;
        this.recipeTime = recipeTime;
        this.powerUse = powerUse;
    }

    public void addRequire(ItemStack[] items){
        for(ItemStack i : items){
            recipeReq.addAll(i.item);
        }
    }

    public void addRequire(LiquidStack[] liquids){
        for(LiquidStack l : liquids){
            recipeReq.addAll(l.liquid);
        }
    }

    public void addRequire(ItemStack[] items, LiquidStack[] liquids){
        for(ItemStack i : items){
            recipeReq.addAll(i.item);
        }

        for(LiquidStack l : liquids){
            recipeReq.addAll(l.liquid);
        }
    }

    //Checks all input items and liquids
    public boolean recipeIsValid(){
        if (!Vars.state.isCampaign() || !needDoUnlock || recipeReq == null) return true;
        for (UnlockableContent ct : recipeReq){
            if(!ct.unlocked()) return false;
        }
        return true;
    }

    public void init(){
        if(input != null) addConsumers(input);
        if(liquidIn != null) addConsumers(liquidIn);
        if(needDoUnlock){
            if(input != null) addRequire(input);
            if(liquidIn != null) addRequire(liquidIn);
        }
    }

    public void addConsumers(ItemStack[] i){
        if(i != null){
            consumers.add(new ConsumeItems(i));
        }
    }

    public void addConsumers(LiquidStack[] l){
        if(l != null){
            consumers.add(new ConsumeLiquids(l));
        }
    }

    public void addConsumers(ItemStack[] i, LiquidStack[] l){
        if(i != null){
            consumers.add(new ConsumeItems(i));
        }
        if(l != null){
            consumers.add(new ConsumeLiquids(l));
        }
    }

    public void apply(Block block){
        if(output != null || input != null) block.hasItems = true;
        if(liquidIn != null || liquidOut != null) block.hasLiquids = true;
        if(powerUse != 0) {
            block.hasPower = true;
            block.consumesPower = true;
        }
    }

    public void display(Table table){
        Stats recipeStats = new Stats();
        recipeStats.timePeriod = recipeTime;
        for (Consume consume : consumers) {
            consume.display(recipeStats);
        }
        if(output != null || liquidOut != null) displayOut(recipeStats);
        table.table(Styles.grayPanel, t -> {
            if(recipeIsValid()){

                t.table(in ->{
                    in.left();
                    OrderedMap<Stat, Seq<StatValue>> map = recipeStats.toMap().get(StatCat.crafting);
                    Seq<StatValue> arr = map.get(Stat.input);
                    if(arr != null) {
                        for (StatValue value : arr) {
                            value.display(in);
                        }
                    }
                    if(this.powerUse != 0) in.table(pwrUse -> {
                        pwrUse.image(Icon.power).color(Pal.accent).size(40);
                        pwrUse.add(this.powerUse + "/s");
                    });
                }).left().pad(10f);


                t.table(arrow ->{
                    arrow.image(Icon.right).color(Pal.darkishGray).size(40f);
                    arrow.left();
                });

                t.table(time -> {
                    time.image(Icon.crafting).color(Pal.accent).size(40);
                    time.left();
                });

                t.add(Core.bundle.format("ui.craftTime", Strings.autoFixed(this.recipeTime / 60, 3))).color(Pal.accent).pad(10f).left();

                t.table(out ->{
                    out.right();
                    OrderedMap<Stat, Seq<StatValue>> map = recipeStats.toMap().get(StatCat.crafting);
                    Seq<StatValue> arr = map.get(Stat.output);
                    if(arr != null) {
                        for (StatValue value : arr) {
                            value.display(out);
                        }
                    }
                }).right().grow().pad(10f);
            }else {
                t.image(Icon.lock).color(Pal.darkerGray).size(40f).grow().pad(10f);
            }
        }).growX();
    }
    public void displayOut(Stats stats) {
        if(output != null)stats.add(Stat.output, stats.timePeriod < 0 ? StatValues.items(output) : StatValues.items(stats.timePeriod, output));
        if(liquidOut != null)stats.add(Stat.output, StatValues.liquids(1, liquidOut));
    }

    public boolean shouldConsume(Building build){
        if(output != null) {
            for (ItemStack i : output) {
                if (i.amount > build.block.itemCapacity) return false;
            }
        }
        if(liquidOut != null){
            for (LiquidStack l : liquidOut) {
                if (l.amount > build.block.liquidCapacity) return false;
            }
        }
        return true;
    }

    public boolean consumesItem(Item item) {
        return itemFilter[item.id];
    }

    public boolean consumesLiquid(Liquid liquid) {
        return liquidFilter[liquid.id];
    }
}
