package com.pathogengame.pathogen;

import android.view.*;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.view.Menu;
import android.content.Intent;
import android.widget.EditText;
import android.opengl.*;

public class MainActivity extends Activity 
{
    private GLSurfaceView mGLView;
	
    void Init()
    {
    	
    }
    
    void Deinit()
    {
    	
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(  
                WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
        Init();
    }
    
    @Override
    protected void onStop()
    {
    	Deinit();
    	finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) 
    {
    	//System.out.println("Welcome To My First Java Program ");
        // Do something in response to button
    	//Intent intent = new Intent(this, DisplayMessageActivity.class);
    	//EditText editText = (EditText) findViewById(R.id.edit_message);
    	//String message = editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
    	//System.out.println("1:" + message);
    	//startActivity(intent);
    }
}
