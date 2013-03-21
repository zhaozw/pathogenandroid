package com.pathogengame.pathogen;

public class CVector3 
{

	float x, y, z;
	
	CVector3() {}
    
	CVector3(float X, float Y, float Z)
	{
		x = X; y = Y; z = Z;
	}
    
	CVector3(float[] values)
	{
		Set(values);
	}
    
	void Set(float[] values)
	{
		x = values[0];
		y = values[1];
		z = values[2];
	}
    
	void Transform(CMatrix m)
	{
		
	}
	
	void Transform3(CMatrix m)
	{
		
	}
}
