package com.jams.music.player.Utils;

import android.view.animation.Interpolator;

public class EaseInOutInterpolator implements Interpolator {

    private EasingType.Type type;

    public EaseInOutInterpolator(EasingType.Type type) {
        this.type = type;
    }

    public float getInterpolation(float t) {
        if (type == EasingType.Type.IN) {
            return in(t);
        } else if (type == EasingType.Type.OUT) {
            return out(t);
        } else if (type == EasingType.Type.INOUT) {
            return inout(t);
        }

        return 0;
    }

    private float in(float t) {
        return (float) (-Math.cos(t * (Math.PI/2)) + 1);
    }
    private float out(float t) {
        return (float) Math.sin(t * (Math.PI/2));
    }
    private float inout(float t) {
        return (float) (-0.5f * (Math.cos(Math.PI*t) - 1));
    }

    public static class EasingType {
        public enum Type {
            IN, OUT, INOUT
        }

    }

}
