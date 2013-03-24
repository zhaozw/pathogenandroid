package com.pathogengame.pathogen;

public class CDecal
{
	boolean on;
	int type;
	CVector3 a, b, c, d;
	CVector3 lpos;
	float life;
	
	CDecal()
	{
		on = false;
		a = new CVector3();
		b = new CVector3();
		c = new CVector3();
		d = new CVector3();
		lpos = new CVector3();
	}
}
