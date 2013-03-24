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
    	CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
        p.hp = p.MaxHP();
        CEntity e = mActivity.mEntity[p.entity];
        e.frame[0].value = 0;
        e.frame[1].value = 0;
        
        p.items.clear();
        p.equipped = -1;
        /*
        if(p->equipped > 0)
        {
            CHold* h = &p->items[p->equipped];
            CItemType* t = &g_itemType[h->type];
            EquipFrame(p, p->equipped, t);
            //EquipFrame(p, t);
        }*/
        
        mActivity.mArrest = false;
        mActivity.mGUI.CloseView("game over");
        
        mActivity.UnloadMap();
        new Click_GoToStory(mActivity).func();
    }
}
