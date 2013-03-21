package com.pathogengame.pathogen;

public class Click_EquipNext extends CFuncPtr
{
	public Click_EquipNext(MainActivity act)
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
}
