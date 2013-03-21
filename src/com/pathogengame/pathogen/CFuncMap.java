package com.pathogengame.pathogen;

public class CFuncMap 
{
	CVector3 pos;
	CVector3 vmin;
	CVector3 vmax;
	String map;
    
	CFuncMap(CVector3 ps, CVector3 mn, CVector3 mx, String mp)
	{
		pos = Math3D.Copy(ps);
		vmin = Math3D.Copy(mn);
		vmax = Math3D.Copy(mx);
		map = mp;
	}
}
