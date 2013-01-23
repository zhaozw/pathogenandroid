package com.pathogengame.pathogen;

import android.opengl.*;
import android.app.Activity;
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;
import android.view.Window;
import android.os.Bundle;
import java.io.IOException;
import android.content.res.AssetManager;
import android.view.Menu;
import java.io.InputStream;
import android.view.View;

public class MainActivity extends Activity 
{
	public static final float MIN_D = 1.0f;
	public static final float MAX_D = 9000.0f;
	public static final int TEXTURES = 128;
	
    private MyGLSurfaceView mGLView;
    public MyGL20Renderer mRenderer;
    public CShader mShader[] = new CShader[ CShader.SHADERS ];
    public CFont mFont[] = new CFont[ CFont.FONTS ];
    public CGUI mGUI;

	private float[] mViewMatrix = new float[16];
	private float[] mProjMatrix = new float[16];
	private float[] mModelMatrix = new float[16];

	int mWidth = 1;
	int mHeight = 1;
	public float mRetinaScale = 1.0f;
	
	CTexture mTexture[] = new CTexture[TEXTURES];
	
	int rotational;
	
	public void LoadFonts()
	{
		for(int i=0; i<CFont.FONTS; i++)
			mFont[i] = new CFont(this);
	}
	
    public void Init()
    {
    	for(int i=0; i<TEXTURES; i++)
    		mTexture[i] = new CTexture();
    	
    	mRenderer.mTriangle = new Triangle();
    	mRenderer.mTriangle.mTextureDataHandle = CreateTexture("textures/texture", true);
    	rotational = CreateTexture("gui/rotational", true);
    	
    	mShader[CShader.MODEL] = new CShader(this, "model.vert", "model.frag");
    	mShader[CShader.MAP] = new CShader(this, "map.vert", "map.frag");
    	mShader[CShader.SKY] = new CShader(this, "sky.vert", "sky.frag");
    	mShader[CShader.ORTHO] = new CShader(this, "ortho.vert", "ortho.frag");
    	mShader[CShader.BILLBOARD] = new CShader(this, "billboard.vert", "billboard.frag");
    	
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	mWidth = display.getWidth();
    	mHeight = display.getHeight();
    	
    	if(mHeight > 320)
    		mRetinaScale = 2.0f;
    	
    	LoadFonts();
    	
    	mGUI = new CGUI(this);
    }
    
    public void Deinit()
    {
    	FreeTextures();
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
		GLES20.glEnableVertexAttribArray(s.slot[CShader.POSITION]);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.TEXCOORD]);
        mRenderer.mTriangle.draw(s);
        
        s = mShader[CShader.ORTHO];
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        s.Use();
        GLES20.glUniform1f(s.slot[CShader.WIDTH], (float)mWidth);
        GLES20.glUniform1f(s.slot[CShader.HEIGHT], (float)mHeight);
        GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.POSITION]);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.TEXCOORD]);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        mGUI.draw();
        //DrawShadowedText(MSGOTHIC16, 0, 0, "Hello world. My name is Denis.");
        //mGUI.DrawImage(rotational, 0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    
    public void Resize(int width, int height)
    {
    	mWidth = width;
    	mHeight = height;
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

        /*
    	AssetManager am = getAssets();
    	try
    	{
    		System.out.println(am.list("gui/"));
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}*/
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
    
    public int NewTexture()
    {
    	for(int i=0; i<TEXTURES; i++)
    		if(!mTexture[i].on)
    			return i;
    	
    	return -1;
    }
    
    public int FindTexture(String strFileName)
    {
    	for(int i=0; i<TEXTURES; i++)
    		if(mTexture[i].on && mTexture[i].file.equals(strFileName))
    			return i;
    	
    	return -1;
    }
    
    public String FindTextureExtension(String strFileName)
    {
    	AssetManager am = getAssets();
    	
    	String jpgpath = strFileName + ".jpg";
    	String pngpath = strFileName + ".png";
    	boolean found = true;
        InputStream is = null;
        
        try 
        {
        	is = am.open(jpgpath);
        } 
        catch (final IOException e) 
        {
            found = false;
        }
        
        if(found)
        	return jpgpath;
        found = true;
    	
        try 
        {
        	is = am.open(pngpath);
        } 
        catch (final IOException e) 
        {
            found = false;
        }
      
        if(found)
        	return pngpath;
        found = true;
    	
    	return strFileName;
    }
    
    public int CreateTexture(String strFileName, boolean search)
    {
    	int texture = 0;
    	strFileName = FindTextureExtension(strFileName);
    	
    	if(search && (texture = FindTexture(strFileName)) >= 0)
    		return texture;
    		
    	int i = NewTexture();
    	mTexture[i].Load(this, strFileName);
    	
    	return mTexture[i].tex[0];
    }
    
    public void FreeTextures()
    {
    	for(int i=0; i<TEXTURES; i++)
    	{
    		if(!mTexture[i].on)
    			continue;
    		
    		GLES20.glDeleteTextures(1, mTexture[i].tex, 0);
    	}
    }
    
    public String StripPathExtension(String s0)
    {
    	int sep0 = s0.lastIndexOf('\\');
    	int sep1 = s0.lastIndexOf('/');
    	int sep = Math.max(sep0, sep1);
    	String s1;

        if (sep != -1)
            s1 = s0.substring(sep + 1);
    	else
    		s1 = s0;

    	int dot = s1.lastIndexOf('.');
    	String s2;

    	if (dot != -1)
    		s2 = s1.substring(0, dot);
    	else
    		s2 = s1;
    	
    	return s2;
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
