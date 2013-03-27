
#ifndef pathogen_Player_h
#define pathogen_Player_h

#include <vector>
#include "item.h"

using namespace std;

struct CVector3;

enum ACTIVITY{NOACT, ONSIGHT};

class CPlayer
{
public:
	bool on;
    int ID;
    bool ai;
    float hp;
	bool forward;
	bool backward;
	bool left;
	bool right;
	bool jump;
	bool crouched;
	bool crouching;
	bool run;
	bool shoot;
	bool action;
	bool reload;
	bool pain;
	int entity;
	int client;
	CVector3 goal;
	int target;
	float stamina;
	vector<CHold> items;
	int equipped;
	long long last;
	int activity;
    int ticksleft;
    
    float HPRegen();
    float MaxHP();
	float StaminaRegen();
	float MaxStamina();
    
	CPlayer()
	{
		on = false;
        ID = -1;
        ai = false;
        hp = 100;
		forward = false;
		backward = false;
		left = false;
		right = false;
		jump = false;
		crouched = false;
		crouching = false;
		run = false;
		shoot = false;
		action = false;
		reload = false;
		pain = false;
		entity = -1;
		client = -1;
		target = -1;
		stamina = 1;
		equipped = -1;
		activity = -1;
        ticksleft = 0;
	}
};

#define PLAYERS		32
extern CPlayer g_player[PLAYERS];

extern int g_localP;

int NewAI(int activity);
void UpdateAI();
void UpdatePlayers();
void Animate();
void Damage(CPlayer* p, float damage, bool shot);
void NewGoal(CPlayer* p);
void SpawnPlayer();
void SpawnZombies();
int NewAI(int activity);
void ProjectAction();

#endif