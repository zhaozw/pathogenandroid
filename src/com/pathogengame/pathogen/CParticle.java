package com.pathogengame.pathogen;

public class CParticle
{
	boolean on;
	int type;
	float life;
	CVector3 vel;
	float dist;
    MainActivity mActivity;
    
	CParticle(MainActivity act) 
	{ 
		mActivity = act;
		on = false; 
	}
	
	CParticle(MainActivity act, CVector3 p, CVector3 v) 
	{ 
		mActivity = act;
		on = true; 
		vel = Math3D.Copy(v); 
	}
    
	void Update(CBillboard billb)
	{
		CParticleType t = mActivity.mParticleType[type];
		life -= t.decay;
	    
		if(life <= 0.0f)
		{
			on = false;
			billb.on = false;
			return;
		}
	    
		CVector3 to = Math3D.Add(billb.pos, vel);
	    
		CVector3 trace = mActivity.mMap.TraceRay(billb.pos, to);
	    
		if(!Math3D.Equals(trace, to) && t.collision != null)
			t.collision.collision(this, billb, trace, mActivity.mMap.CollisionNormal());
	    
		billb.pos = to;
		CVector3 accel = new CVector3();
		accel.x = t.minacceleration.x + t.accelvariation.x * (float)(Math.random());
		accel.y = t.minacceleration.y + t.accelvariation.y * (float)(Math.random());
		accel.z = t.minacceleration.z + t.accelvariation.z * (float)(Math.random());
	    
		vel = Math3D.Add(vel, accel);
	}
}
