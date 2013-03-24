package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class LDown extends CFuncPtr
{
	public LDown(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
        if(mActivity.mMode == GAMEMODE.LOGO)
            mActivity.SkipLogo();
        else if(mActivity.mMode == GAMEMODE.INTRO)
        	mActivity.SkipIntro();
    	
    }
}
