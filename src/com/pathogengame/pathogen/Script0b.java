package com.pathogengame.pathogen;

public class Script0b extends CFuncPtr
{
	public Script0b(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("But I've digressed from the main subject, the EPIDEMIC! An unknown virus, which scientists have named The Pathogen. "+
    	           "It turned people into animals, into walking zombies, ready to kill you at any moment. Back then my only goal was to survive "+
    	           "and get to Kerrisdale, find my family and live peacefully. But life is an unpredictable thing. The moment it all began I was scared and my only thought was to SURVIVE!!!", new Script0c(mActivity));

    }
}
