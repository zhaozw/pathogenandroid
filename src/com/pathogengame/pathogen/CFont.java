package com.pathogengame.pathogen;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.opengl.GLES20;

public class CFont 
{
	public static final int MSGOTHIC16 = 0;
	public static final int MSGOTHIC16B = 1;
	public static final int MSUIGOTHIC16 = 2;
	public static final int FONTS = 3;
	
	public static final int MAX_CHARS = 256;

	MainActivity mActivity;
	
	public CGlyph mGlyph[] = new CGlyph[MAX_CHARS];
	public int mTex = 0;
	public float mWidth = 2;		//image width
	public float mHeight = 2;	//image height
	public float mGHeight = 2;	//glyph height
	
	int mJ, mW, mH, mX, mY;
	
	void FSub(String str)
	{
		mJ = 0;
	    int i;
	    char c = '\0';
	    
	    for(i=0; i<str.length(); i++)
	    {
	    	c = str.charAt(i);
	        if(c != ' ' && c != '\n' && c != '\r' && c != '\t')
	            break;
	    }
	    
	    if(c == '"')
	    {
	        i++;    //skip over the "
	    	c = str.charAt(i);
	        mJ = (int)c;
	        i++;    //skip over c
	        i++;    //skip over the "
	    }
	    else
	    {
	        String hexstr = "";
	        
	        //if(str[i] == ' ')
	        //   i++;
	        
	        for(; i<str.length(); i++)
	        {
		    	c = str.charAt(i);
	            if(c == ',')
	                break;
	            
	            hexstr += c;
	        }
	        
	        mJ = Integer.parseInt(hexstr.trim(), 16);
	    }
	    
	    i++;    //skip over the ,
	    
	    String substr[] = new String[4];
	    
	    for(int k=0; k<4; k++)
	    {
	        substr[k] = "";
	        
	        for(; i<str.length(); i++)
	        {
		    	c = str.charAt(i);
	            if(c == ',')
	            {
	                i++;
	                break;
	            }
	            
	            substr[k] += c;
	        }
	    }
	    
	    mX = Integer.parseInt(substr[0], 10);
	    mY = Integer.parseInt(substr[1], 10);
	    mW = Integer.parseInt(substr[2], 10);
	    mH = Integer.parseInt(substr[3], 10);
	}
	
	public CFont(MainActivity act, String filepath)
	{
		mActivity = act;
		
		for(int i=0; i<MAX_CHARS; i++)
			mGlyph[i] = new CGlyph();
		
		mTex = mActivity.CreateTexture(filepath, true);
		mWidth = mActivity.mTexWidth;
		mHeight = mActivity.mTexHeight;

    	String file = CFile.ReadText(filepath + ".fnt", mActivity);
    	
        int i;
        String substr;
        char c;
        
        for(i = 0; i<file.length(); i++)
        {
            substr = "";
            
            //skip whitespace
            for(; i<file.length(); i++)
            {
            	c = file.charAt(i);
                if(c != ' ' && c != '\n' && c != '\r' && c != '\t')
                    break;
            }
            
            //write directive
            for(; i<file.length(); i++)
            {
            	c = file.charAt(i);
                if(c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '=')
                    break;
                
                substr += c;
            }
            
            i++;
            
            if(substr.equals("[HGEFONT]"))
                continue;
            else if(substr.equals("Bitmap"))
            {
                //skip the file name
                for(; i<file.length(); i++)
                {
                	c = file.charAt(i);
                    if(c == ' ' || c == '\n' || c == '\r' || c == '\t')
                        break;
                }
                continue;
            }
            else if(substr.equals("Char"))
            {
                substr = "";
                
                for(; i<file.length(); i++)
                {
                	c = file.charAt(i);
                    if(c == '\n' || c == '\r')
                        break;
                    
                    substr += c;
                }
                
                FSub(substr);
                
                CGlyph g = mGlyph[mJ];
                g.x = mX;
                g.y = mY;
                g.w = mW;
                g.h = mH;
            }
        }

        mGHeight = mGlyph[(int)'A'].h;
        mGlyph[(int)' '].w = mGlyph[(int)'A'].w;
        mGlyph[(int)' '].x = (int)mWidth - mGlyph[(int)' '].w;
        mGlyph[(int)' '].y = (int)mHeight - 1 - (int)mGHeight;
	}
	
	void DrawGlyph(float left, float top, float right, float bottom, float texleft, float textop, float texright, float texbottom)
	{
		CShader s = mActivity.mShader[CShader.ORTHO];

	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTex);
	    GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	    
	    float vertices[] =
	    {
	        //posx, posy    texx, texy
	        left, top,0,          texleft, textop,
	        right, top,0,         texright, textop,
	        right, bottom,0,      texright, texbottom,
	        
	        right, bottom,0,      texright, texbottom,
	        left, bottom,0,       texleft, texbottom,
	        left, top,0,          texleft, textop
	    };

	    FloatBuffer vertexBuffer;
	    FloatBuffer texCBuffer;
	    
	    ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //bb.order(ByteOrder.LITTLE_ENDIAN);
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        
        ByteBuffer bb2 = ByteBuffer.allocateDirect(vertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        //bb2.order(ByteOrder.LITTLE_ENDIAN);
        texCBuffer = bb2.asFloatBuffer();
        texCBuffer.put(vertices);
        texCBuffer.position(3);
	    
	    GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT,  false, 4*5, vertexBuffer);
	    GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2,  GLES20.GL_FLOAT,  false, 4*5, texCBuffer);
	    
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}
	
	public void DrawText(int x, int y, String str)
	{
		int size = str.length();
		
		CGlyph g;
	    int left, top, right, bottom;
	    float texleft, textop, texright, texbottom;
	    
	    for(int i=0; i<size; i++)
	    {
	        g = mGlyph[(int)str.charAt(i)];
	        left = x;
	        right = (int)(x + g.w * mActivity.mRetinaScale);
	        top = y;
	        bottom = (int)(y + g.h * mActivity.mRetinaScale);
	        texleft = (float)g.x / mWidth;
	        texright = (float)(g.x + g.w) / mWidth;
	        textop = (float)g.y / mHeight;
	        texbottom = (float)(g.y + g.h) / mHeight;
	        DrawGlyph(left, top, right, bottom, texleft, textop, texright, texbottom);
	        x += (int)(g.w * mActivity.mRetinaScale);
	    }
	}
	
	public void DrawShadowed(int x, int y, String str, float color[])
	{
		CShader s = mActivity.mShader[CShader.ORTHO];
		GLES20.glUniform4f(s.slot[CShader.COLOR], 0, 0, 0, 1);
	    DrawText(x+(int)(1.0f*mActivity.mRetinaScale), y+(int)(1.0f*mActivity.mRetinaScale), str);
	    
	    if(color == null)
	    	GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	    else
	    	GLES20.glUniform4f(s.slot[CShader.COLOR], color[0], color[1], color[2], color[3]);
	    DrawText(x, y, str);
	    
	    GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	}
	
	public void DrawBoxedShad(int startx, int starty, int width, int height, String str, float color[])
	{
		int size = str.length();
	    
	    CGlyph g;
	    CGlyph g2;
	    int left, top, right, bottom;
	    float texleft, textop, texright, texbottom;
	    int x = startx + (int)(1.0f*mActivity.mRetinaScale);
	    int y = starty + (int)(1.0f*mActivity.mRetinaScale);
	    int nexti = 0;  //next [i] to skip line
	    int lastspace;
	    int j, x1;

		CShader s = mActivity.mShader[CShader.ORTHO];
		GLES20.glUniform4f(s.slot[CShader.COLOR], 0, 0, 0, 1);
	    
	    for(int i=0; i<size; i++)
	    {
	        g = mGlyph[(int)str.charAt(i)];
	        
	        if(i == nexti)
	        {
	            if(nexti != 0)
	            {
	                x = startx+(int)(1.0f*mActivity.mRetinaScale);
	                y += (int)(mGHeight * mActivity.mRetinaScale);
	            }
	            
	            lastspace = -1;
	            x1 = startx;
	            
	            for(j=i; j<size; j++)
	            {
	                g2 = mGlyph[(int)str.charAt(j)];
	                x1 += (int)(g2.w * mActivity.mRetinaScale);
	                
	                if(str.charAt(j) == ' ' || str.charAt(j) == '\t')
	                    lastspace = j;
	                
	                if(x1 > width)
	                {
	                    if(lastspace < 0)
	                        continue;
	                    
	                    nexti = lastspace+1;
	                    break;
	                }
	            }
	        }
	        
	        left = x;
	        right = x + (int)(g.w * mActivity.mRetinaScale);
	        top = y;
	        bottom = y + (int)(g.h * mActivity.mRetinaScale);
	        texleft = (float)g.x / mWidth;
	        texright = (float)(g.x + g.w) / mWidth;
	        textop = (float)g.y / mHeight;
	        texbottom = (float)(g.y + g.h) / mHeight;
	        DrawGlyph(left, top, right, bottom, texleft, textop, texright, texbottom);
	        x += (int)(g.w * mActivity.mRetinaScale);
	    }
	    
	    nexti = 0;
	    x = startx;
	    y = starty;
	    
	    if(color == null)
	    	GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	    else
	    	GLES20.glUniform4f(s.slot[CShader.COLOR], color[0], color[1], color[2], color[3]);

	    for(int i=0; i<size; i++)
	    {
	        g = mGlyph[(int)str.charAt(i)];
	        
	        if(i == nexti)
	        {
	            if(nexti != 0)
	            {
	                x = startx;
	                y += (int)(mGHeight * mActivity.mRetinaScale);
	            }
	            
	            lastspace = -1;
	            x1 = startx;
	            
	            for(j=i; j<size; j++)
	            {
	                g2 = mGlyph[(int)str.charAt(j)];
	                x1 += (int)(g2.w * mActivity.mRetinaScale);
	                
	                if(str.charAt(j) == ' ' || str.charAt(j) == '\t')
	                    lastspace = j;
	                
	                if(x1 > width)
	                {
	                    if(lastspace < 0)
	                        continue;
	                    
	                    nexti = lastspace+1;
	                    break;
	                }
	            }
	        }
	        
	        left = x;
	        right = x + (int)(g.w * mActivity.mRetinaScale);
	        top = y;
	        bottom = y + (int)(g.h * mActivity.mRetinaScale);
	        texleft = (float)g.x / mWidth;
	        texright = (float)(g.x + g.w) / mWidth;
	        textop = (float)g.y / mHeight;
	        texbottom = (float)(g.y + g.h) / mHeight;
	        DrawGlyph(left, top, right, bottom, texleft, textop, texright, texbottom);
	        x += (int)(g.w * mActivity.mRetinaScale);
	    }

	    GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	}
}
