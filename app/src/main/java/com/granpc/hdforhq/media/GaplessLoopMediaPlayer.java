package com.granpc.hdforhq.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class GaplessLoopMediaPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener
{
    private MediaPlayer currentPlayer;
    private MediaPlayer nextPlayer;
    private File file;
    private Context context;

    public GaplessLoopMediaPlayer( Context context, String path )
    {
        this.context = context;
        file = new File( path );
        if ( file.exists() )
        {
            currentPlayer = MediaPlayer.create( context, Uri.fromFile( file ) );
            nextPlayer = MediaPlayer.create( context, Uri.fromFile( file ) );

            if ( !isValid() ) return;

            try
            {
                currentPlayer.prepareAsync();
                nextPlayer.prepareAsync();
            }
            catch ( Exception e )
            {
                Log.d( "HD4HQ", "GaplessLoopMediaPlayer: failed to prepare.");
                e.printStackTrace();
            }

            currentPlayer.setOnCompletionListener( this );
            nextPlayer.setOnCompletionListener( this );

            currentPlayer.setNextMediaPlayer( nextPlayer );
        }
    }

    @Override
    public void onPrepared( MediaPlayer mp )
    {
        currentPlayer.setNextMediaPlayer( mp );
        mp.setOnPreparedListener( null );
    }

    @Override
    public void onCompletion( MediaPlayer mp )
    {
        Log.d( "HD4HQ", "playback completed" );
        currentPlayer = nextPlayer;
        nextPlayer = mp;
        nextPlayer.reset();
        try
        {
            nextPlayer.setDataSource( context, Uri.fromFile( file ) );
            nextPlayer.setOnPreparedListener( this );
            nextPlayer.prepareAsync();
        }
        catch ( Exception e )
        {
            Log.d( "HD4HQ", "GaplessLoopMediaPlayer: failed to prepare next, loop will break." );
            e.printStackTrace();
        }
    }

    public Boolean isValid()
    {
        return currentPlayer != null && nextPlayer != null;
    }

    public void start()
    {
        currentPlayer.start();
    }

    public void pause()
    {
        currentPlayer.pause();
    }
}
