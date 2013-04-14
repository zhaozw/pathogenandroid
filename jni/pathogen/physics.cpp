

#include "physics.h"
#include "quake3bsp.h"
#include "entity.h"
#include "player.h"
#include "zombie.h"
#include "model.h"
#include "menu.h"
#include "gui.h"

CVector3 EMin(CEntity* e, CEntityType* t)
{
	CVector3 vMin = t->vMin;
    
	if(e->controller >= 0)
	{
		CPlayer* p = &g_player[e->controller];
        
		if(p->crouched)
			vMin.y += t->crouch;
	}
    
	return vMin;
}

// Test AABB against other AABB's and trimeshes
int Collides(int i, CVector3 prev)
{
    CEntity* e = &g_entity[i];
    CCamera* c = &e->camera;
    CEntityType* t = &g_entityType[e->type];
    CVector3 vMin = EMin(e, t);
    
    CVector3 prevcenter = (c->Position() + prev)/2.0f + (vMin + t->vMax)/2.0f;
    CVector3 center = c->Position() + (vMin + t->vMax)/2.0f;
    CVector3 radius = (t->vMax - vMin)/2.0f;
    
	CVector3 scaledown;
	scaledown.x = 1.0f / radius.x;
	scaledown.y = 1.0f / radius.y;
	scaledown.z = 1.0f / radius.z;
    
    CEntity* e2;
    CCamera* c2;
    CEntityType* t2;
    CVector3 center2;
    CVector3 radius2;
    CVector3 vMin2;
    CPlayer* p;
    
    for(int j=0; j<ENTITIES; j++)
    {
        if(i == j)
            continue;
        
        e2 = &g_entity[j];
        if(!e2->on)
            continue;
        
		if(!g_map.IsClusterVisible(e->cluster, e2->cluster) && !g_map.IsClusterVisible(e2->cluster, e->cluster))
			continue;
        
        c2 = &e2->camera;
        t2 = &g_entityType[e2->type];
        vMin = EMin(e2, t2);
        
        if(e2->controller >= 0)
        {
            p = &g_player[e2->controller];
            
			if(p->hp <= 0.0f)
				continue;
        }
        
        center2 = c2->Position() + (vMin + t2->vMax)/2.0f;
        radius2 = (t2->vMax - vMin)/2.0f;
        
        if((fabs(center.x-center2.x) < radius.x+radius2.x &&
           fabs(center.y-center2.y) < radius.y+radius2.y &&
           fabs(center.z-center2.z) < radius.z+radius2.z)
            ||
            (fabs(prevcenter.x-center2.x) < radius.x+radius2.x &&
            fabs(prevcenter.y-center2.y) < radius.y+radius2.y &&
            fabs(prevcenter.z-center2.z) < radius.z+radius2.z))
        {
            // AABB-to-polygon collision detection
            if(t2->category == DOOR || t2->category == FIXEDENT)
            {
                if(e2->Collision(scaledown, center))
                    return j;
                else if(e2->Collision(scaledown, prevcenter))
                    return j;
                else
                    continue;
            }
            
            return j;
        }
    }
    
    return -1;
}

// Test trimesh against non-fixed AABB's only
// Only checks BODY_LOWER model
int Collides2(int i)
{
	CEntity* e = &g_entity[i];
	CEntityType* t = &g_entityType[e->type];
	CModel* m = &g_model[t->model[BODY_LOWER]];
	CVertexArray* va = &m->vertexArrays[(int)e->frame[BODY_LOWER]];
	CTriangle* tris = new CTriangle[ m->numverts/3 ];
	CCamera* c = &e->camera;
    
	int j;
	int tri = 0;
	for(j=0; j<va->numverts; j+=3)
	{
		tris[tri].a = c->Position() + Rotate(va->vertices[j+0], c->Yaw(), 0, 1, 0);
		tris[tri].b = c->Position() + Rotate(va->vertices[j+1], c->Yaw(), 0, 1, 0);
		tris[tri].c = c->Position() + Rotate(va->vertices[j+2], c->Yaw(), 0, 1, 0);
		tri++;
	}
    
	CEntity* e2;
	CEntityType* t2;
	CCamera* c2;
	CVector3 center;
	CVector3 radius;
	CVector3 scaledown;
	CVector3 vMin;
	int k;
    
	for(j=0; j<ENTITIES; j++)
	{
		if(j == i)
			continue;
        
		e2 = &g_entity[j];
        
		if(!e2->on)
			continue;
        
		if(!g_map.IsClusterVisible(e->cluster, e2->cluster) && !g_map.IsClusterVisible(e2->cluster, e->cluster))
			continue;
        
		c2 = &e2->camera;
		t2 = &g_entityType[e2->type];
        
		if(t2->category == FIXEDENT)
			continue;
        
		if(t2->category == DOOR)
			continue;
        
        vMin = EMin(e2, t2);
        
		center = c2->Position() + (vMin + t2->vMax)/2.0f;
		radius = (t2->vMax - vMin)/2.0f;
        
		scaledown.x = 1.0f / radius.x;
		scaledown.y = 1.0f / radius.y;
		scaledown.z = 1.0f / radius.z;
        
		for(k=0; k<tri; k++)
			if(TriBoxOverlap2(scaledown, center, tris[k]))
			{
				delete [] tris;
				return j;
			}
	}
    
	delete [] tris;
	return -1;
}

void Bump(CEntity* e, int j)
{
    CEntity* e2 = &g_entity[j];
	CEntityType* t2 = &g_entityType[e2->type];
    
	if(t2->category == DOOR || t2->category == FIXEDENT)
	{
		//e->camera.Velocity( CVector3(0, 0, 0) );
		return;
	}
    
    CVector3 temp = e->camera.Velocity();
    e->camera.Velocity( e2->camera.Velocity() );
    e2->camera.Velocity( temp );
}

bool CollisionResponse(int i, CEntity* e, CCamera* c, CPlayer* p, CVector3 prev)
{
	int j = Collides(i, prev);
    
	if(j >= 0 && !CheckItem(e, j))
	{
		c->MoveTo(prev);
		Bump(e, j);
		e->cluster = g_map.FindCluster(c->Position());
		bool z = CheckZombie(i, j);
        
		if(e->controller >= 0 && !z)
			if(p->ai && p->target < 0)
				NewGoal(p);
        
		return true;
	}
    
	return false;
}

void Physics()
{
	CEntity* e;
	CEntityType* t;
	CCamera* c;
	CPlayer* p;
	CVector3 old;
	CVector3 trace;
	CVector3 vMin;
	float speed;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
        
		if(!e->on)
			continue;
        
		t = &g_entityType[e->type];
        
		if(t->category == DOOR || t->category == FIXEDENT)
			continue;

		if(t->category == ITEM)
			continue;
        
		c = &e->camera;
		speed = t->speed;
        
		vMin = EMin(e, t);
		old = c->Position();
        
		if(e->controller >= 0)
		{
			p = &g_player[e->controller];
            
			if(p->forward)
				c->Move(speed);
			if(p->backward)
				c->Move(-speed);
			if(p->left)
				c->Strafe(-speed);
			if(p->right)
				c->Strafe(speed);
			if(p->jump && c->Grounded())
				c->Rise(t->jump);
            //c->Rise(GRAVITY + 0.5f);
			if(p->crouching && !p->crouched)
			{
				if(c->Grounded())
				{
					old.y -= t->crouch;
					c->MoveTo(old);
				}
                
				p->crouched = true;
				p->run = false;
			}
			if(p->crouched)
				speed /= 2.0f;
			else if(p->run && p->stamina > 0.0f)
				speed *= 2.0f;
		}
        
		c->LimitHVel(speed);
		c->Step();
        
		trace = g_map.TraceBox(old, c->Position(), vMin, t->vMax, t->maxStep);
        
		if(g_map.Ladder())
		{
			CVector3 v = c->Velocity();
			v.y += (c->View().y - c->Position().y) * 50.0f;
			//v.y += 50.0f;
			v.y = Clip(v.y, -speed/3.0f, speed/3.0f);
			c->Velocity(v);
		}
        
		if(e->controller >= 0 && (trace.x != c->Position().x || trace.z != c->Position().z))
			if(p->ai && p->target < 0)
				NewGoal(p);
        
		c->MoveTo(trace);
        
		e->cluster = g_map.FindCluster(c->Position() + t->vCenterOff);
        
		if(CollisionResponse(i, e, c, p, old))
			continue;
        
		c->Grounded( g_map.IsOnGround() );
        
		if(!c->Grounded() && !g_map.Ladder())
			c->Rise(-GRAVITY);
		//else
		c->Friction();
        
		if(e->controller >= 0)
		{
			if(!p->crouching && p->crouched)
			{
				CVector3 newP = trace + CVector3(0, t->crouch, 0);
				newP = g_map.TraceBox(newP, newP, t->vMin, t->vMax, t->maxStep);
                
				if(!g_map.Collided() && !g_map.Stuck())
				{
					c->MoveTo(newP);
					p->crouched = false;
                    
					if(CollisionResponse(i, e, c, p, trace))
						continue;
                    
					c->Grounded( g_map.IsOnGround() );
				}
			}
		}
	}
}