package hjsonpp.expand.wproc.instructions;

import arc.util.Log;
import hjsonpp.expand.wproc.CustomUI;
import hjsonpp.expand.wproc.DialogueStyles;
import mindustry.logic.*;
import mindustry.type.UnitType;

public class TextDialogI implements LExecutor.LInstruction{
    public LVar text, unitIconName, duration, useBundle, uiTemplate, nametag;

    public TextDialogI(LVar text, LVar unitIconName, LVar duration, LVar useBundle, LVar uiTemplate, LVar nametag){
        this.text = text;
        this.unitIconName = unitIconName;
        this.duration = duration;
        this.useBundle = useBundle;
        this.uiTemplate = uiTemplate;
        this.nametag = nametag;
    }

    public TextDialogI(){}

    @Override
    public void run(LExecutor exec){
        Log.info(uiTemplate.obj());
        if(unitIconName.obj() instanceof UnitType icon && text.obj() instanceof String t){
            CustomUI.textDialog(t, icon.name, duration.numf(), useBundle.bool(), uiTemplate.obj().toString(), nametag.obj().toString());
        }
    }
}