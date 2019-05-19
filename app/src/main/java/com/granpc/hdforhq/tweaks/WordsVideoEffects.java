package com.granpc.hdforhq.tweaks;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;

import com.granpc.hdforhq.views.VideoEffectView;

import java.lang.reflect.Constructor;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findConstructorExact;

public class WordsVideoEffects implements IXposedHookLoadPackage
{
    public void handleLoadPackage( final LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        findAndHookMethod( "com.intermedia.words.WordsActivity", lpparam.classLoader,
            "onCreate", Bundle.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object thiz = param.thisObject;

                // Step 1: Replace the game's SurfaceView used for video rendering with a TextureView (HW rendering required)
                Object streamController = XposedHelpers.getObjectField( thiz, "r" );
                Object streamViewHost = XposedHelpers.getObjectField( streamController, "c" );
                SurfaceView videoSurface = (SurfaceView) XposedHelpers.getObjectField( streamViewHost, "videoSurface" );

                TextureView videoView = new TextureView( (Activity) thiz );
                FrameLayout.LayoutParams videoViewParams = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
                Object words_view_layout = videoSurface.getParent(); /* android.support.constraint.ConstraintLayout */
                XposedHelpers.callMethod( words_view_layout, "addView", videoView, 1 );

                // Hiding the SurfaceView fixes the black flicker issue that appears in regular HQ sometimes
                videoSurface.setVisibility( View.GONE );

                // This ugly hack fixes the stream looking pixelated on some devices. Thanks, I hate it.
                videoView.setScaleX( 1.0000001f );

                // Step 2: Give the stream controller our TextureView - it will later pass it on to the stream
                XposedHelpers.setAdditionalInstanceField( streamController, "HDTextureView", videoView );

                // Step 3: Insert our VideoEffectView
                // 2131362178 = R.id.gameDrawer
                ViewGroup wordsActivityView = (ViewGroup) XposedHelpers.callMethod( thiz, "b", 2131362178 );

                final VideoEffectView videoFx = new VideoEffectView( (Activity) thiz, VideoEffectView.TYPE_WORDS );
                videoFx.videoView = videoView;

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT );
                wordsActivityView.addView( videoFx, (ViewGroup.MarginLayoutParams) params );
                wordsActivityView.bringChildToFront( videoFx );
                videoFx.setVisibility( View.INVISIBLE );

                XposedHelpers.setAdditionalInstanceField( thiz, "HDFXView", videoFx );

                Log.d( "HD4HQ", "All done!" );
            }
        } );

        // Steps 4 and 5: see tweaks/VideoHijackHelper

        findAndHookConstructor( "com.intermedia.words.Ya", lpparam.classLoader,
            "com.intermedia.model.Aa", ViewGroup.class, Context.class, "com.intermedia.websocket.ba", new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                View wheel_modal = (View) XposedHelpers.getObjectField( param.thisObject, "d" );
                wheel_modal.setVisibility( View.INVISIBLE );
            }
        } );

        // Step 6: Start the animation when the wheel appears
        findAndHookMethod("com.intermedia.words.Ya", lpparam.classLoader,
            "b", new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object wordsActivity = XposedHelpers.getObjectField( param.thisObject, "t" );
                View wheel_modal = (View) XposedHelpers.getObjectField( param.thisObject, "a" );
                VideoEffectView videoFx = (VideoEffectView) XposedHelpers.getAdditionalInstanceField( wordsActivity, "HDFXView" );

                wheel_modal.setAlpha( 0.0f );
                wheel_modal.setVisibility( View.VISIBLE );

                ObjectAnimator anim = ObjectAnimator.ofFloat( wheel_modal, "alpha", 0.0f, 0.0f, 1.0f );
                anim.setDuration( 200 );
                anim.start();

                Log.d( "HD4HQ", "Wheel shown!" );

                if ( videoFx != null )
                {
                    videoFx.wheelContainer = (View) XposedHelpers.getObjectField( param.thisObject, "c" );
                    videoFx.bringToFront();

                    videoFx.setScaleX( 1 );
                    videoFx.setScaleY( 1 );

                    videoFx.setVisibility( View.VISIBLE );
                    videoFx.onWheelShown();
                }
            }
        } );

        // Step 7: Hide our video effect view when the wheel disappears.
        findAndHookMethod("com.intermedia.words.Ya", lpparam.classLoader,
            "a", new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Log.d( "HD4HQ", "Wheel is gone!" );

                Object wordsActivity = XposedHelpers.getObjectField( param.thisObject, "t" );
                VideoEffectView videoFx = (VideoEffectView) XposedHelpers.getAdditionalInstanceField( wordsActivity, "HDFXView" );

                if ( videoFx != null )
                {
                    videoFx.setVisibility( View.INVISIBLE );
                }
            }
        } );

        // And we're done. Phew!
    }
}
