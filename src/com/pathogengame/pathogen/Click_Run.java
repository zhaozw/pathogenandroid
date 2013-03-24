package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_Run extends CFuncPtr
{
	public Click_Run(MainActivity act)
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
        
    	if(mActivity.mPlayer[mActivity.mLocalP].crouched)
    		return;
        
    	mActivity.mPlayer[mActivity.mLocalP].run = !mActivity.mPlayer[mActivity.mLocalP].run;
    }
}
