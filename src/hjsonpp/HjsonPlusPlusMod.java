package hjsonpp;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.Log;
import hjsonpp.expand.ChanceCrafter;
import hjsonpp.expand.MultiRecipeCrafter;
import hjsonpp.expand.wproc.*;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.*;

import static mindustry.Vars.mods;
import static mindustry.Vars.ui;

public class HjsonPlusPlusMod extends Mod{

    String[] modTurnOffBlacklist = {"azimut", "enceladus", "mov"};

    public HjsonPlusPlusMod(){
        ClassMap.classes.put("AdvancedConsumeGenerator", hjsonpp.expand.AdvancedConsumeGenerator.class);
        ClassMap.classes.put("AdvancedHeaterGenerator", hjsonpp.expand.AdvancedHeaterGenerator.class);
        ClassMap.classes.put("TileGenerator", hjsonpp.expand.TileGenerator.class);
        ClassMap.classes.put("AdvancedCoreBlock", hjsonpp.expand.AdvancedCoreBlock.class);
        ClassMap.classes.put("GeneratorCoreBlock", hjsonpp.expand.GeneratorCoreBlock.class);
        ClassMap.classes.put("ChanceCrafter", ChanceCrafter.class);
        ClassMap.classes.put("AccelItemTurret", hjsonpp.expand.AccelItemTurret.class);
        ClassMap.classes.put("OverHeatTurret", hjsonpp.expand.OverHeatTurret.class);
        ClassMap.classes.put("RestorableWall", hjsonpp.expand.RestorableWall.class);
        ClassMap.classes.put("AdjustableShieldWall", hjsonpp.expand.AdjustableShieldWall.class);
        ClassMap.classes.put("AdjustableBeamNode", hjsonpp.expand.AdjustableBeamNode.class);
        ClassMap.classes.put("TiledFloor", hjsonpp.expand.TiledFloor.class);
        ClassMap.classes.put("DrawTeam", hjsonpp.expand.DrawTeam.class);
        ClassMap.classes.put("EffectWeapon", hjsonpp.expand.EffectWeapon.class);
        ClassMap.classes.put("CustomEffects", hjsonpp.expand.CustomEffects.class);
        ClassMap.classes.put("BlackHoleBulletType", hjsonpp.expand.BlackHoleBulletType.class);
        ClassMap.classes.put("ModeTurret", hjsonpp.expand.ModeTurret.class);
        ClassMap.classes.put("MultiRecipeCrafter", hjsonpp.expand.MultiRecipeCrafter.class);
    }

    @Override
    public void init(){
        super.init();
        CustomStyles.load();
    }

    @Override
    public void loadContent(){
        HjsonppLogic.init();
        Events.on(EventType.ClientLoadEvent.class, e->{
            boolean r = false;
            Seq<String> modNames = new Seq<>();
            for(String n : modTurnOffBlacklist){
                Mods.LoadedMod m  = mods.getMod(n);
                if(m != null && m.enabled()) {
                    r = true;
                    Log.info("Idi nahuy: " + m.name);
                    modNames.add(m.name + ";");
                    mods.setEnabled(m, false);
                }
            }
            if(r){
                ui.showOkText("Lol","All blacklist mods are disabled\n"+ modNames, ()->{
                    Core.app.exit();
                });
            }
        });
    }
}
