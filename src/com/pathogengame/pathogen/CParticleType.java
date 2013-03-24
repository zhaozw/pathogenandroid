package com.pathogengame.pathogen;

public class CParticleType
{
	static final int BLOODPART = 0;
	static final int PARTICLE_TYPES = 1;
	
	int billbT;
	int delay;
	float decay;
	CVector3 minvelocity;
	CVector3 velvariation;
	CVector3 minacceleration;
	CVector3 accelvariation;
	float minsize;
	float sizevariation;
	CFuncPtr collision;
	
	CParticleType()
	{
		minvelocity = new CVector3();
		velvariation = new CVector3();
		minacceleration = new CVector3();
		accelvariation = new CVector3();	
		collision = null;
	}
}
