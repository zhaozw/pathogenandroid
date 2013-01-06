package com.pathogengame.pathogen;

import android.opengl.GLES20;
import android.content.*;
import android.content.res.AssetManager;
import android.opengl.*;
import android.graphics.*;
import java.io.*;

import android.R.*;

public class CShader 
{
	public int mProgram;
	public int mVertS;
	public int mFragS;
	
	public static final int TEXTURE = 0;
	public static final int TEXCOORD = 1;
	public static final int POSITION = 2;
	public static final int COLOR = 3;
	//public static final int MVPMAT = 4;
	public static final int PROJMAT = 4;
	public static final int MODELMAT = 5;
	public static final int VIEWMAT = 6;
	public static final int TEXCOORD2 = 7;
	public static final int TEXTURE2 = 8;
	public static final int WIDTH = 9;
	public static final int HEIGHT = 10;
	public static final int SLOTS = 11;
	public int slot[] = new int[ SLOTS ];
	
	//public static final int SHADER = 0;
	public static final int BILLBOARD = 0;
	public static final int MAP = 1;
	public static final int MODEL = 2;
	public static final int ORTHO = 3;
	public static final int SKY = 4;
	public static final int SHADERS = 5;
	
	
	CShader(Context context, String vertF, String fragF)
	{
    	AssetManager am = context.getAssets();
    	
    	String vertexShaderCode = "";
    	String fragmentShaderCode = "";
    	
    	InputStream is = null;
        
        try 
        {
        	is = am.open("shaders/" + vertF);
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }
		
        vertexShaderCode = textF(is);

        try 
        {
        	is = am.open("shaders/" + fragF);
        }
        catch (final IOException e) 
        {
            e.printStackTrace();
        }

        fragmentShaderCode = textF(is);
        
		mVertS = CompileShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		mFragS = CompileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, mVertS);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, mFragS); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
/*
        slot[TEXTURE] = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        slot[TEXCOORD] = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
        slot[POSITION] = GLES20.glGetAttribLocation(mProgram, "vPosition");
        slot[COLOR] = GLES20.glGetUniformLocation(mProgram, "vColor");
        //slot[MVPMAT] = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
  */      

        slot[TEXTURE] = GLES20.glGetUniformLocation(mProgram, "Texture");
        slot[TEXCOORD] = GLES20.glGetAttribLocation(mProgram, "TexCoordIn");
        slot[POSITION] = GLES20.glGetAttribLocation(mProgram, "Position");
        slot[COLOR] = GLES20.glGetUniformLocation(mProgram, "Color");
        slot[PROJMAT] = GLES20.glGetUniformLocation(mProgram, "Projection");
        slot[MODELMAT] = GLES20.glGetUniformLocation(mProgram, "Model");
        slot[VIEWMAT] = GLES20.glGetUniformLocation(mProgram, "View");
        slot[TEXCOORD2] = GLES20.glGetAttribLocation(mProgram, "TexCoordIn2");
        slot[TEXTURE2] = GLES20.glGetUniformLocation(mProgram, "Texture2");
        slot[WIDTH] = GLES20.glGetUniformLocation(mProgram, "Width");
        slot[HEIGHT] = GLES20.glGetUniformLocation(mProgram, "Height");
	}
	
	public static String textF(InputStream is) 
	{
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public static int CompileShader(int type, String shaderCode)
    {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
	
	void Use()
	{
		GLES20.glUseProgram(mProgram);
		GLES20.glEnableVertexAttribArray(slot[POSITION]);
        GLES20.glEnableVertexAttribArray(slot[TEXCOORD]);
    }
}
