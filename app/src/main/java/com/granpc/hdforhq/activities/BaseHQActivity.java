package com.granpc.hdforhq.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;

import com.granpc.hdforhq.api.AuthedApi;
import com.granpc.hdforhq.deobfuscation.HQR;
import com.granpc.hdforhq.deobfuscation.O;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class BaseHQActivity
{
    public static Class hijackedActivity;
    public static BaseHQActivity currentHijackActivity;
    public static Method baseOnCreate;
    public static Method baseGetSupportActionBar;
    public static Method supportActionBarHide;
    public static Method baseGetAuthedApi;
    public Activity thiz;

    protected Typeface hqFont;
    protected Boolean isMock = true;

    private AuthedApi api;

    public static void launchActivity( BaseHQActivity impl, Context ctx )
    {
        currentHijackActivity = impl;
        impl.isMock = false;

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

    protected AuthedApi getAuthedApi()
    {
        // We always want to run in mock mode for now
        if ( isMock || true )
        {
            Log.w( "HD4HQ", "getAuthedApi: running in mock mode" );
            if (api == null)
            {
                Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl( "http://192.168.1.81:8047" )
                    .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                    .addConverterFactory( MoshiConverterFactory.create() )
                    .build();

                api = retrofit.create( AuthedApi.class );
            }

            return api;
        }

        try
        {
            Object authedApi = baseGetAuthedApi.invoke( thiz );
            Log.d( "HD4HQ", "Authed API is " + authedApi.getClass().getPackage() + " / " + authedApi.getClass().getCanonicalName() );
            return null;
        }
        catch ( Exception e )
        {
            Log.wtf( "HD4HQ", "Failed to invoke getAuthedApi: " + e.toString() );
        }

        return null;
    }
}