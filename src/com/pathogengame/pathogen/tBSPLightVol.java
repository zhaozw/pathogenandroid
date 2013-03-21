package com.pathogengame.pathogen;

public class tBSPLightVol
{
	tVector3uc ambient;
	tVector3uc directional;
	tVector2uc dir; //0=phi, 1=theta
	
	tBSPLightVol()
	{
		ambient = new tVector3uc();
		directional = new tVector3uc();
		dir = new tVector2uc();
	}
};
