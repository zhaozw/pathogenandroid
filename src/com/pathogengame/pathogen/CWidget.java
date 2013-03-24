package com.pathogengame.pathogen;

import android.content.*;
import android.content.res.AssetManager;
import android.opengl.*;
import android.graphics.*;
import java.io.*;
import android.R.*;
import java.util.*;

public class CWidget 
{
	public static final int IMAGE = 1;
	public static final int BUTTON = 2;
	public static final int TEXT = 3;
	public static final int LINK = 4;
	public static final int DROPDOWN = 5;
	public static final int DPAD = 6;
	public static final int TEXTBOX = 7;
	public static final int TEXTFIELD = 8;
	
	public static final int MAX_OPTIONS_SHOWN = 5;
	
	public MainActivity mActivity;
	
	public int type;
	public float pos[] = new float[4];
	public float tpos[] = new float[2];
	public int tex;
	public int bgtex;
	public int bgovertex;
	public boolean over;
	public boolean ldown;
	public String name;
	public String text;
	public int font;
	public int frametex, filledtex, uptex, downtex;
	public boolean opened;
	public Vector<String> options;
	public int selected;
	public float scroll;
	public boolean mousescroll;
	public int mousedowny;
	public float healthbar;
	public int param;
	public float rgba[] = new float[4];
	public String value;

	public CFuncPtr clickfunc;
	public CFuncPtr clickfunc2;
	public CFuncPtr overfunc;
	public CFuncPtr outfunc;
	public CFuncPtr changefunc;
	public CFuncPtr dpadfunc;
	
	//void (*clickfunc)();
	//void (*clickfunc2)(int p);
	//void (*overfunc)();
	//void (*outfunc)();
	//void (*changefunc)();
    //void (*dpadfunc)(float dx, float dy);
	
	// Initializers
	
	public CWidget(MainActivity act)
	{
		mActivity = act;
	}
	
	public void Image(String filepath, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		type = IMAGE;
		tex = mActivity.CreateTexture(filepath, true);
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		ldown = false;
		rgba[0] = r;
		rgba[1] = g;
		rgba[2] = b;
		rgba[3] = a;
	}
	
	public void Image(int t, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		type = IMAGE;
		tex = t;
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		ldown = false;
		rgba[0] = r;
		rgba[1] = g;
		rgba[2] = b;
		rgba[3] = a;
	}
	
	public void Button(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click, CFuncPtr overf, CFuncPtr out)
	{
		type = BUTTON;
		text = t;
		font = f;
		float length = 0;
		int c;
		for(int i=0; i<t.length(); i++)
		{
			c = (int)t.charAt(i);
			length += mActivity.mFont[f].mGlyph[c].w*mActivity.mRetinaScale;
		}
		tpos[0] = (left+right)/2.0f - length/2.0f;
		tpos[1] = (top+bottom)/2.0f - mActivity.mFont[f].mGHeight*mActivity.mRetinaScale/2.0f;
		over = false;
		ldown = false;
		tex = mActivity.CreateTexture(filepath, true);
		
		//System.out.println("Button( " + t + " = " + tex);
		
		bgtex = mActivity.CreateTexture("gui/buttonbg", true);
		bgovertex = mActivity.CreateTexture("gui/buttonbgover", true);
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		clickfunc = click;
		overfunc = overf;
		outfunc = out;
	}
	
	public void Button(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click2, int parm)
	{
		type = BUTTON;
		text = t;
		font = f;
		float length = 0;
		for(int i=0; i<t.length(); i++)
		{
			int c = (int)t.charAt(i);
			length += mActivity.mFont[f].mGlyph[c].w;
		}
		tpos[0] = (left+right)/2.0f - length/2.0f;
		tpos[1] = (top+bottom)/2.0f - mActivity.mFont[f].mGHeight/2.0f;
		over = false;
		ldown = false;
		tex = mActivity.CreateTexture(filepath, true);
		bgtex = mActivity.CreateTexture("gui/buttonbg", true);
		bgovertex = mActivity.CreateTexture("gui/buttonbgover", true);
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		clickfunc2 = click2;
		param = parm;
	}
	
	public void Text(String t, int f, float left, float top)
	{
		type = TEXT;
		name = "";
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		ldown = false;
	}
	
	public void Text(String n, String t, int f, float left, float top)
	{
		type = TEXT;
		name = n;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		ldown = false;
	}
	
	public void Link(String t, int f, float left, float top, CFuncPtr click)
	{
		type = LINK;
		over = false;
		ldown = false;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		clickfunc = click;
	}
	
	public void DropDown(String n, int f, float left, float top, float width, CFuncPtr change)
	{
		type = DROPDOWN;
		name = n;
		font = f;
		opened = false;
		selected = 0;
		scroll = 0;
		mousescroll = false;
		ldown = false;
		changefunc = change;
		pos[0] = left;
		pos[1] = top;
		pos[2] = left+width;
		pos[3] = top+mActivity.mFont[f].mGHeight;
		frametex = mActivity.CreateTexture("gui/frame", true);
		filledtex = mActivity.CreateTexture("gui/filled", true);
		uptex = mActivity.CreateTexture("gui/up", true);
		downtex = mActivity.CreateTexture("gui/down", true);
	}
	
	public void DPad(String n, String texf, float left, float top, float right, float bottom, CFuncPtr dpad)
	{
		type = DPAD;
		name = n;
		tex = mActivity.CreateTexture(texf, true);
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		dpadfunc = dpad;
	}
	
	public void TextBox(String n, String t, int f, float left, float top, float right, float bottom)
	{
		type = TEXTBOX;
		name = n;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		pos[2] = right;
		pos[3] = bottom;
		ldown = false;
	}
	
	// Drawers
	
	public void Image_draw()
	{
		CShader s = mActivity.mShader[CShader.ORTHO];
    	GLES20.glUniform4f(s.slot[CShader.COLOR], rgba[0], rgba[1], rgba[2], rgba[3]);
		mActivity.mGUI.DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
	}
	
	public void Button_draw()
	{
		mActivity.mGUI.DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
		
		CFont f = mActivity.mGUI.mActivity.mFont[font];
		f.DrawShadowed((int)tpos[0], (int)tpos[1], text, null);
	}

	public void Text_draw()
	{
		CFont f = mActivity.mFont[font];
		f.DrawShadowed((int)pos[0], (int)pos[1], text, null);
	}
	
	public void Link_draw()
	{
		CShader s = mActivity.mShader[CShader.ORTHO];
		
		if(!over)
	        GLES20.glUniform4f(s.slot[CShader.COLOR], 0.8f, 0.8f, 0.8f, 1);

        CFont f = mActivity.mFont[font];
        f.DrawShadowed((int)pos[0], (int)pos[1], text, null);
        
        GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	}

	public void DropDown_draw()
	{
		CShader s = mActivity.mGUI.mActivity.mShader[CShader.ORTHO];
        GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
        
        mActivity.mGUI.DrawImage(frametex, pos[0], pos[1]+5, pos[2], pos[3]+5);
        
        if(!opened)
        	mActivity.mGUI.DrawImage(downtex, pos[2]-square(), pos[1]+5, pos[2], pos[1]+5+square());
        
		CFont f = mActivity.mGUI.mActivity.mFont[font];
		String o = options.get(selected);
		f.DrawShadowed((int)pos[0]+30, (int)pos[1], o, null);
	}
	
	public void DropDown_draw2()
	{
		if(!opened)
			return;
		
		CFont f = mActivity.mFont[font];
		
		mActivity.mGUI.DrawImage(frametex, pos[0], pos[1]+5+f.mGHeight, pos[2], pos[3]+5+f.mGHeight*rowsshown());
		mActivity.mGUI.DrawImage(frametex, pos[2]-square(), pos[1]+5, pos[2], pos[3]+5+f.mGHeight*rowsshown());
		mActivity.mGUI.DrawImage(uptex, pos[2]-square(), pos[1]+5, pos[2], pos[1]+5+square());
		mActivity.mGUI.DrawImage(downtex, pos[2]-square(), pos[3]+5+f.mGHeight*rowsshown()-square(), pos[2], pos[3]+5+f.mGHeight*rowsshown());
		mActivity.mGUI.DrawImage(filledtex, pos[2]-square(), pos[3]+5+scrollspace()*topratio(), pos[2], pos[3]+5+scrollspace()*bottomratio());
		
		String o;
		
		for(int i=(int)scroll; i<(int)scroll+rowsshown(); i++)
		{
			o = options.get(i);
			f.DrawShadowed((int)pos[0]+30, (int)(pos[3]+f.mGHeight*(i-(int)scroll)), o, null);
		}
	}
	
	public void DPad_draw()
	{
		mActivity.mGUI.DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
	}
	
	public void TextBox_draw()
	{
		float width = pos[2] - pos[0];
		float height = pos[3] - pos[1];
		
		CFont f = mActivity.mFont[font];
		float color[] = {1, 1, 1, 1};
		f.DrawBoxedShad((int)pos[0], (int)pos[1], (int)width, (int)height, text, color);
	}

	// L button up
	
	public boolean Button_lbuttonup(float x, float y)
	{
		if(over && ldown)
		{
			if(clickfunc != null)
				clickfunc.func();
			
			if(clickfunc2 != null)
				clickfunc2.func(param);
			
			ldown = false;
			
			return true;	// intercept mouse event
		}
		
		return false;
	}

	public boolean Link_lbuttonup(float x, float y)
	{
		if(over && ldown)
		{
			if(clickfunc != null)
				clickfunc.func();
			
			ldown = false;
			
			return true;	// intercept mouse event
		}
		
		return false;
	}
	
	public boolean DropDown_lbuttonup(float x, float y)
	{
		if(mousescroll)
		{
			mousescroll = false;
			return true;	// intercept mouse event
		}
		
		return false;
	}

	// Touch frame
	
	boolean DPad_touchframe(float x, float y)
	{
	    if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
	    {
	        if(dpadfunc != null)
	        {
	            float dx = x - (pos[0] + pos[2])/2.0f;
	            float dy = y - (pos[1] + pos[3])/2.0f;
	            dpadfunc.func(dx, dy);
	        }
	        
	        return true;
	    }
	    
	    return false;
	}
	
	// L button down
	
	public boolean Button_lbuttondown(float x, float y)
	{
		Button_mousemove(x, y);
		
		if(over)
		{
			ldown = true;
			return true;	// intercept mouse event
		}
		
		return false;
	}
	
	public boolean Link_lbuttondown(float x, float y)
	{
		Link_mousemove(x, y);
		
		if(over)
		{
			ldown = true;
			return true;	// intercept mouse event
		}
		
		return false;
	}
	
	public boolean DropDown_lbuttondown(float x, float y)
	{
		DropDown_mousemove(x, y);
		
		if(opened)
		{
			CFont f = mActivity.mFont[font];
			
			for(int i=(int)scroll; i<(int)scroll+rowsshown(); i++)
			{
				// list item?
				if(x >= pos[0] && x <= pos[2]-square() && y >= pos[3]+f.mGHeight*(i-(int)scroll)
						&& y <= pos[3]+f.mGHeight*(i-(int)scroll+1))
				{
					selected = i;
					opened = false;
					if(changefunc != null)
						changefunc.func();
					return true;	// intercept mouse event
				}
			}
			
			// scroll bar?
			if(x >= pos[2]-square() && y >= pos[3]+5+scrollspace()*topratio() && x <= pos[2]
					&& y <= pos[3]+5+scrollspace()*bottomratio())
			{
				mousescroll = true;
				mousedowny = (int)y;
				return true;	// intercept mouse event
			}
			
			// up button?
			if(x >= pos[2]-square() && y >= pos[1]+5 && x <= pos[2] && y <= pos[3]+5)
			{
				if(rowsshown() < MAX_OPTIONS_SHOWN)
				{
					opened = false;
					return true;
				}
				
				scroll--;
				if(scroll < 0)
					scroll = 0;
				
				return true;
			}
			
			// down button?
			if(x >= pos[2]-square() && y >= pos[3]+5+scrollspace() && x <= pos[2] && y <= pos[3]+5+scrollspace()+f.mGHeight)
			{
				scroll++;
				if(scroll+rowsshown() > options.size())
					scroll = options.size() - rowsshown();
				
				return true;
			}
			
			opened = false;
			return true;
		}
		
		if(x >= pos[2]-square() && y >= pos[1]+5 && x <= pos[2] && y <= pos[1]+5+square())
		{
			opened = true;
			return true;
		}
		
		return false;
	}
	
	// Mouse move
	
	public void Button_mousemove(float x, float y)
	{
		if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
		{
			if(overfunc != null)
				overfunc.func();
			
			over = true;
		}
		else
		{
			if(over && outfunc != null)
				outfunc.func();
			
			over = false;
		}
	}
	
	public void Link_mousemove(float x, float y)
	{
		CFont f = mActivity.mFont[font];
		
		if(x >= pos[0] && y >= pos[1] &&
				x <= pos[0]+text.length()*f.mGHeight/2 &&
				y <= pos[1]+f.mGHeight)
			over = true;
		else
			over = false;
	}
	
	public void DropDown_mousemove(float x, float y)
	{
		if(!mousescroll)
			return;
		
		int dy = (int)y - mousedowny;
		int topspace = (int)(topratio() * scrollspace());
		int bottomspace = (int)(scrollspace() - bottomratio() * scrollspace());
		
		if(dy < 0 && Math.abs(dy) > topspace)
		{
			scroll = 0;
			return;
		}
		else if(dy > 0 && dy > bottomspace)
		{
			scroll = options.size() - rowsshown();
			if(scroll < 0)
				scroll = 0;
			
			return;
		}
		
		float topy = pos[3]+5+scrollspace()*topratio();
		float newtopy = topy + dy;
		
		scroll = (newtopy - pos[3] - 5)*(float)(options.size()-1)/scrollspace();
		mousedowny = (int)y;
	}
	
	/*// Mouse update
	
	public boolean DPad_mouseupdate(float x, float y)
	{
		if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
		{
			if(dpadfunc != null)
			{
				float dx = x - (pos[0] + pos[2])/2.0f;
				float dy = y - (pos[1] + pos[3])/2.0f;
				dpadfunc.func(dx, dy);
			}
			
			return true;
		}
		
		return false;
	}*/
	
	// Touch check
	
	void Button_touchcheck()
	{
	    Point touch;
	    
	    if(!ldown)
	        return;
	    
	    boolean found = false;
	    
	    for(int i=0; i<mActivity.mGLView.mTouch.size(); i++)
	    {
	        touch = mActivity.mGLView.mTouch.get(i);
	        
	        if(touch.x >= pos[0] && touch.x <= pos[2] && touch.y >= pos[1] && touch.y <= pos[3])
	        {
	            found = true;
	            break;
	        }
	    }
	    
	    if(found)
	        return;
	    
	    ldown = false;
	    over = false;
	    
	    if(outfunc != null)
	        outfunc.func();
	}
	
	// Misc.
	
	public int rowsshown()
	{
		int rows = MAX_OPTIONS_SHOWN;
		if(rows > options.size())
			rows = options.size();
	
		return rows;
	}
	
	public int square()
	{
		CFont f = mActivity.mFont[font];
		return (int)f.mGHeight;
	}
	
	public float topratio()
	{
		return scroll / (float)(options.size()-1);
	}
	
	public float bottomratio()
	{
		return (scroll+rowsshown()-1) / (float)(options.size()-1);
	}
	
	public float scrollspace()
	{
		CFont f = mActivity.mFont[font];
		return f.mGHeight*(rowsshown()-1);
	}
	
	// Common
	
	void touchcheck()
    {
        switch(type)
        {
            case BUTTON: Button_touchcheck(); break;
            default: break;
        }
    }
	
	boolean touchframe(float x, float y)
    {
        switch(type)
        {
            case DPAD: return DPad_touchframe(x, y);
            default: return false;
        }
    }
	
	public boolean lbuttonup(float x, float y)
	{
		switch(type)
		{
		case BUTTON: return Button_lbuttonup(x, y);
		case LINK: return Link_lbuttonup(x, y);
		case DROPDOWN: return DropDown_lbuttonup(x, y);
		default: return false;
		}
	}

	public boolean lbuttondown(float x, float y)
	{
		switch(type)
		{
		case BUTTON: return Button_lbuttondown(x, y);
        case LINK: return Link_lbuttondown(x, y);
        case DROPDOWN: return DropDown_lbuttondown(x, y);
        default: return false;
		}
	}
	
	public void mousemove(float x, float y)
	{
		switch(type)
		{
		case BUTTON: Button_mousemove(x, y); break;
		case LINK: Link_mousemove(x, y); break;
		case DROPDOWN: DropDown_mousemove(x, y); break;
		default: break;
		}
	}
	
	/*
	public boolean mouseupdate(float x, float y)
	{
		switch(type)
		{
		case DPAD: return DPad_mouseupdate(x, y);
		default: return false;
		}
	}*/
	
	public void draw()
	{
		switch(type)
		{
		case IMAGE: Image_draw(); break;
		case BUTTON: Button_draw(); break;
		case TEXT: Text_draw(); break;
		case LINK: Link_draw(); break;
		case DROPDOWN: DropDown_draw(); break;
		case DPAD: DPad_draw(); break;
		case TEXTBOX: TextBox_draw(); break;
		default: break;
		}
	}
	
	public void draw2()
	{
		switch(type)
		{
		case DROPDOWN: DropDown_draw2(); break;
		default: break;
		}
	}
}
