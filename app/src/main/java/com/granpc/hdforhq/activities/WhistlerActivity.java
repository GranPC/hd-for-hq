package com.granpc.hdforhq.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class WhistlerActivity extends BaseHQActivity
{
    public WhistlerActivity()
    {
    }

    private RelativeLayout generateLayout()
    {
        RelativeLayout layout = new RelativeLayout( thiz );
        RelativeLayout.LayoutParams fill = new RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT );
        View colorView = new View( thiz );
        colorView.setBackgroundColor( 0xff303392 );
        layout.addView( colorView, fill );
        return layout;
    }

    @Override
    public void onCreate( Bundle bundle )
    {
        super.onCreate( bundle );
        Log.d( "HD4HQ", "Whistler activity is alive!" );
        thiz.setContentView( generateLayout() );
    }
}
