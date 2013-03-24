package com.pathogengame.pathogen;

public class Script3 extends CFuncPtr
{
	public Script3(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("Walking out into the streets I saw real chaos, wrecked cars, sounds "+
    	           "of shooting in the distance. This was all in reality very frightening but what I needed to do now "+
    	           "was to go along the familiar streets, ahead into the unknown, saving my life with the hope to find at least somebody alive.", new Script3a(mActivity));
    	    

    }
}
