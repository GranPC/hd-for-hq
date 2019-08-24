package com.granpc.hdforhq.activities;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.granpc.hdforhq.models.ApiWhistlerAnswerResult;
import com.granpc.hdforhq.models.ApiWhistlerGame;

import java.io.File;
import java.util.Arrays;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WhistlerActivity extends BaseHQActivity
{
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MediaPlayer bgMusic;

    public WhistlerActivity()
    {
    }

    private MediaPlayer playSound( String name )
    {
        File f = new File( Environment.getExternalStorageDirectory().getPath() + "/HD4HQ/res/sfx/" + name + ".mp3" );

        if ( f.exists() )
        {
            MediaPlayer player = MediaPlayer.create( thiz, Uri.fromFile( f ) );
            if ( player != null )
            {
                player.start();
                return player;
            }
        }

        return null;
    }

    // I really ought to figure out a way to import XML layouts...
    private RelativeLayout generateLayout()
    {
        int scrW = Resources.getSystem().getDisplayMetrics().widthPixels;
        RelativeLayout layout = new RelativeLayout( thiz );
        layout.setFitsSystemWindows( false );
        layout.setBackgroundColor( 0xff36399a );

        RelativeLayout.LayoutParams center = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
        center.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );

        LinearLayout hero = new LinearLayout( thiz );
        int padding = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 60, thiz.getResources().getDisplayMetrics() );
        hero.setPadding( padding, 0, padding, 0 );
        hero.setOrientation( LinearLayout.VERTICAL );
        hero.setHorizontalGravity( Gravity.CENTER );

        TextView dailyHeader = new TextView( thiz );
        dailyHeader.setText( "Daily Challenge" );
        dailyHeader.setTypeface( hqFont, Typeface.BOLD );
        dailyHeader.setTextColor( Color.WHITE );
        dailyHeader.setTextSize( TypedValue.COMPLEX_UNIT_SP, 55.f );
        dailyHeader.setGravity( Gravity.CENTER );
        hero.addView( dailyHeader );

        TextView dailySubtitle = new TextView( thiz );
        dailySubtitle.setText( "Answer trivia questions to win rewards!" );
        dailySubtitle.setTypeface( hqFont );
        dailySubtitle.setTextColor( Color.WHITE );
        dailySubtitle.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18.f );
        dailySubtitle.setLineSpacing( 0.0f, 1.2f );
        dailySubtitle.setGravity( Gravity.CENTER );
        int spacing = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 25, thiz.getResources().getDisplayMetrics() );
        dailySubtitle.setPadding( 0, spacing, 0, 0 );
        hero.addView( dailySubtitle, (int) (scrW / 1.8f), LinearLayout.LayoutParams.WRAP_CONTENT );

        layout.addView( hero, center );

        float[] borderRadius = new float[8];
        float radius = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 30, thiz.getResources().getDisplayMetrics() );
        Arrays.fill( borderRadius, radius );
        RoundRectShape r = new RoundRectShape( borderRadius, null, null );
        ShapeDrawable buttonShape = new ShapeDrawable( r );
        buttonShape.getPaint().setColor( 0xFFFFD02D );
        RippleDrawable rippleDrawable = new RippleDrawable( ColorStateList.valueOf( 0xFFC4A020 ), buttonShape, buttonShape );

        final Button startButton = new Button( thiz );
        startButton.setText( "Start" );
        startButton.setTypeface( hqFont, Typeface.BOLD );
        startButton.setAllCaps( false );
        startButton.setTextColor( 0xff36399a );
        startButton.setBackground( rippleDrawable );
        final int buttonMargin = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 20, thiz.getResources().getDisplayMetrics() );
        final RelativeLayout.LayoutParams buttonLayout = new RelativeLayout.LayoutParams(
            (int) (scrW / 2.33f), RelativeLayout.LayoutParams.WRAP_CONTENT );
        buttonLayout.addRule( RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE );
        buttonLayout.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE );

        ViewCompat.setOnApplyWindowInsetsListener( thiz.getWindow().getDecorView(), new OnApplyWindowInsetsListener()
        {
            @Override
            public WindowInsetsCompat onApplyWindowInsets( View v, WindowInsetsCompat insets )
            {
                buttonLayout.setMargins( 0, 0, 0, insets.getStableInsetBottom() + buttonMargin );
                startButton.setLayoutParams( buttonLayout );
                return insets.consumeSystemWindowInsets();
            }
        } );

        layout.addView( startButton, buttonLayout );
        return layout;
    }

    @Override
    public void onCreate( Bundle bundle )
    {
        super.onCreate( bundle );
        Log.d( "HD4HQ", "Whistler activity is alive!" );
        doCreate( bundle );
    }


    @Override
    public void onPause()
    {
        super.onPause();
        if ( bgMusic != null )
            bgMusic.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if ( bgMusic != null )
            bgMusic.start();
    }

    public void doCreate( Bundle bundle )
    {
        thiz.getWindow().setFlags( WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS );
        thiz.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
        thiz.getWindow().setStatusBarColor( Color.TRANSPARENT );
        thiz.setContentView( generateLayout() );

        playSound( "whistlerSplash1.0" );
        bgMusic = playSound( "whistlerBed3.1" );
        if ( bgMusic != null )
            bgMusic.setLooping( true );

        Log.d( "HD4HQ", "whistler starting game" );
        Flowable<ApiWhistlerGame> game = getAuthedApi().whistlerStartGame();
        game.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() ).subscribe();
    }
}
