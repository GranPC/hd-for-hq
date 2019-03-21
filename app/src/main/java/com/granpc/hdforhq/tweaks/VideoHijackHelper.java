package com.granpc.hdforhq.tweaks;

import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by granpc on 3/4/19.
 */

public class VideoHijackHelper implements IXposedHookLoadPackage
{
    public void handleLoadPackage( final XC_LoadPackage.LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        // Tell the stream controller to give the stream object our view
        findAndHookMethod( "com.intermedia.game.z", lpparam.classLoader,
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

        // Hijack the stream so it gets drawn to our TextureView
        findAndHookMethod( "com.tendigi.hq.hqplayer.HQStream", lpparam.classLoader,
            "lambda$play$0", "com.tendigi.hq.hqplayer.HQStream", String.class, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object hqStream = param.args[0];
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
                while ( XposedHelpers.getBooleanField( hqStream, "play" ) && surf == null )
                {
                    if ( tv != null && tv.getSurfaceTexture() != null )
                    {
                        Log.d( "HD4HQ", "Got video surface!" );
                        Log.d( "HD4HQ", "Size: " + tv.getWidth() + "x" + tv.getHeight() );
                        surf = new Surface( tv.getSurfaceTexture() );
                    }
                }
                boolean done = false;
                while ( XposedHelpers.getBooleanField( hqStream, "play" ) && !done )
                {
                    Class<?> HQStreamContext = XposedHelpers.findClass( "com.tendigi.hq.hqplayer.HQStreamContext", lpparam.classLoader );
                    done = (boolean) XposedHelpers.callMethod( hqStream, "connect", param.args[1], XposedHelpers.newInstance( HQStreamContext, surf ) );
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
    }
}
