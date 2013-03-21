package com.pathogengame.pathogen;

public class Click_Retry extends CFuncPtr
{
	public Click_Retry(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
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
        Click_GoToStory c_gts = new Click_GoToStory(mActivity);
        c_gts.func();
    }
}
