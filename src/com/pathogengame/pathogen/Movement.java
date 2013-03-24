package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Movement extends CFuncPtr
{
	public Movement(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func(float dx, float dy)
	{
    	if(mActivity.mMode != GAMEMODE.PLAY)
            return;
        
        if(mActivity.mArrest)
            return;
        
        if(dx < -mActivity.MOV_THRESH*mActivity.mRetinaScale)
            mActivity.Left();
        else if(dx > mActivity.MOV_THRESH*mActivity.mRetinaScale)
        	mActivity.Right();
        
        if(dy < -mActivity.MOV_THRESH*mActivity.mRetinaScale)
        	mActivity.Forward();
        else if(dy > mActivity.MOV_THRESH*mActivity.mRetinaScale)
        	mActivity.Back();
	}
}
