package com.pathogengame.pathogen;

import android.view.*;
import android.R.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.app.Activity;
import android.view.Menu;
import android.content.Intent;
import android.widget.EditText;
import android.opengl.*;

public class MainActivity extends Activity 
{
	public static final float MIN_D = 1.0f;
	public static final float MAX_D = 9000.0f;
	public static final int TEXTURES = 128;
	
    private MyGLSurfaceView mGLView;
    public MyGL20Renderer mRenderer;
    public CShader mShader[] = new CShader[ CShader.SHADERS ];

	private float[] mViewMatrix = new float[16];
	private float[] mProjMatrix = new float[16];
	private float[] mModelMatrix = new float[16];
	//private float[] mRotationMatrix = new float[16];

	int m_width = 1;
	int m_height = 1;
	
	CTexture mTexture[] = new CTexture[TEXTURES];
	
    public void Init()
    {
    	for(int i=0; i<TEXTURES; i++)
    		mTexture[i] = new CTexture();
    	
    	mRenderer.mTriangle = new Triangle();
        
    	mRenderer.mTriangle.mTextureDataHandle = CTexture.loadTexture(this, "textures/texture.jpg");
    	
    	//mShader[CShader.SHADER] = new CShader(this, "shader.vert", "shader.frag");
    	mShader[CShader.MODEL] = new CShader(this, "model.vert", "model.frag");
    	mShader[CShader.MAP] = new CShader(this, "map.vert", "map.frag");
    	mShader[CShader.SKY] = new CShader(this, "sky.vert", "sky.frag");
    	mShader[CShader.ORTHO] = new CShader(this, "ortho.vert", "ortho");
    	mShader[CShader.BILLBOARD] = new CShader(this, "billboard.vert", "billboard.frag");
    }
    
    public void Deinit()
    {
    	
    }
    
    public void Draw()
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //public static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);

        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
        
        Matrix.setRotateM(mModelMatrix, 0, 0, 0, 0, 1.0f);
        
        CShader s = mShader[CShader.MODEL];
        s.Use();
        GLES20.glUniformMatrix4fv(s.slot[CShader.PROJMAT], 1, false, mProjMatrix, 0);
        GLES20.glUniformMatrix4fv(s.slot[CShader.VIEWMAT], 1, false, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(s.slot[CShader.MODELMAT], 1, false, mModelMatrix, 0);
        mRenderer.mTriangle.draw(s);
    }
    
    public void Resize(int width, int height)
    {
    	m_width = width;
    	m_height = height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        //public static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far);
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, MIN_D, MAX_D);
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
        mRenderer = new MyGL20Renderer();
        mRenderer.mActivity = this;
        mGLView.setRenderer(mRenderer);
        setContentView(mGLView);
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
    
    public int CreateTexture(String strFileName, boolean search)
    {
    	int texture = 0;
    	
    	
    	
    	return texture;
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
