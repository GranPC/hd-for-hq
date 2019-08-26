package com.granpc.hdforhq.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.transition.Transition;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.PathInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.granpc.hdforhq.interfaces.WhistlerAnswerListener;

import java.util.Arrays;

public class WhistlerAnswerButtonView extends RelativeLayout implements View.OnClickListener
{
    private static final PathInterpolator fadeAlphaInterpolator =
        new PathInterpolator( 0.f, 0.f, 0.f, 1.00f );
    private int fadeInYOffset;

    private CharSequence text;
    private String offairAnswerId;
    private Typeface typeface;
    private AppCompatTextView textView;

    private ShapeDrawable defaultBackground;
    private ShapeDrawable incorrectBackground;
    private ShapeDrawable correctBackground;

    private RippleDrawable rippleBackground;

    private WhistlerAnswerListener answerListener;

    private ShapeDrawable createShape( int color )
    {
        float[] borderRadius = new float[8];
        float radius = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics() );
        Arrays.fill( borderRadius, radius );
        RoundRectShape r = new RoundRectShape( borderRadius, null, null );
        ShapeDrawable shape = new ShapeDrawable( r );
        shape.getPaint().setStrokeWidth( TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics() ) );
        shape.getPaint().setColor( color );
        return shape;
    }

    public WhistlerAnswerButtonView( Context context )
    {
        super( context );
        setOnClickListener( this );

        defaultBackground = createShape( 0xFF6C6FD5 );
        defaultBackground.getPaint().setStyle( Paint.Style.STROKE );
        correctBackground = createShape( 0xff4ac38c );
        incorrectBackground = createShape( 0xffe52465 );

        rippleBackground = new RippleDrawable( ColorStateList.valueOf( 0xFFEEEEEE ), defaultBackground, defaultBackground );
        setBackground( rippleBackground );

        setClipToOutline( true );

        textView = new AppCompatTextView( getContext() );
        textView.setGravity( Gravity.CENTER );
        textView.setTextColor( Color.WHITE );
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            textView, 11, 20, 2, TypedValue.COMPLEX_UNIT_SP );
        addView( textView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT );

        fadeInYOffset = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics() );
    }

    public CharSequence getText()
    {
        return text;
    }

    public void setText( CharSequence text )
    {
        this.text = text;
        textView.setText( text );
    }

    public String getOffairAnswerId()
    {
        return offairAnswerId;
    }

    public void setOffairAnswerId( String offairAnswerId )
    {
        this.offairAnswerId = offairAnswerId;
    }

    public Typeface getTypeface()
    {
        return typeface;
    }

    public void setTypeface( Typeface typeface )
    {
        this.typeface = typeface;
        textView.setTypeface( typeface );
    }

    public void fadeIn( long off )
    {
        AnimationSet set = new AnimationSet( false );

        AlphaAnimation fadeIn = new AlphaAnimation( 0.f, 1.0f );
        fadeIn.setDuration( 800 );
        fadeIn.setInterpolator( fadeAlphaInterpolator );
        fadeIn.setRepeatCount( 0 );
        set.addAnimation( fadeIn );

        TranslateAnimation translate = new TranslateAnimation( 0, 0, fadeInYOffset, 0 );
        translate.setDuration( 800 );
        translate.setInterpolator( fadeAlphaInterpolator );
        translate.setRepeatCount( 0 );
        set.addAnimation( translate );

        set.setRepeatCount( 0 );
        set.setStartOffset( off );

        startAnimation( set );
    }

    private void transitionBackground( ShapeDrawable from, ShapeDrawable to )
    {
        ShapeDrawable[] drawables = { from, to };
        TransitionDrawable bg = new TransitionDrawable( drawables );
        bg.startTransition( 150 );
        setBackground( bg );
    }

    public void transitionCorrect()
    {
        Log.d( "HD4HQ", "hell yeah ur right" );
        transitionBackground( defaultBackground, correctBackground );
    }

    public void transitionIncorrect()
    {
        transitionBackground( defaultBackground, incorrectBackground );
    }

    @Override
    public void onClick( View v )
    {
        if ( !isEnabled() ) return;

        defaultBackground.getPaint().setStyle( Paint.Style.FILL_AND_STROKE );

        if ( answerListener != null )
        {
            answerListener.onAnswerTapped( offairAnswerId );
        }
    }

    public void reset()
    {
        defaultBackground.getPaint().setStyle( Paint.Style.STROKE );
        setBackground( rippleBackground );
        setEnabled( true );
    }

    public WhistlerAnswerListener getAnswerListener()
    {
        return answerListener;
    }

    public void setAnswerListener( WhistlerAnswerListener answerListener )
    {
        this.answerListener = answerListener;
    }
}
