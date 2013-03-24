package com.pathogengame.pathogen;

public class Script0d extends CFuncPtr
{
	public Script0d(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	 mActivity.mGUI.Dialog("Home from work I quickly ate and without undressing fell into bed tiredly. The day was tough. But what awaited me was an unwelcome guest, "+
    	           "acquaintance with The Zombie, fear and bloodshed. Already, and the day had begun so well.", new Script0e(mActivity));

    }
}
