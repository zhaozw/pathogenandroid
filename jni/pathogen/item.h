
#ifndef ITEM_H
#define ITEM_H

#include <vector>
#include "3dmath.h"
#include "sound.h"

using namespace std;

enum ITEMSOUND{DRYSHOT, SHOT, RELOAD, COCK, DRYFIRE, HIT, ITEM_SOUNDS};

class CItemType
{
public:
	int model;
	bool equip;
	unsigned int icon;
	CVector3 front;
	int delay;
	int ammo;
	int clip;
	float damage;
	float range;
	int split;
	float inacc;
	int reloadrate;
	vector<CSound> dryShotSound;
	vector<CSound> shotSound;
	vector<CSound> reloadSound;
	vector<CSound> cockSound;
	vector<CSound> dryFireSound;
	vector<CSound> hitSound;
    
	CItemType()
	{
		model = -1;
		equip = false;
	}
};

enum ITEM{NOAMMO=-1, MP5, MOSSBERG500, M1911, PRIMARYAMMO, SECONDARYAMMO, TERTAMMO, BBAT, KNIFE, WTALKIE, ITEM_TYPES};

extern CItemType g_itemType[ITEM_TYPES];

void Items();

class CHold
{
public:
	int type;
	float amount;
	float clip;
    
	CHold(int t, float a, float c)
	{
		type = t;
		amount = a;
		clip = c;
	}
};

class CEntity;
class CPlayer;

void ItemIcon(unsigned int tex, const char* msg);
void Equip(CPlayer* p, int hold, CItemType* t);
void EquipFrame(CPlayer* p, int hold, CItemType* t);
float SubtractItem(CPlayer* p, int item, float amount);
bool CheckItem(CEntity* e, int j);
bool IsAmmo(int item);
bool HasAmmo(CPlayer* p, int ammo);

#endif