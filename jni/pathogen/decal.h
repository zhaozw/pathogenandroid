
#include "3dmath.h"

class CDecalType
{
public:
	unsigned int tex;
	float decay;
	float size;
};

enum DECAL{BLOODSPLAT, BULLETHOLE, DECAL_TYPES};
extern CDecalType g_decalT[DECAL_TYPES];

class CDecal
{
public:
	bool on;
	int type;
	CVector3 a, b, c, d;
	CVector3 lpos;
	float life;
};

#define DECALS 128
extern CDecal g_decal[DECALS];

void Decals();
void UpdateDecals();
void DrawDecals();
void PlaceDecal(int type, CVector3 pos, CVector3 norm);