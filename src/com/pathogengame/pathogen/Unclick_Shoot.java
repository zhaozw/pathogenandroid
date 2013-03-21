package com.pathogengame.pathogen;

public class Unclick_Shoot extends CFuncPtr
{
	public Unclick_Shoot(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(g_mode != PLAY)
            return;
        
        g_player[g_localP].shoot = false;
    }
}
