package com.granpc.hdforhq.tweaks;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.granpc.hdforhq.activities.BaseHQActivity;
import com.granpc.hdforhq.deobfuscation.O;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class ActivityHijackHelper implements IXposedHookLoadPackage
{
    private final String baseActivity = "com.intermedia.friends.InviteContactsActivity";
    public void handleLoadPackage( final XC_LoadPackage.LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        BaseHQActivity.hijackedActivity = XposedHelpers.findClass( baseActivity, lpparam.classLoader );
        BaseHQActivity.baseOnCreate = XposedHelpers.findMethodExact( O.BaseInjectedActivity, lpparam.classLoader, "onCreate", Bundle.class );
        BaseHQActivity.baseGetSupportActionBar = XposedHelpers.findMethodExact( O.AppCompatActivity, lpparam.classLoader, "getSupportActionBar" );
        BaseHQActivity.supportActionBarHide = XposedHelpers.findMethodExact( O.AppCompatWindowDecorActionBar, lpparam.classLoader, O.AppCompatWindowDecorActionBar_hide );

        findAndHookMethod( baseActivity, lpparam.classLoader,
            "onCreate", Bundle.class, new XC_MethodHook()
        {
            @Override
            protected void beforeHookedMethod( MethodHookParam param ) throws Throwable
            {
                if ( BaseHQActivity.currentHijackActivity != null )
                {
                    BaseHQActivity hijackActivity = BaseHQActivity.currentHijackActivity;
                    BaseHQActivity.currentHijackActivity = null;
                    hijackActivity.thiz = (Activity) param.thisObject;
                    hijackActivity.onCreate( (Bundle) param.args[ 0 ] );
                    param.setResult( null );
                }
            }
        } );
    }
}