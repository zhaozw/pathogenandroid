package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Movement extends CFuncPtr
{
	public Movement(MainActivity act)
	{
		super(act);
	}
	
	void Left()
	{
		
	}
	
	void Right()
	{
		
	}
	
	void Forward()
	{
		
	}
	
	void Back()
	{
		
	}
	
    @Override
    public void func(float dx, float dy)
	{
    	if(mActivity.mMode != GAMEMODE.PLAY)
            return;
        
        if(mActivity.mArrest)
            return;
        
        if(dx < -mActivity.MOV_THRESH*mActivity.mRetinaScale)
            Left();
        else if(dx > mActivity.MOV_THRESH*mActivity.mRetinaScale)
            Right();
        
        if(dy < -mActivity.MOV_THRESH*mActivity.mRetinaScale)
            Forward();
        else if(dy > mActivity.MOV_THRESH*mActivity.mRetinaScale)
            Back();
	}
}
