package com.pathogengame.pathogen;

import java.util.Vector;

import android.opengl.*;
import android.view.*;
import android.content.Context;
import android.graphics.Point;

public class MyGLSurfaceView extends GLSurfaceView 
{
	//float mPreviousX;
	//float mPreviousY;
	//public MyGL20Renderer mRenderer;
	
	Vector<Point> mTouch = new Vector<Point>();
	MainActivity mActivity;
	
	public MyGLSurfaceView(MainActivity context)
    {
        super(context);
        
        mActivity = context;

        setEGLContextClientVersion(2);
        
        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        //mRenderer = new MyGL20Renderer();
        
        // Set the Renderer for drawing on the GLSurfaceView
        //setRenderer(mRenderer);
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent e) 
	{
	    // MotionEvent reports input details from the touch screen
	    // and other input controls. In this case, you are only
	    // interested in events where the touch position changed.

	    //float TOUCH_SCALE_FACTOR = 0.1f;

	    switch (e.getAction()) // & MotionEvent.ACTION_MASK) 
	    {
		    case MotionEvent.ACTION_DOWN:
		    {
		    	/*
		    	int numPointers = e.getPointerCount();
                for (int i = 0; i < numPointers; i++)
                {
            	    float x = e.getX(i);
            	    float y = e.getY(i);
                }
                */
		    	
		    	int count = e.getPointerCount();
		        Point touch;
		        
		        for(int i=0; i<count; i++)
		        {
		        	touch = new Point();
		        	touch.x = (int)e.getX(i);
		        	touch.y = (int)e.getY(i);
		            
		            mTouch.add(touch);
		            mActivity.mGUI.lbuttondown(touch.x, touch.y);
		        }
		    }break;
		    case MotionEvent.ACTION_UP:
		    {
		    	/*
		    	int numPointers = e.getPointerCount();
                for (int i = 0; i < numPointers; i++)
                {
            	    float x = e.getX(i);
            	    float y = e.getY(i);
                }
                */
		    	
		        int count = e.getPointerCount();
		        
		        Point touch;
		        Point t;
		        int j;
		        int nearest;
		        float nearestD;
		        float D;
		        
		        for(int i=0; i<count; i++)
		        {
		        	touch = new Point();
		        	touch.x = (int)e.getX(i);
		        	touch.y = (int)e.getY(i);
		        	
		            nearestD = 999999999.0f;
		            nearest = -1;
		            for(j=0; j<mTouch.size(); j++)
		            {
		                t = mTouch.get(j);
		                D = Math3D.Magnitude2(t.x - touch.x, t.y - touch.y);
		                
		                if(D < nearestD)
		                    nearest = j;
		            }
		            
		            if(nearest >= 0)
		                mTouch.remove( nearest );
		            
		            mActivity.mGUI.lbuttonup(touch.x, touch.y);
		        }
		        
		        mActivity.mGUI.touchcheck();
		    }break;
	        case MotionEvent.ACTION_MOVE:
	        {
	        	/*
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
                */
	        	
	            int count = e.getPointerCount();
	            Point touch;
	            Point t;
	            int j;
	            int nearest;
	            float nearestD;
	            float D;
	            int k;
	            Vector<Integer> modified = new Vector<Integer>();
	            boolean found;
	            
	            for(int i=0; i<count; i++)
	            {
	            	touch = new Point();
		        	touch.x = (int)e.getX(i);
		        	touch.y = (int)e.getY(i);
	                
	                nearestD = 999999999.0f;
	                nearest = -1;
	                for(j=0; j<mTouch.size(); j++)
	                {
	                    found = false;
	                    for(k=0; k<modified.size(); k++)
	                    {
	                        if(modified.get(k).intValue() != j)
	                            continue;
	                        
	                        found = true;
	                        break;
	                    }
	                    if(found)
	                        continue;
	                    
	                    t = mTouch.get(j);
	                    D = Math3D.Magnitude2(t.x - touch.x, t.y - touch.y);
	                    
	                    if(D < nearestD)
	                        nearest = j;
	                }
	                
	                if(nearest >= 0)
	                {
	                    mTouch.get(nearest).x = touch.x;
	                    mTouch.get(nearest).y = touch.y;
	                    modified.add(new Integer(nearest));
	                }
	                else
	                {
	                    mTouch.add(touch);
	                    modified.add(new Integer(mTouch.size()-1));
	                }
	                
	                mActivity.mGUI.mousemove(touch.x, touch.y);
	            }
	            
	            mActivity.mGUI.touchcheck();
	        }break;
	    }
	    
	    return true;
	}
}
