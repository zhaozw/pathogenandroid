
#include "entity.h"
#include "3dmath.h"
#include "player.h"
#include "model.h"
#include "frustum.h"
#include "billboard.h"
#include "skybox.h"
#include "image.h"
#include "quake3bsp.h"
#include "animations.h"
#include "physics.h"
#include "script.h"
#include "gui.h"
#include "menu.h"
#include "shader.h"

vector<CEntityType> g_entityType;
CEntity g_entity[ENTITIES];
vector<CSpawn> g_sspawn; //story spawn
vector<CSpawn> g_spawn;  //human spawn
vector<CSpawn> g_zspawn; //zombie spawn

int g_lastEnt = -1;

CVector3 CEntity::TraceRay(CVector3 vLine[])
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

//bool TriBoxOverlap(CVector3 vPos, CVector3 vMin, CVector3 vMax, CVector3* vTri);
bool CEntity::Collision(CVector3 vScaleDown, CVector3 vCenter)
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

void DrawHands()
{
	if(g_viewmode == THIRDPERSON)
		return;
    
	glClear(GL_DEPTH_BUFFER_BIT);
    
    CEntity* e;
    CEntityType* t;
    CCamera* c;
    CModel* m;
    CVector3 bounds[2];
    
	CPlayer* p = &g_player[g_localP];
	CHold* h;
	int item;
	CItemType* iT;
	CModel* iM;
    
	float precolor[] = {1,1,1,1};
    
    if(g_reddening > 0.0f)
    {
		precolor[1] = 1.0f - g_reddening;
		precolor[2] = 1.0f - g_reddening;
	}
    
	float colorf[] = {1, 1, 1, 1};
	CVector3 colorv;
    
	int i = p->entity;
    
	e = &g_entity[i];
    c = &e->camera;
    t = &g_entityType[e->type];
    
	colorv = g_map.LightVol((c->Position()) + (t->vCenterOff));
	colorf[0] = colorv.x;
	colorf[1] = precolor[1] * colorv.y;
	colorf[2] = precolor[2] * colorv.z;
    glUniform4f(g_slots[MODEL][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
    
    m = &g_model[t->model[BODY_UPPER]];
    m->Draw(e->frame[BODY_UPPER], c->Position(), -c->Pitch(), c->Yaw());
    //m->Draw(e->frame[BODY_UPPER], c->Position(), -c->Pitch(), 0);
    
	if(p->equipped < 0)
		return;
    
	h = &p->items[p->equipped];
	item = h->type;
	iT = &g_itemType[item];
	iM = &g_model[iT->model];
	iM->Draw(e->frame[BODY_UPPER], c->Position(), -c->Pitch(), c->Yaw());
}

void SortEntities()
{
    return;
    
    static int sort = -1;
	sort++;
    
	CVector3 pos = g_camera->Position();
	//int cluster = g_map.FindCluster(pos);
    
    CEntity* e;
	CCamera* c;
    CEntityType* t;
    CModel* m;
	bool foundt;
    
	vector<int> is;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
        
		if(!e->on)
			continue;
        
		t = &g_entityType[e->type];
        
		foundt = false;
        
		if(t->model[BODY_LOWER] >= 0)
		{
			m = &g_model[t->model[BODY_LOWER]];
			if(m->transp)
				foundt = true;
		}
        
        if(t->model[BODY_UPPER] >= 0)
        {
            m = &g_model[t->model[BODY_UPPER]];
			if(m->transp)
				foundt = true;
		}
        
		if(!foundt)
			continue;
        
		c = &e->camera;
        
		/*
         e->cluster = g_map.FindCluster(c->Position());
         if(!g_map.IsClusterVisible(cluster, e->cluster))
         continue;*/
        
		e->dist = Magnitude2(pos - c->Position());
        
		is.push_back(i);
	}
    
	CEntity temp;
	int leftoff = 0;
	int last = -1;
	bool backtracking = false;
	CEntity* e2 = NULL;
	int earliest = 0;

	for(int i=0; i<ENTITIES; i++)
	{
        //if(sort > 100)
        //    return;
        
        //NSLog(@"sort %d", i);
        //sort++;
        
		if(i >= earliest)
		{
			e = &g_entity[i];
            
			if(!e->on)
				continue;
            
			t = &g_entityType[e->type];
            
			foundt = false;
            
			if(t->model[BODY_LOWER] >= 0)
			{
				m = &g_model[t->model[BODY_LOWER]];
				if(m->transp)
					foundt = true;
			}
            
			if(t->model[BODY_UPPER] >= 0)
			{
				m = &g_model[t->model[BODY_UPPER]];
				if(m->transp)
					foundt = true;
			}
            
			if(!foundt)
			{
                //NSLog(@"!foundt %d", i);
                
				if(backtracking)
                {
                    //NSLog(@"backtracking %d", i);
					i-=2;
                }
                
				continue;
			}
            
            //NSLog(@"foundt %d", i);
            
			c = &e->camera;
            
			/*
             if(!g_map.IsClusterVisible(cluster, e->cluster))
             {
             if(backtracking)
             i-=2;
             
             continue;
             }*/
            
			/*
             g_log<<"?";
             g_log.flush();
             
             g_log<<"1 @ i="<<i<<endl;
             g_log.flush();*/
            
			if(e2 == NULL)
			{
                //NSLog(@"e2 == NULL %d", i);
				e2 = e;
				last = i;
				earliest = i;
				continue;
			}
            
			if(e == e2)
            {
                //NSLog(@"e == e2 %d", i);
                
                if(backtracking)
                {
                   // NSLog(@"backtracking2 %d", i);
					i-=2;
                }
                
				continue;
            }
            
			if((!backtracking && e->dist > e2->dist) || (backtracking && e2->dist > e->dist))
			{
                //if(!backtracking && e->dist > e2->dist)
                //    NSLog(@"(!backtracking && e->dist > e2->dist) %d", i);
                //else if(backtracking && e2->dist > e->dist)
                //    NSLog(@"(backtracking && e2->dist > e->dist) %d", i);
                
				/*
                 //g_log<<"SORT["<<sort<<"] ["<<i<<"].dist("<<e->dist<<") > ["<<last<<"].dist("<<e2->dist<<")"<<endl;
                 
                 for(int j=0; j<is.size(); j++)
                 {
                 if(is[j] == i)
                 {
                 g_log<<"[#"<<sort<<"] g_entity["<<is[j]<<"].dist = "<<g_entity[is[j]].dist<<" <<<<<< i"<<endl;
                 }
                 else if(is[j] == last)
                 {
                 g_log<<"[#"<<sort<<"] g_entity["<<is[j]<<"].dist = "<<g_entity[is[j]].dist<<" <<<<<< last"<<endl;
                 }
                 else
                 {
                 g_log<<"[#"<<sort<<"] g_entity["<<is[j]<<"].dist = "<<g_entity[is[j]].dist<<endl;
                 }
                 }
                 
                 g_log<<"============================================="<<endl;
                 
                 g_log.flush();*/
                
				if(!backtracking)
				{
					leftoff = i;
					backtracking = true;
				}
                
				temp = *e;
				(*e) = *e2;
				(*e2) = temp;
                
				if(e->controller >= 0)
				{
					g_player[e->controller].entity = i;
                    
					if(e->controller == g_localP)
						g_camera = &e->camera;
				}
                
				if(e2->controller >= 0)
				{
					g_player[e2->controller].entity = last;
                    
					if(e2->controller == g_localP)
						g_camera = &e2->camera;
				}
                
				//temp = g_entity[i];
				//g_entity[i] = g_entity[last];
				//g_entity[last] = temp;
                
				if(last < i)
				{
					i-=3;
				}
				else
				{
					e2 = e;
					last = i;
					i-=2;
				}
                
				//i = leftoff-1;
			}
			else
			{
                //NSLog(@"else %d", i);
                
				//g_log<<"else"<<endl;
				//g_log.flush();
                
				if(backtracking)
				{
                    
                    //g_log<<"back to a"<<endl;
                    //g_log.flush();
                    
					backtracking = false;
					i = leftoff;
					e2 = &g_entity[i];
					last = i;
				}
				else
				{
                    //g_log<<"else 2"<<endl;
                    //g_log.flush();
                    
					e2 = e;
					last = i;
				}
			}
		}
		else
		{
            
            //g_log<<"2"<<endl;
            //g_log.flush();
            
			backtracking = false;
			i = leftoff;
			e2 = &g_entity[i];
			last = i;
		}
	}
    
	/*
     float lastd = 999999999999999.0f;
     
     for(int i=0; i<ENTITIES; i++)
     {
     e = &g_entity[i];
     
     if(!e->on)
     continue;
     
     t = &g_entityType[e->type];
     
     foundt = false;
     
     if(t->model[BODY_LOWER] >= 0)
     {
     m = &g_model[t->model[BODY_LOWER]];
     if(m->transp)
     foundt = true;
     }
     
     if(t->model[BODY_UPPER] >= 0)
     {
     m = &g_model[t->model[BODY_UPPER]];
     if(m->transp)
     foundt = true;
     }
     
     if(!foundt)
     continue;
     
     c = &e->camera;
     cluster2 = g_map.FindCluster(c->Position());
     if(!g_map.IsClusterVisible(cluster, cluster2))
     continue;
     
     if(e->dist > lastd)
     {
     g_log<<"SORT["<<sort<<"] out of order ["<<i<<"].dist("<<e->dist<<") > ["<<last<<"].dist("<<g_entity[last].dist<<")"<<endl;
     g_log.flush();
     break;
     }
     
     lastd = e->dist;
     last = i;
     }*/
}

void DrawEntities(bool transp)
{
    CEntity* e;
    CEntityType* t;
    CCamera* c;
    CModel* m;
    CVector3 bounds[2];
    
	int localE = g_player[g_localP].entity;
	CPlayer* p;
	CHold* h;
	int item;
	CItemType* iT;
	CModel* iM;
    
    float precolor[] = {1,1,1,1};
    
    if(g_reddening > 0.0f)
    {
		precolor[1] = 1.0f - g_reddening;
		precolor[2] = 1.0f - g_reddening;
	}
    
	float colorf[] = {1, 1, 1, 1};
	CVector3 colorv;
    
	bool foundt;	// found transparency?
    
    for(int i=0; i<ENTITIES; i++)
    {
        e = &g_entity[i];
        if(!e->on)
            continue;
        
        c = &e->camera;
        t = &g_entityType[e->type];
        
		if(e->controller >= 0)
			p = &g_player[e->controller];
        
        if(i == localE && g_viewmode == FIRSTPERSON)
            continue;
        
		foundt = false;
        
		if(t->model[BODY_LOWER] >= 0)
		{
			m = &g_model[t->model[BODY_LOWER]];
			if(m->transp)
				foundt = true;
		}
        
        if(t->model[BODY_UPPER] >= 0)
        {
            m = &g_model[t->model[BODY_UPPER]];
			if(m->transp)
				foundt = true;
		}
        
		if(foundt != transp)
			continue;
        
        if(e->nolightvol)
			colorv = CVector3(1, 1, 1);
		else
			colorv = g_map.LightVol(c->Position() + t->vCenterOff);
        
		colorf[0] = colorv.x;
		colorf[1] = precolor[1] * colorv.y;
		colorf[2] = precolor[2] * colorv.z;
        
        glUniform4f(g_slots[MODEL][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
        
        bounds[0] = c->Position() + t->vMin;
        bounds[1] = c->Position() + t->vMax;
        
		if(!g_frustum.BoxInFrustum(bounds[0].x, bounds[0].y, bounds[0].z, bounds[1].x, bounds[1].y, bounds[1].z))
			continue;
        
        if(t->model[BODY_LOWER] >= 0)
        {
            m = &g_model[t->model[BODY_LOWER]];
            m->Draw(e->frame[BODY_LOWER], c->Position(), 0, c->Yaw());
        }
        if(t->model[BODY_UPPER] >= 0)
        {
            m = &g_model[t->model[BODY_UPPER]];
            m->Draw2(e->frame[BODY_UPPER], c->Position(), -c->Pitch(), c->Yaw());
        }
        
		if(e->controller >= 0 && p->equipped >= 0)
		{
			h = &p->items[p->equipped];
			item = h->type;
			iT = &g_itemType[item];
			iM = &g_model[iT->model];
			iM->Draw2(e->frame[BODY_UPPER], c->Position(), -c->Pitch(), c->Yaw());
		}
    }
    
    glUniform4f(g_slots[MODEL][COLOR], precolor[0], precolor[1], precolor[2], precolor[3]);
}

int NewEntity()
{
	for(int i=0; i<ENTITIES; i++)
		if(!g_entity[i].on)
			return i;
    
	return -1;
}

bool PlaceEntity(int type, int controller, float amount, float clip, CVector3 pos, float yaw, int* ID, bool nolightvol, int script)
{
	int i = NewEntity();
	if(i < 0)
		return i;
    
    CEntity* e = &g_entity[i];

	e->on = true;
	e->frame[BODY_LOWER] = 0;
	e->frame[BODY_UPPER] = 0;
	e->type = type;
	e->controller = controller;
	e->amount = amount;
	e->clip = clip;
	e->state = STATE_NONE;
	e->nolightvol = nolightvol;
	e->script = script;
    
    CCamera* c = &e->camera;
    
	c->PositionCamera(pos.x, pos.y, pos.z, pos.x+1, pos.y, pos.z, 0, 1, 0);
	c->RotateView(DEGTORAD(yaw), 0, 1, 0);
    
	CEntityType* t = &g_entityType[type];
	e->cluster = g_map.FindCluster(c->Position() + t->vCenterOff);
    
	CPlayer* p = NULL;
	if(controller >= 0)
	{
		g_player[controller].entity = i;
		p = &g_player[controller];
	}
    
	if(ID)
		(*ID) = i;
    
	if(controller >= 0)
		p->goal = pos;
    
    return true;
}

int Entity(int category, const char* lowermodel, float animrate, int collider)
{
	g_entityType.push_back(CEntityType());
	int i = g_entityType.size() - 1;
	CEntityType* t = &g_entityType[i];
    
	char raw[32];
	StripPathExtension(lowermodel, raw);
	strcpy(t->lmodel, raw);
	t->model[BODY_LOWER] = LoadModel(raw, CVector3(1,1,1));
	t->model[BODY_UPPER] = -1;
    
	/*
    //if(strstr(lowermodel, "bed"))
    if(false)
    {
        NSLog(@"=======");
        NSLog(@"entity raw = %s", raw);
        NSLog(@"t->lmodel = %@", t->lmodel);
        NSLog(@"entity lmodel = %d", t->model[BODY_LOWER]);
        
        NSLog(@"entity lmodel name = %@", g_model[t->model[BODY_LOWER]].name);
    }*/
    
	t->category = category;
	ModelMinMax(t->model[BODY_LOWER], &t->vMin, &t->vMax);
	t->maxStep = 15;
	t->speed = 200;
	t->jump = 0;
	t->crouch = 0;
	t->animrate = animrate;
	t->vCenterOff = (t->vMin + t->vMax)/2.0f;
	t->collider = collider;
    
	g_lastEnt = i;
    
	return i;
}

void Entity(int category, int item, const char* lowermodel, const char* uppermodel, CVector3 scale, CVector3 translate, CVector3 vMin, CVector3 vMax, float maxStep, float speed, float jump, float crouch, float animrate)
{
    g_entityType.push_back(CEntityType());
	int i = g_entityType.size() - 1;
	CEntityType* t = &(g_entityType[i]);
    
    strcpy(t->lmodel, lowermodel);
    
    if(stricmp(lowermodel, "") == 0)
        t->model[BODY_LOWER] = -1;
    else
    {
        char raw[32];
		StripPathExtension(lowermodel, raw);
        t->model[BODY_LOWER] = LoadModel(raw, scale);
    }
    
    if(stricmp(uppermodel, "") == 0)
        t->model[BODY_UPPER] = -1;
    else
    {
        char raw[32];
		StripPathExtension(uppermodel, raw);
        t->model[BODY_UPPER] = LoadModel(raw, scale);
    }

	t->category = category;
	t->item = item;
    t->vMin = vMin;
	t->vMax = vMax;
	t->maxStep = maxStep;
	t->speed = speed;
	t->jump = jump;
	t->crouch = crouch;
	t->animrate = animrate;
    t->collider = -1;
}

int EntityID(const char* lmodel)
{
    char raw[64];
	StripPathExtension(lmodel, raw);
    for(int i=0; i<g_entityType.size(); i++)
        if(stricmp(g_entityType[i].lmodel, raw) == 0)
            return i;
    
    return -1;
}

void Entities()
{
    CVector3 charMin = CVector3(-10, -50, -10);
    CVector3 charMax = CVector3(10, 5, 10);
    float crouch = 14.29f;
    
	Entity(HUMAN, -1, "human2lower", "human2upper", CVector3(1, 1, 1), CVector3(0, 0, 0), charMin, charMax, 15, 100, 100, crouch, 3.0f);
	Entity(ZOMBIE, -1, "zombie2lower", "zombie2upper", CVector3(1, 1, 1), CVector3(0, 0, 0), charMin, charMax, 15, 50, 100, crouch, 1.5f);
	Entity(ZOMBIE, -1, "zombie3lower", "zombie3upper", CVector3(1, 1, 1), CVector3(0, 0, 0), charMin, charMax, 15, 50, 100, crouch, 1.5f);
    
    //void Entity(int category, int item, NSString* lowermodel, NSString* uppermodel, CVector3 scale, CVector3 translate, CVector3 vMin, CVector3 vMax, float maxStep, float speed, float jump, float crouch, float animrate)
    
	Entity(ITEM, MP5, "mp5", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-10, 0, -10), CVector3(10, 10, 10), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, PRIMARYAMMO, "ammo1", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
    
	Entity(ITEM, MOSSBERG500, "mossberg500", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-10, 0, -10), CVector3(10, 10, 10), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, M1911, "m1911", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-10, 0, -10), CVector3(10, 10, 10), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, SECONDARYAMMO, "ammo2", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, TERTAMMO, "ammo3", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, BBAT, "bbat", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, KNIFE, "knife", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
	Entity(ITEM, WTALKIE, "wtalkie", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-4.2f, 0, -4.2f), CVector3(4.2f, 2.3f, 4.2f), 15, 200, 100, crouch, 1.0f);
    
    
	Entity(NOCAT, -1, "washmchn", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-17.24f, -17.24f, -17.24f), CVector3(17.24f, 17.24f, 17.24f), 15, 200, 100, crouch, 1.0f);
	Entity(NOCAT, -1, "dryer", "", CVector3(1, 1, 1), CVector3(0, 0, 0), CVector3(-17.24f, -17.24f, -17.24f), CVector3(17.24f, 17.24f, 17.24f),  15, 200, 100, crouch, 1.0f);
}

void Swizzle(CVector3 &v)
{
	float temp = v.z;
	v.z = -v.y;
	v.y = temp;
}

void Swizzle(CVector3 &vmin, CVector3 &vmax)
{
	float temp = vmin.z;
	vmin.z = -vmin.y;
	vmin.y = temp;
    
	temp = vmax.z;
	vmax.z = -vmax.y;
	vmax.y = temp;
    
	float tempmin = min(vmin.z, vmax.z);
	float tempmax = max(vmin.z, vmax.z);
	vmin.z = tempmin;
	vmax.z = tempmax;
}

bool CheckEntitySound(vector<CSound>* vec, const char* file)
{
	for(int i=0; i<(*vec).size(); i++)
		if(!stricmp((*vec)[i].file, file))
			return true;
    
	return false;
}

void EntitySound(int category, const char* file)
{
	CEntityType* t = &g_entityType[g_lastEnt];
	vector<CSound>* vec;
    
	if(category == CLOSESND)
		vec = &t->closeSound;
	else if(category == OPENSND)
		vec = &t->openSound;
    
	char raw[64];
	StripPathExtension(file, raw);
    
	if(CheckEntitySound(vec, raw))
		return;
    
	(*vec).push_back(CSound(raw));
}

void ReadEntity(string classname, string origin, string angle, string model, string size, string type, string sky, string count, string clip, string collider,
				string opensound, string closesound, string activity, string nolightvol, string bbmin, string bbmax, string map, string script)
{
    bool nolvol = false;
    
	if(stricmp(nolightvol.c_str(), "true") == 0)
		nolvol = true;
    
	int scrpt = -1;
	if(stricmp(script.c_str(), "") != 0)
		sscanf(script.c_str(), "%d", &scrpt);
    
	if(stricmp(classname.c_str(), "info_player_start") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		pos.y += 50.0f - 27.5f + 4.0f;
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		CSpawn spawn(pos, ang, -1, -1);
		g_sspawn.push_back(spawn);
	}
	if(stricmp(classname.c_str(), "info_player_deathmatch") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		float temp = pos.z;
		pos.z = -pos.y;
		pos.y = temp;
		pos.y += 50.0f - 27.5f + 4.0f;
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		CSpawn spawn(pos, ang, -1, -1);
		g_spawn.push_back(spawn);
	}
    else if(stricmp(classname.c_str(), "info_player_zombie") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		pos.y += 50.0f - 27.5f + 4.0f;
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		//ang += DEGTORAD(180);
		int act = ONSIGHT;
        
		if(stricmp(activity.c_str(), "none") == 0)
			act = NOACT;
		else if(stricmp(activity.c_str(), "onsight") == 0)
			act = ONSIGHT;
        
		CSpawn spawn(pos, ang, act, scrpt);
		g_zspawn.push_back(spawn);
	}
    
	else if(stricmp(classname.c_str(), "func_map") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
        
		CVector3 vmin, vmax;
		sscanf(bbmin.c_str(), "%f %f %f", &vmin.x, &vmin.y, &vmin.z);
		sscanf(bbmax.c_str(), "%f %f %f", &vmax.x, &vmax.y, &vmax.z);
		Swizzle(vmin, vmax);
        
		CFuncMap funcmap(pos, vmin, vmax, map.c_str());
		g_funcmap.push_back(funcmap);
	}
	else if(stricmp(classname.c_str(), "func_proxy") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
        
		CVector3 vmin, vmax;
		sscanf(bbmin.c_str(), "%f %f %f", &vmin.x, &vmin.y, &vmin.z);
		sscanf(bbmax.c_str(), "%f %f %f", &vmax.x, &vmax.y, &vmax.z);
		Swizzle(vmin, vmax);
        
		CFuncProxy funcproxy(pos, vmin, vmax, scrpt);
		g_funcproxy.push_back(funcproxy);
	}
	else if(stricmp(classname.c_str(), "_entity") == 0 || stricmp(classname.c_str(), "item_enviro") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		int eid = EntityID(model.c_str());
        
		if(eid < 0)
			eid = Entity(NOCAT, model.c_str(), 1, -1);
        
		float amt = 1;
        
		PlaceEntity(eid, -1, amt, -1, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "weapon_mp5") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		float clp = 30;
		if(stricmp(clip.c_str(), "") != 0)
			sscanf(clip.c_str(), "%f", &clp);
        
		int eid = EntityID("mp5");
        
		PlaceEntity(eid, -1, 1, clp, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "weapon_mossberg500") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		float clp = 6;
		if(stricmp(clip.c_str(), "") != 0)
			sscanf(clip.c_str(), "%f", &clp);
        
		int eid = EntityID("mossberg500");
        
		PlaceEntity(eid, -1, 1, clp, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "weapon_m1911") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		float clp = 7;
		if(stricmp(clip.c_str(), "") != 0)
			sscanf(clip.c_str(), "%f", &clp);
        
		int eid = EntityID("m1911");
        
		PlaceEntity(eid, -1, 1, clp, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "weapon_bat") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		float clp = 0;
        
		int eid = EntityID("bbat");
        
		PlaceEntity(eid, -1, 1, clp, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "weapon_knife") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		float clp = 0;
        
		int eid = EntityID("knife");
        
		PlaceEntity(eid, -1, 1, clp, pos, ang, NULL, nolvol, scrpt);
	}
	else if(stricmp(classname.c_str(), "ammo_primary") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		float amt;
		sscanf(count.c_str(), "%f", &amt);
        
		int eid = EntityID("ammo1");
        
		//if(eid < 0)
		//	eid = Entity(ENTITY::ITEM, "mp5", 1);
        
		PlaceEntity(eid, -1, amt, -1, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "ammo_secondary") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		float amt;
		sscanf(count.c_str(), "%f", &amt);
        
		int eid = EntityID("ammo2");
        
		//if(eid < 0)
		//	eid = Entity(ENTITY::ITEM, "mp5", 1);
        
		PlaceEntity(eid, -1, amt, -1, pos, ang, NULL, nolvol, scrpt);
	}
	else if(stricmp(classname.c_str(), "ammo_tertiary") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
		float amt;
		sscanf(count.c_str(), "%f", &amt);
        
		int eid = EntityID("ammo3");
        
		//if(eid < 0)
		//	eid = Entity(ENTITY::ITEM, "mp5", 1);
        
		PlaceEntity(eid, -1, amt, -1, pos, ang, NULL, nolvol, scrpt);
	}
	else if(stricmp(classname.c_str(), "fixed_entity") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		int eid = EntityID(model.c_str());
        
		if(eid < 0)
		{
			int coli = -1;
            
			if(stricmp(collider.c_str(), "") != 0)
			{
				char colmdl[128];
				strcpy(colmdl, collider.c_str());
				coli = LoadModel(colmdl, CVector3(1,1,1));
			}
            
			eid = Entity(FIXEDENT, model.c_str(), 1, coli);
		}
        
		PlaceEntity(eid, -1, -1, -1, pos, ang, NULL, nolvol, scrpt);
	}
	else if(stricmp(classname.c_str(), "func_door") == 0)
	{
		CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		Swizzle(pos);
		float ang;
		sscanf(angle.c_str(), "%f", &ang);
        
		int eid = EntityID(model.c_str());
        
		if(eid < 0)
			eid = Entity(DOOR, model.c_str(), 1, -1);
            //eid = Entity(ENTITY::FIXEDENT, model.c_str(), 1, -1);
        
		g_lastEnt = eid;
        
		if(stricmp(opensound.c_str(), ""))
			EntitySound(OPENSND, opensound.c_str());
        
		if(stricmp(closesound.c_str(), ""))
			EntitySound(CLOSESND, closesound.c_str());
        
		PlaceEntity(eid, -1, -1, -1, pos, ang, NULL, nolvol, scrpt);
	}
    else if(stricmp(classname.c_str(), "_billboard") == 0)
    {
        //void PlaceBillboard(const char* name, CVector3 pos, float size)
        CVector3 pos;
		sscanf(origin.c_str(), "%f %f %f", &pos.x, &pos.y, &pos.z);
		float temp = pos.z;
		pos.z = -pos.y;
		pos.y = temp;

        float sizef;
        sscanf(size.c_str(), "%f", &sizef);
        
		PlaceBillboard(type.c_str(), pos, sizef, -1, nolvol);
    }
	else if(stricmp(classname.c_str(), "worldspawn") == 0)
	{
		if(stricmp(sky.c_str(), "") != 0)
			LoadSkyBox(sky.c_str());
	}
}

void ReadEntities(char* str)
{
	g_spawn.clear();
	g_zspawn.clear();
    
	string classname;
	string origin;
	string angle;
	string model;
    string size;
	string type;
	string sky;
	string count;
	string clip;
	string collider;
	string opensound;
	string closesound;
	string activity;
	string nolightvol;
	string bbmin;
	string bbmax;
	string map;
	string script;
	string var;
	string val;
    
	int len = strlen(str);
    
	for(int i=0; i<len; i++)
	{
		classname = "";
		angle = "";
		origin = "";
		model = "";
		size = "";
		type = "";
		sky = "";
		count = "";
		clip = "";
		collider = "";
		opensound = "";
		closesound = "";
		activity = "";
		nolightvol = "";
		bbmin = "";
		bbmax = "";
		script = "";
		map = "";
        
		for(; i<len; i++)
			if(str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r')
				break;
        
		for(; i<len; i++)
			if(str[i] == '{')
			{
				i++;
				break;
			}
        
		for(; i<len; i++)
			if(str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r')
				break;
        
		while(str[i] != '}' && i < len)
		{
			var = "";
			val = "";
            
			for(; i<len; i++)
				if(str[i] == '"')
				{
					i++;
					break;
				}
            
			for(; i<len; i++)
			{
				if(str[i] == '"')
				{
					i++;
					break;
				}
                
				var = var + str[i];
			}
            
			for(; i<len; i++)
				if(str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r')
					break;
            
			for(; i<len; i++)
				if(str[i] == '"')
				{
					i++;
					break;
				}
            
			for(; i<len; i++)
			{
				if(str[i] == '"')
				{
					i++;
					break;
				}
                
				val = val + str[i];
			}
            
			if(stricmp(var.c_str(), "classname") == 0)
				classname = val;
			else if(stricmp(var.c_str(), "origin") == 0)
				origin = val;
			else if(stricmp(var.c_str(), "angle") == 0)
				angle = val;
			else if(stricmp(var.c_str(), "model") == 0)
				model = val;
			else if(stricmp(var.c_str(), "size") == 0)
				size = val;
			else if(stricmp(var.c_str(), "type") == 0)
				type = val;
			else if(stricmp(var.c_str(), "sky") == 0)
				sky = val;
			else if(stricmp(var.c_str(), "count") == 0)
				count = val;
			else if(stricmp(var.c_str(), "clip") == 0)
				clip = val;
			else if(stricmp(var.c_str(), "collider") == 0)
				collider = val;
			else if(stricmp(var.c_str(), "opensound") == 0)
				opensound = val;
			else if(stricmp(var.c_str(), "closesound") == 0)
				closesound = val;
			else if(stricmp(var.c_str(), "activity") == 0)
				activity = val;
			else if(stricmp(var.c_str(), "nolightvol") == 0)
				nolightvol = val;
			else if(stricmp(var.c_str(), "min") == 0)
				bbmin = val;
			else if(stricmp(var.c_str(), "max") == 0)
				bbmax = val;
			else if(stricmp(var.c_str(), "map") == 0)
				map = val;
			else if(stricmp(var.c_str(), "script") == 0)
				script = val;
            
			for(; i<len; i++)
				if(str[i] != ' ' && str[i] != '\t' && str[i] != '\n' && str[i] != '\r')
					break;
		}
        
		ReadEntity(classname, origin, angle, model, size, type, sky, count, clip, collider, opensound, closesound, activity, nolightvol, bbmin, bbmax, map, script);
	}
}

bool IsHuman(int type)
{
	CEntityType* t = &g_entityType[type];
    
	if(t->category == HUMAN)
		return true;
    
    return false;
}

bool IsZombie(int type)
{
	CEntityType* t = &g_entityType[type];
    
	if(t->category == ZOMBIE)
		return true;
    
    return false;
}

int RandomHuman()
{
	vector<int> h;
	CEntityType* t;
    
	for(int i=0; i<g_entityType.size(); i++)
	{
		t = &g_entityType[i];
        
		if(t->category != HUMAN)
			continue;
        
		h.push_back(i);
	}
    
	return h[ rand()%h.size() ];
}

int RandomZombie()
{
	vector<int> z;
	CEntityType* t;
    
	for(int i=0; i<g_entityType.size(); i++)
	{
		t = &g_entityType[i];
        
		if(t->category != ZOMBIE)
			continue;
        
		z.push_back(i);
	}
    
	return z[ rand()%z.size() ];
}

void UpdateDoor(CEntity* e, int i)
{
	if(e->state == STATE_OPENING)
	{
		if(e->frame[BODY_LOWER] >= ANIM_OPENING_E)
		{
			if(e->script > 0)
			{
				DoScriptFunc(e->script);
				e->script = -1;
			}
            
			return;
		}
        
		e->frame[BODY_LOWER] += 1;
        
		if(Collides2(i) >= 0)
			e->frame[BODY_LOWER] -= 1;
	}
	else if(e->state == STATE_CLOSING)
	{
		if(e->frame[BODY_LOWER] <= ANIM_OPENING_S)
			return;
        
		e->frame[BODY_LOWER] -= 1;
        
		if(Collides2(i) >= 0)
			e->frame[BODY_LOWER] += 1;
	}
}

void UpdateObjects()
{
	CEntity* e;
	CEntityType* t;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
		if(!e->on)
			continue;
        
		t = &g_entityType[e->type];
        
		if(t->category == DOOR)
			UpdateDoor(e, i);
	}
}