package com.granpc.hdforhq.views;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.text.TextPaint;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.PathInterpolator;

public class AnimatedTextView extends AppCompatTextView
{
    private boolean animating;
    protected long animateStartMs;

    public AnimatedTextView( Context context )
    {
        super( context );
    }

    public void startAnimation()
    {
        animating = true;
        animateStartMs = SystemClock.uptimeMillis();
        postInvalidate();
    }

    // Returns true if animation is finished
    protected boolean drawPart( CharSequence text, int index, int count,
                             int part, int x, int y, int w, Paint paint, Canvas c )
    {
        c.drawText( text, index, count, x, y, paint );
        return true;
    }

    @Override
    protected void onDraw( Canvas c )
    {
        TextPaint paint = getPaint();
        paint.setColor( getCurrentTextColor() );
        paint.drawableState = getDrawableState();

        onPreDraw();

        Layout layout = getLayout();

        int x;
        int y;

        int wordIdx = 0;

        CharSequence text = getText();

        boolean done = false;

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

            if ( getGravity() == Gravity.CENTER )
            {
                y += layout.getLineTop( layout.getLineCount() ) / 2;
            }

            for ( int pos = layout.getLineStart( i ); pos < layout.getLineStart( i + 1 ); pos++ )
            {
                int wordEndPos = pos + 1;

                // Calculate width and last character for this word
                int wordWidth = 0;
                for ( int wpos = pos; wpos < layout.getLineStart( i + 1 ); wpos++ )
                {
                    int charWidth = (int) paint.measureText( text, wpos, wpos + 1 );
                    wordWidth += charWidth;
                    wordEndPos = wpos + 1;

                    if ( text.charAt( wpos ) == ' ' )
                        break;
                }

                done = drawPart( text, pos, wordEndPos, wordIdx, x, y, wordWidth, paint, c );
                x = x + (int) paint.measureText( text, pos, wordEndPos );
                pos = wordEndPos - 1;

                if ( pos == text.length() || text.charAt( pos ) == ' ' )
                {
                    wordIdx++;
                }
            }
        }

        if ( done )
            animating = false;

        if ( animating )
            postInvalidate();
    }
}
