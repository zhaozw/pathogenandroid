package com.pathogengame.pathogen;

public class CBillboard
{
    boolean on;
    int type;
    float size;
    CVector3 pos;
    float dist;
    int particle;
	boolean nolightvol;
    
    CBillboard()
    {
        on = false;
        particle = -1;
        pos = new CVector3();
        nolightvol = false;
    }
}
