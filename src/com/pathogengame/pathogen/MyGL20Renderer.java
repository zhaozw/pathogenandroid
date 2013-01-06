package com.pathogengame.pathogen;

import android.opengl.*;
import java.nio.*;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;
import android.os.*;
import android.app.Activity;

public class MyGL20Renderer implements GLSurfaceView.Renderer 
{
	
	public Triangle mTriangle;
	public MainActivity mActivity;
	//Square mSquare;
	
	/** Store our model data in a float buffer. */
	private final FloatBuffer mTriangle1Vertices;
	//private final FloatBuffer mTriangle2Vertices;
	//private final FloatBuffer mTriangle3Vertices;
	 
	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;
	
	float mAngle;
	
	public MyGL20Renderer()
	{
	    // This triangle is red, green, and blue.
	    final float[] triangle1VerticesData = {
	            // X, Y, Z,
	            // R, G, B, A
	            -0.5f, -0.25f, 0.0f,
	            1.0f, 0.0f, 0.0f, 1.0f,
	 
	            0.5f, -0.25f, 0.0f,
	            0.0f, 0.0f, 1.0f, 1.0f,
	 
	            0.0f, 0.559016994f, 0.0f,
	            0.0f, 1.0f, 0.0f, 1.0f};
	 
	    //...
	 
	    // Initialize the buffers.
	    mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * mBytesPerFloat)
	    .order(ByteOrder.nativeOrder()).asFloatBuffer();
	 
	    //...
	 
	    mTriangle1Vertices.put(triangle1VerticesData).position(0);
	 
	    //...
        //mTriangle = new Triangle();
	}
	
    public void onSurfaceCreated(GL10 unused, EGLConfig config) 
    {
        // Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        

		//GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glFrontFace(GLES20.GL_CW);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // initialize a triangle
        
        // initialize a square
        //mSquare = new Square();
		
		mActivity.Init();
        
        /*
     // Position the eye behind the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = 1.5f;
     
        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;
     
        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
     
        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
        */
        
        
    }

    public void onDrawFrame(GL10 unused) 
    {
    	mActivity.Draw();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) 
    {
    	mActivity.Resize(width, height);
    }
}
