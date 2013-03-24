package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_Action extends CFuncPtr
{
	public Click_Action(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mMode != GAMEMODE.PLAY)
    		return;
        
    	if(mActivity.mArrest)
    		return;
        
    	CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
    	CEntity e = mActivity.mEntity[p.entity];
    	CCamera c = e.camera;
        
    	CVector3 vLine[] = new CVector3[2];
    	vLine[0] = Math3D.Copy(c.Position());
    	CVector3 d = Math3D.Normalize(Math3D.Subtract(c.View(), c.Position()));
    	vLine[1] = Math3D.Add(c.Position(), Math3D.Multiply(d, MainActivity.INTERACTION_D));
    	vLine[1] = mActivity.mMap.TraceRay(vLine[0], vLine[1]);
        
    	CEntity e2;
    	int hit = -1;
    	CVector3 trace;
        
    	for(int i=0; i<MainActivity.ENTITIES; i++)
    	{
    		e2 = mActivity.mEntity[i];
            
    		if(!e2.on)
    			continue;
            
    		if(i == p.entity)
    			continue;
            
    		trace = e2.TraceRay(vLine);
            
    		if(Math3D.Equals(trace, vLine[1]))
    			continue;
            
    		hit = i;
    		vLine[1] = trace;
    	}
        
    	if(hit < 0)
    		return;
        
    	e2 = mActivity.mEntity[hit];
    	CEntityType t = mActivity.mEntityType.get(e2.type);
        
    	if(t.category == CEntity.DOOR)
    	{
    		if(e2.state == CEntity.STATE_OPENING)
    		{
    			if(t.closeSound.size() > 0)
    				t.closeSound.get((int)Math.round(Math.random()*t.closeSound.size()) ).Play();
                
    			e2.state = CEntity.STATE_CLOSING;
    		}
    		else
    		{
    			if(t.openSound.size() > 0)
    				t.openSound.get( (int)Math.round(Math.random()*t.openSound.size()) ).Play();
                
    			e2.state = CEntity.STATE_OPENING;
    		}
    	}
    }
}
