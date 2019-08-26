package com.granpc.hdforhq.views;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.PathInterpolator;
import android.view.animation.TranslateAnimation;

public class WhistlerQuestionTextView extends AnimatedTextView
{
    private static final int START_COLOR = 0x00FFFFFF;
    private static final int END_COLOR   = 0xFFFFFFFF;
    private static final ArgbEvaluator colorInterpolator = new ArgbEvaluator();

    private static final PathInterpolator wordPositionInterpolator =
        new PathInterpolator( 0.f, 0.f, 0.f, 1.f );

    private static final PathInterpolator fadeAlphaInterpolator =
        new PathInterpolator( 0.f, 0.f, 0.f, 1.00f );

    public WhistlerQuestionTextView( Context context )
    {
        super( context );
    }

    @Override
    protected boolean drawPart( CharSequence text, int index, int count,
                                int part, int x, int y, int w, Paint paint, Canvas c )
    {
        long colorAnimTime = SystemClock.uptimeMillis() - 25 * part - animateStartMs;
        long colorAnimLength = 250;
        float colorAnimPct = colorAnimTime < 0 ? 0 : (float) colorAnimTime / (float) colorAnimLength;
        if ( colorAnimPct == 0 ) return false;
        if ( colorAnimPct > 1.f ) colorAnimPct = 1.f;

        long posAnimTime = SystemClock.uptimeMillis() - 25 * part - animateStartMs;
        long posAnimLength = 250;
        float posAnimPct = posAnimTime < 0 ? 0 : (float) posAnimTime / (float) posAnimLength;
        if ( posAnimPct == 0 ) return false;
        if ( posAnimPct > 1.f ) posAnimPct = 1.f;

        paint.setColor( (int) colorInterpolator.evaluate( colorAnimPct, START_COLOR, END_COLOR ) );

        float translatePct = 1.f - wordPositionInterpolator.getInterpolation( posAnimPct );
        float ty = translatePct * (float) -getLayout().getLineAscent( 0 ) * 0.4f;

        c.drawText( text, index, count, x, y + ty, paint );

        return colorAnimPct >= 1.f && posAnimPct >= 1.f;
    }

    public void fadeOut( long off )
    {
        AlphaAnimation fadeOut = new AlphaAnimation( 1.f, 0.f );
        fadeOut.setDuration( 500 );
        fadeOut.setInterpolator( fadeAlphaInterpolator );
        fadeOut.setRepeatCount( 0 );
        fadeOut.setStartOffset( off );

        fadeOut.setAnimationListener( new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart( Animation animation )
            {
            }

            @Override
            public void onAnimationEnd( Animation animation )
            {
                setAlpha( 0.f );
            }

            @Override
            public void onAnimationRepeat( Animation animation )
            {
            }
        } );

        startAnimation( fadeOut );
    }
}
