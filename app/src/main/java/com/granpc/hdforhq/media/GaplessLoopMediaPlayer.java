package com.granpc.hdforhq.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class GaplessLoopMediaPlayer implements MediaPlayer.OnCompletionListener
{
    private MediaPlayer currentPlayer;
    private MediaPlayer nextPlayer;
    private File file;

    public GaplessLoopMediaPlayer( Context context, String path )
    {
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
    public void onCompletion( MediaPlayer mp )
    {
        currentPlayer = nextPlayer;
        nextPlayer = mp;
        nextPlayer.pause();
        nextPlayer.seekTo( 0 );
        currentPlayer.setNextMediaPlayer( nextPlayer );
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
