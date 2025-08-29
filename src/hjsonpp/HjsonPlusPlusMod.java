package hjsonpp;

import mindustry.mod.*;

public class HjsonPlusPlusMod extends Mod{

    public HjsonPlusPlusMod(){
        ClassMap.classes.put("AdvancedConsumeGenerator", hjsonpp.expand.AdvancedConsumeGenerator.class);
        ClassMap.classes.put("AdvancedHeaterGenerator", hjsonpp.expand.AdvancedHeaterGenerator.class);
        ClassMap.classes.put("TileGenerator", hjsonpp.expand.TileGenerator.class);
        ClassMap.classes.put("AdvancedCoreBlock", hjsonpp.expand.AdvancedCoreBlock.class);
        ClassMap.classes.put("GeneratorCoreBlock", hjsonpp.expand.GeneratorCoreBlock.class);
        ClassMap.classes.put("ColliderCrafter", hjsonpp.expand.ColiderCrafter.class);
        ClassMap.classes.put("AccelTurret", hjsonpp.expand.AccelTurret.class);
        ClassMap.classes.put("OverheatTurret", hjsonpp.expand.OverHeatTurret.class);
        //ClassMap.classes.put("HealingWall", hjsonpp.expand.HealingWall.class);
        ClassMap.classes.put("AdjustableShieldWall", hjsonpp.expand.AdjustableShieldWall.class);
        ClassMap.classes.put("AdjustableBeamNode", hjsonpp.expand.AdjustableBeamNode.class);
        ClassMap.classes.put("TiledFloor", hjsonpp.expand.TiledFloor.class);
        ClassMap.classes.put("DrawTeam", hjsonpp.expand.DrawTeam.class);
        ClassMap.classes.put("EffectWeapon", hjsonpp.expand.EffectWeapon.class);
        ClassMap.classes.put("CustomEffects", hjsonpp.expand.CustomEffects.class);
    }
}
