package com.pathogengame.pathogen;

import java.io.IOException;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

public class CSound 
{
	static final int DRYSHOT = 0;
	static final int SHOT = 1;
	static final int RELOAD = 2;
	static final int COCK = 3;
	static final int DRYFIRE = 4;
	static final int HIT = 5;
	static final int ITEM_SOUNDS = 6;
	
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
