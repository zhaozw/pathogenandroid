package com.pathogengame.pathogen;

public class CSpawn 
{
	CVector3 pos;
	float angle;
	int activity;
	int script;
    
	CSpawn(CVector3 p, float ang, int act, int scrpt)
	{
		pos = Math3D.Copy(p);
		angle = ang;
		activity = act;
		script = scrpt;
	}
}
