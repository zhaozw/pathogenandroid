package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class CloseDialog extends CFuncPtr
{
	public CloseDialog(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
	    mActivity.mGUI.CloseView("dialog");
	    mActivity.mMode = GAMEMODE.PLAY;
    }
}
