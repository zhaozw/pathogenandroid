
#include "player.h"
#include "entity.h"
#include "model.h"
#include "menu.h"
#include "quake3bsp.h"
#include "animations.h"
#include "zombie.h"
#include "weapon.h"
#include "item.h"
#include "3dmath.h"
#include "gui.h"
#include "script.h"

int g_localP = 0;	//local player
CPlayer g_player[PLAYERS];

float CPlayer::HPRegen()
{
    return 1.0f;
}

float CPlayer::MaxHP()
{
    return 100;
}

float CPlayer::StaminaRegen()
{
	return 1.0f / 10.0f;	//10 seconds to regenerate 1 stamina
}

float CPlayer::MaxStamina()
{
	return 1.0f;
}

int NewAI()
{
    CPlayer* p;
    
    for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        
        if(p->on)
            continue;
        
        p->ai = true;
        p->on = true;
		p->target = -1;
		p->stamina = 1;
        return i;
    }
    
    return -1;
}

bool Unobstructed(CCamera* zc, CVector3 pos, CEntity* ignore1, CEntity* ignore2)
{
	CVector3 trace = g_map.TraceRay(zc->Position(), pos);
	if(trace != pos)
		return false;
    
	CEntity* e;
	CVector3 vLine[2];
	vLine[0] = zc->Position();
	vLine[1] = pos;
	int cluster = g_map.FindCluster(zc->Position());
    CEntityType* t;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
        
		if(!e->on)
			continue;
        
        /*
		if(e == ignore1)
			continue;
        
		if(e == ignore2)
			continue;*/
        
		if(!g_map.IsClusterVisible(cluster, e->cluster))
			continue;
        
        t = &g_entityType[e->type];
        
        if(t->category != DOOR)
            continue;
        
		trace = e->TraceRay(vLine);
        
		if(trace == vLine[1])
			continue;
        
		return false;
	}
    
	return true;
}

bool Visible(CCamera* zc, CVector3 pos, CEntity* ignore1, CEntity* ignore2)
{
	//check angle
	if(!WithinAngle(zc, pos, DEGTORAD(Z_FOV/2.0f)))
		return false;
    
	//check obstruction
	if(!Unobstructed(zc, pos, ignore1, ignore2))
		return false;
    
	return true;
}

bool HumanVisible(CCamera* zc, CEntity* ze)
{
    CPlayer* p;
    CEntity* e;
    CCamera* c;
    
	for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        
        if(!p->on)
            continue;
        
        if(p->ai)
            continue;
        
        if(p->entity < 0)
            continue;
        
		if(p->hp <= 0.0f)
			continue;
        
        e = &g_entity[p->entity];
        
        if(!IsHuman(e->type))
            continue;
        
        c = &e->camera;
        
		if(!Unobstructed(zc, c->Position(), e, ze))
			continue;
        
		return true;
    }
    
	return false;
}

int NearestVisHuman(CCamera* zc, CEntity* ze)
{
    CPlayer* p;
    CEntity* e;
    CCamera* c;
    int nearest = -1;
    float nearestD2 = 999999999.0f;
    float D2;
	CVector3 trace;
	CVector3 light;
	CEntityType* t;
    
    for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        
        if(!p->on)
            continue;
        
        if(p->ai)
            continue;
        
        if(p->entity < 0)
            continue;
        
		if(p->hp <= 0.0f)
			continue;
        
        e = &g_entity[p->entity];
        
        if(!IsHuman(e->type))
            continue;
        
        c = &e->camera;
        
        D2 = Magnitude2(c->Position() - zc->Position());
        
		//check distance
        if(D2 > nearestD2)
            continue;
        
		if(!Visible(zc, c->Position(), e, ze))
			continue;
        
		t = &g_entityType[e->type];
		light = g_map.LightVol(c->Position() + t->vCenterOff);
        
		//check lighting
		if(light.x < VISIBLE_LIGHT && light.y < VISIBLE_LIGHT && light.z < VISIBLE_LIGHT)
			continue;
        
        nearestD2 = D2;
        nearest = i;
    }
    
    return nearest;
}

void NewGoal(CPlayer* p)
{
    CEntity* e = &g_entity[p->entity];
    CCamera* c = &e->camera;
	p->goal = c->Position();
	p->goal.x += rand()%1000 - 500;
	p->goal.z += rand()%1000 - 500;
}

void NoAct(CPlayer* p)
{
	p->forward = false;
}

void UpdateAI(CPlayer* p)
{
    if(p->entity < 0)
        return;
    
	if(p->activity == NOACT)
		return;
    
    CEntity* e = &g_entity[p->entity];
    CCamera* c = &e->camera;
    
	if(p->activity == ONSIGHT && !HumanVisible(c, e))
	{
		NoAct(p);
		return;
	}
    
    int target = p->target;
    
	if(target >= 0)
	{
		CPlayer* p2 = &g_player[target];
		CEntity* e2 = &g_entity[p2->entity];
		CCamera* c2 = &e2->camera;
        
		if(!Visible(c, c2->Position(), e2, e))
			target = -1;
	}
    
	if(target < 0)
		target = NearestVisHuman(c, e);
    
	p->target = target;
	float dyaw;
    
    //return;
    
    if(target < 0)
    {
		CVector3 d = c->Position() - p->goal;
		d.y = 0;
		if(Magnitude2(d) < 100)
			NewGoal(p);
        
		if(WithinAngle(c, p->goal, DEGTORAD(Z_TURN_RATE*2.0f)))
			p->forward = true;
		else
			p->forward = false;
        
		dyaw = DYaw(c, p->goal);
    }
	else
	{
		CPlayer* p2 = &g_player[target];
		CEntity* e2 = &g_entity[p2->entity];
		CCamera* c2 = &e2->camera;
        
		p->goal = c2->Position();
		p->forward = true;
        
		dyaw = DYaw(c, c2->Position());
        
		if(Visible(c, c2->Position(), e2, e) && Magnitude2(c->Position() - c2->Position()) <= GRASP_D * GRASP_D)
			Grasp(p, e, p2);
	}
    
	if(fabs(dyaw) < DEGTORAD(Z_TURN_RATE*2.0f))
		c->View(p->goal);
	else if(dyaw < 0.0f)
		c->RotateView(-DEGTORAD(Z_TURN_RATE), 0, 1, 0);
	else if(dyaw > 0.0f)
		c->RotateView(DEGTORAD(Z_TURN_RATE), 0, 1, 0);
}

void UpdateAI()
{
    CPlayer* p;
    
    for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        
        if(!p->on)
            continue;
        
        if(!p->ai)
            continue;
        
		if(p->hp <= 0.0f)
			continue;
        
        UpdateAI(p);
    }
}

void UpdateDead(CPlayer* p)
{
	CEntity* e = &g_entity[p->entity];
    
	if(!IsZombie(e->type))
		return;
    
	p->ticksleft --;
    
	if(p->ticksleft <= 0)
	{
		e->on = false;
		p->on = false;
	}
}

void UpdatePlayers()
{
    CPlayer* p;
	float maxhp;
	float maxstamina;
	CHold* h;
	CItemType* t;
	CEntity* e;
    
    for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        
        if(!p->on)
            continue;
        
        if(p->hp <= 0.0f)
		{
			UpdateDead(p);
			continue;
		}
        
        maxhp = p->MaxHP();
        if(p->hp < maxhp)
        {
            p->hp += p->HPRegen() * g_FrameInterval;
            if(p->hp > maxhp)
                p->hp = maxhp;
            
            if(p == &g_player[g_localP])
                RedoHP();
        }
        
		maxstamina = p->MaxStamina();
		if(p->run && !p->crouched)
		{
			p->stamina -= RUN_DSTAMINA * g_FrameInterval;
			if(p->stamina < 0.0f)
			{
				p->stamina = 0.0f;
				p->run = false;
			}
            
			if(p == &g_player[g_localP])
				RedoStamina();
		}
		else if(p->stamina < maxstamina)
		{
			p->stamina += p->StaminaRegen() * g_FrameInterval;
			if(p->stamina > maxstamina)
				p->stamina = maxstamina;
            
			if(p == &g_player[g_localP])
				RedoStamina();
		}
        
		if(p->shoot && p->equipped >= 0)
		{
			h = &p->items[p->equipped];
			t = &g_itemType[h->type];
			e = &g_entity[p->entity];
            
			if(h->clip >= 1.0f)
			{
				Shot(i);
			}
			else
				p->shoot = false;
		}
    }
}

void Animate()
{
    CPlayer* p;
    CEntity* e;
    CEntityType* t;
    int leftright;
    int forwardback;
	float animrate;
	CHold* h;
	CItemType* iT;
    
    for(int i=0; i<PLAYERS; i++)
    {
        p = &g_player[i];
        if(!p->on)
            continue;
        
        if(p->entity < 0)
            continue;
        
        leftright = 0;
        forwardback = 0;
        
        e = &g_entity[p->entity];
		t = &g_entityType[e->type];
        
        if(p->forward)
            forwardback ++;
        if(p->backward)
            forwardback --;
        if(p->left)
            leftright --;
        if(p->right)
            leftright ++;
        
		animrate = t->animrate;
        
		if(p->crouched)
			animrate /= 2.0f;
		else if(p->run && p->stamina > 0.0f)
			animrate *= 2.0f;
        
		if(p->hp > 0.0f)
		{
			if(!p->crouched)
			{
				if(forwardback == 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_S, true, animrate);
				else if(forwardback > 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_E, true, animrate);
				else if(forwardback < 0 && leftright == 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_E, true, animrate);
				else if(forwardback == 0 && leftright > 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_STRAFER_S, ANIM_STRAFER_E, true, animrate);
				else if(forwardback == 0 && leftright < 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_STRAFER_S, ANIM_STRAFER_E, true, animrate);
				else if(forwardback > 0 && leftright > 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_WALKFWR_S, ANIM_WALKFWR_E, true, animrate);
				else if(forwardback > 0 && leftright < 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_WALKFWL_S, ANIM_WALKFWL_E, true, animrate);
				else if(forwardback < 0 && leftright > 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALKFWL_S, ANIM_WALKFWL_E, true, animrate);
				else if(forwardback < 0 && leftright < 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALKFWR_S, ANIM_WALKFWR_E, true, animrate);
			}
			else
			{
				if(forwardback == 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_S, true, animrate);
				else if(forwardback > 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_E, true, animrate);
				else if(forwardback < 0 && leftright == 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_E, true, animrate);
				else if(forwardback == 0 && leftright > 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CSTRAFER_S, ANIM_CSTRAFER_E, true, animrate);
				else if(forwardback == 0 && leftright < 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_CSTRAFER_S, ANIM_CSTRAFER_E, true, animrate);
				else if(forwardback > 0 && leftright > 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALKFWR_S, ANIM_CWALKFWR_E, true, animrate);
				else if(forwardback > 0 && leftright < 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALKFWL_S, ANIM_CWALKFWL_E, true, animrate);
				else if(forwardback < 0 && leftright > 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALKFWL_S, ANIM_CWALKFWL_E, true, animrate);
				else if(forwardback < 0 && leftright < 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALKFWR_S, ANIM_CWALKFWR_E, true, animrate);
			}
		}
        
		if(p->hp <= 0.0f)
		{
			if(e->frame[BODY_UPPER] >= ANIM_UDEATHFW_S && e->frame[BODY_UPPER] <= ANIM_UDEATHFW_E)
			{
				PlayAnimation(e->frame[BODY_UPPER], ANIM_UDEATHFW_S, ANIM_UDEATHFW_E, false, 1.0f);
				PlayAnimation(e->frame[BODY_LOWER], ANIM_LDEATHFW_S, ANIM_LDEATHFW_E, false, 1.0f);
			}
			else if(e->frame[BODY_UPPER] >= ANIM_UDEATHBW_S && e->frame[BODY_UPPER] <= ANIM_UDEATHBW_E)
			{
				PlayAnimation(e->frame[BODY_UPPER], ANIM_UDEATHBW_S, ANIM_UDEATHBW_E, false, 1.0f);
				PlayAnimation(e->frame[BODY_LOWER], ANIM_LDEATHBW_S, ANIM_LDEATHBW_E, false, 1.0f);
			}
		}
		else if(e->frame[BODY_UPPER] >= ANIM_ZGRASP_S && e->frame[BODY_UPPER] < ANIM_ZGRASP_E)					PlayAnimation(e->frame[BODY_UPPER], ANIM_ZGRASP_S, ANIM_ZGRASP_E, false, 1.0f);
		else if(e->frame[BODY_UPPER] >= ANIM_SHOTSHOULDER_S && e->frame[BODY_UPPER] < ANIM_SHOTSHOULDER_E)		PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTSHOULDER_S, ANIM_SHOTSHOULDER_E, false, ANIM_SHOTSHOULDER_R);
		else if(e->frame[BODY_UPPER] >= ANIM_PISTOLSHOT_S && e->frame[BODY_UPPER] < ANIM_PISTOLSHOT_E)			PlayAnimation(e->frame[BODY_UPPER], ANIM_PISTOLSHOT_S, ANIM_PISTOLSHOT_E, false, 1.0f);
		else if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNSHOT_S && e->frame[BODY_UPPER] < ANIM_SHOTGUNSHOT_E)		PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNSHOT_S, ANIM_SHOTGUNSHOT_E, false, ANIM_SHOTGUNSHOT_R);
		else if(e->frame[BODY_UPPER] >= ANIM_BATSWING_S && e->frame[BODY_UPPER] < ANIM_BATSWING_E)				PlayAnimation(e->frame[BODY_UPPER], ANIM_BATSWING_S, ANIM_BATSWING_E, false, 1.0f);
		else if(e->frame[BODY_UPPER] >= ANIM_KNIFESTAB_S && e->frame[BODY_UPPER] < ANIM_KNIFESTAB_E)			PlayAnimation(e->frame[BODY_UPPER], ANIM_KNIFESTAB_S, ANIM_KNIFESTAB_E, false, 1.0f);
		else if(p->reload)
		{
			h = &p->items[p->equipped];
			iT = &g_itemType[h->type];
            
			if(iT->ammo == PRIMARYAMMO && PlayAnimation(e->frame[BODY_UPPER], ANIM_RIFLERELOAD_S, ANIM_RIFLERELOAD_E, false, ANIM_RIFLERELOAD_R))
				Reload(i);
			if(iT->ammo == SECONDARYAMMO)
			{
				if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNRELD_S && e->frame[BODY_UPPER] <= ANIM_SHOTGUNRELD_E)
				{
					if(PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNRELD_S, ANIM_SHOTGUNRELD_E, false, ANIM_SHOTGUNRELD_R))
						Reload(i);
				}
				else if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNCOCK_S && e->frame[BODY_UPPER] <= ANIM_SHOTGUNCOCK_E)
				{
					if(PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNCOCK_S, ANIM_SHOTGUNCOCK_E, false, ANIM_SHOTGUNRELD_R))
						DoneReload(i);
				}
			}
			if(iT->ammo == TERTAMMO && PlayAnimation(e->frame[BODY_UPPER], ANIM_PISTOLRLD_S, ANIM_PISTOLRLD_E, false, 1.0f))
				Reload(i);
		}
		else if(p->pain)
		{
			if(PlayAnimation(e->frame[BODY_UPPER], ANIM_PAIN_S, ANIM_PAIN_E, false, 1.0f))
			{
				e->frame[BODY_UPPER] = 0;
				p->pain = false;
			}
		}
    }
}

void GameOver()
{
    g_arrest = true;
    OpenAnotherView("game over");
    g_viewmode = THIRDPERSON;
    
    CloseView("shoot");
    CloseView("swing");
    CloseView("stab");
    CloseView("open door");
    CloseView("close door");
    CloseView("reload");
    CloseView("switch item");
    CloseView("switch view");
    CloseView("jump");
    CloseView("crouch");
    CloseView("run");
}

void Damage(CPlayer* p, float damage, bool shot)
{
	p->hp -= damage;
    
	if(p == &g_player[g_localP])
		Reddening();
    
	if(p->hp <= 0.0f)
	{
		CEntity* e = &g_entity[p->entity];
        
		if(rand()%2 == 1)
		{
			e->frame[BODY_UPPER] = ANIM_UDEATHFW_S;
			e->frame[BODY_LOWER] = ANIM_LDEATHFW_S;
		}
		else
		{
			e->frame[BODY_UPPER] = ANIM_UDEATHBW_S;
			e->frame[BODY_LOWER] = ANIM_LDEATHBW_S;
		}
        
		p->forward = false;
		p->backward = false;
		p->left = false;
		p->right = false;
		p->crouched = false;
		p->crouching = false;
		p->jump = false;
        
		CCamera* c = &e->camera;
		c->Pitch(0);
        
		if(p == &g_player[g_localP])
		{
			GameOver();
		}
        
		if(IsZombie(e->type))
		{
            g_score += 50;
            RedoScore();
            
			if(e->script > 0)
			{
				DoScriptFunc(e->script);
				e->script = -1;
			}
            
			p->ticksleft = 2 * FRAME_RATE;
            
			if(g_zdeathSnd.size() > 0)
				g_zdeathSnd[ rand()%g_zdeathSnd.size() ].Play();
		}
	}
	else if(shot)
		p->pain = true;
}

void SpawnPlayer()
{
	CPlayer* p = &g_player[g_localP];
	p->on = true;
	p->ai = false;
	p->stamina = 1;
	CSpawn spawn = g_sspawn[0];
	int e;
    
	PlaceEntity(RandomHuman(), g_localP, -1, -1, spawn.pos, spawn.angle, &e, false, -1);
    
	g_camera = &g_entity[e].camera;
}

void SpawnZombies()
{
    //return;
    
    for(int i=0; i<g_zspawn.size(); i++)
    {
        CSpawn spawn = g_zspawn[i];
        PlaceEntity(RandomZombie(), NewAI(spawn.activity), -1, -1, spawn.pos, spawn.angle, NULL, false, spawn.script);
    }
}

int NewAI(int activity)
{
	CPlayer* p;
    
    for(int i=0; i<PLAYERS; i++)
    {
		p = &g_player[i];
        
        if(p->on)
            continue;
        
        p->ai = true;
		p->on = true;
		p->target = -1;
		p->stamina = 1;
		p->activity = activity;
        p->hp = p->MaxHP();
        return i;
    }
    
    return -1;
}

// Check if a door is in crosshair or something
void ProjectAction()
{
    CloseView("open door");
    CloseView("close door");
    
	if(g_arrest)
		return;
    
	CPlayer* p = &g_player[g_localP];
	CEntity* e = &g_entity[p->entity];
	CCamera* c = &e->camera;
    
	CVector3 vLine[2];
	vLine[0] = c->Position();
	CVector3 d = Normalize(c->View() - c->Position());
	vLine[1] = c->Position() + d * INTERACTION_D;
	vLine[1] = g_map.TraceRay(vLine[0], vLine[1]);
    
    CEntity* e2;
	int hit = -1;
	CVector3 trace;
    CEntityType* t;
    
	for(int i=0; i<ENTITIES; i++)
	{
		e2 = &g_entity[i];
        
		if(!e2->on)
			continue;
        
		if(i == p->entity)
			continue;
        
        t = &g_entityType[e2->type];
        
        if(t->category != DOOR)
            continue;
        
        if(!g_map.IsClusterVisible(e->cluster, e2->cluster) && !g_map.IsClusterVisible(e2->cluster, e->cluster))
            continue;
        
		trace = e2->TraceRay(vLine);
        
		if(trace == vLine[1])
			continue;
        
		hit = i;
		vLine[1] = trace;
	}
    
	if(hit < 0)
		return;
    
    e2 = &g_entity[hit];
	t = &g_entityType[e2->type];
    
	if(t->category == DOOR)
	{
		if(e2->state == STATE_OPENING)
		{
			OpenAnotherView("close door");
		}
		else
		{
			OpenAnotherView("open door");
		}
	}
}