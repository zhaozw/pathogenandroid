package com.pathogengame.pathogen;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.VideoView;

import java.io.IOException;

public class VideoActivity extends Activity implements OnClickListener 
{
	// http://stackoverflow.com/questions/3028717/how-to-play-videos-in-android-from-assets-folder-or-raw-folder
	// http://stackoverflow.com/questions/11356601/androidplay-video-from-assets
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        /*
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
		
		view.start();*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_video, menu);
		return true;
	}
	
	void SkipIntro()
	{
		Intent intent = new Intent(this, com.pathogengame.pathogen.Main2Activity.class);
		startActivity(intent);
	}
	
	public void onClick(View view) 
	{
		switch (view.getId()) 
		{
			case R.id.touch_listen:
				SkipIntro();
			break;
		}
	}

}
