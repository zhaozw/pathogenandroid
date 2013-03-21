package com.pathogengame.pathogen;

import java.util.Vector;

public class CPlayer 
{
	static final int NOACT = 0;
	static final int ONSIGHT = 1;
	
	boolean on;
    int ID;
    boolean ai;
    float hp;
    boolean forward;
    boolean backward;
    boolean left;
    boolean right;
    boolean jump;
    boolean crouched;
    boolean crouching;
    boolean run;
	boolean shoot;
	boolean action;
	boolean reload;
	boolean pain;
	int entity;
	int client;
	CVector3 goal = new CVector3();
	int target;
	float stamina;
	Vector<CHold> items = new Vector<CHold>();
	int equipped;
	long last;
	int activity;
    int ticksleft;
    
    float HPRegen()
    {
        return 1.0f;
    }
    
    float MaxHP()
    {
        return 100;
    }
    
	float StaminaRegen()
	{
		return 1.0f / 10.0f;	//10 seconds to regenerate 1 stamina
	}
	
	float MaxStamina()
	{
		return 1.0f;
	}
    
	CPlayer()
	{
		on = false;
        ID = -1;
        ai = false;
        hp = 100;
		forward = false;
		backward = false;
		left = false;
		right = false;
		jump = false;
		crouched = false;
		crouching = false;
		run = false;
		shoot = false;
		action = false;
		reload = false;
		pain = false;
		entity = -1;
		client = -1;
		target = -1;
		stamina = 1;
		equipped = -1;
		activity = -1;
        ticksleft = 0;
	}
}
