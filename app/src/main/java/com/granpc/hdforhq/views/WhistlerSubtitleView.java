package com.granpc.hdforhq.views;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatTextView;
import android.text.BoringLayout;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;

public class WhistlerSubtitleView extends AnimatedTextView
{
    private static final int START_COLOR = 0xFFFFD02D;
    private static final int END_COLOR   = 0xFFFFFFFF;
    private static final ArgbEvaluator colorInterpolator = new ArgbEvaluator();

    private static final PathInterpolator wordScaleInterpolator =
        new PathInterpolator( 0.35f, 0.f, 0.40f, 1.3f );

    private static final PathInterpolator wordPositionInterpolator =
        new PathInterpolator( 0.f, 0.f, 0.45f, 1.f );

    public WhistlerSubtitleView( Context context )
    {
        super( context );
    }

    @Override
    public void startAnimation()
    {
        super.startAnimation();
        // small delay while our view starts showing up
        animateStartMs = SystemClock.uptimeMillis() + 500;
    }

    @Override
    protected boolean drawPart( CharSequence text, int index, int count,
                                int part, int x, int y, int w, Paint paint, Canvas c )
    {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + getLineHeight();

        long posAnimTime = SystemClock.uptimeMillis() - 100 * part - animateStartMs;
        long posAnimLength = 250;
        float posAnimPct = posAnimTime < 0 ? 0 : (float) posAnimTime / (float) posAnimLength;
        if ( posAnimPct == 0 ) return false;
        if ( posAnimPct > 1.f ) posAnimPct = 1.f;

        long scaleAnimLength = 350;
        float scaleAnimPct = posAnimTime < 0 ? 0 : (float) posAnimTime / (float) scaleAnimLength;
        if ( scaleAnimPct > 1.f ) scaleAnimPct = 1.f;

        long colorAnimTime = SystemClock.uptimeMillis() - 150 * part - animateStartMs;
        long colorAnimLength = 300;
        float colorAnimPct = colorAnimTime < 0 ? 0 : (float) colorAnimTime / (float) colorAnimLength;
        if ( colorAnimPct > 1.f ) colorAnimPct = 1.f;

        paint.setColor( (int) colorInterpolator.evaluate( colorAnimPct, START_COLOR, END_COLOR ) );

        float translatePct = 1.f - wordPositionInterpolator.getInterpolation( posAnimPct );
        float tx = translatePct * (centerX - x - w / 2) * 0.9f;
        float ty = translatePct * (centerY - y) * 0.8f;

        float scalePct = wordScaleInterpolator.getInterpolation( scaleAnimPct );

        c.save();
        c.translate( x, y );
        c.translate( tx, ty );
        c.scale( scalePct, scalePct, w / 2, 0 );
        c.drawText( text, index, count, 0, 0, paint );
        c.restore();

        return scaleAnimPct >= 1.f && colorAnimPct >= 1.f && posAnimPct >= 1.f;
    }
}
