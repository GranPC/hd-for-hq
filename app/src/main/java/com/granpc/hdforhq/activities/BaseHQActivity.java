package com.granpc.hdforhq.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import java.lang.reflect.Method;

public class BaseHQActivity
{
    public static Class hijackedActivity;
    public static BaseHQActivity currentHijackActivity;
    public static Method baseOnCreate;
    public Activity thiz;

    public static void launchActivity( BaseHQActivity impl, Context ctx )
    {
        currentHijackActivity = impl;
        Intent intent = new Intent( ctx, hijackedActivity );
        ctx.startActivity( intent );
    }

    public void onCreate( Bundle bundle )
    {
        try
        {
            baseOnCreate.invoke( thiz, bundle );
        }
        catch ( Exception e )
        {
            Log.wtf( "HD4HQ", "Failed to invoke super.onCreate" + e.toString() );
        }
        thiz.setActionBar( null );
    }
}