

#include "script.h"
#include "main.h"
#include "player.h"
#include "entity.h"
#include "gui.h"
#include "menu.h"
#include "physics.h"
#include "quake3bsp.h"
#include "billboard.h"
#include "file.h"
#include "shader.h"

vector<CFuncMap> g_funcmap;
vector<CFuncProxy> g_funcproxy;

#define SCRIPT_FUNCS	10
void (*g_scriptfunc[SCRIPT_FUNCS])();

void ProxyTest()
{
	char msg[128];
	sprintf(msg, "func_proxy @ %d", (int)GetTickCount());
	Chat(msg);
}

void Script0()
{
    Dialog("My name is Michael or Mike as my friends call me. This story took place a month ago and sitting at home, remembering these terrible moments, I want to tell you about the horror "\
           "that happened with me and billions of other people. Recording all this, maybe it will interest somebody. I want to leave this for generations to come and tell them in more detail "\
           "about the horror known as The Pathogen. ", &Script0a);
}

void Script0a()
{
    Dialog("The virus swallowed cities and the epicenter became Kerrisdale City. At that moment I was in Oakridge City. I worked at a brokerage firm as a broker. "\
           "My whole family, parents, wife with baby lived in Kerrisdale and I moved to a temporary job in Oakridge with good pay and promotion opportunities.", &Script0a2);
}

void Script0a2()
{
    Dialog("But as I regret I wasn't near my family when the epidemic began. I still search and hope to find my family but so far my search hasn't had any luck. From Kerrisdale remain "\
           "only ruins and the city is being rebuilt anew, sweeping up the remains of the horrifying tragedy. ", &Script0b);
}

void Script0b()
{
    Dialog("But I've digressed from the main subject, the EPIDEMIC! An unknown virus, which scientists have named The Pathogen. "\
           "It turned people into animals, into walking zombies, ready to kill you at any moment. Back then my only goal was to survive "\
           "and get to Kerrisdale, find my family and live peacefully. But life is an unpredictable thing. The moment it all began I was scared and my only thought was to SURVIVE!!!", &Script0c);
}

void Script0c()
{
    Dialog("Chapter 1 - Unwelcome guest", &Script0d);
}

void Script0d()
{
    Dialog("Home from work I quickly ate and without undressing fell into bed tiredly. The day was tough. But what awaited me was an unwelcome guest, "\
           "acquaintance with The Zombie, fear and bloodshed. Already, and the day had begun so well.", &Script0e);
}

void Script0e()
{
    g_doorKnock.Play();
    
	Dialog("Oh my god, who could have decided to to pour into my home at 4 in the MORNING! I need to see who it is.", &CloseDialog);
}

void Script1()
{
    Dialog("I'm coming, I'm coming, I said, and opening the door saw THIS! A monster roaring in blood that pounced on me. "\
           "I needed to stop him. I ran to get a baseball bat and it was a small matter to kill the unwelcome guest.", &CloseDialog);
}

void Script2()
{
    Dialog("Already I thought to kill somebody would be harder than it is, although what kind of person is this. This is a real "\
           "animal - a zombie that they scared us with in the movies became a reality.", &CloseDialog);
}

void Script3()
{
    Dialog("Walking out into the streets I saw real chaos, wrecked cars, sounds "\
           "of shooting in the distance. This was all in reality very frightening but what I needed to do now "\
           "was to go along the familiar streets, ahead into the unknown, saving my life with the hope to find at least somebody alive.", &Script3a);
    
}

void Script3a()
{
    Dialog("But one thing kept bugging me - how my parents were, "\
           "my wife, daughter. I left for only a week and was supposed to be back in two days "\
           "and here is such chaos! Life is an unpredictable thing. There weren't any cars nearby, and those that were weren't in any condition to drive.", &Script3b);
}

void Script3b()
{
    Dialog("My path through the streets "\
           "will be long. The main thing was to find at least any signs of life and survive.", &CloseDialog);
}

void Script4()
{
    Dialog("I had found a shotgun in the other backyard.", &CloseDialog);
}

void Script5()
{
    OpenAnotherView("crouch");
    OpenAnotherView("jump");
    
    Dialog("I climbed up the dumpster behind the convenience store and broke through the vent.", &CloseDialog);
}

void Script6()
{
    Dialog("I found a pistol and some ammo at the top of the shelves in the backroom.", &CloseDialog);
}

void Script7()
{
    Dialog("Having gone through familiar streets, having killed a decent number of zombies I heard a sound from a broken down car, somebody was talking "\
           "from a walkie-talkie. I needed to immediately find out who this was.", &CloseDialog);
}

void Script8()
{
    Dialog("Walking up to the car I heard a voice and decided to answer. \"Who is this?\" I asked.", &Script8a);
}

void Script8a()
{
     g_staticSound.Play();
    
	Dialog("TO BE CONTINUED.", &Script8b);
}

void RemoveItems()
{
    CPlayer* p = &g_player[g_localP];
    p->items.clear();
    p->equipped = -1;
}

void Script8b()
{
    RemoveItems();
    CloseDialog();
    UnloadMap();
    g_mode = MENU;
    OpenSoleView("credits");
}

void ScriptFuncs()
{
	g_scriptfunc[0] = &Script0;
	g_scriptfunc[1] = &Script1;
	g_scriptfunc[2] = &Script2;
	g_scriptfunc[3] = &Script3;
	g_scriptfunc[4] = &Script4;
	g_scriptfunc[5] = &Script5;
	g_scriptfunc[6] = &Script6;
	g_scriptfunc[7] = &Script7;
	g_scriptfunc[8] = &Script8;
	g_scriptfunc[9] = &Script0;
}

void NoMove()
{
	CPlayer* p = &g_player[g_localP];
	p->forward = false;
	p->backward = false;
	p->left = false;
	p->right = false;
	p->run = false;
	p->jump = false;
}

void DoScriptFunc(int script)
{
	//char msg[128];
	//sprintf(msg, "func_proxy #%d", script);
	//Chat(msg);
    //return;

	NoMove();
    
	g_scriptfunc[script]();
}

void FreeEntities()
{
	CEntity* e;
	for(int i=0; i<ENTITIES; i++)
	{
		e = &g_entity[i];
		e->on = false;
	}
}

void FreePlayers(int ignore)
{
	CPlayer* p;
	for(int i=0; i<PLAYERS; i++)
	{
		if(i == ignore)
			continue;
        
		p = &g_player[i];
		p->on = false;
		p->items.clear();
	}
}

void FreeBillboards()
{
	CBillboard* b;
	for(int i=0; i<BILLBOARDS; i++)
	{
		b = &g_billb[i];
		b->on = false;
	}
}

void UnloadMap()
{
	//g_map.Destroy(false);
	g_map.Destroy();
	FreeBillboards();
	FreeEntities();
	FreePlayers(g_localP);
	g_zspawn.clear();
	g_spawn.clear();
	g_sspawn.clear();		//For mobile version
	g_funcproxy.clear();
	g_funcmap.clear();
    g_showdialog = true;
}

void GoToMap(int funcmap)
{
	CFuncMap* f = &g_funcmap[funcmap];
	LOGI("unloading map");
	//g_log<<"unloading map"<<endl;
	//g_log.flush();
	//g_log<<"loading bsp "<<f->map<<endl;
	//g_log.flush();
    char map[32];
    StripPathExtension(f->map, map);
    
    if(strcasestr(map, "testmap"))
        return;
    
	UnloadMap();
	
	LOGI("loading bsp %d", f->map);
	g_map.LoadBSP(map);
	//g_log<<"spawning players"<<endl;
	//g_log.flush();
	LOGI("Spawning players");
	SpawnPlayer();
	//g_log<<"spawning zombies"<<endl;
	//g_log.flush();
	LOGI("Spawning zombies");
	SpawnZombies();
	//g_log<<"GoToMap done"<<endl;
	//g_log.flush();
	//g_debug3 = true;
	LOGI("gotomap done");
    
    CPlayer* p = &g_player[g_localP];
    if(p->equipped >= 0)
    {
        CHold* h = &p->items[ p->equipped ];
        CItemType* t = &g_itemType[h->type];
        EquipFrame(p, p->equipped, t);
    }

	//LoadShaders();
}

void CheckFuncs()
{
	CPlayer* p = &g_player[g_localP];
	CEntity* e = &g_entity[p->entity];
	CCamera* c = &e->camera;
    CEntityType* t = &g_entityType[e->type];
    CVector3 vMin = EMin(e, t);
    CVector3 center = c->Position() + (vMin + t->vMax)/2.0f;
    CVector3 radius = (t->vMax - vMin)/2.0f;
    
    CVector3 center2;
    CVector3 radius2;
    
	CFuncMap* funcmap;
	for(int i=0; i<g_funcmap.size(); i++)
	{
		funcmap = &g_funcmap[i];
		center2 = funcmap->pos + (funcmap->vmin + funcmap->vmax)/2.0f;
		radius2 = (funcmap->vmax - funcmap->vmin)/2.0f;
        
		if(fabs(center.x-center2.x) < radius.x+radius2.x &&
           fabs(center.y-center2.y) < radius.y+radius2.y &&
           fabs(center.z-center2.z) < radius.z+radius2.z)
		{
			GoToMap(i);
			return;
		}
	}
    
	CFuncProxy* funcproxy;
	for(int i=0; i<g_funcproxy.size(); i++)
	{
		funcproxy = &g_funcproxy[i];
		center2 = funcproxy->pos + (funcproxy->vmin + funcproxy->vmax)/2.0f;
		radius2 = (funcproxy->vmax - funcproxy->vmin)/2.0f;
        
		if(fabs(center.x-center2.x) < radius.x+radius2.x &&
           fabs(center.y-center2.y) < radius.y+radius2.y &&
           fabs(center.z-center2.z) < radius.z+radius2.z)
		{
            int script = funcproxy->script;
            
            g_funcproxy.erase( g_funcproxy.begin() + i );
            i--;
            
			DoScriptFunc(script);
            
			return;
		}
	}
}
