/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jams.music.player.Views;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.SystemClock;

public class CyclicTransitionDrawable extends LayerDrawable implements Drawable.Callback {
	
    protected enum TransitionState {
        STARTING, PAUSED, RUNNING
    }

    protected Drawable[] drawables;
    protected int currentDrawableIndex;
    protected int alpha = 0;
    protected int fromAlpha;
    protected int toAlpha;
    protected long duration;
    protected long startTimeMillis;
    protected long pauseDuration;

    protected TransitionState transitionStatus;

    public CyclicTransitionDrawable(Drawable[] drawables) {
        super(drawables);
        this.drawables = drawables;
    }

    public void startTransition(int durationMillis, int pauseTimeMillis) {
        fromAlpha = 0;
        toAlpha = 255;
        duration = durationMillis;
        pauseDuration = pauseTimeMillis;
        startTimeMillis = SystemClock.uptimeMillis();
        transitionStatus = TransitionState.PAUSED;
        currentDrawableIndex = 0;

        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        boolean done = true;

        switch (transitionStatus) {
            case STARTING:
                done = false;
                transitionStatus = TransitionState.RUNNING;
                break;

            case PAUSED:
                if ((SystemClock.uptimeMillis() - startTimeMillis) < pauseDuration)
                    break;
                else {
                    done = false;
                    startTimeMillis = SystemClock.uptimeMillis();
                    transitionStatus = TransitionState.RUNNING;
                }

            case RUNNING:
                break;
        }

        // Determine position within the transition cycle
        if (startTimeMillis >= 0) {
            float normalized = (float) (SystemClock.uptimeMillis() - startTimeMillis) / duration;
            done = normalized >= 1.0f;
            normalized = Math.min(normalized, 1.0f);
            alpha = (int) (fromAlpha + (toAlpha - fromAlpha) * normalized);
        }

        if (transitionStatus == TransitionState.RUNNING) {
            // Cross fade the current
            int nextDrawableIndex = 0;

            if (currentDrawableIndex + 1 < drawables.length)
                nextDrawableIndex = currentDrawableIndex + 1;

            Drawable currentDrawable = getDrawable(currentDrawableIndex);
            Drawable nextDrawable = getDrawable(nextDrawableIndex);

            // Apply cross fade and draw the current drawable
            currentDrawable.setAlpha(255 - alpha);
            currentDrawable.draw(canvas);
            currentDrawable.setAlpha(0xFF);

            if (alpha > 0) {
                nextDrawable.setAlpha(alpha);
                nextDrawable.draw(canvas);
                nextDrawable.setAlpha(0xFF);
            }

            // If we have finished, move to the next transition
            if (done) {
                currentDrawableIndex = nextDrawableIndex;
                startTimeMillis = SystemClock.uptimeMillis();

                transitionStatus = TransitionState.PAUSED;
            }
            
        } else {
        	try {
        		getDrawable(currentDrawableIndex).draw(canvas);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
            
        }

        invalidateSelf();
    }
    
}
