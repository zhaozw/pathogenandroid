package com.pathogengame.pathogen;

public class Click_GoToOnline extends CFuncPtr
{
	public Click_GoToOnline(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.OpenSoleView("online", 0);
    }
}