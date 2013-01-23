package com.pathogengame.pathogen;

import java.nio.*;
import android.opengl.*;

class Triangle 
{
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCBuffer;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    static float triangleCoords[] = 
    { // in counterclockwise order:
         0.0f,  0.622008459f, 0.0f,   // top
         -0.5f, -0.311004243f, 0.0f,   // bottom left
         0.5f, -0.311004243f, 0.0f    // bottom right
    };
    
    final float[] texCoords =
		{												
				// Front face
				0.5f, 0.0f, 
				0.0f, 1.0f,				
				1.0f, 1.0f
		};

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f ,
    		0.63671875f, 0.76953125f, 0.22265625f, 1.0f ,
    		0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	
	private final String vertexShaderCode =
			"attribute vec2 a_TexCoordinate;" +
			"varying vec2 v_TexCoordinate;" +
		    "attribute vec4 vPosition;" +
		    "uniform mat4 uMVPMatrix;" +
		    "void main() " +
		    "{" +
		    "  v_TexCoordinate = a_TexCoordinate;" +
		    "  gl_Position = uMVPMatrix * vPosition;" +
		    "}";

	private final String fragmentShaderCode =
		    "precision mediump float;" +
			"uniform sampler2D u_Texture;" +
			"varying vec2 v_TexCoordinate;" +
		    "uniform vec4 vColor;" +
		    "void main() " +
		    "{" +
		    "  gl_FragColor = vColor * texture2D(u_Texture, v_TexCoordinate);" +
		    //"  gl_FragColor = vColor;" +
		    "}";
	
	//private int mProgram;
	//private int mPositionHandle;
	//private int mColorHandle;
	//private int mMVPMatrixHandle;
	public int mTextureDataHandle;

    public static int loadShader(int type, String shaderCode)
    {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    public void draw(CShader s) 
    {
        // Add program to OpenGL ES environment
        //GLES20.glUseProgram(mProgram);
        
        //int mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        //int mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        //mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        //GLES20.glUniformMatrix4fv(s.slot[CShader.MVPMAT], 1, false, mvpMatrix, 0);
        
        // get handle to vertex shader's vPosition member
        //mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        //GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        //GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
        
        texCBuffer.position(0);
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, 0, texCBuffer);

        // get handle to fragment shader's vColor member
        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(s.slot[CShader.COLOR], 1, color, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);

        // Disable vertex array
        //GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
    
    public Triangle() 
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //bb.order(ByteOrder.LITTLE_ENDIAN);
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);
        
        ByteBuffer bb2 = ByteBuffer.allocateDirect(texCoords.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        //bb2.order(ByteOrder.LITTLE_ENDIAN);
        texCBuffer = bb2.asFloatBuffer();
        texCBuffer.put(texCoords);
        texCBuffer.position(0);
        
        /*
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL ES program executables
        */
    }
}
