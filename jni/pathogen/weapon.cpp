#include "main.h"
#include "weapon.h"
#include "player.h"
#include "entity.h"
#include "quake3bsp.h"
#include "menu.h"
#include "animations.h"
#include "gui.h"
#include "model.h"
#include "billboard.h"
#include "particle.h"
#include "item.h"
#include "decal.h"
#include "3dmath.h"

void Shot(int player)
{
	CPlayer* p = &g_player[player];
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
    
	if(GetTickCount() - p->last < t->delay)
		return;
    
	p->last = GetTickCount();
	h->clip -= 1.0f;
    
	if(p == &g_player[g_localP])
		RedoAmmo();
    
	CEntity* e = &g_entity[p->entity];
    
	//if(t->ammo == ITEM::PRIMARYAMMO)
	if(t->ammo == PRIMARYAMMO)
		e->frame[BODY_UPPER] = ANIM_SHOTSHOULDER_S;
	//else if(t->ammo == ITEM::SECONDARYAMMO)
	else if(t->ammo == SECONDARYAMMO)
		e->frame[BODY_UPPER] = ANIM_SHOTGUNSHOT_S;
	//else if(t->ammo == ITEM::TERTAMMO)
	else if(t->ammo == TERTAMMO)
		e->frame[BODY_UPPER] = ANIM_PISTOLSHOT_S;
	//else if(h->type == ITEM::BBAT)
	else if(h->type == BBAT)
		e->frame[BODY_UPPER] = ANIM_BATSWING_S;
	//else if(h->type == ITEM::KNIFE)
	else if(h->type == KNIFE)
		e->frame[BODY_UPPER] = ANIM_KNIFESTAB_S;
    
	if(t->shotSound.size() > 0)
		t->shotSound[ rand()%t->shotSound.size() ].Play();
    
	CCamera* c = &e->camera;
    
	CVector3 d = Normalize(c->View() - c->Position());
	CVector3 horizontal = Normalize(Cross( CVector3(0, 1, 0), d ));
	CVector3 vertical = Normalize(Cross( horizontal, d ));
	CVector3 vLine[2];
	vLine[0] = c->Position();
	CVector3 vert;
	CVector3 horiz;
	CVector3 o;
    
	CEntity* e2;
	int hit;
	CVector3 trace;
    
	for(int s=0; s<t->split; s++)
	{
		horiz = horizontal * (t->inacc * (rand()%1000-500)/500.0f);
		vert = vertical * (t->inacc * (rand()%1000-500)/500.0f);
		vLine[1] = c->Position() + d * t->range + horiz + vert;
        
		if(g_map.BreakFaces(vLine[0], vLine[1]))
			continue;
        
		o = vLine[1] = g_map.TraceRay(vLine[0], vLine[1]);
        
		hit = -1;
        
		for(int i=0; i<ENTITIES; i++)
		{
			e2 = &g_entity[i];
            
			if(!e2->on)
				continue;
            
			if(!g_map.IsClusterVisible(e->cluster, e2->cluster))
				continue;
            
			if(i == p->entity)
				continue;
            
			trace = e2->TraceRay(vLine);
            
			if(trace == vLine[1])
				continue;
            
			hit = i;
			vLine[1] = trace;
		}
        
		if(hit < 0)
		{
			if(g_map.Collided())
			{
				PlaceDecal(BULLETHOLE, vLine[1], g_map.CollisionNormal());
                
				if(t->hitSound.size() > 0)
					t->hitSound[ rand()%t->hitSound.size() ].Play();
			}
            
			continue;
		}
        
		if(t->hitSound.size() > 0)
			t->hitSound[ rand()%t->hitSound.size() ].Play();
        
		e2 = &g_entity[hit];
		c = &e2->camera;
        
		CVector3 v = c->Velocity();
		c->Velocity( v + d*100.0f );
        
		if(e2->controller < 0)
			return;
        
		if(!IsZombie(e2->type))
			return;
        
		CPlayer* p2 = &g_player[e2->controller];
        
		if(p2->hp > 0.0f)
		{
			float damage = t->damage;
            
			if(vLine[1].y >= c->Position().y + HEAD_OFFSET)
				damage *= 100.0f;
            
			Damage(p2, damage, true);
		}
        
		for(int i=0; i<30; i++)
			EmitParticle(BLOODPART, vLine[1]);
        
		// TO DO
	}
}

void DoneReload(int player)
{
	CPlayer* p = &g_player[player];
	p->reload = false;
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
	EquipFrame(p, p->equipped, t);
}

/*
#ifndef max
#define max(a,b) (((a) > (b)) ? (a) : (b))
#endif
#ifndef min
#define min(a,b) (((a) < (b)) ? (a) : (b))
#endif
*/

void Reload(int player)
{
	CPlayer* p = &g_player[player];
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
    
	float amount = SubtractItem(p, t->ammo, min((float)t->reloadrate, t->clip - h->clip));
	h = &p->items[p->equipped]; //item might have shifted
	h->clip += amount;
    
	if(h->clip >= t->clip || !HasAmmo(p, t->ammo))	// Done reloading?
	{
		if(h->type == MOSSBERG500)	// Need to cock gun?
		{
			CEntity* e = &g_entity[p->entity];
			e->frame[BODY_UPPER] = ANIM_SHOTGUNCOCK_S;
            
			if(t->cockSound.size() > 0)
				t->cockSound[ rand()%t->cockSound.size() ].Play();
		}
		else	// Assume aiming stance
			DoneReload(player);
	}
	else if(h->type == MOSSBERG500)	// Continuing to reload shotgun?
	{
		CEntity* e = &g_entity[p->entity];
		e->frame[BODY_UPPER] = ANIM_SHOTGUNRELD_M;
        
		if(t->reloadSound.size() > 0)
			t->reloadSound[ rand()%t->reloadSound.size() ].Play();
	}
    
	if(p == &g_player[g_localP])
		RedoAmmo();
}