package com.granpc.hdforhq.tweaks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Constructor;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class EnableNewLobby implements IXposedHookLoadPackage
{
	public void handleLoadPackage( final XC_LoadPackage.LoadPackageParam lpparam ) throws Throwable
	{
		if ( !lpparam.packageName.equals( "com.intermedia.hq" ) )
			return;

		findAndHookMethod( "com.intermedia.EntryActivity", lpparam.classLoader,
			"onCreate", Bundle.class, new XC_MethodHook()
		{
			@Override
			protected void afterHookedMethod( MethodHookParam param ) throws Throwable
			{
				Activity thiz = (Activity) param.thisObject;

				Object sessionManager = XposedHelpers.getObjectField( thiz, "a" );
				Class destination;

				if ( (boolean) XposedHelpers.callMethod( sessionManager, "c" ) )
				{
					destination = XposedHelpers.findClass( "com.intermedia.MainBottomNavActivity", lpparam.classLoader );
				}
				else
				{
					destination = XposedHelpers.findClass( "com.intermedia.login.LoginActivity", lpparam.classLoader );
				}

				Intent intent = new Intent( thiz, destination );
				intent.setFlags( Intent.FLAG_ACTIVITY_NO_ANIMATION );
				thiz.startActivity( intent );
				thiz.finish();
			}
		} );
	}
}