package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_EquipNext extends CFuncPtr
{
	public Click_EquipNext(MainActivity act)
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
        
    	CHold h;
    	CItemType t;
        
    	for(int i=p.equipped+1; i<p.items.size(); i++)
    	{
    		h = p.items.get(i);
    		t = mActivity.mItemType[h.type];
            
    		if(!t.equip)
    			continue;
            
    		mActivity.Equip(p, i, t);
    		mActivity.mGUI.ItemIcon(t.icon, "");
    		return;
    	}
        
    	for(int i=0; i<p.equipped; i++)
    	{
    		h = p.items.get(i);
    		t = mActivity.mItemType[h.type];
            
    		if(!t.equip)
    			continue;
            
    		mActivity.Equip(p, i, t);
    		mActivity.mGUI.ItemIcon(t.icon, "");
    		return;
    	}
    }
}
