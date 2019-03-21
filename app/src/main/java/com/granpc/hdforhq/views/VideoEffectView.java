package com.granpc.hdforhq.views;

import android.content.Context;
import android.graphics.Canvas;
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
    public static final int TYPE_WORDS  = 1;

    public static final int ANIMATING_IN_QUESTION    = 0;
    public static final int ANIMATING_OUT_PILL       = 1;
    public static final int ANIMATING_IN_WHEEL       = 2;

    private static final float QUESTION_HEAD_SCALE = 0.15f;
    private static final float WHEEL_HEAD_SCALE = 0.32f;

    private static final int TRIVIA_ANIM_IN_MILLIS        = 300;
    private static final int WORDS_ANIM_WHEEL_IN_MILLIS   = 300;
    private static final int TRIVIA_ANIM_PILL_OUT_MILLIS  = 700;

    private static final PathInterpolator scaleInAnimateInterpolator =
        new PathInterpolator( 0.05f, 0.70f, 0.2f, 1.07f );

    public TextureView videoView;
    public FrameLayout countdownContainer;
    public View wheelContainer;

    private int anchorPos[];
    private Path circlePath;
    private float countdownBorderSize;

    private int animating = -1;
    private long animateStartMs;
    private long animateEndMs;

    private int type = TYPE_TRIVIA;

    public VideoEffectView( Context context, int _type )
    {
        super( context );
        setLayerType( LAYER_TYPE_HARDWARE, null );

        anchorPos = new int[2];
        circlePath = new Path();
        videoView = null;

        // According to progress_circle.xml, countdown thickness is 5.1sp
        countdownBorderSize = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_SP, 5.1f, getResources().getDisplayMetrics() );
        type = _type;
    }

    public VideoEffectView( Context context )
    {
        this( context, TYPE_TRIVIA );
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

    public void onWheelShown()
    {
        animating = ANIMATING_IN_WHEEL;
        animateStartMs = SystemClock.uptimeMillis();
        animateEndMs = animateStartMs + WORDS_ANIM_WHEEL_IN_MILLIS;
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

            View anchor = countdownContainer;
            if ( type == TYPE_WORDS && animating == ANIMATING_IN_WHEEL )
            {
                anchor = wheelContainer;
            }

            canvas.save();

                anchor.getLocationOnScreen( anchorPos );
                circlePath.reset();
                float radius = 0;

                float x = 0;
                float y = 0;
                float w = 0;
                float h = 0;

                float px = 0;
                float py = 0;

                float scale = QUESTION_HEAD_SCALE;

                if ( animating == ANIMATING_IN_QUESTION )
                {
                    radius = countdownContainer.getWidth() / 2 - countdownBorderSize;

                    x = anchorPos[0] + countdownBorderSize;
                    y = anchorPos[1] + countdownBorderSize;
                    w = countdownContainer.getWidth() * countdownContainer.getScaleX() - countdownBorderSize * 2;
                    h = countdownContainer.getHeight() * countdownContainer.getScaleX() - countdownBorderSize * 2;
                }
                else if ( animating == ANIMATING_IN_WHEEL )
                {
                    scale = WHEEL_HEAD_SCALE;

                    radius = wheelContainer.getWidth() / 3;

                    x = anchorPos[0] + wheelContainer.getWidth() / 2 - radius / 2;
                    y = anchorPos[1] + wheelContainer.getHeight() / 2 - radius / 2;
                    w = radius;
                    h = radius;
                }

                if ( w < 0 ) w = 0;
                if ( h < 0 ) h = 0;

                if ( animating == ANIMATING_IN_QUESTION || animating == ANIMATING_IN_WHEEL )
                {
                    scale = 1 - scaleInAnimateInterpolator.getInterpolation( animPct ) * ( 1 - scale );
                    x *= animScalar;
                    y *= animScalar;
                    w = easeOutExpo( animLapsed, getWidth(), -getWidth() + w, animLength );
                    h = easeOutExpo( animLapsed, getHeight(), -getHeight() + h, animLength );
                }

                setPivotX( x + w / 2.0f );
                setPivotY( y + h / 2.0f );

                if ( animating == ANIMATING_IN_QUESTION )
                {
                    px = x + w / 2;
                    py = y + radius / 2;
                }
                else if ( animating == ANIMATING_IN_WHEEL )
                {
                    px = x + w / 2;
                    py = y + radius * 0.95f;
                }

                float borderRadius = (animating == ANIMATING_IN_QUESTION || animating == ANIMATING_IN_WHEEL) ? (w / 2) * animScalar : w / 2;
                circlePath.addRoundRect( x, y, x + w, y + h, borderRadius, borderRadius, Path.Direction.CW );

                canvas.clipPath( circlePath );

                canvas.scale( scale, scale, px, py ); // h / 5

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
