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
    	if(DialogContinue != null)
            DialogContinue.func();
    }
}
