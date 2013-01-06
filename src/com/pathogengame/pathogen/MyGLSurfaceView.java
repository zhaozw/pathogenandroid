package com.pathogengame.pathogen;

import android.opengl.*;
import android.view.*;
import android.content.Context;

public class MyGLSurfaceView extends GLSurfaceView 
{
	float mPreviousX;
	float mPreviousY;
	//public MyGL20Renderer mRenderer;
	
	public MyGLSurfaceView(Context context)
    {
        super(context);

        setEGLContextClientVersion(2);
        
        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        //mRenderer = new MyGL20Renderer();
        
        // Set the Renderer for drawing on the GLSurfaceView
        //setRenderer(mRenderer);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.

	    float TOUCH_SCALE_FACTOR = 0.1f;

	    switch (e.getAction()) // & MotionEvent.ACTION_MASK) 
	    {
		    case MotionEvent.ACTION_DOWN:
		    {
		    	int numPointers = e.getPointerCount();
                for (int i = 0; i < numPointers; i++)
                {
            	    float x = e.getX(i);
            	    float y = e.getY(i);
                }
		    }break;
		    case MotionEvent.ACTION_UP:
		    {
		    	int numPointers = e.getPointerCount();
                for (int i = 0; i < numPointers; i++)
                {
            	    float x = e.getX(i);
            	    float y = e.getY(i);
                }
		    }break;
	        case MotionEvent.ACTION_MOVE:
	        {
                int numPointers = e.getPointerCount();
                //System.out.println("" + numPointers + " moved");
                
                for (int i = 0; i < numPointers; i++)
                {
            	    float x = e.getX(i);
            	    float y = e.getY(i);
		        	//System.out.println("move");
		        	
		            float dx = x - mPreviousX;
		            float dy = y - mPreviousY;
	
		            // reverse direction of rotation above the mid-line
		            if (y > getHeight() / 2) 
		            {
		              dx = dx * -1 ;
		            }
	
		            // reverse direction of rotation to left of the mid-line
		            if (x < getWidth() / 2) 
		            {
		              dy = dy * -1 ;
		            }
	
		            //mRenderer.mAngle += (dx + dy) * TOUCH_SCALE_FACTOR;  // = 180.0f / 320
		            //requestRender();

		    	    mPreviousX = x;
		    	    mPreviousY = y;
                }
	        }break;
	    }
	    
	    return true;
	}
}
