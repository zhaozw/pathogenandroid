package com.pathogengame.pathogen;

import android.content.*;
import android.content.res.AssetManager;
import android.opengl.*;
import android.graphics.*;
import java.io.*;
import android.R.*;
import java.util.*;

public class CWidget 
{
	public static final int IMAGE = 1;
	public static final int BUTTON = 2;
	public static final int TEXT = 3;
	public static final int LINK = 4;
	public static final int DROPDOWN = 5;
	public static final int DPAD = 6;
	public static final int TEXTBOX = 7;
	
	int type;
	float pos[] = new float[4];
    float tpos[] = new float[2];
	int tex;
	int bgtex;
	int bgovertex;
	boolean over;
	boolean ldown;
	String name;
	String text;
	int font;
	int frametex, filledtex, uptex, downtex;
	boolean opened;
	Vector<String> options;
	int selected;
	float scroll;
	bool mousescroll;
	int mousedowny;
	float healthbar;
	int param;
	float rgba[] = new float[4];
	String value;

	CFuncPtr clickfunc;
	CFuncPtr clickfunc2;
	CFuncPtr overfunc;
	CFuncPtr outfunc;
	CFuncPtr changefunc;
	CFuncPtr dpadfunc;
	
	//void (*clickfunc)();
	//void (*clickfunc2)(int p);
	//void (*overfunc)();
	//void (*outfunc)();
	//void (*changefunc)();
    //void (*dpadfunc)(float dx, float dy);
}
