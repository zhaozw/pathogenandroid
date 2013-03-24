package com.pathogengame.pathogen;

public class Script5 extends CFuncPtr
{
	public Script5(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.OpenAnotherView("crouch", 0);
    	mActivity.mGUI.OpenAnotherView("jump", 0);
        
    	mActivity.mGUI.Dialog("I climbed up the dumpster behind the convenience store and broke through the vent.", new CloseDialog(mActivity));

    }
}
