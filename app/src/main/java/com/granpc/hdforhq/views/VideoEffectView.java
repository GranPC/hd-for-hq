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
    public static final int TYPE_WORDS  = 1;

    public static final int ANIMATING_IN_QUESTION    = 0;
    public static final int ANIMATING_IN_WHEEL       = 1;

    private static final float QUESTION_HEAD_SCALE = 0.15f;
    private static final float WHEEL_HEAD_SCALE = 0.32f;

    private static final int TRIVIA_ANIM_IN_MILLIS        = 300;
    private static final int WORDS_ANIM_WHEEL_IN_MILLIS   = 300;

    private static final PathInterpolator scaleInAnimateInterpolator =
        new PathInterpolator( 0.05f, 0.70f, 0.2f, 1.07f );

    private static final PathInterpolator wheelHeadScaleInterpolator =
        new PathInterpolator( 0.05f, 0.66f, 0.25f, 1.75f );

    private static final PathInterpolator wheelHeadYInterpolator =
        new PathInterpolator( 0.00f, 0.50f, 0.30f, 0.60f );

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

    private void startAnimation( int duration )
    {
        animateStartMs = SystemClock.uptimeMillis();
        animateEndMs = animateStartMs + duration;
        postInvalidate();
    }

    public void onQuestionShown()
    {
        animating = ANIMATING_IN_QUESTION;
        startAnimation( TRIVIA_ANIM_IN_MILLIS );
    }

    public void onWheelShown()
    {
        animating = ANIMATING_IN_WHEEL;
        startAnimation( WORDS_ANIM_WHEEL_IN_MILLIS );
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

                float borderRadius = (animating == ANIMATING_IN_QUESTION || animating == ANIMATING_IN_WHEEL) ? (w / 2) * animScalar : w / 2;

                if ( animating == ANIMATING_IN_QUESTION )
                {
                    scale = 1 - scaleInAnimateInterpolator.getInterpolation( animPct ) * ( 1 - scale );
                    x *= animScalar;
                    y *= animScalar;
                    w = easeOutExpo( animLapsed, getWidth(), -getWidth() + w, animLength );
                    h = easeOutExpo( animLapsed, getHeight(), -getHeight() + h, animLength );

                    px = x + w / 2;
                    py = y + radius / 2;
                }
                else if ( animating == ANIMATING_IN_WHEEL )
                {
                    x *= wheelHeadScaleInterpolator.getInterpolation( animPct );
                    y *= wheelHeadYInterpolator.getInterpolation( animPct ) * wheelHeadScaleInterpolator.getInterpolation( animPct );
                    w = getWidth() + (wheelHeadScaleInterpolator.getInterpolation( animPct )) * (-getWidth() + w);
                    h = w;

                    scale = w / getWidth();

                    borderRadius = w;

                    px = x;
                    py = y;
                }

                setPivotX( x + w / 2.0f );
                setPivotY( y + h / 2.0f );

                circlePath.addRoundRect( x, y, x + w, y + h, borderRadius, borderRadius, Path.Direction.CW );

                canvas.clipPath( circlePath );

                if ( animating == ANIMATING_IN_WHEEL )
                {
                    canvas.translate( px, py );
                    canvas.scale( scale, scale );
                }
                else
                {
                    canvas.scale( scale, scale, px, py );
                }

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
