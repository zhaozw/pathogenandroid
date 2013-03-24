package com.pathogengame.pathogen;

public class Script7 extends CFuncPtr
{
	public Script7(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("Having gone through familiar streets, having killed a decent number of zombies I heard a sound from a broken down car, somebody was talking "+
    	           "from a walkie-talkie. I needed to immediately find out who this was.", new CloseDialog(mActivity));

    }
}
