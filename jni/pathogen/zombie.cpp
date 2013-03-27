#include "zombie.h"
#include "entity.h"
#include "player.h"
#include "main.h"
#include "menu.h"
#include "animations.h"

void Reddening()
{
	g_reddening = 1.0f;
	RedoHP();
}

bool CheckZombie(int i, int j)
{
    CEntity* a = &g_entity[i];
    CEntity* b = &g_entity[j];
    
    CEntity* hum;
    CEntity* zom;
    
    if(IsHuman(a->type) && IsZombie(b->type))
    {
        hum = a;
        zom = b;
    }
    else if(IsHuman(b->type) && IsZombie(a->type))
    {
        hum = b;
        zom = a;
    }
    else
        return false;

    
    CPlayer* p;
    
	if(zom->controller >= 0)
	{
		p = &g_player[zom->controller];
		if(p->hp <= 0.0f)
			return false;
	}
    
    if(hum->controller >= 0)
    {
        p = &g_player[hum->controller];
        
		if(p->hp <= 0.0f)
			return false;
        
		//Damage(p, 5.0f, false);
    }
    
    /*
    p = &g_player[g_localP];
    CEntity* e = &g_entity[p->entity];
    
    if(hum == e)
    {
        g_reddening = 1.0f;
        RedoHP();
    }*/
    
    return true;
}

void Grasp(CPlayer* zom, CEntity* zE, CPlayer* hum)
{
	if(GetTickCount() - zom->last < Z_ATTACK_DELAY)
		return;
    
	zom->last = GetTickCount();
	zE->frame[BODY_UPPER] = ANIM_ZGRASP_S;
	Damage(hum, Z_DAMAGE, false);
    
	if(g_zgraspSnd.size() > 0)
		g_zgraspSnd[ rand()%g_zgraspSnd.size() ].Play();
}