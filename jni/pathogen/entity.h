

#ifndef pathogen_Entity_h
#define pathogen_Entity_h

#include <vector>
#include <string>
#include "3dmath.h"
#include "sound.h"

using namespace std;

#define BODY_LOWER   0
#define BODY_UPPER   1

enum ENTITY{NOCAT, HUMAN, ZOMBIE, DOOR, FIXEDENT, ITEM, ENTITY_CATEGORIES};

enum ENTITYSND{OPENSND, CLOSESND};

class CEntityType
{
public:
    char lmodel[64];
	int collider;
    int model[2];
	CVector3 vMin, vMax;
	float maxStep;
	float speed;
	float jump;
	float crouch;
	float animrate;
	int category;
	int item;
	CVector3 vCenterOff;
	vector<CSound> openSound;
	vector<CSound> closeSound;
    
	CEntityType()
	{
		model[0] = -1;
		model[1] = -1;
        collider = -1;
		item = -1;
	}
};

extern vector<CEntityType> g_entityType;

#define MID_HEIGHT_OFFSET	-13.49f   //-15.0f
#define HEAD_OFFSET			(-6.9f*0.7143f)

#define STATE_NONE		0
#define STATE_OPENING	1
#define STATE_CLOSING	2

class CEntity
{
public:
	bool on;
    float frame[2];
	int type;
	int controller;
	CCamera camera;
	float amount;
	float clip;
	int state;
	int cluster;
	float dist;
	bool nolightvol;
	int script;
	unsigned int flashvbo;
    
	CEntity()
	{
		on = false;
		controller = -1;
        frame[BODY_LOWER] = 0;
        frame[BODY_UPPER] = 0;
        amount = -1;
        clip = -1;
		flashvbo = 0;
	}
    
	CVector3 TraceRay(CVector3 vLine[]);
    bool Collision(CVector3 vScaleDown, CVector3 vCenter);
};

#define ENTITIES	256
extern CEntity g_entity[ENTITIES];

class CSpawn
{
public:
	CVector3 pos;
	float angle;
	int activity;
	int script;
    
	CSpawn(CVector3 p, float ang, int act, int scrpt)
	{
		pos = p;
		angle = ang;
		activity = act;
		script = scrpt;
	}
};

extern vector<CSpawn> g_sspawn; //story spawn
extern vector<CSpawn> g_spawn;  //human spawn
extern vector<CSpawn> g_zspawn; //zombie spawn

void DrawHands();
void SortEntities();
void DrawEntities(bool transp);
int EntityID(const char* lmodel);
int Entity(int category, const char* lowermodel, float animrate);
bool PlaceEntity(int type, int controller, float amount, float clip, CVector3 pos, float yaw, int* ID, bool nolightvol, int script);
void Entities();
void ReadEntities(char* str);
bool IsHuman(int type);
bool IsZombie(int type);
int RandomHuman();
int RandomZombie();
void UpdateObjects();

#endif
