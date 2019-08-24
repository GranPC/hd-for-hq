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

public class WhistlerSubtitleView extends AppCompatTextView
{
    private static final int START_COLOR = 0xFFFFD02D;
    private static final int END_COLOR   = 0xFFFFFFFF;
    private static final ArgbEvaluator colorInterpolator = new ArgbEvaluator();
    private boolean animating;
    private long animateStartMs;

    private static final PathInterpolator wordScaleInterpolator =
        new PathInterpolator( 0.35f, 0.f, 0.40f, 1.3f );

    private static final PathInterpolator wordPositionInterpolator =
        new PathInterpolator( 0.f, 0.f, 0.45f, 1.f );

    public WhistlerSubtitleView( Context context )
    {
        super( context );
    }

    public void startAnimation()
    {
        animating = true;
        // small delay while our view starts showing up
        animateStartMs = SystemClock.uptimeMillis() + 500;
        postInvalidate();
    }

    protected void onDraw( Canvas c )
    {
        TextPaint paint = getPaint();
        paint.setColor( getCurrentTextColor() );
        paint.drawableState = getDrawableState();

        Layout layout = getLayout();

        int x;
        int y;

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 + getLineHeight();

        int wordIdx = 0;

        CharSequence text = getText();
        float lastAnimPct = 0.0f;

        for ( int i = 0; i < layout.getLineCount(); i++ )
        {
            int textW = (int) paint.measureText( text, layout.getLineStart( i ), layout.getLineStart( i + 1 ) );

            // If the last character of this line is a space, ignore it
            if ( text.charAt( layout.getLineStart( i + 1 ) - 1 ) == ' ' )
            {
                textW -= paint.measureText( " " );
            }

            x = (getWidth() - textW) / 2;
            y = getPaddingTop() + layout.getLineTop( i + 1 ) - layout.getLineDescent( i );

            /* paint.setColor( 0xFFFF00FF );
            c.drawRect( x, y, x + textW, y + 5, paint ); */

            int wordX = x;
            int wordY = y;
            int wordWidth = -1;

            for ( int pos = layout.getLineStart( i ); pos < layout.getLineStart( i + 1 ); pos++ )
            {
                long posAnimTime = SystemClock.uptimeMillis() - 100 * wordIdx - animateStartMs;
                long posAnimLength = 250;
                float posAnimPct = posAnimTime < 0 ? 0 : (float) posAnimTime / (float) posAnimLength;
                if ( posAnimPct == 0 ) break; // this word is invisible - so all the other words are invisible too
                if ( posAnimPct > 1.f ) posAnimPct = 1.f;

                long scaleAnimLength = 350;
                float scaleAnimPct = posAnimTime < 0 ? 0 : (float) posAnimTime / (float) scaleAnimLength;
                if ( scaleAnimPct > 1.f ) scaleAnimPct = 1.f;

                long colorAnimTime = SystemClock.uptimeMillis() - 150 * wordIdx - animateStartMs;
                long colorAnimLength = 300;
                float colorAnimPct = colorAnimTime < 0 ? 0 : (float) colorAnimTime / (float) colorAnimLength;
                if ( colorAnimPct > 1.f ) colorAnimPct = 1.f;
                lastAnimPct = colorAnimPct;

                int wordEndPos = pos + 1;

                // Calculate width and last character for this word
                wordWidth = 0;
                for ( int wpos = pos; wpos < layout.getLineStart( i + 1 ); wpos++ )
                {
                    int charWidth = (int) paint.measureText( text, wpos, wpos + 1 );
                    wordWidth += charWidth;
                    wordEndPos = wpos + 1;

                    if ( text.charAt( wpos ) == ' ' )
                        break;
                }

                paint.setColor( (int) colorInterpolator.evaluate( colorAnimPct, START_COLOR, END_COLOR ) );

                float translatePct = 1.f - wordPositionInterpolator.getInterpolation( posAnimPct );
                float tx = translatePct * (centerX - wordX - wordWidth / 2) * 0.9f;
                float ty = translatePct * (centerY - wordY) * 0.8f;

                float scalePct = wordScaleInterpolator.getInterpolation( scaleAnimPct );

                c.save();
                c.translate( x, y );
                c.translate( tx, ty );
                c.scale( scalePct, scalePct, wordWidth / 2, 0 );
                c.drawText( text, pos, wordEndPos, 0, 0, paint );
                c.restore();

                x = x + (int) paint.measureText( text, pos, wordEndPos );
                pos = wordEndPos - 1;

                if ( pos == text.length() || text.charAt( pos ) == ' ' )
                {
                    wordIdx++;
                    wordX = x;
                }
            }
        }

        if ( lastAnimPct >= 1.f )
            animating = false;

        if ( animating )
            postInvalidate();
    }
}
