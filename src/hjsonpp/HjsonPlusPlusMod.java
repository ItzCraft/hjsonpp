package hjsonpp;

import hjsonpp.expand.ColliderCrafter;
import hjsonpp.expand.wproc.CustomStyles;
import hjsonpp.expand.wproc.HjsonppLogic;
import mindustry.mod.*;

public class HjsonPlusPlusMod extends Mod{

    public HjsonPlusPlusMod(){
        ClassMap.classes.put("AdvancedConsumeGenerator", hjsonpp.expand.AdvancedConsumeGenerator.class);
        ClassMap.classes.put("AdvancedHeaterGenerator", hjsonpp.expand.AdvancedHeaterGenerator.class);
        ClassMap.classes.put("TileGenerator", hjsonpp.expand.TileGenerator.class);
        ClassMap.classes.put("AdvancedCoreBlock", hjsonpp.expand.AdvancedCoreBlock.class);
        ClassMap.classes.put("GeneratorCoreBlock", hjsonpp.expand.GeneratorCoreBlock.class);
        //ClassMap.classes.put("ShieldCoreBlock", hjsonpp.expand.ShieldCoreBlock.class);
        ClassMap.classes.put("ColliderCrafter", ColliderCrafter.class);
        ClassMap.classes.put("MultiCrafter", hjsonpp.expand.MultiCrafter.class);
        ClassMap.classes.put("AccelTurret", hjsonpp.expand.AccelTurret.class);
        ClassMap.classes.put("OverHeatTurret", hjsonpp.expand.OverHeatTurret.class);
        ClassMap.classes.put("RestorableWall", hjsonpp.expand.RestorableWall.class);
        ClassMap.classes.put("AdjustableShieldWall", hjsonpp.expand.AdjustableShieldWall.class);
        ClassMap.classes.put("AdjustableBeamNode", hjsonpp.expand.AdjustableBeamNode.class);
        ClassMap.classes.put("DrawableBlock", hjsonpp.expand.DrawableBlock.class);
        ClassMap.classes.put("TiledFloor", hjsonpp.expand.TiledFloor.class);
        ClassMap.classes.put("DrawTeam", hjsonpp.expand.DrawTeam.class);
        //ClassMap.classes.put("CustomEffects", hjsonpp.expand.CustomEffects.class);
    }

    @Override
    public void init(){
        super.init();
        CustomStyles.load();
    }

    @Override
    public void loadContent(){
        HjsonppLogic.init();
    }
}
