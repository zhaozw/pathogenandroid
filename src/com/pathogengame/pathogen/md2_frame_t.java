package com.pathogengame.pathogen;

public class md2_frame_t 
{
	//vec3_t scale;
    //vec3_t translate;
    //char name[16];
    //struct md2_vertex_t *verts;
	
	float scale[] = new float[3];
	float translate[] = new float[3];
	String name;
	md2_vertex_t verts[];
}
