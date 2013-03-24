package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_Crouch extends CFuncPtr
{
	public Click_Crouch(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mMode != GAMEMODE.PLAY)
    		return;
        
    	if(mActivity.mArrest)
    		return;
        
    	mActivity.mPlayer[mActivity.mLocalP].crouching =! mActivity.mPlayer[mActivity.mLocalP].crouching;
    }
}
