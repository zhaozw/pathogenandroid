

#ifndef GUI_H
#define GUI_H

#include <vector>
#include <string>
#include "main.h"
#include "logger.h"

using namespace std;

void DrawImage(unsigned int tex, float left, float top, float right, float bottom);

#define MAX_OPTIONS_SHOWN	5

enum WIDGET{IMAGE, BUTTON, TEXT, LINK, DROPDOWN, DPAD, TEXTBOX};

class CWidget
{
public:
	int type;
	float pos[4];
    float tpos[2];
	unsigned int tex;
	unsigned int bgtex;
	unsigned int bgovertex;
	bool over;
	bool ldown;
	string name;
	string text;
	int font;
	unsigned int frametex, filledtex, uptex, downtex;
	bool opened;
	vector<string> options;
	int selected;
	float scroll;
	bool mousescroll;
	int mousedowny;
	float healthbar;
	int param;
	float rgba[4];
	string value;
    
	void (*clickfunc)();
	void (*clickfunc2)(int p);
	void (*overfunc)();
	void (*outfunc)();
	void (*changefunc)();
    void (*dpadfunc)(float dx, float dy);
    
	CWidget()
	{
		clickfunc = NULL;
		clickfunc2 = NULL;
		overfunc = NULL;
		outfunc = NULL;
		changefunc = NULL;
        dpadfunc = NULL;
    }
    
	// Initializers
    
	void Image(const char* filepath, float left, float top, float right, float bottom, float r=1, float g=1, float b=1, float a=1);
	void Image(unsigned int t, float left, float top, float right, float bottom, float r=1, float g=1, float b=1, float a=1);
    
	void Button(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)());
	void Button(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click2)(int p), int parm);
    
	void Text(const char* t, int f, float left, float top)
	{
		type = TEXT;
		name = "";
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		ldown = false;
	}
    
	void Text(const char* n, const char* t, int f, float left, float top)
	{
		type = TEXT;
		name = n;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		ldown = false;
	}
    
	void Link(const char* t, int f, float left, float top, void (*click)())
	{
		type = LINK;
		over = false;
		ldown = false;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
		clickfunc = click;
	}
    
	void DropDown(const char* n, int f, float left, float top, float width, void (*change)());
    
    void DPad(const char* n, const char* texf, float left, float top, float right, float bottom, void (*dpad)(float x, float y));
    
	void TextBox(const char* n, const char* t, int f, float left, float top, float right, float bottom)
	{
		type = TEXTBOX;
		name = n;
		text = t;
		font = f;
		pos[0] = left;
		pos[1] = top;
        pos[2] = right;
        pos[3] = bottom;
		ldown = false;
	}
    
	// Drawers
    
	void Image_draw();
    
	void Button_draw();
    
	void Text_draw();
    
	void Link_draw();
    
	void DropDown_draw();
	void DropDown_draw2();
    
    void DPad_draw();
    
    void TextBox_draw();
    
	// L button up
    
	bool Button_lbuttonup(float x, float y)
	{
		if(over && ldown)
		{
			if(outfunc != NULL)
                outfunc();
            
			ldown = false;
            
			return true;	// intercept mouse event
		}
        
		return false;
	}
    
	bool Link_lbuttonup(float x, float y)
	{
		if(over && ldown)
		{
			if(clickfunc != NULL)
				clickfunc();
            
			ldown = false;
            
			return true;	// intercept mouse event
		}
        
		return false;
	}
    
	bool DropDown_lbuttonup(float x, float y);
    
	// L button down
    
	bool Button_lbuttondown(float x, float y)
	{
        if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
            over = true;
        else
            over = false;
        
		if(over)
		{
			ldown = true;

			//LOGI("button down");

			if(clickfunc != NULL)
			{
			//LOGI("clickfunc");
				clickfunc();
			}
            
			if(clickfunc2 != NULL)
				clickfunc2(param);
            
            return true;	// intercept mouse event
		}
        
		return false;
	}
    
	bool Link_lbuttondown(float x, float y)
	{
        Link_mousemove(x, y);
        
		if(over)
		{
			ldown = true;
			return true;	// intercept mouse event
		}
        
		return false;
	}
    
	bool DropDown_lbuttondown(float x, float y);
    
	// Mouse move
    
	void Button_mousemove(float x, float y);
	void Link_mousemove(float x, float y);
	void DropDown_mousemove(float x, float y);
    
    // Mouse update
    
    bool DPad_touchframe(float x, float y);
    
    // Touch check
    
    void Button_touchcheck();
    
	// Misc.
    
	int rowsshown()
	{
		int rows = MAX_OPTIONS_SHOWN;
		if(rows > options.size())
			rows = options.size();
        
		return rows;
	}
    
	int square();
    
	float topratio()
	{
		return scroll / (float)(options.size()-1);
	}
    
	float bottomratio()
	{
		return (scroll+rowsshown()-1) / (float)(options.size()-1);
	}
    
	float scrollspace();
    
	// Common

    void touchcheck()
    {
        switch(type)
        {
            case BUTTON: Button_touchcheck(); break;
            default: break;
        }
    }
    
	bool lbuttonup(float x, float y)
	{
		switch(type)
		{
            case BUTTON: return Button_lbuttonup(x, y);
            case LINK: return Link_lbuttonup(x, y);
            case DROPDOWN: return DropDown_lbuttonup(x, y);
            default: return false;
		}
	}
    
	bool lbuttondown(float x, float y)
	{
		switch(type)
		{
            case BUTTON: return Button_lbuttondown(x, y);
            case LINK: return Link_lbuttondown(x, y);
            case DROPDOWN: return DropDown_lbuttondown(x, y);
            default: return false;
		}
	}
    
	void mousemove(float x, float y)
	{
		switch(type)
		{
            case BUTTON: Button_mousemove(x, y); break;
            case LINK: Link_mousemove(x, y); break;
            case DROPDOWN: DropDown_mousemove(x, y); break;
            default: break;
		}
	}
    
    bool touchframe(float x, float y)
    {
        switch(type)
        {
            case DPAD: return DPad_touchframe(x, y);
            default: return false;
        }
    }
    
	void draw()
	{
		switch(type)
		{
            case IMAGE: Image_draw(); break;
            case BUTTON: Button_draw(); break;
            case TEXT: Text_draw(); break;
            case LINK: Link_draw(); break;
            case DROPDOWN: DropDown_draw(); break;
            case DPAD: DPad_draw(); break;
            case TEXTBOX: TextBox_draw(); break;
            default: break;
		}
	}
    
	void draw2()
	{
		switch(type)
		{
            case DROPDOWN: DropDown_draw2(); break;
            default: break;
		}
	}
};

class CView
{
public:
	string name;
	int page;
	bool opened;
	vector<CWidget> widget;
    //vector<UITextField*> textfield;
    
	CView(const char* n, int p=0)
	{
		name = n;
		opened = false;
		page = p;
	}
    
    void close()
    {
        opened = false;
		/*
        for(int i=0; i<textfield.size(); i++)
            [textfield[i] setHidden:true];
        */
        CWidget* w;
        for(int i=0; i<widget.size(); i++)
        {
            w = &widget[i];
            
            w->ldown = false;
            w->over = false;
        }
    }
    
    void open()
    {
        opened = true;
        //for(int i=0; i<textfield.size(); i++)
        //    [textfield[i] setHidden:false];
    }
    
	void mousemove(float x, float y)
	{
		for(int i=0; i<widget.size(); i++)
			widget[i].mousemove(x, y);
	}
    
    bool touchframe(float x, float y)
    {
		for(int i=widget.size()-1; i>=0; i--)
			if(widget[i].touchframe(x, y))
                return true;
        
        return false;
    }
    
	bool lbuttonup(float x, float y)
	{
		for(int i=widget.size()-1; i>=0; i--)
			if(widget[i].type == DROPDOWN && widget[i].opened && widget[i].lbuttonup(x, y))
				return true;	// intercept mouse event
        
		for(int i=widget.size()-1; i>=0; i--)
			if(widget[i].lbuttonup(x, y))
				return true;	// intercept mouse event
        
		return false;
	}
    
	bool lbuttondown(float x, float y)
	{
		for(int i=widget.size()-1; i>=0; i--)
			if(widget[i].type == DROPDOWN && widget[i].opened && widget[i].lbuttondown(x, y))
				return true;	// intercept mouse event
        
		for(int i=widget.size()-1; i>=0; i--)
			if(widget[i].lbuttondown(x, y))
				return true;	// intercept mouse event
        
		return false;
	}
    
    void touchcheck()
    {
        for(int i=0; i<widget.size(); i++)
            widget[i].touchcheck();
    }
    
	CWidget* gettext(const char* name)
	{
		for(int i=0; i<widget.size(); i++)
			if(widget[i].type == TEXT && stricmp(widget[i].name.c_str(), name) == 0)
				return &widget[i];
        
		return NULL;
	}
    
	CWidget* getdropdown(const char* name)
	{
		for(int i=0; i<widget.size(); i++)
			if(widget[i].type == DROPDOWN && stricmp(widget[i].name.c_str(), name) == 0)
				return &widget[i];
        
		return NULL;
	}
    /*
    UITextField* gettextfield(int tag)
    {
        for(int i=0; i<textfield.size(); i++)
            if(textfield[i].tag == tag)
                return textfield[i];
        
        return NULL;
    }*/
    
	void draw();
};

class CButtonToAdd
{
public:
	char sprite[MAX_PATH];
	char view[32];
	void (*clickfunc)();
	void (*overfunc)();
	void (*outfunc)();
    
	CButtonToAdd(char* spr, char* m, void (*clickf)(), void (*overf)(), void (*outf)())
	{
		strcpy(sprite, spr);
		strcpy(view, m);
		clickfunc = clickf;
		overfunc = overf;
		outfunc = outf;
	}
};

class CGUI
{
public:
	vector<CView> view;
	vector<CButtonToAdd> button;
	void (*mousemovefunc)(float x, float y);
    void (*touchframefunc)(float x, float y);
	void (*lbuttondownfunc)(float x, float y);
	void (*lbuttonupfunc)(float x, float y);
    
	CGUI()
	{
		mousemovefunc = NULL;
        touchframefunc = NULL;
        lbuttondownfunc = NULL;
        lbuttonupfunc = NULL;
	}
    
	CView* getview(const char* name)
	{
		for(int i=0; i<view.size(); i++)
			if(strcasecmp(view[i].name.c_str(), name) == 0)
				return &view[i];
        
		return NULL;
	}
    
	void assignLButton(void (*down)(float x, float y), void (*up)(float x, float y))
	{
		lbuttondownfunc = down;
		lbuttonupfunc = up;
	}
    
	void assignMouseMove(void (*mouse)(float x, float y))
	{
		mousemovefunc = mouse;
	}
    
    void assignTouchFrame(void (*touchf)(float x, float y))
    {
        touchframefunc = touchf;
    }
    
	void lbuttondown(float x, float y)
	{
		for(int i=view.size()-1; i>=0; i--)
			if(view[i].opened && view[i].lbuttondown(x, y))
				return;	// intercept mouse event
        
		if(lbuttondownfunc != NULL)
			lbuttondownfunc(x, y);
	}
	void lbuttonup(float x, float y)
	{
		for(int i=view.size()-1; i>=0; i--)
			if(view[i].opened && view[i].lbuttonup(x, y))
				return;	// intercept mouse event
        
		if(lbuttonupfunc != NULL)
			lbuttonupfunc(x, y);
	}
    
	void mousemove(float x, float y)
	{
		for(int i=0; i<view.size(); i++)
			if(view[i].opened)
				view[i].mousemove(x, y);
        
		if(mousemovefunc != NULL)
			mousemovefunc(x, y);
	}
    
    void touchframe(float x, float y)
    {
		for(int i=view.size()-1; i>=0; i--)
			if(view[i].opened)
                if(view[i].touchframe(x, y))
                    return;
        
		if(touchframefunc != NULL)
			touchframefunc(x, y);
    }
    
    void touchcheck()
    {
		for(int i=view.size()-1; i>=0; i--)
			if(view[i].opened)
                view[i].touchcheck();
    }
    
	void draw();
};

extern CGUI g_GUI;

void AssignMouseMove(void (*mouse)(float x, float y));
void AssignMouseUpdate(void (*mouseu)(float x, float y));
void AssignLButton(void (*down)(float x, float y), void (*up)(float x, float y));
void AddView(const char* name, int page=0);
void AddImage(const char* filepath, float left, float top, float right, float bottom, float r=1, float g=1, float b=1, float a=1);
void AddImage(CView* v, unsigned int tex, float left, float top, float right, float bottom, float r=1, float g=1, float b=1, float a=1);
void AddLink(char* text, int font, float left, float top, void (*clickfunc)());
void AddButton(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)());
void AddButton(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click2)(int p), int parm);
void AddButton(CView* v, const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)());
void AddText(const char* n, const char* t, int f, float left, float top);
void AddText(CView* v, const char* n, char* t, int f, float left, float top);
void AddText(const char* t, int f, float left, float top);
void AddDPad(const char* n, const char* texf, float left, float top, float right, float bottom, void (*dpadf)(float x, float y));
void AddTextField(const char* placeholder, float left, float top, float width, float height, bool passw, int tag);
void AddTextBox(const char* n, const char* t, int f, float left, float top, float right, float bottom);
void CloseView(const char* name);
void OpenSoleView(const char* name, int page=0);
bool OpenAnotherView(const char* name, int page=0);
void NextPage(const char* name);

void RedoGUI();

#endif