package com.pathogengame.pathogen;

public class Click_Crouch extends CFuncPtr
{
	public Click_Crouch(MainActivity act)
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
        
    	g_player[g_localP].crouching =! g_player[g_localP].crouching;
    }
}
