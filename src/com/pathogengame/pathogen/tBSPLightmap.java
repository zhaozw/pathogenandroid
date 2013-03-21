package com.pathogengame.pathogen;

public class tBSPLightmap
{
    //byte imageBits[128][128][3];   // The RGB data in a 128x128 image
	
	//byte imageBits[][][];
	byte imageBits[];
	
	tBSPLightmap()
	{
		//imageBits = new byte[128][128][3];
		imageBits = new byte[128*128*3];
	}
};
