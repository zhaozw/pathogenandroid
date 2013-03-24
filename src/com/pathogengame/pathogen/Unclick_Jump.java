package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Unclick_Jump extends CFuncPtr
{
	public Unclick_Jump(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mMode != GAMEMODE.PLAY)
    		return;
        
    	mActivity.mPlayer[mActivity.mLocalP].jump = false;
    }
}
