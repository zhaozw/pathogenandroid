
#include "model.h"
#include "main.h"
#include "entity.h"
#include "item.h"
#include "player.h"
#include "image.h"
#include "gui.h"
#include "animations.h"
#include "menu.h"
#include "script.h"

int g_lastItem = -1;
CItemType g_itemType[ITEM_TYPES];

void Item(int i, const char* model, const char* icon, bool equip, int delay, int ammo, int clip, int reloadrate, float damage, float range, int split, float inacc)
{
	g_lastItem = i;
	CItemType* t = &g_itemType[i];
    
	t->model = LoadModel(model, CVector3(1, 1, 1));
	if(equip)
	{
		if(ammo == PRIMARYAMMO)
			t->front = ModelFront(t->model, ANIM_SHOTSHOULDER_S, ANIM_SHOTSHOULDER_S+4);
		else if(ammo == SECONDARYAMMO)
			t->front = ModelFront(t->model, ANIM_SHOTGUNSHOT_S, ANIM_SHOTGUNSHOT_S+4);
		else if(ammo == TERTAMMO)
			t->front = ModelFront(t->model, ANIM_PISTOLSHOT_S, ANIM_PISTOLSHOT_S+4);
	}
	t->equip = equip;
	t->icon = CreateTexture(icon);
	t->delay = delay;
	t->ammo = ammo;
	t->clip = clip;
	t->reloadrate = reloadrate;
	t->damage = damage;
	t->range = range;
	t->split = split;
	t->inacc = inacc;
}

void ItemSound(int type, const char* filepath)
{
	int i = g_lastItem;
	CItemType* t = &g_itemType[i];
    
	if(type == DRYSHOT)
		t->dryShotSound.push_back(CSound(filepath));
	else if(type == SHOT)
		t->shotSound.push_back(CSound(filepath));
	else if(type == RELOAD)
		t->reloadSound.push_back(CSound(filepath));
	else if(type == COCK)
		t->cockSound.push_back(CSound(filepath));
	else if(type == DRYFIRE)
		t->dryFireSound.push_back(CSound(filepath));
	else if(type == HIT)
		t->hitSound.push_back(CSound(filepath));
    
    /*
    if(i == ITEM::M1911 && type == ITEMSOUND::RELOAD)
    {
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        NSLog(@"Added size = %d", (int)t->reloadSound.size());
        
        t->reloadSound[0].Play();
    }*/
}

void Items()
{
	//Item(int i, char* model, char* icon, bool equip, int delay, int ammo, int clip, int reloadrate, float damage, float range, int split, float inacc)
	Item(MP5, "mp5", "mp5icon", true, 75, PRIMARYAMMO, 30, 30, 10.0f, 1000.0f, 1, 50.0f);	// 800 RPM = 13.33 RPS -> 1000 / 13.33 = 75 ms
	ItemSound(SHOT, "mp5shot1");
	ItemSound(SHOT, "mp5shot2");
	ItemSound(SHOT, "mp5shot3");
	ItemSound(SHOT, "mp5shot4");
	ItemSound(RELOAD, "mp5reload");
	ItemSound(DRYFIRE, "dryfire");
	Item(MOSSBERG500, "mossberg500", "mossberg500icon", true, 1000, SECONDARYAMMO, 6, 1, 30.0f, 500.0f, 8, 150.0f);
	ItemSound(SHOT, "moss500shotcock");
	ItemSound(RELOAD, "moss500load");
	ItemSound(COCK, "moss500cock");
	ItemSound(DRYFIRE, "dryfire");
	Item(M1911, "m1911", "m1911icon", true, 0, TERTAMMO, 7, 7, 30.0f, 1000.0f, 1, 0.0f);
	ItemSound(SHOT, "gun_pistol1");
	ItemSound(RELOAD, "pistolreload");
	ItemSound(DRYFIRE, "dryfire");
	Item(PRIMARYAMMO, "ammo1", "ammo1icon", false, -1, NOAMMO, -1, -1, 0.0f, 0, -1, -1.0f);
	Item(SECONDARYAMMO, "ammo2", "ammo2icon", false, -1, NOAMMO, -1, -1, 0.0f, 0, -1, -1.0f);
	Item(TERTAMMO, "ammo3", "ammo3icon", false, -1, NOAMMO, -1, -1, 0.0f, 0, -1, -1.0f);
	Item(BBAT, "bbat", "bbaticon", true, 0, NOAMMO, -1, -1, 45.0f, INTERACTION_D, 1, 0.0f);
	ItemSound(HIT, "thud");
	Item(KNIFE, "knife", "knifeicon", true, 0, NOAMMO, -1, -1, 30.0f, INTERACTION_D, 1, 0.0f);
	ItemSound(HIT, "stab");
	Item(WTALKIE, "wtalkie", "wtalkieicon", false, 0, NOAMMO, -1, -1, 0.0f, 0.0f, 0, 0.0f);
    
    //NSLog(@"1 reld sz = %d", (int)g_itemType[ITEM::M1911].reloadSound.size());
}

void EquipFrame(CPlayer* p, int hold, CItemType* t)
{
	CEntity* e = &g_entity[p->entity];
    
	if(t == NULL)
		e->frame[BODY_UPPER] = 0;
	else if(t->ammo == PRIMARYAMMO)
		e->frame[BODY_UPPER] = ANIM_SHOTSHOULDER_E;
	else if(t->ammo == SECONDARYAMMO)
		e->frame[BODY_UPPER] = ANIM_SHOTGUNSHOT_E;
	else if(t->ammo == TERTAMMO)
		e->frame[BODY_UPPER] = ANIM_PISTOLSHOT_E;
	else if(p->items[hold].type == BBAT)
		e->frame[BODY_UPPER] = ANIM_BATSWING_E;
	else if(p->items[hold].type == KNIFE)
		e->frame[BODY_UPPER] = ANIM_KNIFESTAB_E;
}

void Equip(CPlayer* p, int hold, CItemType* t)
{
	p->equipped = hold;
	EquipFrame(p, hold, t);
    
	if(p == &g_player[g_localP])
		RedoAmmo();
}

void EquipAny(CPlayer* p)
{
	CHold* h;
	CItemType* t;
    
	for(int i=0; i<p->items.size(); i++)
	{
		h = &p->items[i];
		t = &g_itemType[h->type];
        
		if(!t->equip)
			continue;
        
		p->equipped = i;
		EquipFrame(p, i, t);
		return;
	}
    
	EquipFrame(p, -1, NULL);
}

bool IsAmmo(int item)
{
	if(item == PRIMARYAMMO)
		return true;
    
	if(item == SECONDARYAMMO)
		return true;
    
	if(item == TERTAMMO)
		return true;
    
	return false;
}

float SubtractItem(CPlayer* p, int item, float amount)
{
	CHold* h = NULL;
	int i;
    
	for(i=0; i<p->items.size(); i++)
	{
		if(p->items[i].type != item)
			continue;
        
		h = &p->items[i];
		break;
	}
    
	if(h == NULL)
		return 0;
    
	if(h->amount < amount)
		amount = h->amount;
    
	h->amount -= amount;
    
	if(h->amount <= 0.0f)
	{
		p->items.erase( p->items.begin() + i );
        
		if(p->equipped > i)
			p->equipped--;
		else if(p->equipped == i)
			EquipAny(p);
	}
    
	if(p == &g_player[g_localP] && IsAmmo(item))
		RedoAmmo();
    
	return amount;
}

void ItemIcon(unsigned int tex, const char* msg)
{
	CView* v = g_GUI.getview("pick up");
	CWidget* w = &v->widget[0];
	w->tex = tex;
	w->rgba[3] = 1;
	CWidget* w2 = &v->widget[1];
	w2->text = msg;
	OpenAnotherView("pick up");
}

void AddItem(CPlayer* p, int item, float amount, float clip)
{
	if(amount <= 0.0f)
		return;
    
	CHold* hold;
	int added = -1;
    
	for(int i=0; i<p->items.size(); i++)
	{
		hold = &p->items[i];
        
		if(hold->type != item)
			continue;
        
		hold->amount += amount;
		added = i;
		break;
	}
    
	if(added < 0)
	{
		CHold h(item, amount, clip);
		p->items.push_back(h);
		added = p->items.size() - 1;
	}
    
	CItemType* t = &g_itemType[item];
    
	if(p->equipped < 0 && t->equip)
		Equip(p, added, t);
    
	if(&g_player[g_localP] != p)
		return;
    
    int equippable = 0;
    CItemType* hT;
    for(int i=0; i<p->items.size(); i++)
	{
		hold = &p->items[i];
        hT = &g_itemType[hold->type];
        if(hT->equip)
            equippable ++;
    }
    
    if(equippable > 1)
        OpenAnotherView("switch item");
    
    //g_viewmode = FIRSTPERSON;
    
	RedoAmmo();
    
	char msg[16];
	sprintf(msg, "+%d", (int)amount);
	ItemIcon(t->icon, msg);
}

bool CheckItem(CEntity* e, int j)
{
	CEntityType* t = &g_entityType[e->type];
	CEntity* e2 = &g_entity[j];
	CEntityType* t2 = &g_entityType[e2->type];
    
	CEntity* item;
	CEntity* hum;
	CEntityType* itemT;
    
	if(t->category == HUMAN && t2->category == ITEM)
	{
		hum = e;
		item = e2;
		itemT = t2;
	}
	else if(t->category == ITEM && t2->category == HUMAN)
	{
		hum = e2;
		item = e;
		itemT = t;
	}
	else
		return false;
    
	if(item->script > 0 && hum->controller == g_localP)
	{
		DoScriptFunc(item->script);
		item->script = -1;
	}
    
	CPlayer* p = &g_player[hum->controller];
	AddItem(p, itemT->item, item->amount, item->clip);
	item->on = false;
    
	return true;
}

bool HasAmmo(CPlayer* p, int ammo)
{
	CHold* h;
    
	for(int i=0; i<p->items.size(); i++)
	{
		h = &p->items[i];
        
		if(h->type != ammo)
			continue;
        
		if(h->amount < 1.0f)
			continue;
        
		return true;
	}
    
	return false;
}