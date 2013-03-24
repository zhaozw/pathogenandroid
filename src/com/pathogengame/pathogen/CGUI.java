package com.pathogengame.pathogen;

import android.content.*;
import java.util.*;
import android.opengl.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ByteOrder;

import com.pathogengame.pathogen.MainActivity.GAMEMODE;

public class CGUI 
{
	public Vector<CView> mView = new Vector();
	
	CFuncPtr mousemovefunc = null;
	//CFuncPtr mouseupdfunc = null;
	CFuncPtr touchframefunc = null;
	CFuncPtr lbuttondownfunc = null;
	CFuncPtr lbuttonupfunc = null;
	
	MainActivity mActivity;
	
	public CGUI(MainActivity act)
	{
		mActivity = act;
		
		float halign = mActivity.mWidth/2 - 80*mActivity.mRetinaScale;
	    float fieldwidth = 280*mActivity.mRetinaScale;
	    float buttonw = 120*mActivity.mRetinaScale;
	    float centerw = mActivity.mWidth/2;

	    AddView("logo", 0);
	    AddImage("logo", (float)mActivity.mWidth/2 - (float)mActivity.mHeight/2, 0, (float)mActivity.mWidth/2 + (float)mActivity.mHeight/2, (float)mActivity.mHeight, 1, 1, 1, 0);
	    
		AddView("main", 0);
		AddImage("gui/bg", 0.0f, 0.0f, (float)mActivity.mWidth, (float)mActivity.mHeight, 1.0f, 1.0f, 1.0f, 1.0f);
		AddButton("gui/buttonbg", "Play Story", CFont.MSGOTHIC16, centerw-buttonw/2, 100.0f*mActivity.mRetinaScale, centerw+buttonw/2, 144.0f*mActivity.mRetinaScale, new Click_GoToStory(mActivity), null, null);
		//AddButton("gui/buttonbg", "Play Online", CFont.MSGOTHIC16, centerw-buttonw/2, 140.0f*mActivity.mRetinaScale, centerw+buttonw/2, 170.0f*mActivity.mRetinaScale, new Click_GoToOnline(mActivity), null, null);
		AddButton("gui/buttonbg", "Credits", CFont.MSGOTHIC16, centerw-buttonw/2, 180.0f*mActivity.mRetinaScale, centerw+buttonw/2, 224.0f*mActivity.mRetinaScale, new Click_GoToCredits(mActivity), null, null);
		
		//AddView("login", 0);
		//AddImage("gui/bg", 0.0f, 0.0f, (float)mActivity.mWidth, (float)mActivity.mHeight, 1.0f, 1.0f, 1.0f, 1.0f);
		
		AddView("credits", 0);
		AddImage("gui/bg", 0.0f, 0.0f, (float)mActivity.mWidth, (float)mActivity.mHeight, 1.0f, 1.0f, 1.0f, 1.0f);
		AddText("PROGRAMMING", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*4*mActivity.mRetinaScale);
		AddText("Denis Ivanov", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*3*mActivity.mRetinaScale);
		//AddText("Volodar", CFont.MSGOTHIC16, halign, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*2*mActivity.mRetinaScale);
		AddText("MAPS", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*4*mActivity.mRetinaScale);
		AddText("Robert Jarrett", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*3*mActivity.mRetinaScale);
		AddText("MODELS", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*1*mActivity.mRetinaScale);
		AddText("Robert Jarrett", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*0*mActivity.mRetinaScale);
		AddText("Sergey Harchenko", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + mActivity.mFont[CFont.MSGOTHIC16].mGHeight*1*mActivity.mRetinaScale);
		AddText("Denis Ivanov", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + mActivity.mFont[CFont.MSGOTHIC16].mGHeight*2*mActivity.mRetinaScale);
		AddText("Andrei Shirokov", CFont.MSGOTHIC16, halign + 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + mActivity.mFont[CFont.MSGOTHIC16].mGHeight*3*mActivity.mRetinaScale);
		AddText("STORY", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*1*mActivity.mRetinaScale);
		AddText("Ruslan Sabitov", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight*0*mActivity.mRetinaScale);
		AddText("VIDEO", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + mActivity.mFont[CFont.MSGOTHIC16].mGHeight*2*mActivity.mRetinaScale);
		AddText("Ruslan Sabitov", CFont.MSGOTHIC16, halign - 100*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + mActivity.mFont[CFont.MSGOTHIC16].mGHeight*3*mActivity.mRetinaScale);
	    AddButton("buttonbg", "Back", CFont.MSGOTHIC16, centerw-buttonw/2, 240.0f*mActivity.mRetinaScale, centerw+buttonw/2, 284.0f*mActivity.mRetinaScale, new Click_GoToMain(mActivity), null, null);

	    //float dpadsize = 128.0f * (float)mActivity.mHeight / 320;
	    float dpadsize = 128.0f * mActivity.mRetinaScale;
	  
	    AddView("play", 0);
		AddText("chat0", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+0*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("chat1", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+1*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("chat2", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+2*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("chat3", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+3*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("chat4", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+4*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("chat5", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+5*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("score", "Score: 0", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+6*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("hp", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+7*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("stamina", "Stamina: 1.00 / 1.0", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+8*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);
		AddText("ammo", "", CFont.MSGOTHIC16, 5*mActivity.mRetinaScale, (5+9*mActivity.mFont[CFont.MSGOTHIC16].mGHeight)*mActivity.mRetinaScale);

		//AddDpad(String n, String texf, float left, float top, float right, float bottom, CFuncPtr dpadf)
		
	    AddDPad("rotational", "gui/rotational", (float)mActivity.mWidth-dpadsize, (float)mActivity.mHeight-dpadsize, (float)mActivity.mWidth, (float)mActivity.mHeight, new Rotational(mActivity));
	    AddDPad("movement", "gui/rotational", 0*mActivity.mRetinaScale, (float)mActivity.mWidth-dpadsize, dpadsize, (float)mActivity.mHeight, new Movement(mActivity));
		AddImage("gui/crosshairtop", (float)mActivity.mWidth/2 - 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 16*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 16*mActivity.mRetinaScale, 1, 1, 1, 1);
		AddImage("gui/crosshairbottom", (float)mActivity.mWidth/2 - 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 16*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 16*mActivity.mRetinaScale, 1, 1, 1, 1);
		AddImage("gui/crosshairleft", (float)mActivity.mWidth/2 - 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 16*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 16*mActivity.mRetinaScale, 1, 1, 1, 1);
		AddImage("gui/crosshairright", (float)mActivity.mWidth/2 - 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 16*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 16*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 16*mActivity.mRetinaScale, 1, 1, 1, 1);
		
		/*
		AddView("switch view", 0);
	    AddButton("buttonbg", "Switch View", CFont.MSGOTHIC16, (float)mActivity.mWidth-buttonw, 0*mActivity.mRetinaScale, (float)mActivity.mWidth, 44*mActivity.mRetinaScale, new Click_SwitchView(mActivity), null, null);
	    
	    AddView("shoot", 0);
	    AddButton("buttonbg", "Shoot", CFont.MSGOTHIC16, centerw-buttonw/2, (float)mActivity.mHeight-90*mActivity.mRetinaScale, centerw+buttonw/2, (float)mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("swing", 0);
	    AddButton("buttonbg", "Swing", CFont.MSGOTHIC16, centerw-buttonw/2, (float)mActivity.mHeight-90*mActivity.mRetinaScale, centerw+buttonw/2, (float)mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("stab", 0);
	    AddButton("buttonbg", "Stab", CFont.MSGOTHIC16, centerw-buttonw/2, (float)mActivity.mHeight-90*mActivity.mRetinaScale, centerw+buttonw/2, (float)mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("switch item", 0);
	    AddButton("buttonbg", "Switch Item", CFont.MSGOTHIC16, (float)mActivity.mWidth-buttonw, 54*mActivity.mRetinaScale, (float)mActivity.mWidth, 98*mActivity.mRetinaScale, new Click_EquipNext(mActivity), null, null);
	    
	    AddView("reload", 0);
	    AddButton("buttonbg", "Reload", CFont.MSGOTHIC16, (float)mActivity.mWidth-buttonw, 108*mActivity.mRetinaScale, (float)mActivity.mWidth, 152*mActivity.mRetinaScale, new Click_Reload(mActivity), null, null);
	    
	    AddView("crouch", 0);
	    AddButton("buttonbg", "Crouch", CFont.MSGOTHIC16, 0*mActivity.mRetinaScale, 0*mActivity.mRetinaScale, buttonw, 60*mActivity.mRetinaScale, new Click_Crouch(mActivity), null, null);
	    
	    AddView("jump", 0);
	    AddButton("buttonbg", "Jump", CFont.MSGOTHIC16, buttonw+10*mActivity.mRetinaScale, 00*mActivity.mRetinaScale, buttonw*2+10*mActivity.mRetinaScale, 60*mActivity.mRetinaScale, new Click_Jump(mActivity), null, new Unclick_Jump(mActivity));
	    
	    AddView("run", 0);
	    AddButton("buttonbg", "Run", CFont.MSGOTHIC16, buttonw*2+20*mActivity.mRetinaScale, 00*mActivity.mRetinaScale, buttonw*3+20*mActivity.mRetinaScale, 60*mActivity.mRetinaScale, new Click_Run(mActivity), null, null);
		 */
		
		AddView("switch view", 0);
	    //AddButton("buttonbg", "Switch View", MSGOTHIC16, g_width-buttonw, 0, g_width, 44, &Click_SwitchView, NULL, NULL);
	    AddButton("buttonbg", "Switch View", CFont.MSGOTHIC16, 0, 0, buttonw, 44*mActivity.mRetinaScale, new Click_SwitchView(mActivity), null, null);
	    
	    AddView("shoot", 0);
	    //AddButton("buttonbg", "Shoot", MSGOTHIC16, centerw-buttonw/2, g_height-90, centerw+buttonw/2, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
	    AddButton("buttonbg", "Shoot", CFont.MSGOTHIC16, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale-buttonw, mActivity.mHeight-90*mActivity.mRetinaScale, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale, mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("swing", 0);
	    AddButton("buttonbg", "Swing", CFont.MSGOTHIC16, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale-buttonw, mActivity.mHeight-90*mActivity.mRetinaScale, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale, mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("stab", 0);
	    AddButton("buttonbg", "Stab", CFont.MSGOTHIC16, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale-buttonw, mActivity.mHeight-90*mActivity.mRetinaScale, mActivity.mWidth-dpadsize-10*mActivity.mRetinaScale, mActivity.mHeight-30*mActivity.mRetinaScale, new Click_Shoot(mActivity), null, new Unclick_Shoot(mActivity));
	    
	    AddView("switch item", 0);
	    //AddButton("buttonbg", "Switch Item", MSGOTHIC16, g_width-buttonw, 54, g_width, 98, &Click_EquipNext, NULL, NULL);
	    AddButton("buttonbg", "Switch Item", CFont.MSGOTHIC16, 0, 54, buttonw, 98, new Click_EquipNext(mActivity), null, null);
	    
	    AddView("reload", 0);
	    AddButton("buttonbg", "Reload", CFont.MSGOTHIC16, mActivity.mWidth-buttonw, mActivity.mHeight-dpadsize-10*mActivity.mRetinaScale-60*mActivity.mRetinaScale, mActivity.mWidth, mActivity.mHeight-dpadsize-10*mActivity.mRetinaScale, new Click_Reload(mActivity), null, null);
	    
	    AddView("crouch", 0);
	    //AddButton("buttonbg", "Crouch", MSGOTHIC16, g_width-buttonw*2-10, 0, g_width-buttonw-10, 60, &Click_Crouch, NULL, NULL);
	    AddButton("buttonbg", "Crouch", CFont.MSGOTHIC16, mActivity.mWidth-buttonw*3-20*mActivity.mRetinaScale, 0, mActivity.mWidth-buttonw*2-20*mActivity.mRetinaScale, 60*mActivity.mRetinaScale,  new Click_Crouch(mActivity), null, null);
	    
	    AddView("jump", 0);
	    //AddButton("buttonbg", "Jump", MSGOTHIC16, buttonw+10, 00, buttonw*2+10, 60, &Click_Jump, NULL, &Unclick_Jump);
	    AddButton("buttonbg", "Jump", CFont.MSGOTHIC16, mActivity.mWidth-buttonw, 00*mActivity.mRetinaScale, mActivity.mWidth, 60*mActivity.mRetinaScale, new Click_Jump(mActivity), null, new Unclick_Jump(mActivity));
	    
	    AddView("run", 0);
	    //AddButton("buttonbg", "Run", MSGOTHIC16, buttonw*2+20, 00, buttonw*3+20, 60, &Click_Run, NULL, NULL);
	    AddButton("buttonbg", "Run", CFont.MSGOTHIC16, mActivity.mWidth-buttonw*2-10*mActivity.mRetinaScale, 0*mActivity.mRetinaScale, mActivity.mWidth-buttonw-10*mActivity.mRetinaScale, 60*mActivity.mRetinaScale, new Click_Run(mActivity), null, null);
	    
	    AddView("pick up", 0);
		AddImage("mp5icon", (float)mActivity.mWidth-256*mActivity.mRetinaScale, (float)mActivity.mHeight/2-128*mActivity.mRetinaScale, (float)mActivity.mWidth, (float)mActivity.mHeight/2+128*mActivity.mRetinaScale, 1, 1, 1, 0);
		AddText("+1", CFont.MSGOTHIC16, (float)mActivity.mWidth-100*mActivity.mRetinaScale, (float)mActivity.mHeight/2+28*mActivity.mRetinaScale);
	    
		/*
		AddView("open door", 0);
	    AddButton("buttonbg", "Open", CFont.MSGOTHIC16, (float)mActivity.mWidth/2 - 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 90*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 30*mActivity.mRetinaScale, new Click_Action(mActivity), null, null);
	    
		AddView("close door", 0);
	    AddButton("buttonbg", "Close", CFont.MSGOTHIC16, (float)mActivity.mWidth/2 - 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 90*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 30*mActivity.mRetinaScale, new Click_Action(mActivity), null, null);
	    */
		
		AddView("open door", 0);
	    AddButton("buttonbg", "Open", CFont.MSGOTHIC16, mActivity.mWidth/2 - 75*mActivity.mRetinaScale, mActivity.mHeight/2 - 90*mActivity.mRetinaScale, mActivity.mWidth/2 + 75*mActivity.mRetinaScale, mActivity.mHeight/2 - 30*mActivity.mRetinaScale, new Click_Action(mActivity), null, null);
	    
		AddView("close door", 0);
	    AddButton("buttonbg", "Close", CFont.MSGOTHIC16, mActivity.mWidth/2 - 75*mActivity.mRetinaScale, mActivity.mHeight/2 - 90*mActivity.mRetinaScale, mActivity.mWidth/2 + 75*mActivity.mRetinaScale, mActivity.mHeight/2 - 30*mActivity.mRetinaScale, new Click_Action(mActivity), null, null);
		
		AddView("game over", 0);
		AddText("Game Over", CFont.MSGOTHIC16, (float)mActivity.mWidth/2 - 32*mActivity.mRetinaScale, (float)mActivity.mHeight/2 - 8*mActivity.mRetinaScale);
	    AddButton("buttonbg", "Try Again", CFont.MSGOTHIC16, (float)mActivity.mWidth/2 - 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 50*mActivity.mRetinaScale, (float)mActivity.mWidth/2 + 75*mActivity.mRetinaScale, (float)mActivity.mHeight/2 + 94*mActivity.mRetinaScale, new Click_Retry(mActivity), null, null);
	    
	    AddView("message", 0);
		AddImage("bg", 0, 0, (float)mActivity.mWidth, (float)mActivity.mHeight, 1, 1, 1, 1);
	    AddTextBox("error", "Hosho was the world's first commissioned ship to be designed and built as an aircraft carrier, and the first aircraft carrier of the Imperial Japanese Navy (IJN). Commissioned in 1922, the ship was used for testing aircraft and equipment, and to evaluate operational methods and tactics. She provided valuable lessons and experience for the IJN in early carrier air operations.", CFont.MSGOTHIC16, 50*mActivity.mRetinaScale, 100*mActivity.mRetinaScale, (float)mActivity.mWidth-50*mActivity.mRetinaScale, (float)mActivity.mHeight-100*mActivity.mRetinaScale);
	    AddButton("buttonbg", "Back", CFont.MSGOTHIC16, centerw-buttonw/2, (float)mActivity.mHeight-45*mActivity.mRetinaScale, centerw+buttonw/2, (float)mActivity.mHeight-10*mActivity.mRetinaScale, new Click_GoToMain(mActivity), null, null);
	    
	    AddView("dialog", 0);
		AddImage("bg", 0, 0, (float)mActivity.mWidth, (float)mActivity.mHeight, 1, 1, 1, 1);
	    AddTextBox("dialog", "", CFont.MSGOTHIC16, 50*mActivity.mRetinaScale, 100*mActivity.mRetinaScale, (float)mActivity.mWidth-50*mActivity.mRetinaScale, (float)mActivity.mHeight-100*mActivity.mRetinaScale);
	    AddButton("buttonbg", "Continue", CFont.MSGOTHIC16, centerw-20*mActivity.mRetinaScale-buttonw, (float)mActivity.mHeight-45*mActivity.mRetinaScale, centerw-20*mActivity.mRetinaScale, (float)mActivity.mHeight-1*mActivity.mRetinaScale, new Click_DialogContinue(mActivity), null, null);
	    AddButton("buttonbg", "Don't Show Any More", CFont.MSGOTHIC16, centerw, (float)mActivity.mHeight-45*mActivity.mRetinaScale, centerw+buttonw*1.5f, (float)mActivity.mHeight-1*mActivity.mRetinaScale, new Click_DontShow(mActivity), null, null);
	    
	    AddView("status", 0);
		AddImage("bg", 0, 0, (float)mActivity.mWidth, (float)mActivity.mHeight, 1, 1, 1, 1);
	    AddText("status", "Status...", CFont.MSGOTHIC16, halign, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight/2);
	    
	    AddView("status back", 0);
		AddImage("bg", 0, 0, (float)mActivity.mWidth, (float)mActivity.mHeight, 1, 1, 1, 1);
	    AddText("status", "Status", CFont.MSGOTHIC16, halign, (float)mActivity.mHeight/2 - mActivity.mFont[CFont.MSGOTHIC16].mGHeight/2);
	    AddButton("buttonbg", "Back", CFont.MSGOTHIC16, centerw-buttonw/2, (float)mActivity.mHeight-45*mActivity.mRetinaScale, centerw+buttonw/2, (float)mActivity.mHeight-10*mActivity.mRetinaScale, new Click_GoToMain(mActivity), null, null);

		//OpenSoleView("main", 0);
		OpenSoleView("logo", 0);
	}
	
	void UpdateGUI()
	{
		CView v = getview("pick up");
	    
		if(!v.opened)
			return;
	    
		CWidget w = v.mWidget.get(0);
	    
		w.rgba[3] -= MainActivity.FRAME_INTERVAL;
	    
		if(w.rgba[3] <= 0.0f)
			v.opened = false;
	}
	
	void Dialog(String msg, CFuncPtr Continue)
	{
	    getview("dialog").mWidget.get(1).text = msg;
	    mActivity.DialogContinue = Continue;
	    
	    if(mActivity.mShowDialog)
	    {
	    	mActivity.mMode = GAMEMODE.MENU;
	        OpenAnotherView("dialog", 0);
	    }
	    else
	    {
	        new CloseDialog(mActivity).func();
	        new Click_DialogContinue(mActivity).func();
	    }
	}
	
	void ItemIcon(int tex, String msg)
	{
		CView v = getview("pick up");
		CWidget w = v.mWidget.get(0);
		w.tex = tex;
		w.rgba[3] = 1;
		CWidget w2 = v.mWidget.get(1);
		w2.text = msg;
		OpenAnotherView("pick up", 0);
	}
	
	void RedoHP()
	{
	    CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
	    
	    String msg = "HP: " + p.hp + " / " + p.MaxHP();
	    getview("play").gettext("hp").text = msg;
	}

	void RedoStamina()
	{
		CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
		String msg = "Stamina: " + p.stamina + " / " + p.MaxStamina();
		//sprintf(msg, "Yaw: %f", g_entity[p->entity].camera.Yaw());
		getview("play").gettext("stamina").text = msg;
	}

	void RedoScore()
	{
		CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
		String msg = "Score: " + mActivity.mScore;
		getview("play").gettext("score").text = msg;
	}

	void RedoAmmo()
	{
		CPlayer p = mActivity.mPlayer[mActivity.mLocalP];
	    
	    CloseView("shoot");
	    CloseView("swing");
	    CloseView("stab");
	    CloseView("reload");
	    
		if(p.equipped < 0)
		{
			getview("play").gettext("ammo").text = "";
			return;
		}
	    
		CHold h = p.items.get(p.equipped);
		CItemType t = mActivity.mItemType[h.type];
	    
		if(!mActivity.IsAmmo(t.ammo))
		{
	        if(h.type == CItemType.BBAT)
	            OpenAnotherView("swing", 0);
	        else if(h.type == CItemType.KNIFE)
	            OpenAnotherView("stab", 0);
	        
			getview("play").gettext("ammo").text = "";
			return;
		}
	    
		int clip = (int)h.clip;
		int ammo = 0;
	    
		for(int i=0; i<p.items.size(); i++)
		{
			h = p.items.get(i);
	        
			if(h.type == t.ammo)
				ammo += h.amount;
		}
	    
		String msg = "Ammo: " + clip + " / " + ammo;
		getview("play").gettext("ammo").text = msg;
	    OpenAnotherView("shoot", 0);
	    OpenAnotherView("reload", 0);
	}
	
	public CView getview(String n)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equalsIgnoreCase(n))
				return v;
		}
		
		return new CView(mActivity);
	}
	
	public void assignLButton(CFuncPtr down, CFuncPtr up)
	{
		lbuttondownfunc = down;
		lbuttonupfunc = up;
	}
	
	public void assignMouseMove(CFuncPtr mouse)
	{
		mousemovefunc = mouse;
	}
	
	/*
	public void assignMouseUpdate(CFuncPtr mouseu)
	{
		mouseupdfunc = mouseu;
	}*/
	
	void assignTouchFrame(CFuncPtr touchf)
	{
		touchframefunc = touchf;
	}
	
	void touchframe(float x, float y)
    {
		CView v;
		
		for(int i=mView.size()-1; i>=0; i--)
		{
			v = mView.get(i);
			if(v.opened)
                if(v.touchframe(x, y))
                    return;
		}
		
		if(touchframefunc != null)
			touchframefunc.func(x, y);
    }
	
	void touchcheck()
    {
		for(int i=mView.size()-1; i>=0; i--)
			if(mView.get(i).opened)
				mView.get(i).touchcheck();
    }
	
	public void lbuttondown(float x, float y)
	{
		CView v;
		
		for(int i=mView.size()-1; i>=0; i--)
		{
			v = mView.get(i);
			if(v.opened && v.lbuttondown(x, y))
				return;
		}
		
		if(lbuttondownfunc != null)
			lbuttondownfunc.func(x, y);
	}
	
	public void lbuttonup(float x, float y)
	{
		CView v;
		
		for(int i=mView.size()-1; i>=0; i--)
		{
			v = mView.get(i);
			if(v.opened && v.lbuttonup(x, y))
				return;
		}
		
		if(lbuttonupfunc != null)
			lbuttonupfunc.func(x, y);
	}
	
	public void mousemove(float x, float y)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				v.mousemove(x, y);
			
			if(mousemovefunc != null)
				mousemovefunc.func(x, y);
		}
	}
	
	/*
	public void mouseupdate(float x, float y)
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				if(v.mouseupdate(x, y))
					return;
			
			if(mouseupdfunc != null)
				mouseupdfunc.func(x, y);
		}
	}*/
	
	public void draw()
	{
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.opened)
				v.draw();
		}
	}
	
	public void AddView(String n, int page)
	{
		CView v = new CView(mActivity, n, page);
		mView.add(v);
	}
	
	public void AddImage(String filepath, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		int last = mView.size()-1;
		CWidget image = new CWidget(mActivity);
		image.Image(filepath, left, top, right, bottom, r, g, b, a);
		CView v = mView.get(last);
		v.mWidget.add(image);
	}
	
	public void AddImage(CView v, int tex, float left, float top, float right, float bottom, float r, float g, float b, float a)
	{
		CWidget image = new CWidget(mActivity);
		image.Image(tex, left, top, right, bottom, r, g, b, a);
		v.mWidget.add(image);
	}
	
	public void AddLink(String t, int f, float left, float top, CFuncPtr click)
	{
		int last = mView.size()-1;
		CWidget link = new CWidget(mActivity);
		link.Link(t, f, left, top, click);
		CView v = mView.get(last);
		v.mWidget.add(link);
	}
	
	public void AddButton(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click, CFuncPtr overf, CFuncPtr out)
	{
		int last = mView.size()-1;
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
		CView v = mView.get(last);
		v.mWidget.add(button);
		
		System.out.println("addb " + t + " = " + button.tex);
	}
	
	public void AddButton(String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click2, int parm)
	{
		int last = mView.size()-1;
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click2, parm);
		CView v = mView.get(last);
		v.mWidget.add(button);
	}

	public void AddButton(CView v, String filepath, String t, int f, float left, float top, float right, float bottom, CFuncPtr click, CFuncPtr overf, CFuncPtr out)
	{
		CWidget button = new CWidget(mActivity);
		button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
		v.mWidget.add(button);
	}
	
	public void AddText(String n, String t, int f, float left, float top)
	{
		int last = mView.size()-1;
		CWidget text = new CWidget(mActivity);
		text.Text(n, t, f, left, top);
		CView v = mView.get(last);
		v.mWidget.add(text);
	}
	
	public void AddText(CView v, String n, String t, int f, float left, float top)
	{
		CWidget text = new CWidget(mActivity);
		text.Text(n, t, f, left, top);
		v.mWidget.add(text);
	}
	
	public void AddText(String t, int f, float left, float top)
	{
		int last = mView.size()-1;
		CWidget text = new CWidget(mActivity);
		text.Text(t, f, left, top);
		CView v = mView.get(last);
		v.mWidget.add(text);
	}
	
	public void AddDropDown(String n, int f, float left, float top, float width, CFuncPtr change)
	{
		int last = mView.size()-1;
		CWidget dropdown = new CWidget(mActivity);
		dropdown.DropDown(n, f, left, top, width, change);
		CView v = mView.get(last);
		v.mWidget.add(dropdown);
	}
	
	public void SelectOption()
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		int lastdropdown = v.mWidget.size()-1;
		CWidget dropdown = v.mWidget.get(lastdropdown);
		int lastoption = dropdown.options.size()-1;
		dropdown.selected = lastoption;
		dropdown.scroll = lastoption+1;
		if(dropdown.scroll + dropdown.rowsshown() > dropdown.options.size())
			dropdown.scroll = dropdown.options.size() - dropdown.rowsshown();
		if(dropdown.scroll < 0)
			dropdown.scroll = 0;
	}
	
	public void AddDPad(String n, String texf, float left, float top, float right, float bottom, CFuncPtr dpadf)
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		CWidget dpad = new CWidget(mActivity);
		dpad.DPad(n, texf, left, top, right, bottom, dpadf);
		v.mWidget.add(dpad);
	}
	
	public void AddTextField(String placeholder, float left, float top, float width, float height, boolean passw, int tag)
	{
		// TO DO
	}
	
	public void AddTextBox(String n, String t, int f, float left, float top, float right, float bottom)
	{
		int last = mView.size()-1;
		CView v = mView.get(last);
		CWidget textbox = new CWidget(mActivity);
		textbox.TextBox(n, t, f, left, top, right, bottom);
		v.mWidget.add(textbox);
	}
	
	public void CloseView(String n)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n))
				v.close();
		}
	}

	public void OpenSoleView(String n, int page)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.page == page)
				v.open();
			else
				v.close();
		}
	}
	
	public boolean OpenAnotherView(String n, int page)
	{
		CView v;
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.page == page)
			{
				v.open();
				return true;
			}
		}
		
		return false;
	}
	
	public void NextPage(String n)
	{
		int page = 0;
		CView v;
		
		for(int i=0; i<mView.size(); i++)
		{
			v = mView.get(i);
			if(v.name.equals(n) && v.opened)
			{
				page = v.page;
				v.close();
				break;
			}
		}
		
		if(!OpenAnotherView(n, page+1))
			OpenAnotherView(n, 0);
	}
	
	public void DrawImage(int tex, float left, float top, float right, float bottom)
	{
		CShader s = mActivity.mShader[CShader.ORTHO];
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
	    GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	    
	    float vertices[] =
	    {
	        //posx, posy    texx, texy
	        left, top, 0,          0, 0,
	        right, top, 0,         1, 0,
	        right, bottom, 0,      1, 1,
	        
	        right, bottom, 0,      1, 1,
	        left, bottom, 0,       0, 1,
	        left, top, 0,          0, 0
	    };

	    FloatBuffer vertexBuffer;
	    FloatBuffer texCBuffer;
	    
	    ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        //bb.order(ByteOrder.LITTLE_ENDIAN);
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
        
        ByteBuffer bb2 = ByteBuffer.allocateDirect(vertices.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        //bb2.order(ByteOrder.LITTLE_ENDIAN);
        texCBuffer = bb2.asFloatBuffer();
        texCBuffer.put(vertices);
        texCBuffer.position(3);
	    
	    GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT,  false, 4*5, vertexBuffer);
	    GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2,  GLES20.GL_FLOAT,  false, 4*5, texCBuffer);
	    
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	}
}
