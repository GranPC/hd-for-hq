package com.granpc.hdforhq.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

public class VideoEffectView extends View
{
    public TextureView videoView;
    public FrameLayout countdownContainer;

    public static final int TYPE_TRIVIA = 0;
    public static final int ANIMATING_IN = 0;

    private static final float TRIVIA_HEAD_X_OFF    = 2.82f; // -0.42f;
    private static final float TRIVIA_HEAD_Y_OFF    = 0.65f; //-0.1f;
    private static final float TRIVIA_HEAD_SCALE    = 0.15f;

    private static final int TRIVIA_ANIM_IN_MILLIS  = 260;

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

    public void animateIn()
    {
        animating = ANIMATING_IN;
        animateStartMs = SystemClock.uptimeMillis();
        animateEndMs = animateStartMs + TRIVIA_ANIM_IN_MILLIS;
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
        long animLength = animateEndMs - animateStartMs;
        long animLapsed = now - animateStartMs;
        float animScalar = easeOutExpo( animLapsed, 0, 1, animLength );

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
                float w = countdownContainer.getWidth() - countdownBorderSize * 2;
                float h = countdownContainer.getHeight() - countdownBorderSize * 2;

                float translateX = x - radius / 2; // videoView.getWidth() * TRIVIA_HEAD_X_OFF;
                float translateY = y; // videoView.getHeight() * TRIVIA_HEAD_Y_OFF;

                float scale = easeOutExpo( animLapsed, 1, -1 + TRIVIA_HEAD_SCALE, animLength );

                translateX = easeOutExpo( animLapsed, 0, translateX, animLength );
                translateY = easeOutExpo( animLapsed, 0, translateY, animLength );

                x *= animScalar;
                y *= animScalar;
                w = easeOutExpo( animLapsed, getWidth(), -getWidth() + w, animLength );
                h = easeOutExpo( animLapsed, getHeight(), -getHeight() + h, animLength );

                float borderRadius = (w / 2) * animScalar;
                circlePath.addRoundRect( x, y, x+w, y+h, borderRadius, borderRadius, Path.Direction.CW );

                canvas.clipPath( circlePath );

                canvas.translate( translateX, translateY );
                canvas.scale( scale, scale );


                videoView.draw( canvas );

                /*Paint debugTest = new Paint();
                debugTest.setColor( Color.RED );
                debugTest.setStyle( Paint.Style.FILL );

                canvas.drawRect( 0, 0, 2000, 2000, debugTest );*/

            canvas.restore();

        }
        else
        {
            Log.e( "HD4HQ", "Trying to draw video effect without a video surface!z" );
        }

        postInvalidate();
    }
}
