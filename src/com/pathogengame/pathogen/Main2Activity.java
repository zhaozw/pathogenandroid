package com.pathogengame.pathogen;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.VideoView;


//public class Main2Activity extends Activity
public class Main2Activity extends android.app.NativeActivity 
{/*
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        
        System.out.println("video act started");
        
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(  
                WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_video);
		
		VideoView view = (VideoView)findViewById(R.id.touch_listen);
		String path = "android.resource://" + getPackageName() + "/" + R.raw.intro;
		view.setVideoURI(Uri.parse(path));
		
		// http://stackoverflow.com/questions/1847818/listener-or-handler-for-video-finish
		 view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

	            @Override
	            public void onCompletion(MediaPlayer mp) {
	                // not playVideo
	                            // playVideo();

	                            SkipIntro();
	            }
	        });
		
		view.start();
	}

	
	void SkipIntro()
	{
		System.out.println("SkipIntro");
		//Intent intent = new Intent(this, com.pathogengame.pathogen.Main2Activity.class);
		//startActivity(intent);
	}*/
}
