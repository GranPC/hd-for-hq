package com.granpc.hdforhq.tweaks;

import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class GameEventRecorder implements IXposedHookLoadPackage
{
    public void handleLoadPackage( final XC_LoadPackage.LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        findAndHookConstructor( "com.intermedia.websocket.v", lpparam.classLoader,
        new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object thiz = param.thisObject;
                String path = Environment.getExternalStorageDirectory().getPath() + "/HD4HQ/";

                File folder = new File( path );
                boolean ok = true;
                if ( !folder.exists() )
                {
                    ok = folder.mkdirs();
                }

                if ( !ok )
                {
                    Log.e( "HD4HQ", "Couldn't start recording game, mkdirs failed" );
                    return;
                }

                File gameSession = new File( folder, "game-" + System.currentTimeMillis() + ".log" );
                ok = gameSession.createNewFile();

                if ( !ok )
                {
                    Log.e( "HD4HQ", "Couldn't start recording game, createNewFile failed" );
                    return;
                }

                FileOutputStream fos = new FileOutputStream( gameSession );
                OutputStreamWriter writer = new OutputStreamWriter( fos );

                XposedHelpers.setAdditionalInstanceField( thiz, "HDRecordFile", writer );
            }
        } );

        findAndHookMethod( "com.intermedia.websocket.v", lpparam.classLoader,
            "a", String.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                Object thiz = param.thisObject;
                if ( XposedHelpers.getAdditionalInstanceField( thiz, "HDRecordFile" ) != null )
                {
                    OutputStreamWriter writer = ( OutputStreamWriter ) XposedHelpers.getAdditionalInstanceField( thiz, "HDRecordFile" );
                    writer.write( ( String ) param.args[ 0 ] );
                    writer.append( '\n' );
                    writer.flush();
                }
            }
        } );
    }
}
