package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Rotational extends CFuncPtr 
{
	public Rotational(MainActivity act)
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
	    
	    if(Math3D.Magnitude2(dx, dy) < mActivity.MOV_THRESH*mActivity.MOV_THRESH*mActivity.mRetinaScale*mActivity.mRetinaScale)
	       return;
	    
	    mActivity.mCamera.SetViewByMouse(-dx/(float)mActivity.MOV_THRESH*mActivity.mRetinaScale, -dy/(float)mActivity.MOV_THRESH*mActivity.mRetinaScale);
	}
}
