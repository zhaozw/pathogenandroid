package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Script8b extends CFuncPtr
{
	public Script8b(MainActivity act)
	{
		super(act);
	}
	

	void RemoveItems()
	{
	    CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
	    p.items.clear();
	    p.equipped = -1;
	}
	
    @Override
    public void func()
    {
    	RemoveItems();
        new CloseDialog(mActivity).func();
        mActivity.UnloadMap();
        mActivity.mMode = GAMEMODE.MENU;
        mActivity.mGUI.OpenSoleView("credits", 0);
    }
}
