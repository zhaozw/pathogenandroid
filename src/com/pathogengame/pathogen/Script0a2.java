package com.pathogengame.pathogen;

public class Script0a2 extends CFuncPtr
{
	public Script0a2(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("But as I regret I wasn't near my family when the epidemic began. I still search and hope to find my family but so far my search hasn't had any luck. From Kerrisdale remain "+
    	           "only ruins and the city is being rebuilt anew, sweeping up the remains of the horrifying tragedy. ", new Script0b(mActivity));

    }
}
