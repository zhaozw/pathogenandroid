package com.pathogengame.pathogen;

public class CEntity 
{
	public static final int NOCAT = -1;
	public static final int HUMAN = 0;
	public static final int ZOMBIE = 1;
	public static final int DOOR = 2;
	public static final int FIXEDENT = 3;
	public static final int ITEM = 4;
	public static final int ENTITY_CATEGORIES = 5;

	public static final int OPENSND = 0;
	public static final int CLOSESND = 1;
	
	static final int STATE_NONE		= 0;
	static final int STATE_OPENING	= 1;
	static final int STATE_CLOSING	= 2;

	static final int BODY_LOWER   = 0;
	static final int BODY_UPPER   = 1;
	
	static final float MID_HEIGHT_OFFSET = 13.49f;   //-15.0f
	static final float HEAD_OFFSET = (-6.9f*0.7143f);

	boolean on;
    CFloat frame[] = new CFloat[2];
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
	
	MainActivity mActivity;
    
	CEntity(MainActivity act)
	{
		on = false;
		controller = -1;
        frame[BODY_LOWER] = new CFloat();
        frame[BODY_UPPER] = new CFloat();
        amount = -1;
        clip = -1;
        mActivity = act;
	}
    
	CVector3 TraceRay(CVector3 vLine[])
	{
		CVector3 vTrace[] = new CVector3[2];
		vTrace[0] = Math3D.Copy(vLine[0]);
		vTrace[1] = Math3D.Copy(vLine[1]);
	    
		CEntityType t = mActivity.mEntityType.get(type);
		CModel m = mActivity.mModel[t.model[BODY_LOWER]];
	    
		if(t.collider >= 0)
			m = mActivity.mModel[t.collider];
	    
	    if((int)frame[BODY_LOWER].value >= m.header.num_frames)
	        return vLine[1];
	    
		CVertexArray va = m.vertexArrays[(int)frame[BODY_LOWER].value];
		CVector3 vTri[] = new CVector3[3];
		int i, j;
	    
		for(i=0; i<va.numverts; i+=3)
		{
			for(j=0; j<3; j++)
				vTri[j] = Math3D.Add(camera.Position(), Math3D.Rotate(va.vertices[i+j], camera.Yaw(), 0, 1, 0));
	        
			Math3D.IntersectedPolygon(vTri, vTrace, 3, vTrace[1]);
		}
	    
		if(t.model[BODY_UPPER] < 0 || t.collider >= 0)
			return vTrace[1];
	    
		m = mActivity.mModel[t.model[BODY_UPPER]];
		va = m.vertexArrays[(int)frame[BODY_UPPER].value];
	    
		for(i=0; i<va.numverts; i+=3)
		{
			for(j=0; j<3; j++)
			{
				vTri[j] = Math3D.RotateAround(va.vertices[i+j], new CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
				vTri[j] = Math3D.Add(camera.Position(), Math3D.Rotate(vTri[j], camera.Yaw(), 0, 1, 0));
			}
	        
			Math3D.IntersectedPolygon(vTri, vTrace, 3, vTrace[1]);
		}
	    
		return vTrace[1];
	}
	
    boolean Collision(CVector3 vScaleDown, CVector3 vCenter)
    {
    	CEntityType t = mActivity.mEntityType.get(type);
    	CModel m = mActivity.mModel[t.model[BODY_LOWER]];
        
    	if(t.collider >= 0)
    		m = mActivity.mModel[t.collider];
        
    	CVertexArray va = m.vertexArrays[(int)frame[BODY_LOWER].value];
    	CTriangle tri = new CTriangle();
    	int i;
        
    	for(i=0; i<va.numverts; i+=3)
    	{
    		tri.a = Math3D.Add(camera.Position(), Math3D.Rotate(va.vertices[i+0], camera.Yaw(), 0, 1, 0));
    		tri.b = Math3D.Add(camera.Position(), Math3D.Rotate(va.vertices[i+1], camera.Yaw(), 0, 1, 0));
    		tri.c = Math3D.Add(camera.Position(), Math3D.Rotate(va.vertices[i+2], camera.Yaw(), 0, 1, 0));
            
    		if(Math3D.TriBoxOverlap2(vScaleDown, vCenter, tri))
    			return true;
    	}
        
    	if(t.model[BODY_UPPER] < 0 || t.collider >= 0)
    		return false;
        
    	m = mActivity.mModel[t.model[BODY_UPPER]];
    	va = m.vertexArrays[(int)frame[BODY_UPPER].value];
        
    	for(i=0; i<va.numverts; i+=3)
    	{
    		tri.a = Math3D.RotateAround(va.vertices[i+0], new CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.a = Math3D.Add(camera.Position(), Math3D.Rotate(tri.a, camera.Yaw(), 0, 1, 0));
            
    		tri.b = Math3D.RotateAround(va.vertices[i+1], new CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.b = Math3D.Add(camera.Position(), Math3D.Rotate(tri.b, camera.Yaw(), 0, 1, 0));
            
    		tri.c = Math3D.RotateAround(va.vertices[i+2], new CVector3(0, MID_HEIGHT_OFFSET, 0), -camera.Pitch(), 1, 0, 0);
    		tri.c = Math3D.Add(camera.Position(), Math3D.Rotate(tri.c, camera.Yaw(), 0, 1, 0));
            
    		if(Math3D.TriBoxOverlap2(vScaleDown, vCenter, tri))
    			return true;
    	}
        
    	return false;
    }
}
