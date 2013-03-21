package com.pathogengame.pathogen;

public class CFuncProxy 
{
	CVector3 pos;
	CVector3 vmin;
	CVector3 vmax;
	int script;
    
	CFuncProxy(CVector3 ps, CVector3 mn, CVector3 mx, int scrpt)
	{
		pos = Math3D.Copy(ps);
		vmin = Math3D.Copy(mn);
		vmax = Math3D.Copy(mx);
		script = scrpt;
	}
}
