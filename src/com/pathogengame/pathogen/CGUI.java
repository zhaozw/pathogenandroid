package com.pathogengame.pathogen;

import android.content.*;
import java.util.*;
import android.opengl.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

public class CGUI 
{
	public Vector<CView> mView = new Vector();
	
	public CFuncPtr mousemovefunc = null;
	public CFuncPtr mouseupdfunc = null;
	public CFuncPtr lbuttondownfunc = null;
	public CFuncPtr lbuttonupfunc = null;
	
	MainActivity mActivity;
	
	public CGUI(MainActivity act)
	{
		mActivity = act;
		
		float halign = mActivity.mWidth/2 - 80*mActivity.mRetinaScale;
	    float fieldwidth = 280*mActivity.mRetinaScale;
	    float buttonw = 120*mActivity.mRetinaScale;
	    float centerw = mActivity.mWidth/2;
		
		AddView("main", 0);
		AddImage("gui/bg", 0.0f, 0.0f, (float)mActivity.mWidth, (float)mActivity.mHeight, 1.0f, 1.0f, 1.0f, 1.0f);
		AddButton("gui/buttonbg", "Play Story", CFont.MSGOTHIC16, centerw-buttonw/2, 100.0f*mActivity.mRetinaScale, centerw+buttonw/2, 130.0f*mActivity.mRetinaScale, new Click_GoToStory(mActivity), null, null);
		AddButton("gui/buttonbg", "Play Online", CFont.MSGOTHIC16, centerw-buttonw/2, 140.0f*mActivity.mRetinaScale, centerw+buttonw/2, 170.0f*mActivity.mRetinaScale, new Click_GoToOnline(mActivity), null, null);
		AddButton("gui/buttonbg", "Credits", CFont.MSGOTHIC16, centerw-buttonw/2, 180.0f*mActivity.mRetinaScale, centerw+buttonw/2, 210.0f*mActivity.mRetinaScale, new Click_GoToCredits(mActivity), null, null);
		
		AddView("login", 0);
		AddImage("gui/bg", 0.0f, 0.0f, (float)mActivity.mWidth, (float)mActivity.mHeight, 1.0f, 1.0f, 1.0f, 1.0f);
		
		OpenSoleView("main", 0);
	}
	
	public CView getview(String n)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n))
				return v;
		}
		
		return new CView(mActivity);
	}
	
	public void assignLButton(CFuncPtr down, CFuncPtr up)
	{
		lbuttondownfunc = down;
		lbuttonupfunc = up;
	}
	
	public void assignMouseMove(CFuncPtr mouse)
	{
		mousemovefunc = mouse;
	}
	
	public void assignMouseUpdate(CFuncPtr mouseu)
	{
		mouseupdfunc = mouseu;
	}
	
	public void lbuttondown(float x, float y)
	{
		CView v;
		
		for(int i=mView.size()-1; i>=0; i--)
		{
			v = mView.get(i);
			if(v.opened && v.lbuttondown(x, y))
				return;
		}
		
		if(lbuttondownfunc != null)
			lbuttondownfunc.func(x, y);
	}
	
	public void lbuttonup(float x, float y)
	{
		CView v;
		
		for(int i=mView.size()-1; i>=0; i--)
		{
			v = mView.get(i);
			if(v.opened && v.lbuttonup(x, y))
				return;
		}
		
		if(lbuttonupfunc != null)
			lbuttonupfunc.func(x, y);
	}
	
	public void mousemove(float x, float y)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				v.mousemove(x, y);
			
			if(mousemovefunc != null)
				mousemovefunc.func(x, y);
		}
	}
	
	public void mouseupdate(float x, float y)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				if(v.mouseupdate(x, y))
					return;
			
			if(mouseupdfunc != null)
				mouseupdfunc.func(x, y);
		}
	}
	
	public void draw()
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				v.draw();
		}
	}
	
	public void AddView(String n, int page)
	{
		CView v = new CView(mActivity, n, page);
		mView.add(v);
	}
	
	public void AddImage(String filepath, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		int last = mView.size()-1;
		CWidget image = new CWidget(mActivity);
		image.Image(filepath, left, top, right, bottom, r, g, b, a);
		CView v = mView.get(last);
		v.mWidget.add(image);
	}
	
	public void AddImage(CView v, int tex, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		CWidget image = new CWidget(mActivity);
		image.Image(tex, left, top, right, bottom, r, g, b, a);
		v.mWidget.add(image);
	}
	
	public void AddLink(String t, int f, float left, float top, CFuncPtr click)
	{
		int last = mView.size()-1;
		CWidget link = new CWidget(mActivity);
		link.Link(t, f, left, top, click);
		CView v = mView.get(last);
		v.mWidget.add(link);
	}
	
	public void AddButton(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click, CFuncPtr overf, CFuncPtr out)
	{
		int last = mView.size()-1;
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
		CView v = mView.get(last);
		v.mWidget.add(button);
	}
	
	public void AddButton(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click2, int parm)
	{
		int last = mView.size()-1;
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click2, parm);
		CView v = mView.get(last);
		v.mWidget.add(button);
	}

	public void AddButton(CView v, String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click, CFuncPtr overf, CFuncPtr out)
	{
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
		v.mWidget.add(button);
	}
	
	public void AddText(String n, String t, int f, float left, float top)
	{
		int last = mView.size()-1;
		CWidget text = new CWidget(mActivity);
		text.Text(n, t, f, left, top);
		CView v = mView.get(last);
		v.mWidget.add(text);
	}
	
	public void AddText(CView v, String n, String t, int f, float left, float top)
	{
		CWidget text = new CWidget(mActivity);
		text.Text(n, t, f, left, top);
		v.mWidget.add(text);
	}
	
	public void AddText(String t, int f, float left, float top)
	{
		int last = mView.size()-1;
		CWidget text = new CWidget(mActivity);
		text.Text(t, f, left, top);
		CView v = mView.get(last);
		v.mWidget.add(text);
	}
	
	public void AddDropDown(String n, int f, float left, float top, float width, CFuncPtr change)
	{
		int last = mView.size()-1;
		CWidget dropdown = new CWidget(mActivity);
		dropdown.DropDown(n, f, left, top, width, change);
		CView v = mView.get(last);
		v.mWidget.add(dropdown);
	}
	
	public void SelectOption()
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		int lastdropdown = v.mWidget.size()-1;
		CWidget dropdown = v.mWidget.get(lastdropdown);
		int lastoption = dropdown.options.size()-1;
		dropdown.selected = lastoption;
		dropdown.scroll = lastoption+1;
		if(dropdown.scroll + dropdown.rowsshown() > dropdown.options.size())
			dropdown.scroll = dropdown.options.size() - dropdown.rowsshown();
		if(dropdown.scroll < 0)
			dropdown.scroll = 0;
	}
	
	public void AddDpad(String n, String texf, float left, float top, float right, float bottom, CFuncPtr dpadf)
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		CWidget dpad = new CWidget(mActivity);
		dpad.DPad(n, texf, left, top, right, bottom, dpadf);
		v.mWidget.add(dpad);
	}
	
	public void AddTextField(String placeholder, float left, float top, float width, float height, boolean passw, int tag)
	{
		// TO DO
	}
	
	public void AddTextBox(String n, String t, int f, float left, float top, float right, float bottom)
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		CWidget textbox = new CWidget(mActivity);
		textbox.TextBox(n, t, f, left, top, right, bottom);
		v.mWidget.add(textbox);
	}
	
	public void CloseView(String n)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n))
				v.close();
		}
	}

	public void OpenSoleView(String n, int page)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.page == page)
				v.open();
			else
				v.close();
		}
	}
	
	public boolean OpenAnotherView(String n, int page)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.page == page)
			{
				v.open();
				return true;
			}
		}
		
		return false;
	}
	
	public void NextPage(String n)
	{
		int page = 0;
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.opened)
			{
				page = v.page;
				v.close();
				break;
			}
		}
		
		if(!OpenAnotherView(n, page+1))
			OpenAnotherView(n, 0);
	}
	
	public void DrawImage(int tex, float left, float top, float right, float bottom)
	{
		CShader s = mActivity.mShader[CShader.ORTHO];
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
	    GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	    
	    float vertices[] =
	    {
	        //posx, posy    texx, texy
	        left, top, 0,          0, 0,
	        right, top, 0,         1, 0,
	        right, bottom, 0,      1, 1,
	        
	        right, bottom, 0,      1, 1,
	        left, bottom, 0,       0, 1,
	        left, top, 0,          0, 0
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
}
