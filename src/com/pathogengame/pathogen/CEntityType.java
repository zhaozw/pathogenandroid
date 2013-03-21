package com.pathogengame.pathogen;

import java.util.Vector;

public class CEntityType 
{
	String lmodel;
	int collider;
    int model[]  = new int[2];
	CVector3 vMin, vMax;
	float maxStep;
	float speed;
	float jump;
	float crouch;
	float animrate;
	int category;
	int item;
	CVector3 vCenterOff;
	Vector<CSound> openSound;
	Vector<CSound> closeSound;
    
	CEntityType()
	{
		model[0] = -1;
		model[1] = -1;
        collider = -1;
		item = -1;
	}
}
