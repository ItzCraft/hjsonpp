package hjsonpp.expand.wproc.instructions;

import arc.util.Log;
import mindustry.Vars;
import mindustry.logic.LExecutor;
import mindustry.logic.LVar;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.world.Block;

public class ContentUnlockerInstructions implements LExecutor.LInstruction {
    public LVar LType;

    public LVar LcontentName;

    @Override
    public void run(LExecutor lExecutor) {
        Log.info("Trying to unlock");
        if(LcontentName.obj() instanceof  String cname){
            Log.info("Content name: " + cname);
            if(LType.obj() instanceof  String ctype){
                Log.info("Debug... Type: " + ctype + " Content name: " + cname);
                if(Vars.state.isCampaign()){
                    switch (ctype) {
                        case "item": {
                            Log.info("Unlocking item..." + cname);
                            Item var = Vars.content.items().find(i -> i.name.equals(cname));
                            if(var != null && !var.unlocked()) var.unlock();
                        }
                        case "liquid": {
                            Log.info("Unlocking liquid..." + cname);
                            Liquid var = Vars.content.liquids().find(i -> i.name.equals(cname));
                            if(var != null && !var.unlocked()) var.unlock();
                        }
                        case "block": {
                            Log.info("Unlocking block..." + cname);
                            Block var = Vars.content.blocks().find(i -> i.name.equals(cname));
                            if(var != null && !var.unlocked()) var.unlock();
                        }
                        case "unit": {
                            Log.info("Unlocking unit..." + cname);
                            UnitType var = Vars.content.units().find(i -> i.name.equals(cname));
                            if(var != null && !var.unlocked()) var.unlock();
                        }
                        default: Log.info("Cannot execute, not in provided types");
                    }
                } else Log.info("Isn't campaign now");
            }
        } else Log.warn("Invalid content name.");
    }

    public ContentUnlockerInstructions(LVar type ,LVar content){
        this.LcontentName = content;
        this.LType = type;
    }

    public ContentUnlockerInstructions(){}
}
