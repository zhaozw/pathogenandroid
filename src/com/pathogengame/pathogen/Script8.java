package com.pathogengame.pathogen;

public class Script8 extends CFuncPtr
{
	public Script8(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("Walking up to the car I heard a voice and decided to answer. \"Who is this?\" I asked.", new Script8a(mActivity));

    }
}
