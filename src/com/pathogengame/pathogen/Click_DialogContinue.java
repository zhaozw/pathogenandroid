package com.pathogengame.pathogen;

public class Click_DialogContinue extends CFuncPtr
{	
	public Click_DialogContinue(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.DialogContinue != null)
    		mActivity.DialogContinue.func();
    }
}
