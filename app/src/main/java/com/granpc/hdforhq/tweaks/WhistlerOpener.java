package com.granpc.hdforhq.tweaks;

import android.app.Activity;
import android.os.Bundle;

import com.granpc.hdforhq.activities.BaseHQActivity;
import com.granpc.hdforhq.activities.WhistlerActivity;
import com.granpc.hdforhq.deobfuscation.O;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class WhistlerOpener implements IXposedHookLoadPackage
{
    public void handleLoadPackage( final XC_LoadPackage.LoadPackageParam lpparam ) throws Throwable
    {
        if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
            return;

        findAndHookMethod( "com.intermedia.MainBottomNavActivity", lpparam.classLoader,
            "onCreate", Bundle.class, new XC_MethodHook()
        {
            @Override
            protected void afterHookedMethod( MethodHookParam param ) throws Throwable
            {
                BaseHQActivity.launchActivity( new WhistlerActivity(), (Activity) param.thisObject );
            }
        } );
    }
}