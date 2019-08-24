package com.granpc.hdforhq.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.granpc.hdforhq.deobfuscation.HQR;
import com.granpc.hdforhq.deobfuscation.O;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;

public class BaseHQActivity
{
    public static Class hijackedActivity;
    public static BaseHQActivity currentHijackActivity;
    public static Method baseOnCreate;
    public static Method baseGetSupportActionBar;
    public static Method supportActionBarHide;
    public Activity thiz;

    protected Typeface hqFont;

    public static void launchActivity( BaseHQActivity impl, Context ctx )
    {
        currentHijackActivity = impl;
        Intent intent = new Intent( ctx, hijackedActivity );
        ctx.startActivity( intent );
    }

    public void onCreate( Bundle bundle )
    {
        Object actionBar = null;
        try
        {
            baseOnCreate.invoke( thiz, bundle );
            actionBar = baseGetSupportActionBar.invoke( thiz );
            supportActionBarHide.invoke( actionBar );
        }
        catch ( Exception e )
        {
            Log.wtf( "HD4HQ", "Failed to invoke super method: " + e.toString() );
        }

        try
        {
            hqFont = ResourcesCompat.getFont( thiz, HQR.font.circular );
        }
        catch ( Exception e )
        {
            Log.w( "HD4HQ", "BaseHQActivity couldn't load font; maybe running outside of HQ?" );
        }

        // ugly hack to hide toolbar, I'll figure out a better solution...
        View v = (View) XposedHelpers.getObjectField( actionBar, O.AppCompatWindowDecorActionBar_mContainerView );
        v.setVisibility( View.GONE );
    }
}