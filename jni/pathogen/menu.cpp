

#include "menu.h"
#include "gui.h"
#include "3dmath.h"
//#include "net.h"
//#include "sendpackets.h"
#include "video.h"
#include "player.h"
#include "quake3bsp.h"
#include "entity.h"
#include "weapon.h"
#include "script.h"
#include "item.h"
#include "animations.h"

bool g_showdialog = true;

void SkipLogo()
{
    if(g_mode != LOGO)
        return;
    
    //g_mode = MENU;
    //OpenSoleView("main");
    PlayIntro();
}

void UpdateLogo()
{
	static int stage = 0;
    
	if(stage < 60)
	{
		float a = (float)stage / 60.0f;
		g_GUI.getview("logo")->widget[0].rgba[3] = a;
	}
	else if(stage < 120)
	{
		float a = 1.0f - (float)(stage-60) / 60.0f;
		g_GUI.getview("logo")->widget[0].rgba[3] = a;
	}
	else
        SkipLogo();
    
	stage++;
}

//enum  TAG{USERNAME, PASSWORD, REGUSERNAME, REGPASSWORD, REGPASSWORD2};

void Click_Login()
{
	/*
    CView* v = g_GUI.getview("login");
    UITextField* unf = v->gettextfield(USERNAME);
    UITextField* pwf = v->gettextfield(PASSWORD);
    NSString* un = unf.text;
    NSString* pw = pwf.text;
    
    Status("Sending data...");
    Login(un, pw);
	*/
}

void Click_GoToRegister()
{
    OpenSoleView("register");
}

void Click_Register()
{
	/*
    CView* v = g_GUI.getview("register");
    UITextField* unf = v->gettextfield(REGUSERNAME);
    UITextField* pwf = v->gettextfield(REGPASSWORD);
    UITextField* pwf2 = v->gettextfield(REGPASSWORD2);
    UITextField* emf = v->gettextfield(REGEMAIL);
    NSString* un = unf.text;
    NSString* pw = pwf.text;
    NSString* pw2 = pwf2.text;
    NSString* em = emf.text;
    
    if(![pw isEqualToString:pw2])
    {
        StatusBack("Passwords do not match");
        return;
    }
    
    Status("Sending data...");
    Register(un, pw, em);
	*/
}

void Click_GoToLogin()
{
    OpenSoleView("login");
}

void Click_GoToCredits()
{
    OpenSoleView("credits");
}

void Click_GoToStory()
{
    //OpenSoleView("story");
    
    //g_map.LoadBSP(@"map1a");
    //g_map.LoadBSP(@"map1a");
    //g_map.LoadBSP(@"map2");
    //g_sspawn.clear();
    //g_map.Destroy();
    //UnloadMap();
    //g_map.LoadBSP(@"map2");
    g_map.LoadBSP("map1a");
    
    SpawnPlayer();
    SpawnZombies();
    
    g_mode = PLAY;
    OpenSoleView("play");
    OpenAnotherView("switch view");
    OpenAnotherView("run");
    
    g_viewmode = THIRDPERSON;
    g_score = 0;
    RedoScore();
    
    /*
    OpenAnotherView("shoot");
    //OpenAnotherView("swing");
    //OpenAnotherView("stab");
    OpenAnotherView("open door");
    //OpenAnotherView("close door");
    OpenAnotherView("reload");
    OpenAnotherView("switch item");
    OpenAnotherView("switch view");
    OpenAnotherView("jump");
    OpenAnotherView("crouch");
    OpenAnotherView("run");*/
}

void Click_GoToOnline()
{
    OpenSoleView("online");
}

void Click_GoToMain()
{
    OpenSoleView("main");
}

void Click_SwitchView()
{
	if(g_viewmode == FIRSTPERSON)
		g_viewmode = THIRDPERSON;
	else
		g_viewmode = FIRSTPERSON;
}

void Click_Retry()
{
    
    CPlayer* p = &g_player[g_localP];
    p->hp = p->MaxHP();
    CEntity* e = &g_entity[p->entity];
    e->frame[0] = 0;
    e->frame[1] = 0;
    
    p->items.clear();
    p->equipped = -1;
    /*
    if(p->equipped > 0)
    {
        CHold* h = &p->items[p->equipped];
        CItemType* t = &g_itemType[h->type];
        EquipFrame(p, p->equipped, t);
        //EquipFrame(p, t);
    }*/
    
    g_arrest = false;
    CloseView("game over");
    
    UnloadMap();
    Click_GoToStory();
}

void (*DialogContinue)() = NULL;

void Click_DialogContinue()
{
    if(DialogContinue != NULL)
        DialogContinue();
}

void CloseDialog()
{
    CloseView("dialog");
    g_mode = PLAY;
}

void Click_DontShow()
{
    g_showdialog = false;
    Click_DialogContinue();
}

void Dialog(const char* msg, void (*Continue)())
{
    g_GUI.getview("dialog")->widget[1].text = msg;
    DialogContinue = Continue;
    
    if(g_showdialog)
    {
        g_mode = MENU;
        OpenAnotherView("dialog");
    }
    else
    {
        CloseDialog();
        Click_DialogContinue();
    }
}

void Click_EquipNext()
{
	if(g_mode != PLAY)
		return;
    
	if(g_arrest)
		return;
    
	CPlayer* p = &g_player[g_localP];
    
	if(p->reload)
		return;
    
	CHold* h;
	CItemType* t;
    
	for(int i=p->equipped+1; i<p->items.size(); i++)
	{
		h = &p->items[i];
		t = &g_itemType[h->type];
        
		if(!t->equip)
			continue;
        
		Equip(p, i, t);
		ItemIcon(t->icon, "");
		return;
	}
    
	for(int i=0; i<p->equipped; i++)
	{
		h = &p->items[i];
		t = &g_itemType[h->type];
        
		if(!t->equip)
			continue;
        
		Equip(p, i, t);
		ItemIcon(t->icon, "");
		return;
	}
}

void Click_Shoot()
{
    if(g_mode != PLAY)
        return;
    
    if(g_arrest)
        return;
    
    CPlayer* p = &g_player[g_localP];
    
	if(p->reload)
		return;
    
	if(p->equipped < 0)
		return;
    
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
    
	//if(t->ammo == ITEM::NOAMMO)
	//	return;
    
	if(t->damage == 0.0f)
		return;
    
	if(t->ammo != NOAMMO && h->clip < 1.0f)
	{
		//if(t->dryFireSound.size() > 0)
		//	t->dryFireSound[ rand()%t->dryFireSound.size() ].Play();
        
		return;
	}
    
	if(t->delay > 0 && GetTickCount() - p->last < t->delay)
		return;
    
	if(t->ammo == PRIMARYAMMO)
		p->shoot = true;
	else //if(t->ammo == ITEM::SECONDARYAMMO)
	{
		p->shoot = false;
		Shot(g_localP);
	}
}

void Unclick_Shoot()
{
    if(g_mode != PLAY)
        return;
    
    g_player[g_localP].shoot = false;
}

void Click_Reload()
{
	if(g_mode != PLAY)
		return;
    
	if(g_arrest)
		return;
    
	CPlayer* p = &g_player[g_localP];
    
	if(p->equipped < 0)
		return;
    
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
    
	if(h->clip >= t->clip)
		return;
    
	if(!HasAmmo(p, t->ammo))
		return;
    
	p->reload = true;
    
	CEntity* e = &g_entity[p->entity];
    
	if(t->ammo == PRIMARYAMMO)
		e->frame[BODY_UPPER] = ANIM_RIFLERELOAD_S;
	if(t->ammo == SECONDARYAMMO)
		e->frame[BODY_UPPER] = ANIM_SHOTGUNRELD_S;
	if(t->ammo == TERTAMMO)
		e->frame[BODY_UPPER] = ANIM_PISTOLRLD_S;
    
	if(t->reloadSound.size() > 0)
		t->reloadSound[ rand()%t->reloadSound.size() ].Play();
}

void Click_Jump()
{
	if(g_mode != PLAY)
		return;
    
	if(g_arrest)
		return;
    
	g_player[g_localP].jump = true;
}

void Unclick_Jump()
{
	if(g_mode != PLAY)
		return;
    
	g_player[g_localP].jump = false;
}

void Click_Crouch()
{
	if(g_mode != PLAY)
		return;
    
	if(g_arrest)
		return;
    
	g_player[g_localP].crouching =! g_player[g_localP].crouching;
}

void Click_Run()
{
	if(g_mode != PLAY)
		return;
    
	if(g_arrest)
		return;
    
	if(g_player[g_localP].crouched)
		return;
    
	g_player[g_localP].run = !g_player[g_localP].run;
}

void Click_Action()
{
    if(g_mode != PLAY)
		return;
    
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
    
	for(int i=0; i<ENTITIES; i++)
	{
		e2 = &g_entity[i];
        
		if(!e2->on)
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
		return;
    
	e2 = &g_entity[hit];
	CEntityType* t = &g_entityType[e2->type];
    
	if(t->category == DOOR)
	{
		if(e2->state == STATE_OPENING)
		{
			if(t->closeSound.size() > 0)
				t->closeSound[ rand()%t->closeSound.size() ].Play();
            
			e2->state = STATE_CLOSING;
		}
		else
		{
			if(t->openSound.size() > 0)
				t->openSound[ rand()%t->openSound.size() ].Play();
            
			e2->state = STATE_OPENING;
		}
	}
}

#define MOV_THRESH  15

void Rotational(float dx, float dy)
{
    if(g_mode != PLAY)
        return;
    
    if(g_arrest)
        return;
    
    if(Magnitude2(dx, dy) < MOV_THRESH*MOV_THRESH)
       return;
    
    g_camera->SetViewByMouse(-dx/(float)MOV_THRESH, -dy/(float)MOV_THRESH);
}

void Forward()
{
    g_player[g_localP].forward = true;
}

void Unforward()
{
    g_player[g_localP].forward = false;
}

void Back()
{
    g_player[g_localP].backward = true;
}

void Unback()
{
    g_player[g_localP].backward = false;
}

void Left()
{
    g_player[g_localP].left = true;
}

void Unleft()
{
    g_player[g_localP].left = false;
}

void Right()
{
    g_player[g_localP].right = true;
}

void Unright()
{
    g_player[g_localP].right = false;
}

void Movement(float dx, float dy)
{
    if(g_mode != PLAY)
        return;
    
    if(g_arrest)
        return;
    
    if(dx < -MOV_THRESH)
        Left();
    else if(dx > MOV_THRESH)
        Right();
    
    if(dy < -MOV_THRESH)
        Forward();
    else if(dy > MOV_THRESH)
        Back();
}

void RedoHP()
{
    CPlayer* p = &g_player[g_localP];
    
    char msg[128];
    sprintf(msg, "HP: %1.1f/%1.0f", p->hp, p->MaxHP());
    g_GUI.getview("play")->gettext("hp")->text = msg;
}

void RedoStamina()
{
	CPlayer* p = &g_player[g_localP];
	char msg[128];
	sprintf(msg, "Stamina: %1.2f / %1.0f", p->stamina, p->MaxStamina());
	//sprintf(msg, "Yaw: %f", g_entity[p->entity].camera.Yaw());
	g_GUI.getview("play")->gettext("stamina")->text = msg;
}

void RedoScore()
{
	CPlayer* p = &g_player[g_localP];
	char msg[128];
	sprintf(msg, "Score: %d", g_score);
	g_GUI.getview("play")->gettext("score")->text = msg;
}

void RedoAmmo()
{
	CPlayer* p = &g_player[g_localP];
    
    CloseView("shoot");
    CloseView("swing");
    CloseView("stab");
    CloseView("reload");
    
	if(p->equipped < 0)
	{
		g_GUI.getview("play")->gettext("ammo")->text = "";
		return;
	}
    
	CHold* h = &p->items[p->equipped];
	CItemType* t = &g_itemType[h->type];
    
	if(!IsAmmo(t->ammo))
	{
        if(h->type == BBAT)
            OpenAnotherView("swing");
        else if(h->type == KNIFE)
            OpenAnotherView("stab");
        
		g_GUI.getview("play")->gettext("ammo")->text = "";
		return;
	}
    
	int clip = h->clip;
	int ammo = 0;
    
	for(int i=0; i<p->items.size(); i++)
	{
		h = &p->items[i];
        
		if(h->type == t->ammo)
			ammo += h->amount;
	}
    
	char msg[128];
	sprintf(msg, "Ammo: %d / %d", clip, ammo);
	g_GUI.getview("play")->gettext("ammo")->text = msg;
    OpenAnotherView("shoot");
    OpenAnotherView("reload");
}

void Error(const char* msg)
{
    CView* v = g_GUI.getview("message");
    v->widget[1].text = msg;
    OpenSoleView("message");
}

void Status(const char* status)
{
	g_GUI.getview("status")->gettext("status")->text = status;
    OpenSoleView("status");
}

void StatusBack(const char* status)
{
	g_GUI.getview("status back")->gettext("status")->text = status;
    OpenSoleView("status back");
}

void Chat(const char* chat)
{
	g_GUI.getview("play")->gettext("chat0")->text = g_GUI.getview("play")->gettext("chat1")->text;
	g_GUI.getview("play")->gettext("chat1")->text = g_GUI.getview("play")->gettext("chat2")->text;
	g_GUI.getview("play")->gettext("chat2")->text = g_GUI.getview("play")->gettext("chat3")->text;
	g_GUI.getview("play")->gettext("chat3")->text = g_GUI.getview("play")->gettext("chat4")->text;
	g_GUI.getview("play")->gettext("chat4")->text = g_GUI.getview("play")->gettext("chat5")->text;
	g_GUI.getview("play")->gettext("chat5")->text = chat;
}

void UpdateGUI()
{
	CView* v = g_GUI.getview("pick up");
    
	if(!v->opened)
		return;
    
	CWidget* w = &v->widget[0];
    
	w->rgba[3] -= g_FrameInterval;
    
	if(w->rgba[3] <= 0.0f)
		v->opened = false;
}

void SkipIntro()
{
    //[g_intro stop];
    //[moviePlayer release];
    //[g_intro.view removeFromSuperview];
    g_mode = MENU;
    OpenSoleView("main");
}

void LDown(float x, float y)
{
    if(g_mode == LOGO)
        SkipLogo();
    else if(g_mode == INTRO)
        SkipIntro();
}

void LUp(float x, float y)
{
    
}

void Keymap()
{
    AssignLButton(&LDown, &LUp);
}