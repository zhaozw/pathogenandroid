package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;
import com.pathogengame.pathogen.MainActivity.VIEWMODE;

public class Click_GoToStory extends CFuncPtr
{
	public Click_GoToStory(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mMap.LoadBSP("map1a");
        
        mActivity.SpawnPlayer();
        mActivity.SpawnZombies();
        
        mActivity.mMode = GAMEMODE.PLAY;
    	mActivity.mGUI.OpenSoleView("play", 0);
    	mActivity.mGUI.OpenAnotherView("switch view", 0);
    	mActivity.mGUI.OpenAnotherView("run", 0);
        
        mActivity.mViewMode = VIEWMODE.THIRDPERSON;
        mActivity.mScore = 0;
        mActivity.mGUI.RedoScore();
    }
}