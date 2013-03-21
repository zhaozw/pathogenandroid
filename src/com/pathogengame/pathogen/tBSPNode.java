package com.pathogengame.pathogen;

public class tBSPNode
{
    int plane;					// The index into the planes array
    int front;					// The child index for the front node
    int back;					// The child index for the back node
    tVector3i min;				// The bounding box min position.
    tVector3i max;				// The bounding box max position.
    
    tBSPNode()
    {
    	min = new tVector3i();
    	max = new tVector3i();
    }
};
