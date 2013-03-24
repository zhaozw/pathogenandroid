package com.pathogengame.pathogen;

public class Script3a extends CFuncPtr
{
	public Script3a(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("But one thing kept bugging me - how my parents were, "+
    	           "my wife, daughter. I left for only a week and was supposed to be back in two days "+
    	           "and here is such chaos! Life is an unpredictable thing. There weren't any cars nearby, and those that were weren't in any condition to drive.", new Script3b(mActivity));

    }
}
