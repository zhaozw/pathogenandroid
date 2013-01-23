package com.pathogengame.pathogen;

public class CFont 
{
	public static final int MSGOTHIC16 = 0;
	public static final int MSGOTHIC16B = 1;
	public static final int MSUIGOTHIC16 = 2;
	public static final int FONTS = 3;
	
	public static final int MAX_CHARS = 256;

	MainActivity mActivity;
	
	public CGlyph mGlyph[] = new CGlyph[MAX_CHARS];
	public int tex = 0;
	public float width = 2;		//image width
	public float height = 2;	//image height
	public float gheight = 2;	//glyph height
	
	CFont(MainActivity act)
	{
		mActivity = act;
		
		for(int i=0; i<MAX_CHARS; i++)
			mGlyph[i] = new CGlyph();
	}
	
	public void DrawShadowed(float left, float top, String text)
	{
		// TO DO
	}
	
	public void DrawBoxedShad(float left, float top, float width, float height, String text, float color[])
	{
		// TO DO
	}
	
	// TO DO
}
