

#include "3dmath.h"

class CParticle;
class CBillboard;

class CParticleType
{
public:
	int billbT;
	int delay;
	float decay;
	CVector3 minvelocity;
	CVector3 velvariation;
	CVector3 minacceleration;
	CVector3 accelvariation;
	float minsize;
	float sizevariation;
	void (*collision)(CParticle* part, CBillboard* billb, CVector3 trace, CVector3 normal);
};

enum PARTICLETYPE{BLOODPART, PARTICLE_TYPES};
extern CParticleType g_particleType[PARTICLE_TYPES];

class CParticleEmitter
{
private:
	long last;
public:
	CParticleEmitter() { last = GetTickCount(); }
	bool EmitNext(int delay)
	{
		if(GetTickCount()-last > delay)
		{
			last = GetTickCount();
			return true;
		}
		else
			return false;
	}
};

class CBillboard;

class CParticle
{
public:
	bool on;
	int type;
	float life;
	CVector3 vel;
	float dist;
    
	CParticle() { on = false; }
	CParticle(CVector3 p, CVector3 v) { on = true; vel = v; }
    
	void Update(CBillboard* billb);
};

#define PARTICLES 256
extern CParticle g_particle[PARTICLES];

void Particles();
void EmitParticle(int type, CVector3 pos);
void UpdateParticles();