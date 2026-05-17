package hjsonpp.expand.wproc;

import arc.scene.style.Drawable;
import arc.scene.style.TextureRegionDrawable;
import mindustry.gen.Tex;

public class CustomStyles {
    public static Drawable blackBackground;

    public CustomStyles(){}

    public static void load() {
        final TextureRegionDrawable whiteui = (TextureRegionDrawable) Tex.whiteui;
        blackBackground = whiteui.tint(0.0F, 0.0F, 0.0F, 1F);
    }
}