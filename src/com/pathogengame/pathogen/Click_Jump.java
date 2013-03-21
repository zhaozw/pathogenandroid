package com.pathogengame.pathogen;

public class Click_Jump extends CFuncPtr
{
	public Click_Jump(MainActivity act)
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
        
    	g_player[g_localP].jump = true;
    }
}