package com.pathogengame.pathogen;

public class CParticleEmitter
{
	long last;
	MainActivity mActivity;
	
	CParticleEmitter(MainActivity act) 
	{ 
		mActivity = act;
		last = mActivity.GetTickCount(); 
	}
	
	boolean EmitNext(int delay)
	{
		if(mActivity.GetTickCount()-last > delay)
		{
			last = mActivity.GetTickCount();
			return true;
		}
		else
			return false;
	}
}
