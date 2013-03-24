package com.pathogengame.pathogen;

public class Script0a extends CFuncPtr
{
	public Script0a(MainActivity act)
	{
		super(act);
	}
	
    @Override
    public void func()
    {
    	mActivity.mGUI.Dialog("The virus swallowed cities and the epicenter became Kerrisdale City. At that moment I was in Oakridge City. I worked at a brokerage firm as a broker. " +
    	           "My whole family, parents, wife with baby lived in Kerrisdale and I moved to a temporary job in Oakridge with good pay and promotion opportunities.", new Script0a2(mActivity));

    }
}
