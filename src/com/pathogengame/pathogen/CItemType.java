package com.pathogengame.pathogen;

import java.util.Vector;

public class CItemType 
{
	public static final int NOAMMO 			= -1;
	public static final int MP5				= 0;
	public static final int MOSSBERG500		= 1;
	public static final int M1911			= 2;
	public static final int PRIMARYAMMO		= 3;
	public static final int SECONDARYAMMO	= 4;
	public static final int TERTAMMO		= 5;
	public static final int BBAT			= 6;
	public static final int KNIFE			= 7;
	public static final int WTALKIE			= 8;
	public static final int ITEM_TYPES		= 9;
	
	int model;
	boolean equip;
	int icon;
	CVector3 front = new CVector3();
	int delay;
	int ammo;
	int clip;
	float damage;
	float range;
	int split;
	float inacc;
	int reloadrate;
	Vector<CSound> dryShotSound = new Vector<CSound>();
	Vector<CSound> shotSound = new Vector<CSound>();
	Vector<CSound> reloadSound = new Vector<CSound>();
	Vector<CSound> cockSound = new Vector<CSound>();
	Vector<CSound> dryFireSound = new Vector<CSound>();
	Vector<CSound> hitSound = new Vector<CSound>();
    
	CItemType()
	{
		model = -1;
		equip = false;
	}
}
