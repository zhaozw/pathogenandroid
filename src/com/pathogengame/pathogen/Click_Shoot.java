package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_Shoot extends CFuncPtr
{
	public Click_Shoot(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	if(mActivity.mMode != GAMEMODE.PLAY)
            return;
        
        if(mActivity.mArrest)
            return;
        
        CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
        
    	if(p.reload)
    		return;
        
    	if(p.equipped < 0)
    		return;
        
    	CHold h = p.items.get(p.equipped);
    	CItemType t = mActivity.mItemType[h.type];
        
    	//if(t->ammo == ITEM::NOAMMO)
    	//	return;
        
    	if(t->damage == 0.0f)
    		return;
        
    	if(t->ammo != ITEM::NOAMMO && h->clip < 1.0f)
    	{
    		//if(t->dryFireSound.size() > 0)
    		//	t->dryFireSound[ rand()%t->dryFireSound.size() ].Play();
            
    		return;
    	}
        
    	if(t->delay > 0 && GetTickCount() - p->last < t->delay)
    		return;
        
    	if(t->ammo == ITEM::PRIMARYAMMO)
    		p->shoot = true;
    	else //if(t->ammo == ITEM::SECONDARYAMMO)
    	{
    		p->shoot = false;
    		Shot(g_localP);
    	}
    }
}
