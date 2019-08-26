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
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.granpc.hdforhq.interfaces.WhistlerAnswerListener;
import com.granpc.hdforhq.models.ApiOutgoingWhistlerAnswer;
import com.granpc.hdforhq.models.ApiWhistlerAnswerResult;
import com.granpc.hdforhq.models.ApiWhistlerGame;
import com.granpc.hdforhq.models.ApiWhistlerQuestion;
import com.granpc.hdforhq.models.ApiWhistlerQuestionSummary;
import com.granpc.hdforhq.models.ApiWhistlerRound;
import com.granpc.hdforhq.views.WhistlerAnswerButtonView;
import com.granpc.hdforhq.views.WhistlerQuestionTextView;
import com.granpc.hdforhq.views.WhistlerSubtitleView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WhistlerActivity extends BaseHQActivity implements WhistlerAnswerListener
{
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MediaPlayer bgMusic;
    private List<View> splashViews = new ArrayList<View>();

    private ConstraintLayout gameLayout;
    private WhistlerQuestionTextView questionView;
    private List<WhistlerAnswerButtonView> answerViews = new ArrayList<WhistlerAnswerButtonView>();

    private ApiWhistlerGame currentGame;

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
        int scrH = Resources.getSystem().getDisplayMetrics().heightPixels;

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

        WhistlerSubtitleView dailySubtitle = new WhistlerSubtitleView( thiz );
        dailySubtitle.setText( "Answer trivia questions to win rewards!" );
        dailySubtitle.setTypeface( hqFont );
        dailySubtitle.setTextColor( Color.WHITE );
        dailySubtitle.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18.f );
        dailySubtitle.setLineSpacing( 0.0f, 1.2f );
        dailySubtitle.setGravity( Gravity.CENTER_HORIZONTAL );
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

        dailySubtitle.startAnimation();
        startButton.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                WhistlerActivity.this.startGame();
            }
        } );

        splashViews.add( dailyHeader );
        splashViews.add( dailySubtitle );
        splashViews.add( startButton );

        ConstraintLayout gameLayout = new ConstraintLayout( thiz );
        gameLayout.setId( 1 );

        WhistlerQuestionTextView questionLabel = new WhistlerQuestionTextView( thiz );
        questionLabel.setId( 10 );
        questionLabel.setText( "What was the username of the early infamous YouTube “vlog” account that was revealed to be staged?" );
        questionLabel.setTypeface( hqFont );
        questionLabel.setTextColor( Color.WHITE );
        questionLabel.setLineSpacing( 0.0f, 1.25f );
        questionLabel.setGravity( Gravity.CENTER );
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
            questionLabel, 14, 26, 2, TypedValue.COMPLEX_UNIT_SP );
        gameLayout.addView( questionLabel, ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT );

        questionView = questionLabel;

        WhistlerAnswerButtonView answerA = new WhistlerAnswerButtonView( thiz );
        answerA.setId( 11 );
        answerA.setText( "DailyGrace" );
        answerA.setTypeface( hqFont );
        answerViews.add( answerA );

        WhistlerAnswerButtonView answerB = new WhistlerAnswerButtonView( thiz );
        answerB.setId( 12 );
        answerB.setText( "Jenny4U" );
        answerB.setTypeface( hqFont );
        answerViews.add( answerB );

        WhistlerAnswerButtonView answerC = new WhistlerAnswerButtonView( thiz );
        answerC.setId( 13 );
        answerC.setText( "Lonelygirl15" );
        answerC.setTypeface( hqFont );
        answerViews.add( answerC );

        answerA.setAnswerListener( this );
        answerB.setAnswerListener( this );
        answerC.setAnswerListener( this );

        final int answerHeight = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 64, thiz.getResources().getDisplayMetrics() );
        final int answerMargin = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 8, thiz.getResources().getDisplayMetrics() );
        gameLayout.addView( answerA, ConstraintLayout.LayoutParams.WRAP_CONTENT );
        gameLayout.addView( answerB, ConstraintLayout.LayoutParams.WRAP_CONTENT );
        gameLayout.addView( answerC, ConstraintLayout.LayoutParams.WRAP_CONTENT );

        ConstraintSet gameConstraints = new ConstraintSet();
        for ( int i = 11; i <= 13; i++ )
        {
            gameConstraints.constrainWidth( i, (int) ( scrW / 1.35f ) );
            gameConstraints.constrainHeight( i, answerHeight );
            gameConstraints.centerHorizontally( i, 1 );
        }
        gameConstraints.connect( 10, ConstraintSet.TOP, 1, ConstraintSet.TOP );
        gameConstraints.connect( 11, ConstraintSet.TOP, 10, ConstraintSet.BOTTOM, answerMargin );
        gameConstraints.connect( 12, ConstraintSet.TOP, 11, ConstraintSet.BOTTOM, answerMargin );
        gameConstraints.connect( 13, ConstraintSet.TOP, 12, ConstraintSet.BOTTOM, answerMargin );

        gameConstraints.centerHorizontally( 10, 1 );
        gameConstraints.constrainWidth( 10, (int) (scrW / 1.35f) );
        gameConstraints.constrainHeight( 10, (int) (scrH / 1.8f) );
        gameConstraints.applyTo( gameLayout );

        final RelativeLayout.LayoutParams fill = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );

        layout.addView( gameLayout, fill );
        gameLayout.setVisibility( View.GONE );

        WhistlerActivity.this.gameLayout = gameLayout;

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
    }

    private void displayQuestion( ApiWhistlerQuestion question )
    {
        gameLayout.setVisibility( View.VISIBLE );
        questionView.setText( question.getQuestion() );
        questionView.startAnimation();
        questionView.setAlpha( 1.f );

        int i = 0;
        for ( WhistlerAnswerButtonView v : answerViews )
        {
            v.setText( question.getAnswers().get( i ).getText() );
            v.setOffairAnswerId( question.getAnswers().get( i ).getOffairAnswerId() );
            v.reset();
            i++;
            if ( i > 2 ) break; // hmm?
        }
        transitionAnswers();
    }

    private void processQuestionSummary( ApiWhistlerQuestionSummary summary )
    {
        String sound = "incorrect-general";
        String correctAnswer = null;
        String incorrectAnswer = summary.getYourOffairAnswerId();

        if ( summary.getYouGotItRight() )
        {
            sound = "correct-general";
            incorrectAnswer = null;
        }

        for ( ApiWhistlerAnswerResult answer : summary.getAnswerCounts() )
        {
            if ( answer.getCorrect() )
            {
                correctAnswer = answer.getOffairAnswerId();
            }
        }
        playSound( sound );

        for ( WhistlerAnswerButtonView v : answerViews )
        {
            if ( v.getOffairAnswerId().equals( correctAnswer ) )
            {
                v.transitionCorrect();
            }
            if ( incorrectAnswer != null && v.getOffairAnswerId().equals( incorrectAnswer ) )
            {
                v.transitionIncorrect();
            }
        }

        // TODO: show points, etc
        Handler pointsDelay = new Handler();
        pointsDelay.postDelayed( new Runnable()
        {
            @Override
            public void run()
            {
                resetGameView();
            }
        }, 3000 );

        if ( summary.getGameSummary() == null )
        {
            Handler blankScreenDelay = new Handler();
            blankScreenDelay.postDelayed( new Runnable()
            {
                @Override
                public void run()
                {
                    fetchNextRound();
                }
            }, 4500 );
        }
        else
        {
            Log.d( "HD4HQ", "Whistler: game over!" );
        }
    }

    @Override
    public void onAnswerTapped( String offairAnswerId )
    {
        Log.d( "HD4HQ", "answer tapped!" );
        for ( WhistlerAnswerButtonView v : answerViews )
        {
            v.setEnabled( false );
        }

        Flowable<ApiWhistlerQuestionSummary> summary = getAuthedApi().whistlerSubmitAnswer( currentGame.getGameUuid(), new ApiOutgoingWhistlerAnswer( offairAnswerId ) );
        summary.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() ).subscribe( new Consumer<ApiWhistlerQuestionSummary>()
        {
            @Override
            public void accept( ApiWhistlerQuestionSummary apiWhistlerSummary ) throws Exception
            {
                processQuestionSummary( apiWhistlerSummary );
            }
        } );
    }

    private void resetGameView()
    {
        questionView.fadeOut( 0 );

        long off = 150;
        for ( WhistlerAnswerButtonView v : answerViews )
        {
            v.fadeOut( off );
            off += 150;
        }
    }

    private void transitionAnswers()
    {
        long off = 300;
        for ( WhistlerAnswerButtonView v : answerViews )
        {
            v.fadeIn( off );
            off += 80;
        }
    }

    private void fetchNextRound()
    {
        Flowable<ApiWhistlerRound> round = getAuthedApi().whistlerNextRound( currentGame.getGameUuid() );
        round.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() ).subscribe( new Consumer<ApiWhistlerRound>()
        {
            @Override
            public void accept( ApiWhistlerRound apiWhistlerRound ) throws Exception
            {
                displayQuestion( apiWhistlerRound.getQuestion() );
            }
        } );
    }

    private void startGame()
    {
        final AlphaAnimation fadeOut = new AlphaAnimation( 1.0f, 0.0f );
        fadeOut.setDuration( 200 );
        fadeOut.setRepeatCount( 0 );
        fadeOut.setAnimationListener( new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart( Animation animation )
            {
            }

            @Override
            public void onAnimationEnd( Animation animation )
            {
                for ( View v : splashViews )
                {
                    v.setVisibility( View.GONE );
                }
            }

            @Override
            public void onAnimationRepeat( Animation animation )
            {
            }
        } );

        for ( View v : splashViews )
        {
            v.startAnimation( fadeOut );
        }

        Log.d( "HD4HQ", "whistler starting game" );
        Flowable<ApiWhistlerGame> game = getAuthedApi().whistlerStartGame();
        game.subscribeOn( Schedulers.io() ).observeOn( AndroidSchedulers.mainThread() ).subscribe( new Consumer<ApiWhistlerGame>()
        {
            @Override
            public void accept( ApiWhistlerGame apiWhistlerGame ) throws Exception
            {
                Log.d( "HD4HQ", "whistler game id: " + apiWhistlerGame.getGameUuid() );
                currentGame = apiWhistlerGame;
                fetchNextRound();
            }
        } );
    }
}
