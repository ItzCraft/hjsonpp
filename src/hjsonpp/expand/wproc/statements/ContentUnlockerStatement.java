package hjsonpp.expand.wproc.statements;

import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Stack;
import arc.scene.ui.layout.Table;
import hjsonpp.expand.wproc.HjsonppLogic;
import hjsonpp.expand.wproc.instructions.ContentUnlockerInstructions;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.logic.LAssembler;
import mindustry.logic.LCategory;
import mindustry.logic.LExecutor;
import mindustry.logic.LStatement;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;

import static mindustry.Vars.iconSmall;

public class ContentUnlockerStatement extends LStatement {

    public String[] possibleTypes = {"item", "liquid", "unit", "block"};

    public String type = "item";

    public String contentName = "copper";

    private transient TextField tfield;

    private transient int selected = 0;

    public ContentUnlockerStatement(String[] tokens){
        type = tokens[1];
        contentName = tokens[2];
    }

    public ContentUnlockerStatement(){}

    @Override
    public void build(Table table) {
        table.add(" Content type ");
        table.button(b -> {
            b.label(() -> type);
            b.clicked(()->showSelectTable(b, (t, hide)->{
                t.background(Styles.black6);
                t.table(j->{
                    j.left();
                    for(String typeName : possibleTypes){
                        j.button(typeName, Styles.flatt, ()->{
                            type = typeName ;
                            hide.run();
                        }).width(110f).height(40f);
                        j.row();
                    }
                });
            }));
        }, Styles.logict, () -> {}).size(120f, 40f).padLeft(2).color(table.color);

        table.add(" Content name ");
        tfield = field(table, contentName, v -> contentName = v).padRight(0f).get();
        table.button(butt ->{
            butt.image(Icon.pencilSmall);
            butt.clicked(()-> showSelectTable(butt, (t, hide)->{
                Table[] tables = {
                        new Table(i -> {
                            i.left();
                            int c = 0;
                            for(Item item : Vars.content.items()){
                                if(item.hidden) continue;
                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    stype("item", item.name);
                                    hide.run();
                                }).size(40f);

                                if(++c % 6 == 0) i.row();
                            }
                        }),
                        new Table(i -> {
                            i.left();
                            int c = 0;
                            for(Liquid item : Vars.content.liquids()){
                                if(item.hidden) continue;
                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    stype("liquid", item.name);
                                    hide.run();
                                }).size(40f);

                                if(++c % 6 == 0) i.row();
                            }
                        }),
                        new Table(i -> {
                            i.left();
                            int c = 0;
                            for(UnitType item : Vars.content.units()){
                                if(item.hidden) continue;
                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    stype("unit", item.name);
                                    hide.run();
                                }).size(40f);

                                if(++c % 6 == 0) i.row();
                            }
                        }),
                        new Table(i -> {
                            i.left();
                            int c = 0;
                            for(Block item : Vars.content.blocks()){
                                if(item.isHidden()) continue;
                                i.button(new TextureRegionDrawable(item.uiIcon), Styles.flati, iconSmall, () -> {
                                    stype("block", item.name);
                                    hide.run();
                                }).size(40f);

                                if(++c % 6 == 0) i.row();
                            }
                        })
                };
                Drawable[] icons = {Icon.production, Icon.liquid, Icon.units, Icon.box};
                Stack stack = new Stack(tables[selected]);
                ButtonGroup<Button> group = new ButtonGroup<>();
                for(int i = 0; i < tables.length; i++){
                    int fi = i;

                    t.button(icons[i], Styles.squareTogglei, () -> {
                        selected = fi;

                        stack.clearChildren();
                        stack.addChild(tables[selected]);

                        t.parent.parent.pack();
                        t.parent.parent.invalidateHierarchy();
                    }).height(50f).growX().checked(selected == fi).group(group);
                }
                t.row();
                t.add(stack).colspan(4).width(240f).left();

            }));
        }, Styles.logict, () -> {}).size(40f).padLeft(-1).color(table.color);
    }

    private void stype(String type,String cname){
        tfield.setText(cname);
        this.type = type;
        this.contentName = cname;
    }

    @Override
    public boolean privileged() {
        return true;
    }

    @Override
    public LExecutor.LInstruction build(LAssembler lAssembler) {
        return new ContentUnlockerInstructions(lAssembler.var('"' + type + '"'), lAssembler.var('"' + contentName + '"'));
    }


    @Override
    public LCategory category() {
        return HjsonppLogic.Hjsonpp;
    }

    public void write(StringBuilder builder){
        builder.append("unlockcontent");
        builder.append(" ");
        builder.append(type);
        builder.append(" ");
        builder.append(contentName);
    }
}
