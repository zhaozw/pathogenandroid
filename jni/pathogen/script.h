
#ifndef SCRIPT_H
#define SCRIPT_H

#include <vector>

#include "3dmath.h"
#include "main.h"

using namespace std;

class CFuncMap
{
public:
	CVector3 pos;
	CVector3 vmin;
	CVector3 vmax;
	char map[32];
    
	CFuncMap(CVector3 ps, CVector3 mn, CVector3 mx, const char* mp)
	{
		pos = ps;
		vmin = mn;
		vmax = mx;
		strcpy(map, mp);
	}
};

extern vector<CFuncMap> g_funcmap;

class CFuncProxy
{
public:
	CVector3 pos;
	CVector3 vmin;
	CVector3 vmax;
	int script;
    
	CFuncProxy(CVector3 ps, CVector3 mn, CVector3 mx, int scrpt)
	{
		pos = ps;
		vmin = mn;
		vmax = mx;
		script = scrpt;
	}
};

extern vector<CFuncProxy> g_funcproxy;

void ScriptFuncs();
void DoScriptFunc(int script);
void CheckFuncs();
void UnloadMap();
void GoToMap(int funcmap);
void NoMove();

void Script0();
void Script0a();
void Script0a2();
void Script0b();
void Script0c();
void Script0d();
void Script0e();
void Script0a();
void Script1();
void Script2();
void Script3();
void Script3a();
void Script3b();
void Script4();
void Script5();
void Script6();
void Script7();
void Script8();
void Script8a();
void Script8b();

#endif