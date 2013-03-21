package com.pathogengame.pathogen;

public class Click_Action extends CFuncPtr
{
	public Click_Action(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(g_mode != PLAY)
    		return;
        
    	if(g_arrest)
    		return;
        
    	CPlayer* p = &g_player[g_localP];
    	CEntity* e = &g_entity[p->entity];
    	CCamera* c = &e->camera;
        
    	CVector3 vLine[2];
    	vLine[0] = c->Position();
    	CVector3 d = Normalize(c->View() - c->Position());
    	vLine[1] = c->Position() + d * INTERACTION_D;
    	vLine[1] = g_map.TraceRay(vLine[0], vLine[1]);
        
    	CEntity* e2;
    	int hit = -1;
    	CVector3 trace;
        
    	for(int i=0; i<ENTITIES; i++)
    	{
    		e2 = &g_entity[i];
            
    		if(!e2->on)
    			continue;
            
    		if(i == p->entity)
    			continue;
            
    		trace = e2->TraceRay(vLine);
            
    		if(trace == vLine[1])
    			continue;
            
    		hit = i;
    		vLine[1] = trace;
    	}
        
    	if(hit < 0)
    		return;
        
    	e2 = &g_entity[hit];
    	CEntityType* t = &g_entityType[e2->type];
        
    	if(t->category == ENTITY::DOOR)
    	{
    		if(e2->state == STATE_OPENING)
    		{
    			if(t->closeSound.size() > 0)
    				t->closeSound[ rand()%t->closeSound.size() ].Play();
                
    			e2->state = STATE_CLOSING;
    		}
    		else
    		{
    			if(t->openSound.size() > 0)
    				t->openSound[ rand()%t->openSound.size() ].Play();
                
    			e2->state = STATE_OPENING;
    		}
    	}
    }
}
