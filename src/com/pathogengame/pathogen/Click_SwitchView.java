package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.VIEWMODE;

public class Click_SwitchView extends CFuncPtr
{
	public Click_SwitchView(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mViewMode == VIEWMODE.FIRSTPERSON)
    		mActivity.mViewMode = VIEWMODE.THIRDPERSON;
    	else
    		mActivity.mViewMode = VIEWMODE.FIRSTPERSON;
    }
}
