package com.pathogengame.pathogen;

public class Script6 extends CFuncPtr
{
	public Script6(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("I found a pistol and some ammo at the top of the shelves in the backroom.", new CloseDialog(mActivity));

    }
}
