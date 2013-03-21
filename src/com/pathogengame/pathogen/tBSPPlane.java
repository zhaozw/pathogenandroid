package com.pathogengame.pathogen;

//This stores a splitter plane in the BSP tree
public class tBSPPlane
{
    CVector3 vNormal;			// Plane normal.
    float d;					// The plane distance from origin
    
    tBSPPlane()
    {
    	vNormal = new CVector3();
    }
};
