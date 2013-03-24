package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Unclick_Shoot extends CFuncPtr
{
	public Unclick_Shoot(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mMode != GAMEMODE.PLAY)
            return;
        
        mActivity.mPlayer[mActivity.mLocalP].shoot = false;
    }
}
