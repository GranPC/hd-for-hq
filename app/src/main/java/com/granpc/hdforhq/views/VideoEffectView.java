package com.granpc.hdforhq.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;

public class VideoEffectView extends View
{
    public static final int TYPE_TRIVIA = 0;
    public static final int ANIMATING_IN_QUESTION    = 0;
    public static final int ANIMATING_OUT_PILL       = 1;

    private static final float QUESTION_HEAD_SCALE = 0.15f;

    private static final int TRIVIA_ANIM_IN_MILLIS        = 300;
    private static final int TRIVIA_ANIM_PILL_OUT_MILLIS  = 700;

    private static final PathInterpolator scaleInAnimateInterpolator =
        new PathInterpolator( 0.05f, 0.70f, 0.2f, 1.07f );

    public TextureView videoView;
    public FrameLayout countdownContainer;

    private int countdownPos[];
    private Path circlePath;
    private float countdownBorderSize;

    private int animating = -1;
    private long animateStartMs;
    private long animateEndMs;

    private int type = TYPE_TRIVIA;

    public VideoEffectView( Context context )
    {
        super( context );
        setLayerType( LAYER_TYPE_HARDWARE, null );

        countdownPos = new int[2];
        circlePath = new Path();
        videoView = null;

        // According to progress_circle.xml, countdown thickness is 5.1sp
        countdownBorderSize = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP, 5.1f, getResources().getDisplayMetrics() );
    }

    public void onQuestionShown()
    {
        animating = ANIMATING_IN_QUESTION;
        animateStartMs = SystemClock.uptimeMillis();
        animateEndMs = animateStartMs + TRIVIA_ANIM_IN_MILLIS;
        postInvalidate();
    }

    public void onPillShown()
    {
        animating = ANIMATING_OUT_PILL;
        animateStartMs = SystemClock.uptimeMillis();
        animateEndMs = animateStartMs + TRIVIA_ANIM_PILL_OUT_MILLIS;
        postInvalidate();
    }

    // TODO: maybe using animators would be worth it? dunno!
    private float easeOutExpo( float time, float base, float delta, float from )
    {
        if ( time >= from ) return base + delta;

        return delta * ((float) -Math.pow( 2, -10 * time / from ) + 1 ) + base;
    }

    @Override
    protected void onDraw( Canvas canvas )
    {
        long now = SystemClock.uptimeMillis();
        float animLength = animateEndMs - animateStartMs;
        float animLapsed = now - animateStartMs;
        float animScalar = easeOutExpo( animLapsed, 0, 1, animLength );
        float animPct = animLapsed / animLength;

        if ( animPct > 1 ) animPct = 1;

        if ( !canvas.isHardwareAccelerated() )
        {
            Log.e("HD4HQ", "Canvas is not fucking hw accelerated");
        }
        if ( videoView != null )
        {
            // This only works on Nougat+. Got to figure out something for old versions, maybe.

            canvas.save();

                countdownContainer.getLocationOnScreen( countdownPos );
                circlePath.reset();
                float radius = countdownContainer.getWidth() / 2 - countdownBorderSize;

                float x = countdownPos[ 0 ] + countdownBorderSize;
                float y = countdownPos[ 1 ] + countdownBorderSize;
                float w = countdownContainer.getWidth() * countdownContainer.getScaleX() - countdownBorderSize * 2;
                float h = countdownContainer.getHeight() * countdownContainer.getScaleX() - countdownBorderSize * 2;

                if ( w < 0 ) w = 0;
                if ( h < 0 ) h = 0;

                float scale = QUESTION_HEAD_SCALE;

                if ( animating == ANIMATING_IN_QUESTION )
                {
                    scale = 1 - scaleInAnimateInterpolator.getInterpolation( animPct ) * ( 1 - scale );
                    x *= animScalar;
                    y *= animScalar;
                    w = easeOutExpo( animLapsed, getWidth(), -getWidth() + w, animLength );
                    h = easeOutExpo( animLapsed, getHeight(), -getHeight() + h, animLength );
                }


                float borderRadius = animating == ANIMATING_IN_QUESTION ? (w / 2) * animScalar : w / 2;
                circlePath.addRoundRect( x, y, x + w, y + h, borderRadius, borderRadius, Path.Direction.CW );

                canvas.clipPath( circlePath );

                canvas.scale( scale, scale, x + w / 2, y + radius / 2 ); // h / 5

                videoView.draw( canvas );

            canvas.restore();

        }
        else
        {
            Log.e( "HD4HQ", "Trying to draw video effect without a video surface!z" );
        }

        postInvalidate();
    }
}
