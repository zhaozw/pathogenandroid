package com.pathogengame.pathogen;

public class Script8a extends CFuncPtr
{
	public Script8a(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	 mActivity.mStaticSound.Play();
    	    
    	 mActivity.mGUI.Dialog("TO BE CONTINUED.", new Script8b(mActivity));

    }
}