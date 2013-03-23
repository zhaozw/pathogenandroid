package com.pathogengame.pathogen;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class CSound 
{
    String file;
    MediaPlayer mMedia;
	
	CSound() 
	{
		mMedia = null;
	}
 
    CSound(MainActivity context, String f)
    {
		String raw = CFile.StripPathExtension(f);
		file = "/sounds/" + raw + ".wav";
		
    	try
    	{
    		AssetFileDescriptor afd = context.getAssets().openFd(file);
    		mMedia = new MediaPlayer();
    		mMedia.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
    		mMedia.prepare();
    	}
    	catch(IOException e)
    	{
    		//e.printStackTrace();
    		System.out.println("Failed to load sound " + raw + ".wav");
    		mMedia = null;
    	}
    }
    
    void Play()
    {
    	if(mMedia == null)
    		return;
    	
    	if(mMedia.isPlaying())
    		mMedia.stop();
    	mMedia.seekTo(0);
    	mMedia.start();
    }
}
