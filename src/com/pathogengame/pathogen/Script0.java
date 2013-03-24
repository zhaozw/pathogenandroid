package com.pathogengame.pathogen;

public class Script0 extends CFuncPtr
{
	public Script0(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("My name is Michael or Mike as my friends call me. This story took place a month ago and sitting at home, remembering these terrible moments, I want to tell you about the horror " +
    	           "that happened with me and billions of other people. Recording all this, maybe it will interest somebody. I want to leave this for generations to come and tell them in more detail " +
    	           "about the horror known as The Pathogen.", new Script0a(mActivity));
    }
}
