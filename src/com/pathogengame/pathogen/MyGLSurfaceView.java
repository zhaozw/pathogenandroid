package com.pathogengame.pathogen;

import android.opengl.*;
import android.content.Context;

public class MyGLSurfaceView extends GLSurfaceView 
{
	public MyGLSurfaceView(Context context)
    {
        super(context);

        setEGLContextClientVersion(2);
        
        // Render the view only when there is a change in the drawing data
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(new MyGL20Renderer());
    }
}
