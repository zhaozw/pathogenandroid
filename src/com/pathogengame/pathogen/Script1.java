package com.pathogengame.pathogen;

public class Script1 extends CFuncPtr
{
	public Script1(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("I'm coming, I'm coming, I said, and opening the door saw THIS! A monster roaring in blood that pounced on me. "+
    	           "I needed to stop him. I ran to get a baseball bat and it was a small matter to kill the unwelcome guest.", new CloseDialog(mActivity));

    }
}
