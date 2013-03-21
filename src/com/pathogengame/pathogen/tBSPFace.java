package com.pathogengame.pathogen;

public class tBSPFace
{
    int textureID;				// The index into the texture array
    int effect;					// The index for the effects (or -1 = n/a)
    int type;					// 1=polygon, 2=patch, 3=mesh, 4=billboard
    int startVertIndex;			// The starting index into this face's first vertex
    int numOfVerts;				// The number of vertices for this face
    int startIndex;				// The starting index into the indices array for this face
    int numOfIndices;			// The number of indices for this face
    int lightmapID;				// The texture index for the lightmap
    int lMapCorner[];			// The face's lightmap corner in the image
    int lMapSize[];			// The size of the lightmap section
    CVector3 lMapPos;			// The 3D origin of lightmap.
    CVector3 lMapVecs[];		// The 3D space for s and t unit vectors.
    CVector3 vNormal;			// The face normal.
    int size[];				// The bezier patch dimensions.
    
    tBSPFace()
    {
    	lMapCorner = new int[2];
    	lMapSize = new int[2];
    	lMapPos = new CVector3();
    	lMapVecs = new CVector3[2];
    	lMapVecs[0] = new CVector3();
    	lMapVecs[1] = new CVector3();
    	size = new int [2];
    }
};
