package com.granpc.hdforhq.tweaks;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.granpc.hdforhq.views.VideoEffectView;

import java.util.List;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class TriviaVideoInsideCountdown implements IXposedHookLoadPackage
{
    public void handleLoadPackage( final LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        findAndHookMethod( "com.intermedia.trivia.TriviaActivity", lpparam.classLoader,
            "onCreate", Bundle.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object thiz = param.thisObject;

                // Step 1: Replace the game's SurfaceView used for video rendering with a TextureView (HW rendering required)
                Object streamController = XposedHelpers.getObjectField( thiz, "ac" );
                Object streamViewHost = XposedHelpers.getObjectField( streamController, "c" );
                SurfaceView videoSurface = (SurfaceView) XposedHelpers.getObjectField( streamViewHost, "videoSurface" );

                TextureView videoView = new TextureView( (Activity) thiz );
                FrameLayout.LayoutParams videoViewParams = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
                FrameLayout trivia_view_layout = (FrameLayout) videoSurface.getParent();
                trivia_view_layout.addView( videoView, 1 );

                // Hiding the SurfaceView fixes the black flicker issue that appears in regular HQ sometimes
                videoSurface.setVisibility( View.GONE );

                // This ugly hack fixes the stream looking pixelated on some devices. Thanks, I hate it.
                videoView.setScaleX( 1.0000001f );

                // Step 2: Give the stream controller our TextureView - it will later pass it on to the stream
                XposedHelpers.setAdditionalInstanceField( streamController, "HDTextureView", videoView );

                // Step 3: Insert our VideoEffectView and give it to the question view host
                ViewGroup drawerLayout = (ViewGroup) XposedHelpers.getObjectField( thiz, "gameDrawer" );
                ViewGroup gameContainer = (ViewGroup) drawerLayout.getChildAt( 0 );

                // TODO: gameContainer should == trivia_view_layout, make sure

                // I don't really know that this is necessary, we'll find out!
                drawerLayout.forceLayout();

                final VideoEffectView videoFx = new VideoEffectView( (Activity) thiz );
                videoFx.videoView = videoView;

                Object triviaQuestionViewHost = XposedHelpers.getObjectField( thiz, "L" );
                XposedHelpers.setAdditionalInstanceField( triviaQuestionViewHost, "HDFXView", videoFx );

                // Step 4: Our VideoEffectView needs to know where the countdown timer thingy is
                Object triviaQuestionView = XposedHelpers.getObjectField( triviaQuestionViewHost, "a" );
                FrameLayout countdownContainer = (FrameLayout) XposedHelpers.getObjectField( triviaQuestionView, "countdownContainer" );
                videoFx.countdownContainer = countdownContainer;

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
                gameContainer.addView( videoFx, params );
                gameContainer.bringChildToFront( videoFx );
                videoFx.setVisibility( View.INVISIBLE );

                Log.d( "HD4HQ", "All done!" );
            }
        } );

        // Step 5: Tell the stream controller to give the stream object our view
        findAndHookMethod("com.intermedia.game.ab", lpparam.classLoader,
            "a", String.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                if ( XposedHelpers.getAdditionalInstanceField( param.thisObject, "HDTextureView" ) != null )
                {
                    Object hqStream = XposedHelpers.getObjectField( param.thisObject, "h" );
                    XposedHelpers.setAdditionalInstanceField( hqStream, "HDTextureView", XposedHelpers.getAdditionalInstanceField( param.thisObject, "HDTextureView" ) );
                }
            }
        } );

        // Step 6: Hijack the stream so it gets drawn to our TextureView
        findAndHookMethod("com.tendigi.hq.hqplayer.HQStream", lpparam.classLoader,
            "lambda$play$0", "com.tendigi.hq.hqplayer.HQStream", String.class, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object hqStream = param.args[ 0 ];
                TextureView tv = (TextureView) XposedHelpers.getAdditionalInstanceField( hqStream, "HDTextureView" );

                if ( tv == null )
                {
                    Log.d( "HD4HQ", "This stream hasn't been manipulated by HD4HQ, bailing." );
                    return;
                }

                // Point of no return: by calling this we override the original method entirely.
                param.setResult( null );

                Log.d( "HD4HQ", "Obtaining video surface" );
                Surface surf = null;
                while( XposedHelpers.getBooleanField( hqStream, "play" ) && surf == null )
                {
                    if ( tv != null && tv.getSurfaceTexture() != null )
                    {
                        Log.d( "HD4HQ", "Got video surface!" );
                        Log.d( "HD4HQ", "Size: " + tv.getWidth() + "x" + tv.getHeight() );
                        surf = new Surface( tv.getSurfaceTexture() );
                    }
                }
                boolean done = false;
                while( XposedHelpers.getBooleanField( hqStream, "play" ) && !done )
                {
                    Class<?> HQStreamContext = XposedHelpers.findClass( "com.tendigi.hq.hqplayer.HQStreamContext", lpparam.classLoader );
                    done = (boolean) XposedHelpers.callMethod( hqStream, "connect", param.args[ 1 ], XposedHelpers.newInstance( HQStreamContext, surf ) );
                    if ( !done )
                    {
                        XposedHelpers.callMethod( hqStream, "closeFormatContext" );
                    }
                }

                if ( XposedHelpers.getBooleanField( hqStream, "play" ) )
                {
                    XposedHelpers.callMethod( XposedHelpers.getObjectField( hqStream, "streamListener" ), "onConnected" );
                    XposedHelpers.callMethod( hqStream, "demux" );
                }
            }
        } );

        // Step 7: Start the animation when a new question is asked.
        findAndHookMethod("com.intermedia.trivia.TriviaQuestionViewHost", lpparam.classLoader,
            "c", boolean.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                VideoEffectView videoFx = (VideoEffectView) XposedHelpers.getAdditionalInstanceField( param.thisObject, "HDFXView" );

                if ( videoFx != null )
                {
                    if ( (boolean) param.args[ 0 ] )
                    {
                        videoFx.countdownContainer.setScaleX( 1 );
                        videoFx.countdownContainer.setScaleY( 1 );

                        videoFx.setVisibility( View.VISIBLE );
                        videoFx.onQuestionShown();
                    }
                    else
                    {
                        videoFx.setVisibility( View.INVISIBLE );
                    }

                    // Step 8: Give the question view to the countdown handler.
                    Object triviaQuestionView = XposedHelpers.getObjectField( param.thisObject, "a" );
                    Object countdownHandler = XposedHelpers.getObjectField( triviaQuestionView, "e" );
                    XposedHelpers.setAdditionalInstanceField( countdownHandler, "HDTriviaQuestionView", triviaQuestionView );
                }
            }
        } );

        // Step 9: Hide the countdown circle when time is up, with a neat transition
        findAndHookMethod("com.intermedia.trivia.TriviaQuestionView$a", lpparam.classLoader,
            "onFinish", new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object triviaQuestionView = XposedHelpers.getAdditionalInstanceField( param.thisObject, "HDTriviaQuestionView" );
                if ( triviaQuestionView == null )
                {
                    Log.d( "HD4HQ", "Countdown doesn't have trivia question view handle" );
                    return;
                }

                Log.d( "HD4HQ", "animating out" );

                PathInterpolator interpolator = new PathInterpolator( 0.20f, 0.0f, 0.85f, 0.30f );
                FrameLayout countdownContainer = ( FrameLayout ) XposedHelpers.getObjectField( triviaQuestionView, "countdownContainer" );

                ObjectAnimator scaleX = ObjectAnimator.ofFloat( countdownContainer, "scaleX", 0.0f );
                ObjectAnimator scaleY = ObjectAnimator.ofFloat( countdownContainer, "scaleY", 0.0f );

                scaleX.setInterpolator( interpolator );
                scaleX.setDuration( 200 );

                scaleY.setInterpolator( interpolator );
                scaleY.setDuration( 200 );

                scaleX.start();
                scaleY.start();
            }
        } );

        // Step 10: Hide the effect view when showing results, at least for now.
        findAndHookMethod("com.intermedia.trivia.TriviaQuestionViewHost", lpparam.classLoader,
            "a", List.class, int.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                VideoEffectView videoFx = (VideoEffectView) XposedHelpers.getAdditionalInstanceField( param.thisObject, "HDFXView" );

                if ( videoFx != null )
                {
                    videoFx.setVisibility( View.INVISIBLE );
                }
            }
        } );

        // And we're done. Phew!
    }
}
