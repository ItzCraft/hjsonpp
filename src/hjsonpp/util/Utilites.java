package hjsonpp.util;

import arc.graphics.Color;
import arc.math.Mathf;

public class Utilites {
    public static Color lerpColor(Color color1, Color color2, float progress) {
        progress = Mathf.clamp(progress, 0f, 1f);

        Color clr = new Color();

        clr.r = Mathf.lerp(color1.r, color2.r, progress);
        clr.g = Mathf.lerp(color1.g, color2.g, progress);
        clr.b = Mathf.lerp(color1.b, color2.b, progress);
        clr.a = Mathf.lerp(color1.a, color2.a, progress);

        return clr;
    }
}
