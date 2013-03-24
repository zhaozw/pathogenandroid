package com.pathogengame.pathogen;

public class Script0c extends CFuncPtr
{
	public Script0c(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	 mActivity.mGUI.Dialog("Chapter 1 - Unwelcome guest", new Script0d(mActivity));
    }
}
