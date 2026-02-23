package hjsonpp.expand;

import mindustry.world.production.*;

public class AdvancedGenericCrafter extends GenericCrafter{

    // well, it is similar thing as liquidOutputDirections
    public int[] itemOutputDirections = {-1};

    public AdvancedGenericCrafter(String name){
        super(name);
    }

    @Override
    public boolean rotatedOutput(int fromX, int fromY, Tile destination){
        if(!(destination.build instanceof ConduitBuild)) return false;

        Building crafter = world.build(fromX, fromY);
        if(crafter == null) return false;
        int relative = Mathf.mod(crafter.relativeTo(destination) - crafter.rotation, 4);
        for(int dir : liquidOutputDirections){
            if(dir == -1 || dir == relative) return false;
        }
        for(int dir : itemOutputDirections){
            if(dir == -1 || dir == relative) return false;
        }

        return true;
    }

    @Override
    public void drawOverlay(float x, float y, int rotation){
        super.drawOverlay(x,y,rotation);
        if(outputItems != null){
            for(int i = 0; i < outputItems.length; i++){
                int dir = itemOutputDirections.length > i ? itemOutputDirections[i] : -1;

                if(dir != -1){
                    Draw.rect(
                        outputItems[i].item.fullIcon,
                        x + Geometry.d4x(dir + rotation) * (size * tilesize / 2f + 4),
                        y + Geometry.d4y(dir + rotation) * (size * tilesize / 2f + 4),
                        8f, 8f
                    );
                }
            }
        }
    }

    public class AdvancedGenericCrafterBuild extends GenericCrafterBuild{
            @Override
            public void dumpOutputs(){
            if(outputItems != null && timer(timerDump, dumpTime / timeScale)){
                for(ItemStack output : outputItems){
                    dump(output.item);
                }
            }

            if(outputLiquids != null){
                for(int i = 0; i < outputLiquids.length; i++){
                    int dir = liquidOutputDirections.length > i ? liquidOutputDirections[i] : -1;

                    dumpLiquid(outputLiquids[i].liquid, 2f, dir);
                }
            }
        }
    }
}