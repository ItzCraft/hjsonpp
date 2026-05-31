package hjsonpp.expand;

import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;

public class CustomEffects{
    public static Effect
    trailParticleEffect = new Effect(8f, e -> {
        float life = Interp.pow2Out.apply(1f - e.fin());
        Color dropColor = Color.valueOf("db96eb").cpy().a(0.7f * life);

        Draw.color(dropColor);
        Fill.circle(e.x, e.y, (1f - e.fin()));
        Draw.reset();
    }),
    purpleOrbital = new Effect(60f, e -> {
        float t = e.time / 60f;
        float alpha = Interp.pow2Out.apply(1f - t) * (0.8f + Mathf.absin(e.id, 6f, 0.1f));
        int particles = 5;
        float baseRadius = 15f;
        float speed = -900f;
        int times = 13;
        for (int i = 0; i < particles; i++) {
            float phaseOffset = Mathf.randomSeed(e.id * 17L + i * 43) * 360f;
            float angle = t * speed + phaseOffset;
            float rad = angle * Mathf.degreesToRadians;
            float interpRadius = baseRadius * (1f + Mathf.absin(t * 5f, 0.1f));
            float x = Mathf.cos(rad) * interpRadius;
            float y = Mathf.sin(rad) * interpRadius;
            float speedAngle = angle + 90f;
            float moveRad = speedAngle * Mathf.degreesToRadians;
            float pulse = 1f + Mathf.sin(t * 12f + i * 1.5f) * 0.1f;
            float orbSize = Interp.pow2Out.apply(0.8f * pulse);
            float trailWidth = orbSize * 1.5f;
            float trailLength = Interp.sineOut.apply(19f * pulse);
            Color c = Color.valueOf("b697c2").cpy().a(alpha);
            Draw.color(c);
            Fill.circle(e.x + x, e.y + y, orbSize);
            Drawf.tri(
                    e.x + x,
                    e.y + y,
                    trailWidth,
                    trailLength,
                    speedAngle
            );
            float px = e.x + x + Mathf.cos(moveRad) * trailLength * 0.6f;
            float py = e.y + y + Mathf.sin(moveRad) * trailLength * 0.6f;
            for(int s=0; s<times; s++){trailParticleEffect.at(px, py);}
        }

        Draw.reset();
    });
}
