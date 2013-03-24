package com.pathogengame.pathogen;

public class Script3b extends CFuncPtr
{
	public Script3b(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("My path through the streets "+
    	           "will be long. The main thing was to find at least any signs of life and survive.", new CloseDialog(mActivity));

    }
}
