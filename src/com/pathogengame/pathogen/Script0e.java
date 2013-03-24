package com.pathogengame.pathogen;

public class Script0e extends CFuncPtr
{
	public Script0e(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mDoorKnock.Play();
        
    	mActivity.mGUI.Dialog("Oh my god, who could have decided to to pour into my home at 4 in the MORNING! I need to see who it is.", new CloseDialog(mActivity));

    }
}
