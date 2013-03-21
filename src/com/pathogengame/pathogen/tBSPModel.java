package com.pathogengame.pathogen;

public class tBSPModel
{
	CVector3 mins;
	CVector3 maxs;
	int firstFace;
	int numOfFaces;
	int firstBrush;
	int numOfBrushes;
	
	tBSPModel()
	{
		mins = new CVector3();
		maxs = new CVector3();
	}
};
