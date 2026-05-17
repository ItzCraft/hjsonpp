package hjsonpp.expand.wproc.statements;

import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import hjsonpp.expand.wproc.DialogueStyles;
import hjsonpp.expand.wproc.HjsonppLogic;
import hjsonpp.expand.wproc.instructions.TextDialogI;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.logic.*;
import mindustry.type.UnitType;
import mindustry.ui.Styles;

import static mindustry.Vars.iconSmall;
import static mindustry.Vars.ui;

public class TextDialog extends LStatement{
    public String text = ":3", unit = "@dagger";
    public String duration = "5";
    public String useBundles = "true";
    public String uiTemplate = "borderless";
    public String nametag = "dagger";

    public TextDialog(String[] tokens){
        text = tokens[1];
        unit = tokens[2];
        duration = tokens[3];
        useBundles = tokens[4];
        uiTemplate = tokens[5];
        nametag = tokens[6];
    }
    public TextDialog(){}

    @Override
    public void build(Table table){
        table.add(" text ");

        fields(table, text, v -> text = v);

        table.add(" duration ");

        fields(table, duration, c -> duration = c);

        table.add(" use Bundles ");

        fields(table, useBundles, g -> useBundles = g);

        table.add(" UI template ");

        table.button(b -> {
            b.label(() -> uiTemplate)
                    .growX()
                    .wrap()
                    .labelAlign(Align.center);
            b.clicked(() -> showSelectTable(b, (t, hide) -> {
                t.background(Styles.black6);
                t.table(i -> {
                    i.left();
                    int c = 0;
                    for(DialogueStyles style : DialogueStyles.values()){
                        i.button(DialogueStyles.stylesMap.get(style), Styles.flatt, () -> {
                            uiTemplate = DialogueStyles.stylesMap.get(style);
                            hide.run();
                        }).width(110f).height(40f);
                        if(++c % 2 == 0) i.row();
                    }
                }).pad(6f);
            }));
        }, Styles.logict, () -> {}).size(120f, 40f).padLeft(2).color(table.color);
        switch (uiTemplate) {
            case "bordless", "border" -> {
                table.row();
                table.add(" unit ").marginBottom(5f);

                TextField field = field(table, unit, str -> unit = str).get();

                table.button(b -> {
                    b.image(Icon.pencilSmall);
                    b.clicked(() -> showSelectTable(b, (t, hide) -> {
                        t.row();
                        t.table(i -> {
                            i.left();
                            int c = 0;
                            for (UnitType item : Vars.content.units()) {
                                if (!item.unlockedNow() || item.isHidden() || !item.logicControllable) continue;
                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    unit = "@" + item.name;
                                    field.setText(unit);
                                    hide.run();
                                }).size(40f);

                                if (++c % 6 == 0) i.row();
                            }
                        }).colspan(3).width(240f).left();
                    }));
                }, Styles.logict, () -> {
                }).size(40f).padLeft(-1).color(table.color).marginBottom(5f);
            }

                /*case "nametag" -> {
                    table.row();
                    table.add(" unit ").marginBottom(5f);

                    TextField field = field(table, unit, str -> unit = str).get();

                    table.button(b -> {
                        b.image(Icon.pencilSmall);
                        b.clicked(() -> showSelectTable(b, (t, hide) -> {
                            t.row();
                            t.table(i -> {
                                i.left();
                                int c = 0;
                                for (UnitType item : Vars.content.units()) {
                                    if (!item.unlockedNow() || item.isHidden() || !item.logicControllable) continue;
                                    i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                        unit = "@" + item.name;
                                        field.setText(unit);
                                        hide.run();
                                    }).size(40f);

                                    if (++c % 6 == 0) i.row();
                                }
                            }).colspan(3).width(240f).left();
                        }));
                    }, Styles.logict, () -> {
                    }).size(40f).padLeft(-1).color(table.color).marginBottom(5f);

                    table.add(" name ");

                    fields(table, nametag, h -> nametag = h);
                }*/
        }
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler builder) {
        return new TextDialogI(builder.var(text), builder.var(unit), builder.var(duration), builder.var(useBundles), builder.var("\"" + uiTemplate + "\""), builder.var("\"" + nametag + "\""));
    }

    @Override
    public LCategory category() {
        return HjsonppLogic.Hjsonpp;
    }

    public void write(StringBuilder builder){
        builder.append("textdialog");
        builder.append(" ");
        builder.append(text);
        builder.append(" ");
        builder.append(unit);
        builder.append(" ");
        builder.append(duration);
        builder.append(" ");
        builder.append(useBundles);
        builder.append(" ");
        builder.append(uiTemplate);
        builder.append(" ");
        builder.append(nametag);
    }
}