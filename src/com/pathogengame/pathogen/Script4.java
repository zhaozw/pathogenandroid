package com.pathogengame.pathogen;

public class Script4 extends CFuncPtr
{
	public Script4(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	 mActivity.mGUI.Dialog("I had found a shotgun in the other backyard.", new CloseDialog(mActivity));

    }
}
