package com.pathogengame.pathogen;

public class Click_Reload extends CFuncPtr
{
	public Click_Reload(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
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
        
    	if(t->ammo == ITEM::PRIMARYAMMO)
    		e->frame[BODY_UPPER] = ANIM_RIFLERELOAD_S;
    	if(t->ammo == ITEM::SECONDARYAMMO)
    		e->frame[BODY_UPPER] = ANIM_SHOTGUNRELD_S;
    	if(t->ammo == ITEM::TERTAMMO)
    		e->frame[BODY_UPPER] = ANIM_PISTOLRLD_S;
        
    	if(t->reloadSound.size() > 0)
    		t->reloadSound[ rand()%t->reloadSound.size() ].Play();
    }
}
