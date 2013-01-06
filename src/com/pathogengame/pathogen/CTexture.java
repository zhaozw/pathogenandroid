package com.pathogengame.pathogen;

import android.content.*;
import android.content.res.AssetManager;
import android.opengl.*;
import android.graphics.*;
import java.io.*;
import android.R.*;

public class CTexture 
{
	boolean on = false;
	int tex[] = new int[1];
	String file;
	
	public int Load(final Context context, final String texfile)
	{
	    GLES20.glGenTextures(1, tex, 0);
	 
	    if(tex[0] != 0)
	    {
	    	AssetManager am = context.getAssets();
	        //final BitmapFactory.Options options = new BitmapFactory.Options();
	        //options.inScaled = false;   // No pre-scaling
	 
	        // Read in the resource
	        final Bitmap bitmap; // = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
	 
	        InputStream is = null;
	        
	        try 
	        {
	        	is = am.open(texfile);
	        } 
	        catch (final IOException e) 
	        {
	            e.printStackTrace();
	        }
	        
	        bitmap = BitmapFactory.decodeStream(is);
	        
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
	 
	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	 
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	 
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	    }
	 
	   //if(tex[0] == 0)
	   //   throw new RuntimeException("Error loading texture.");
	    
	    System.out.println(texfile);
	 
	    return tex[0];
	}
}
