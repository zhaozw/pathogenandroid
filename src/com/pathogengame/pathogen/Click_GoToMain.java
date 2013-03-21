package com.pathogengame.pathogen;

public class Click_GoToMain extends CFuncPtr
{
	public Click_GoToMain(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.OpenSoleView("main", 0);
    }
}