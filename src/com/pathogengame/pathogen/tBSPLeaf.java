package com.pathogengame.pathogen;

//This stores a leaf (end node) in the BSP tree
public class tBSPLeaf
{
    int cluster;				// The visibility cluster
    int area;					// The area portal
    tVector3i min;				// The bounding box min position
    tVector3i max;				// The bounding box max position
    int leafface;				// The first index into the face array
    int numOfLeafFaces;			// The number of faces for this leaf
    int leafBrush;				// The first index for into the brushes
    int numOfLeafBrushes;		// The number of brushes for this leaf
    
    tBSPLeaf()
    {
    	min = new tVector3i();
    	max = new tVector3i();
    }
};
