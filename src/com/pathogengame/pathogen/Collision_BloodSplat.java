package com.pathogengame.pathogen;

public class Collision_BloodSplat extends CFuncPtr
{
	public Collision_BloodSplat(MainActivity act)
	{
		super(act);
	}
	
    @Override
    void collision(CParticle part, CBillboard billb, CVector3 trace, CVector3 normal)
    {
    	part.on = false;
    	billb.on = false;
    	mActivity.PlaceDecal(CDecalType.BLOODSPLAT, trace, normal);
    }
}
