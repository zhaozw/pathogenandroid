package com.pathogengame.pathogen;

public class CEntity 
{
	enum ENTITY{NOCAT, HUMAN, ZOMBIE, DOOR, FIXEDENT, ITEM, ENTITY_CATEGORIES};

	enum ENTITYSND{OPENSND, CLOSESND};
	
	static final int STATE_NONE		= 0;
	static final int STATE_OPENING	= 1;
	static final int STATE_CLOSING	= 2;

	static final int BODY_LOWER   = 0;
	static final int BODY_UPPER   = 1;
	
	static final float MID_HEIGHT_OFFSET = 13.49f;   //-15.0f

	boolean on;
    float frame[] = new float[2];
	int type;
	int controller;
	CCamera camera = new CCamera();
	float amount;
	float clip;
	int state;
	int cluster;
	float dist;
	boolean nolightvol;
	int script;
    
	CEntity()
	{
		on = false;
		controller = -1;
        frame[BODY_LOWER] = 0;
        frame[BODY_UPPER] = 0;
        amount = -1;
        clip = -1;
	}
    
	CVector3 TraceRay(CVector3 vLine[])
	{
		CVector3 vTrace[2];
		vTrace[0] = vLine[0];
		vTrace[1] = vLine[1];
	    
		CEntityType* t = &g_entityType[type];
		CModel* m = &g_model[t->model[BODY_LOWER]];
	    
		if(t->collider >= 0)
			m = &g_model[t->collider];
	    
	    if((int)frame[BODY_LOWER] >= m->header.num_frames)
	        return vLine[1];
	    
		CVertexArray* va = &m->vertexArrays[(int)frame[BODY_LOWER]];
		CVector3 vTri[3];
		int i, j;
	    
		for(i=0; i<va->numverts; i+=3)
		{
			for(j=0; j<3; j++)
				vTri[j] = camera.Position() + Rotate(va->vertices[i+j], camera.Yaw(), 0, 1, 0);
	        
			IntersectedPolygon(vTri, vTrace, 3, &vTrace[1]);
		}
	    
		if(t->model[BODY_UPPER] < 0 || t->collider >= 0)
			return vTrace[1];
	    
		m = &g_model[t->model[BODY_UPPER]];
		va = &m->vertexArrays[(int)frame[BODY_UPPER]];
	    
		for(i=0; i<va->numverts; i+=3)
		{
			for(j=0; j<3; j++)
			{
				vTri[j] = RotateAround(va->vertices[i+j], CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
				vTri[j] = camera.Position() + Rotate(vTri[j], camera.Yaw(), 0, 1, 0);
			}
	        
			IntersectedPolygon(vTri, vTrace, 3, &vTrace[1]);
		}
	    
		return vTrace[1];
	}
	
    boolean Collision(CVector3 vScaleDown, CVector3 vCenter)
    {
    	CEntityType* t = &g_entityType[type];
    	CModel* m = &g_model[t->model[BODY_LOWER]];
        
    	if(t->collider >= 0)
    		m = &g_model[t->collider];
        
    	CVertexArray* va = &m->vertexArrays[(int)frame[BODY_LOWER]];
    	CTriangle tri;
    	int i;
        
    	for(i=0; i<va->numverts; i+=3)
    	{
    		tri.a = camera.Position() + Rotate(va->vertices[i+0], camera.Yaw(), 0, 1, 0);
    		tri.b = camera.Position() + Rotate(va->vertices[i+1], camera.Yaw(), 0, 1, 0);
    		tri.c = camera.Position() + Rotate(va->vertices[i+2], camera.Yaw(), 0, 1, 0);
            
    		if(TriBoxOverlap2(vScaleDown, vCenter, tri))
    			return true;
    	}
        
    	if(t->model[BODY_UPPER] < 0 || t->collider >= 0)
    		return false;
        
    	m = &g_model[t->model[BODY_UPPER]];
    	va = &m->vertexArrays[(int)frame[BODY_UPPER]];
        
    	for(i=0; i<va->numverts; i+=3)
    	{
    		tri.a = RotateAround(va->vertices[i+0], CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.a = camera.Position() + Rotate(tri.a, camera.Yaw(), 0, 1, 0);
            
    		tri.b = RotateAround(va->vertices[i+1], CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.b = camera.Position() + Rotate(tri.b, camera.Yaw(), 0, 1, 0);
            
    		tri.c = RotateAround(va->vertices[i+2], CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.c = camera.Position() + Rotate(tri.c, camera.Yaw(), 0, 1, 0);
            
    		if(TriBoxOverlap2(vScaleDown, vCenter, tri))
    			return true;
    	}
        
    	return false;
    }
}
