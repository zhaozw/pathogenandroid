package com.pathogengame.pathogen;

public class Script2 extends CFuncPtr
{
	public Script2(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	 mActivity.mGUI.Dialog("Already I thought to kill somebody would be harder than it is, although what kind of person is this. This is a real "+
    	           "animal - a zombie that they scared us with in the movies became a reality.", new CloseDialog(mActivity));

    }
}
