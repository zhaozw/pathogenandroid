package com.pathogengame.pathogen;

public class Click_DontShow extends CFuncPtr
{
	public Click_DontShow(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	g_showdialog = false;
        Click_DialogContinue();
    }
}