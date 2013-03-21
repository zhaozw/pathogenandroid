package com.pathogengame.pathogen;

public class CVertexArray 
{
	int numverts;
	CVector3 vertices[];
	CVector2 texcoords[];
	CVector3 normals[];
    
	CVertexArray()
	{
		numverts = 0;
	}
    
	void free()
	{
		if(numverts <= 0)
			return;
        
		//delete [] vertices;
		//delete [] texcoords;
		//delete [] normals;
		numverts = 0;
	}
}
