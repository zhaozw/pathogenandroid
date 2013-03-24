package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class Click_Reload extends CFuncPtr
{
	public Click_Reload(MainActivity act)
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
        
    	if(p.equipped < 0)
    		return;
        
    	CHold h = p.items.get(p.equipped);
    	CItemType t = mActivity.mItemType[h.type];
        
    	if(h.clip >= t.clip)
    		return;
        
    	if(!mActivity.HasAmmo(p, t.ammo))
    		return;
        
    	p.reload = true;
        
    	CEntity e = mActivity.mEntity[p.entity];
        
    	if(t.ammo == CItemType.PRIMARYAMMO)
    		e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_RIFLERELOAD_S;
    	if(t.ammo == CItemType.SECONDARYAMMO)
    		e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTGUNRELD_S;
    	if(t.ammo == CItemType.TERTAMMO)
    		e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_PISTOLRLD_S;
        
    	if(t.reloadSound.size() > 0)
    		t.reloadSound.get( (int)Math.round(Math.random()*t.reloadSound.size()) ).Play();
    }
}
