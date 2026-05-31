package hjsonpp.expand.wproc;

import arc.graphics.Color;
import hjsonpp.expand.wproc.statements.*;
import mindustry.gen.LogicIO;
import mindustry.logic.*;

public class HjsonppLogic {
    public static LCategory Hjsonpp;

    public static void init(){
        Hjsonpp = new LCategory("hjsonpp-category", Color.valueOf("5edb80"));

        LAssembler.customParsers.put("textdialog", TextDialog::new);
        LogicIO.allStatements.addUnique(TextDialog::new);

        LAssembler.customParsers.put("unlockcontent", ContentUnlockerStatement::new);
        LogicIO.allStatements.addUnique(ContentUnlockerStatement::new);
    }
}