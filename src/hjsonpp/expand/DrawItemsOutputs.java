package hjsonpp.expand;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;

public class DrawItemsOutputs extends DrawBlock{
    public TextureRegion[][] itemOutputRegions;

    @Override
    public void draw(Building build){
        AdvancedGenericCrafter crafter = (AdvancedGenericCrafter)build.block;
        if(crafter.outputItems == null) return;

        for(int i = 0; i < crafter.outputItems.length; i++){
            int side = i < crafter.itemOutputDirections.length ? crafter.itemOutputDirections[i] : -1;
            if(side != -1){
                int realRot = (side + build.rotation) % 4;
                Draw.rect(itemOutputRegions[realRot > 1 ? 1 : 0][i], build.x, build.y, realRot * 90);
            }
        }
    }

    @Override
    public void drawPlan(Block block, BuildPlan plan, Eachable<BuildPlan> list){
        AdvancedGenericCrafter crafter = (AdvancedGenericCrafter)block;
        if(crafter.outputItems == null) return;

        for(int i = 0; i < crafter.outputItems.length; i++){
            int side = i < crafter.itemOutputDirections.length ? crafter.itemOutputDirections[i] : -1;
            if(side != -1){
                int realRot = (side + plan.rotation) % 4;
                Draw.rect(itemOutputRegions[realRot > 1 ? 1 : 0][i], plan.drawx(), plan.drawy(), realRot * 90);
            }
        }
    }

    public AdvancedGenericCrafter expectCrafter(Block block){
        if(!(block instanceof AdvancedGenericCrafter crafter)) throw new ClassCastException("This drawer requires the block to be a AdvancedGenericCrafter. Use a different drawer.");
        return crafter;
    }

    @Override
    public void load(Block block){
        var crafter = expectAdvCrafter(block);

        if(crafter.outputItems == null) return;

        itemOutputRegions = new TextureRegion[2][crafter.outputItems.length];
        for(int i = 0; i < crafter.outputItems.length; i++){
            for(int j = 1; j <= 2; j++){
                itemOutputRegions[j - 1][i] = Core.atlas.find(block.name + "-" + crafter.outputItems[i].item.name + "-output" + j);
            }
        }
    }
}