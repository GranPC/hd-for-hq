package com.granpc.hdforhq;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WhistlerActivity extends AppCompatActivity
{
    com.granpc.hdforhq.activities.WhistlerActivity activity;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        activity = new com.granpc.hdforhq.activities.WhistlerActivity();
        activity.thiz = this;
        super.onCreate( savedInstanceState );
        activity.doCreate( savedInstanceState );
    }
}
