package com.pathogengame.pathogen;

public class Click_GoToCredits extends CFuncPtr
{
	public Click_GoToCredits(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	//mActivity.helloLog("This will log to LogCat via the native call.");  
    	//mActivity.mGUI.OpenSoleView("credits", 0);
    	System.out.println("5 + 2 = " + mActivity.getString(5,2));
    }
}