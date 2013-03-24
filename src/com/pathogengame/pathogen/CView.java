package com.pathogengame.pathogen;

import java.util.*;

public class CView 
{
	public MainActivity mActivity;
	public String name = "";
	public int page = 0;
	public boolean opened = false;
	public Vector<CWidget> mWidget = new Vector();
	
	public CView(MainActivity act, String n, int p)
	{
		mActivity = act;
		name = n;
		page = p;
	}
	
	public CView(MainActivity act)
	{
		mActivity = act;
	}
	
	public void close()
	{
		opened = false;
	}
	
	public void open()
	{
		opened = true;
	}
	
	public void mousemove(float x, float y)
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			w.mousemove(x,  y);
		}
	}
	

	boolean touchframe(float x, float y)
    {
		for(int i=mWidget.size()-1; i>=0; i--)
			if(mWidget.get(i).touchframe(x, y))
                return true;
        
        return false;
    }
	
	/*
	public boolean mouseupdate(float x, float y)
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			if(w.mouseupdate(x, y))
				return true;
		}
		
		return false;
	}*/
	
	void touchcheck()
    {
        for(int i=0; i<mWidget.size(); i++)
        	mWidget.get(i).touchcheck();
    }
	
	public boolean lbuttondown(float x, float y)
	{
		CWidget w;
		
		for(int i=mWidget.size()-1; i>=0; i--)
		{
			w = mWidget.get(i);
			if(w.type == CWidget.DROPDOWN && w.opened && w.lbuttondown(x, y))
				return true;	//intercept mouse event
		}
		
		for(int i=mWidget.size()-1; i>=0; i--)
		{
			w = mWidget.get(i);
			if(w.lbuttondown(x, y))
				return true;	//intercept mouse event
		}
		
		return false;
	}
	
	public boolean lbuttonup(float x, float y)
	{
		CWidget w;
		
		for(int i=mWidget.size()-1; i>=0; i--)
		{
			w = mWidget.get(i);
			if(w.type == CWidget.DROPDOWN && w.opened && w.lbuttonup(x, y))
				return true;	//intercept mouse event
		}
		
		for(int i=mWidget.size()-1; i>=0; i--)
		{
			w = mWidget.get(i);
			if(w.lbuttonup(x, y))
				return true;	//intercept mouse event
		}
		
		return false;
	}
	
	public CWidget gettext(String n)
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			if(w.type == CWidget.TEXT && w.name.equals(n))
				return w;
		}
		
		return new CWidget(mActivity);
	}
	
	public CWidget getdropdown(String n)
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			if(w.type == CWidget.DROPDOWN && w.name.equals(n))
				return w;
		}
		
		return new CWidget(mActivity);
	}
	
	public CWidget gettextfield(String n)
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			if(w.type == CWidget.TEXTFIELD && w.name.equals(n))
				return w;
		}
		
		return new CWidget(mActivity);
	}
	
	public void draw()
	{
		CWidget w;
		
		for(int i=0; i<mWidget.size(); i++)
		{
			w = mWidget.get(i);
			w.draw();
		}
		
		for(int i=mWidget.size()-1; i>=0; i--)
		{
			w = mWidget.get(i);
			w.draw2();
		}
	}
}
