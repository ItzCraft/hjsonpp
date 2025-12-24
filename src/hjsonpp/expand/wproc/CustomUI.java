package hjsonpp.expand.wproc;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.scene.actions.*;
import arc.scene.event.Touchable;
import arc.math.*;
import mindustry.core.UI;
import mindustry.ui.Styles;

public class CustomUI extends UI{
    protected static void bordlessTextDialog(String text, String unitIcon, float duration, boolean useBundle){
        Table table = new Table();
        table.touchable = Touchable.disabled;
        table.setFillParent(true);
        table.actions(Actions.delay(duration * 0.8f), Actions.fadeOut(duration * 0.3f, Interp.fade), Actions.remove());
        table.bottom().table(Styles.black5, t -> t.margin(1).image(Core.atlas.find(unitIcon)).style(Styles.outlineLabel)).padLeft(-15f).padBottom(70f).size(65f, 80f);
        if(useBundle){
            table.bottom().table(Styles.black5, t -> t.margin(10f).add(Core.bundle.get(text)).style(Styles.outlineLabel)).padRight(65f).padBottom(70f).size(Core.bundle.get(text).length() * 11.5f, 60f);
        }
        else{
            table.bottom().table(Styles.black5, t -> t.margin(10f).add(text).style(Styles.outlineLabel)).padRight(65f).padBottom(70f).size(text.length() * 11.5f, 60f);
        }
        Core.scene.add(table);
    }
    protected static void borderTextDialog(String text, String unitIcon, float duration, boolean useBundle){
        Table table = new Table();
        table.touchable = Touchable.disabled;
        table.setFillParent(true);
        table.actions(Actions.delay(duration * 0.8f), Actions.fadeOut(duration * 0.3f, Interp.fade), Actions.remove());
        table.bottom().table(Styles.grayPanelDark, t -> t.margin(1).image(Core.atlas.find(unitIcon)).style(Styles.outlineLabel)).padLeft(-15f).padBottom(70f).size(65f, 80f);
        if(useBundle){
            table.bottom().table(Styles.grayPanelDark, t -> t.margin(10f).add(Core.bundle.get(text)).style(Styles.outlineLabel)).padRight(65f).padBottom(70f).size(Core.bundle.get(text).length() * 11.5f, 60f);
        }
        else{
            table.bottom().table(Styles.grayPanelDark, t -> t.margin(10f).add(text).style(Styles.outlineLabel)).padRight(65f).padBottom(70f).size(text.length() * 11.5f, 60f);
        }
        Core.scene.add(table);
    }
    public static void textDialog(String text, String unitIcon, float duration, boolean useBundle, String uiStyle){
        switch (uiStyle){
            case "bordless" -> bordlessTextDialog(text, unitIcon, duration, useBundle);

            case "border" -> borderTextDialog(text, unitIcon, duration, useBundle);
        };
    }
    public static void blackScreen(float duration){
        Table table = new Table();
        table.touchable = Touchable.disabled;
        table.setFillParent(true);
        table.actions(Actions.delay(duration * 0.8f), Actions.fadeOut(duration * 0.3f, Interp.fade), Actions.remove());
        table.bottom().table(CustomStyles.blackBackground, t -> t.margin(1).image(Core.atlas.find("epsilon-black-screen")).style(Styles.outlineLabel)).size(2500f, 2500f);
        Core.scene.add(table);
    }
}
