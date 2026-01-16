package hjsonpp.expand.meta;

import arc.util.Nullable;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.type.*;


/**
 * @author Sputnuc
 */
public class Recipe{
    //Consume
    @Nullable
    public ItemStack[] inputItems;
    @Nullable
    public LiquidStack[] inputLiquids;

    @Nullable
    public ItemStack inputItem;
    @Nullable
    public LiquidStack inputLiquid;

    public float powerUse = 0f;
    public float craftTime = 60f;

    //Output
    @Nullable
    public ItemStack[] outputItems;
    @Nullable
    public LiquidStack[] outputLiquids;

    @Nullable
    public ItemStack outputItem;
    @Nullable
    public LiquidStack outputLiquid;

    public Effect craftEffect = Fx.drillSteam;

    public Recipe(){}
}
