package com.granpc.hdforhq.activities;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WhistlerActivity extends BaseHQActivity
{
    public WhistlerActivity()
    {
    }

    // I really ought to figure out a way to import XML layouts...
    private RelativeLayout generateLayout()
    {
        RelativeLayout layout = new RelativeLayout( thiz );
        layout.setFitsSystemWindows( false );
        layout.setBackgroundColor( 0xff36399a );

        RelativeLayout.LayoutParams center = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT );
        center.addRule( RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE );

        LinearLayout hero = new LinearLayout( thiz );
        int padding = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 60, thiz.getResources().getDisplayMetrics() );
        hero.setPadding( padding, 0, padding, 0 );
        hero.setOrientation( LinearLayout.VERTICAL );
        hero.setHorizontalGravity( Gravity.CENTER );

        TextView dailyHeader = new TextView( thiz );
        dailyHeader.setText( "Daily Challenge" );
        dailyHeader.setTypeface( hqFont, Typeface.BOLD );
        dailyHeader.setTextColor( Color.WHITE );
        dailyHeader.setTextSize( TypedValue.COMPLEX_UNIT_SP, 55.f );
        dailyHeader.setGravity( Gravity.CENTER );
        hero.addView( dailyHeader );

        TextView dailySubtitle = new TextView( thiz );
        dailySubtitle.setText( "Answer trivia questions to win rewards!" );
        dailySubtitle.setTypeface( hqFont );
        dailySubtitle.setTextColor( Color.WHITE );
        dailySubtitle.setTextSize( TypedValue.COMPLEX_UNIT_SP, 18.f );
        dailySubtitle.setLineSpacing( 0.0f, 1.2f );
        dailySubtitle.setGravity( Gravity.CENTER );
        int spacing = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 25, thiz.getResources().getDisplayMetrics() );
        dailySubtitle.setPadding( 0, spacing, 0, 0 );
        int width = (int) (Resources.getSystem().getDisplayMetrics().widthPixels / 1.8f);
        hero.addView( dailySubtitle, width, LinearLayout.LayoutParams.WRAP_CONTENT );

        layout.addView( hero, center );
        return layout;
    }

    @Override
    public void onCreate( Bundle bundle )
    {
        super.onCreate( bundle );
        Log.d( "HD4HQ", "Whistler activity is alive!" );
        doCreate( bundle );
    }

    public void doCreate( Bundle bundle )
    {
        thiz.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE );
        thiz.getWindow().setStatusBarColor( Color.TRANSPARENT );
        thiz.setContentView( generateLayout() );
    }
}
