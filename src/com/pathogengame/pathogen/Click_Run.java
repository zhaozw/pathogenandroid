package com.pathogengame.pathogen;

public class Click_Run extends CFuncPtr
{
	public Click_Run(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(g_mode != PLAY)
    		return;
        
    	if(g_arrest)
    		return;
        
    	if(g_player[g_localP].crouched)
    		return;
        
    	g_player[g_localP].run = !g_player[g_localP].run;
    }
}
