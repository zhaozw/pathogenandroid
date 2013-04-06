

#include "billboard.h"
#include "3dmath.h"
#include "image.h"
#include "animations.h"
#include "player.h"
#include "item.h"
#include "entity.h"
#include "particle.h"
#include "image.h"
#include "main.h"
#include "menu.h"
#include "quake3bsp.h"
#include "shader.h"
#include "logger.h"

vector<CBillboardType> g_billbT;
CBillboard g_billb[BILLBOARDS];
unsigned int g_muzzle[4];

void Effects()
{
	g_muzzle[0] = CreateTexture("effects/muzzle0");
	g_muzzle[1] = CreateTexture("effects/muzzle1");
	g_muzzle[2] = CreateTexture("effects/muzzle2");
	g_muzzle[3] = CreateTexture("effects/muzzle3");
}

int NewBillboard(char* tex)
{
    CBillboardType t;
	char rawtex[64];
	StripPathExtension(tex, rawtex);
	strcpy(t.name, rawtex);
	char fullpath[128];
	sprintf(fullpath, "billboards/%s", rawtex);
    t.tex = CreateTexture(fullpath);
	g_billbT.push_back(t);
	return g_billbT.size() - 1;
}

int Billboard(const char* name)
{
	char rawname[64];
	StripPathExtension(name, rawname);
    
    for(int i=0; i<g_billbT.size(); i++)
    {
        if(!stricmp(g_billbT[i].name, rawname))
            return i;
    }
    
    return NewBillboard(rawname);
}

int NewBillboard()
{
    for(int i=0; i<BILLBOARDS; i++)
        if(!g_billb[i].on)
            return i;
    
    return -1;
}

void PlaceBillboard(const char* n, CVector3 pos, float size, int particle, bool nolightvol)
{
    int type = Billboard(n);
    if(type < 0)
        return;
    
    PlaceBillboard(type, pos, size, particle, nolightvol);
}

void PlaceBillboard(int type, CVector3 pos, float size, int particle, bool nolightvol)
{
    int i = NewBillboard();
    if(i < 0)
        return;
    
    CBillboard* b = &g_billb[i];
    b->on = true;
    b->type = type;
    b->pos = pos;
    b->size = size;
	b->particle = particle;
	b->nolightvol = nolightvol;
}

void SortBillboards()
{
	//return;

    CVector3 pos = g_camera->LookPos();
    
	for(int i=0; i<BILLBOARDS; i++)
	{
		if(!g_billb[i].on)
			continue;
        
		g_billb[i].dist = Magnitude2(pos - g_billb[i].pos);
	}
    
	CBillboard temp;
	int leftoff = 0;
	bool backtracking = false;
    
	for(int i=1; i<BILLBOARDS; i++)
	{
		if(!g_billb[i].on)
			continue;
        
		if(i > 0)
		{
			if(g_billb[i].dist > g_billb[i-1].dist)
			{
				if(!backtracking)
				{
					leftoff = i;
					backtracking = true;
				}
				temp = g_billb[i];
				g_billb[i] = g_billb[i-1];
				g_billb[i-1] = temp;
				i-=2;
			}
			else
			{
				if(backtracking)
				{
					backtracking = false;
					i = leftoff;
				}
			}
		}
		else
			backtracking = false;
	}
}

void MakeBillbVBO()
{
	//return;

	CBillboard* billb;
    for(int i=0; i<BILLBOARDS; i++)
    {
        billb = &g_billb[i];
		DummyVBO(&billb->vbo);
	}
	
	CEntity* e;
	for(int i=0; i<ENTITIES; i++)
	{
        e = &g_entity[i];
		DummyVBO(&e->flashvbo);
	}
}

void DelBillbVBO()
{
	CBillboard* billb;
    for(int i=0; i<BILLBOARDS; i++)
    {
        billb = &g_billb[i];

		if(billb->vbo)
		{
			glDeleteTextures(1, &billb->vbo);
			billb->vbo = 0;
		}
	}

	CEntity* e;
	for(int i=0; i<ENTITIES; i++)
	{
        e = &g_entity[i];

		if(e->flashvbo)
		{
			glDeleteTextures(1, &e->flashvbo);
			e->flashvbo = 0;
		}
	}
}

void DrawBillboards()
{
	//return;

    CBillboard* billb;
    CBillboardType* t;
    float size;
    
	CVector3 vertical = g_camera->Up2();
	CVector3 horizontal = g_camera->Strafe();
	CVector3 a, b, c, d;
	CVector3 vert, horiz;
    
	CParticle* part;
	CParticleType* pT;
    
    float precolor[] = {1,1,1,1};
    
    if(g_reddening > 0.0f)
    {
		precolor[1] = 1.0f - g_reddening;
		precolor[2] = 1.0f - g_reddening;
	}
    
	//float colorf[] = {1, 1, 1, 1};
	float colorf[4];
	colorf[3] = 1;
	CVector3 colorv;
	
		//for(int i=0; i<30; i++)
		//	EmitParticle(BLOODPART, g_camera->Position());

    for(int i=0; i<BILLBOARDS; i++)
   // for(int i=0; i<1; i++)
    {

        billb = &g_billb[i];
        if(!billb->on)
            continue;
        
		//LOGI("billb");

        t = &g_billbT[billb->type];
        
        if(billb->nolightvol)
			colorv = CVector3(1, 1, 1);
		else
			colorv = g_map.LightVol(billb->pos);
        
		colorf[0] = colorv.x;
		colorf[1] = precolor[1] * colorv.y;
		colorf[2] = precolor[2] * colorv.z;
		
#ifndef USE_OMNI
        glUniform4f(g_slots[BILLBOARD][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
#else
        glUniform4f(g_slots[OMNI][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
#endif

		if(billb->particle >= 0 && billb->particle < PARTICLES)
		{
			part = &g_particle[billb->particle];

			if(part->type < 0)
				LOGE("part->type < 0");
			else if(part->type >= PARTICLE_TYPES)
				LOGE("part->type >= PARTICLE_TYPES");

			pT = &g_particleType[part->type];
			size = pT->minsize + pT->sizevariation*(1.0f - part->life);
		}
		else
			size = billb->size;
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, t->tex);
        //glBindTexture(GL_TEXTURE_2D, g_muzzle[0]);
#ifndef USE_OMNI
        glUniform1i(g_slots[BILLBOARD][TEXTURE], 0);
#else
		glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif

		vert = vertical*size;
		horiz = horizontal*size;
        
		a = billb->pos - horiz + vert;
		b = billb->pos + horiz + vert;
		c = billb->pos + horiz - vert;
		d = billb->pos - horiz - vert;
		
        float vertices[] =
        {
            //posx, posy posz   texx, texy
            a.x, a.y, a.z,          1, 0,
            b.x, b.y, b.z,          1, 1,
            c.x, c.y, c.z,          0, 1,
            
            c.x, c.y, c.z,          0, 1,
            d.x, d.y, d.z,          0, 0,
            a.x, a.y, a.z,          1, 0
        };

		glBindBuffer(GL_ARRAY_BUFFER, billb->vbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_DYNAMIC_DRAW);
        
#ifndef USE_OMNI
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
        glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
        glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
        glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif
        
        glDrawArrays(GL_TRIANGLES, 0, 6);

		//return;
    }
    
    glUniform4f(g_slots[BILLBOARD][COLOR], 1, 1, 1, 1);
    
	CEntity* e;
	CPlayer* p;
	CHold* h;
	CItemType* iT;
	size = 8.0f;
	vert = vertical*size;
	horiz = horizontal*size;
	CVector3 muzz;
	CCamera* cam;
	CVector3 offset;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
        
		if(!e->on)
			continue;
        
		if(e->controller < 0)
			continue;
        
		p = &g_player[e->controller];
        
		if(p->equipped < 0)
			continue;
        
		h = &p->items[p->equipped];
		iT = &g_itemType[h->type];
        
		if((e->frame[BODY_UPPER] < ANIM_SHOTSHOULDER_S || e->frame[BODY_UPPER] > ANIM_SHOTSHOULDER_S+4) &&
           (e->frame[BODY_UPPER] < ANIM_SHOTGUNSHOT_S || e->frame[BODY_UPPER] > ANIM_SHOTGUNSHOT_S+4) &&
           (e->frame[BODY_UPPER] < ANIM_PISTOLSHOT_S || e->frame[BODY_UPPER] > ANIM_PISTOLSHOT_S+4))
			continue;
        
        glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, g_muzzle[rand()%4]);
#ifndef USE_OMNI
        glUniform1i(g_slots[BILLBOARD][TEXTURE], 0);
#else
        glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif
        
		cam = &e->camera;
        
		if(p == &g_player[g_localP] && g_viewmode == FIRSTPERSON)
			muzz = Rotate(iT->front, -cam->Pitch(), 1, 0, 0);
		else
			muzz = RotateAround(iT->front, CVector3(0, MID_HEIGHT_OFFSET, 0), -cam->Pitch(), 1, 0, 0);
        
		muzz = cam->Position() + Rotate(muzz, cam->Yaw(), 0, 1, 0);
        
		a = muzz - horiz + vert;
		b = muzz + horiz + vert;
		c = muzz + horiz - vert;
		d = muzz - horiz - vert;
        
        float vertices[] =
        {
            //posx, posy posz   texx, texy
            a.x, a.y, a.z,          1, 0,
            b.x, b.y, b.z,          1, 1,
            c.x, c.y, c.z,          0, 1,
            
            c.x, c.y, c.z,          0, 1,
            d.x, d.y, d.z,          0, 0,
            a.x, a.y, a.z,          1, 0
        };
        
		glBindBuffer(GL_ARRAY_BUFFER, e->flashvbo);
        glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_DYNAMIC_DRAW);

#ifndef USE_OMNI
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
        glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
        glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
        glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif
        
        glDrawArrays(GL_TRIANGLES, 0, 6);
	}
}
