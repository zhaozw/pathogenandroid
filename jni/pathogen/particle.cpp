#include "particle.h"
#include "image.h"
#include "main.h"
#include "billboard.h"
#include "decal.h"
#include "quake3bsp.h"

CParticleType g_particleType[PARTICLE_TYPES];
CParticle g_particle[PARTICLES];

int NewParticle()
{
	for(int i=0; i<PARTICLES; i++)
		if(!g_particle[i].on)
			return i;
    
	return -1;
}

void Particle(int i, const char* texpath, int del, float dec, CVector3 minV, CVector3 maxV, CVector3 minA, CVector3 maxA, float minS, float maxS, void (*collision)(CParticle* part, CBillboard* billb, CVector3 trace, CVector3 normal))
{
	CParticleType* t = &g_particleType[i];
    
	t->billbT = Billboard(texpath);
	t->delay = del;
	t->decay = dec;
	t->minvelocity = minV;
	t->velvariation = maxV-minV;
	t->minacceleration = minA;
	t->accelvariation = maxA-minA;
	t->minsize = minS/2.0f;
	t->sizevariation = (maxS-minS)/2.0f;
	t->collision = collision;
}

void CParticle::Update(CBillboard* billb)
{
	CParticleType* t = &g_particleType[type];
	life -= t->decay;
    
	if(life <= 0.0f)
	{
		on = false;
		billb->on = false;
		return;
	}
    
	CVector3 to = billb->pos + vel;
    
	CVector3 trace = g_map.TraceRay(billb->pos, to);
    
	if(trace != to && t->collision != NULL)
		t->collision(this, billb, trace, g_map.CollisionNormal());
    
	billb->pos = to;
	CVector3 accel;
	accel.x = t->minacceleration.x + t->accelvariation.x * (float)(rand()%1000)/1000.0f;
	accel.y = t->minacceleration.y + t->accelvariation.y * (float)(rand()%1000)/1000.0f;
	accel.z = t->minacceleration.z + t->accelvariation.z * (float)(rand()%1000)/1000.0f;
    
	vel = vel + accel;
}

void EmitParticle(int type, CVector3 pos)
{
	int i = NewParticle();
	if(i < 0)
		return;
    
	CParticleType* t = &g_particleType[type];
	PlaceBillboard(t->billbT, pos, t->minsize, i, false);
    
	CParticle* p = &g_particle[i];
    
	p->on = true;
	p->life = 1;
	p->vel.x = t->minvelocity.x + t->velvariation.x * (float)(rand()%1000)/1000.0f;
	p->vel.y = t->minvelocity.y + t->velvariation.y * (float)(rand()%1000)/1000.0f;
	p->vel.z = t->minvelocity.z + t->velvariation.z * (float)(rand()%1000)/1000.0f;
	p->type = type;
}

void BloodSplat(CParticle* part, CBillboard* billb, CVector3 trace, CVector3 normal)
{
	part->on = false;
	billb->on = false;
	PlaceDecal(BLOODSPLAT, trace, normal);
}

void Particles()
{
	Particle(BLOODPART, "billboards/bloodpart", 500, 0.05f, CVector3(-1.5f, -0.5f, -1.5f), CVector3(1.5f, 1.8f, 1.5f), CVector3(0, -0.1f, 0), CVector3(0.0f, -0.3f, 0.0f), 5.0f, 10.0f, &BloodSplat);
}

void UpdateParticles()
{
	CBillboard* b;
	CParticle* p;
    
	for(int i=0; i<BILLBOARDS; i++)
	{
		b = &g_billb[i];
        
		if(!b->on)
			continue;
        
		if(b->particle < 0)
			continue;
        
		p = &g_particle[b->particle];
        
		p->Update(b);
	}
}