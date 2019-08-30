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
import com.granpc.hdforhq.network.AuthInterceptor;
import com.granpc.hdforhq.network.HDHeaderInterceptor;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.lang.reflect.Method;

import de.robv.android.xposed.XposedHelpers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class BaseHQActivity
{
    public static Class hijackedActivity;
    public static BaseHQActivity currentHijackActivity;
    public static Method baseOnCreate;
    public static Method baseGetSupportActionBar;
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

    public void onStop()
    {
    }

    public void onPause()
    {
    }

    public void onResume()
    {
    }

    protected AuthedApi getAuthedApi()
    {
        // We always want to run in mock mode for now
        if ( isMock )
        {
            Log.w( "HD4HQ", "getAuthedApi: running in mock mode" );
            if (api == null)
            {
                OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor( new HDHeaderInterceptor() )
                    .build();

                Retrofit retrofit = new Retrofit.Builder()
                    .client( client )
                    .baseUrl( "http://192.168.1.81:8047" )
                    .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                    .addConverterFactory( MoshiConverterFactory.create() )
                    .build();

                api = retrofit.create( AuthedApi.class );
            }

            return api;
        }

        Log.w( "HD4HQ", "getAuthedApi: connecting to the real API" );
        if (api == null)
        {
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor( new HDHeaderInterceptor() )
                .addInterceptor( new AuthInterceptor() )
                .build();

            Retrofit retrofit = new Retrofit.Builder()
                .client( client )
                .baseUrl( "https://api-quiz.hype.space" )
                .addCallAdapterFactory( RxJava2CallAdapterFactory.create() )
                .addConverterFactory( MoshiConverterFactory.create() )
                .build();

            api = retrofit.create( AuthedApi.class );
        }

        return api;
    }
}