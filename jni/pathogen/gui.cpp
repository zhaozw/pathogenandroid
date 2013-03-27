#include "gui.h"
#include "image.h"
#include "menu.h"
#include "font.h"
#include "main.h"
#include "shader.h"
#include "3dmath.h"

CGUI g_GUI;

void DrawImage(unsigned int tex, float left, float top, float right, float bottom)
{
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
    
    float vertices[] =
    {
        //posx, posy    texx, texy
        left, top,0,          0, 0,
        right, top,0,         1, 0,
        right, bottom,0,      1, 1,
        
        right, bottom,0,      1, 1,
        left, bottom,0,       0, 1,
        left, top,0,          0, 0
    };
    
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

inline void DrawSquare(float r, float g, float b, float a, float left, float top, float right, float bottom)
{
    /*
	glDisable(GL_TEXTURE_2D);
	glColor4f(r, g, b, a);
    
	glBegin(GL_QUADS);
	glVertex2f(left, top);
	glVertex2f(left, bottom);
	glVertex2f(right, bottom);
	glVertex2f(right, top);
	glEnd();
    
	glColor4f(1, 1, 1, 1);
	glEnable(GL_TEXTURE_2D);*/
}

void CWidget::Image(const char* filepath, float left, float top, float right, float bottom, float r, float g, float b, float a)
{
	type = IMAGE;
	tex = CreateTexture(filepath);
	pos[0] = left;
	pos[1] = top;
	pos[2] = right;
	pos[3] = bottom;
	ldown = false;
	rgba[0] = r;
	rgba[1] = g;
	rgba[2] = b;
	rgba[3] = a;
}

void CWidget::Image(unsigned int t, float left, float top, float right, float bottom, float r, float g, float b, float a)
{
	type = IMAGE;
	tex = t;
	pos[0] = left;
	pos[1] = top;
	pos[2] = right;
	pos[3] = bottom;
	ldown = false;
	rgba[0] = r;
	rgba[1] = g;
	rgba[2] = b;
	rgba[3] = a;
}

void CWidget::Button(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)())
{
	type = BUTTON;
    text = t;
    font = f;
    float length = 0;
    for(int i=0; i<strlen(t); i++)
        length += g_font[f].glyph[t[i]].w;
    tpos[0] = (left+right)/2.0f - length/2.0f;
    tpos[1] = (top+bottom)/2.0f - g_font[f].gheight/2.0f;
	over = false;
	ldown = false;
	tex = CreateTexture(filepath);
	bgtex = CreateTexture("buttonbg");
	//bgovertex = CreateTexture("buttonbgover");
	pos[0] = left;
	pos[1] = top;
	pos[2] = right;
	pos[3] = bottom;
	clickfunc = click;
	overfunc = overf;
	outfunc = out;
}

void CWidget::Button(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click2)(int p), int parm)
{
	type = BUTTON;
    text = t;
    font = f;
    float length = 0;
    for(int i=0; i<strlen(t); i++)
        length += g_font[f].glyph[t[i]].w;
    tpos[0] = (left+right)/2.0f - length/2.0f;
    tpos[1] = (top+bottom)/2.0f - g_font[f].gheight/2.0f;
	over = false;
	ldown = false;
	tex = CreateTexture(filepath);
	bgtex = CreateTexture("buttonbg");
	//bgovertex = CreateTexture("buttonbgover");
	pos[0] = left;
	pos[1] = top;
	pos[2] = right;
	pos[3] = bottom;
	clickfunc2 = click2;
	param = parm;
}

void CWidget::DropDown(const char* n, int f, float left, float top, float width, void (*change)())
{
	type = DROPDOWN;
	name = n;
	font = f;
	opened = false;
	selected = 0;
	scroll = 0;
	mousescroll = false;
	ldown = false;
	changefunc = change;
	pos[0] = left;
	pos[1] = top;
	pos[2] = left+width;
	pos[3] = top+g_font[f].gheight;
	frametex = CreateTexture("frame");
	filledtex = CreateTexture("filled");
	uptex = CreateTexture("up");
	downtex = CreateTexture("down");
}

void CWidget::DPad(const char* n, const char* texf, float left, float top, float right, float bottom, void (*dpad)(float x, float y))
{
    type = DPAD;
    name = n;
    tex = CreateTexture(texf);
    pos[0] = left;
    pos[1] = top;
    pos[2] = right;
    pos[3] = bottom;
    dpadfunc = dpad;
}

void CWidget::Text_draw()
{
    DrawShadowedText(font, pos[0], pos[1], text.c_str());
}

void CWidget::Link_draw()
{
    float color[] = {0.8f, 0.8f, 0.8f, 1.0f};
    
    if(over)
        color[0] = color[1] = color[2] = 1;
    
    DrawShadowedText(font, pos[0], pos[1], text.c_str(), color);
}

void CWidget::TextBox_draw()
{
    float width = pos[2] - pos[0];
    float height = pos[3] - pos[1];
    
    DrawBoxShadText(font, pos[0], pos[1], width, height, text.c_str(), NULL);
}

void CWidget::Button_mousemove(float x, float y)
{
	if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
		over = true;
}

void CWidget::Link_mousemove(float x, float y)
{
	if(x >= pos[0] && y >= pos[1] &&
       x <= pos[0]+strlen(text.c_str())*g_font[font].gheight/2 &&
       y <= pos[1]+g_font[font].gheight)
		over = true;
	else
		over = false;
}

int CWidget::square()
{
	return g_font[font].gheight;
}

float CWidget::scrollspace()
{
	return g_font[font].gheight*(rowsshown()-1);
}

void CWidget::Image_draw()
{
    glUniform4f(g_slots[ORTHO][COLOR], rgba[0], rgba[1], rgba[2], rgba[3]);
	DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
}

void CWidget::Button_draw()
{
    //if(over)
    //    DrawImage(bgovertex, pos[0], pos[1], pos[2], pos[3]);
    //else
    //    DrawImage(bgtex, pos[0], pos[1], pos[2], pos[3]);
    
    DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
    
    DrawShadowedText(font, tpos[0], tpos[1], text.c_str());
}

void CWidget::DropDown_draw()
{
	//glColor4f(1, 1, 1, 1);
    
	DrawImage(frametex, pos[0], pos[1]+5, pos[2], pos[3]+5);
    
	if(!opened)
		DrawImage(downtex, pos[2]-square(), pos[1]+5, pos[2], pos[1]+5+square());
    
    DrawShadowedText(font, pos[0]+30, pos[1], options[selected].c_str());
}

void CWidget::DropDown_draw2()
{
	if(!opened)
		return;
    
	DrawImage(frametex, pos[0], pos[1]+5+g_font[font].gheight, pos[2], pos[3]+5+g_font[font].gheight*rowsshown());
	DrawImage(frametex, pos[2]-square(), pos[1]+5, pos[2], pos[3]+5+g_font[font].gheight*rowsshown());
	DrawImage(uptex, pos[2]-square(), pos[1]+5, pos[2], pos[1]+5+square());
	DrawImage(downtex, pos[2]-square(), pos[3]+5+g_font[font].gheight*rowsshown()-square(), pos[2], pos[3]+5+g_font[font].gheight*rowsshown());
	DrawImage(filledtex, pos[2]-square(), pos[3]+5+scrollspace()*topratio(), pos[2], pos[3]+5+scrollspace()*bottomratio());
    
	for(int i=(int)scroll; i<(int)scroll+rowsshown(); i++)
		DrawShadowedText(font, pos[0]+30, pos[3]+g_font[font].gheight*(i-(int)scroll), options[i].c_str());
}

void CWidget::DPad_draw()
{
    DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
}

void CWidget::DropDown_mousemove(float x, float y)
{
	if(!mousescroll)
		return;
    
	int dy = y - mousedowny;
	int topspace = topratio() * scrollspace();
	int bottomspace = scrollspace() - bottomratio() * scrollspace();
    
	if(dy < 0 && abs(dy) > topspace)
	{
		scroll = 0;
		return;
	}
	else if(dy > 0 && dy > bottomspace)
	{
		scroll = options.size()-rowsshown();
		if(scroll < 0)
			scroll = 0;
        
		//char msg[128];
		//sprintf(msg, "bottomspace=%d", bottomspace);
		//MessageBox(NULL, msg, " ", NULL);
        
		return;
	}
    
	float topy = pos[3]+5+scrollspace()*topratio();
	float newtopy = topy + dy;
    
	//topratio = (float)scroll / (float)(options.size()-1);
	//topy = pos[3]+5+scrollspace*topratio
	//topy = pos[3]+5+scrollspace*((float)scroll / (float)(options.size()-1))
	//topy - pos[3] - 5 = scrollspace*(float)scroll / (float)(options.size()-1)
	//(topy - pos[3] - 5)*(float)(options.size()-1)/scrollspace = scroll
    
	scroll = (newtopy - pos[3] - 5)*(float)(options.size()-1)/scrollspace();
	mousedowny = y;
}

bool CWidget::DPad_touchframe(float x, float y)
{
    if(x >= pos[0] && x <= pos[2] && y >= pos[1] && y <= pos[3])
    {
        
        if(dpadfunc != NULL)
        {
            float dx = x - (pos[0] + pos[2])/2.0f;
            float dy = y - (pos[1] + pos[3])/2.0f;
            dpadfunc(dx, dy);
        }
        
        return true;
    }
    
    return false;
}

void CWidget::Button_touchcheck()
{
    CTouch* touch;
    
    if(!ldown)
        return;
    
    bool found = false;
    
    for(int i=0; i<g_touch.size(); i++)
    {
        touch = &g_touch[i];
		if(!touch->on)
			continue;
        
        if(touch->x >= pos[0] && touch->x <= pos[2] && touch->y >= pos[1] && touch->y <= pos[3])
        {
            found = true;
            break;
        }
    }
    
    if(found)
        return;
    
    ldown = false;
    over = false;
    
    if(outfunc != NULL)
        outfunc();
}

bool CWidget::DropDown_lbuttondown(float x, float y)
{
    DropDown_mousemove(x, y);
    
	if(opened)
	{
		for(int i=(int)scroll; i<(int)scroll+rowsshown(); i++)
		{
			// list item?
			if(x >= pos[0] && x <= pos[2]-square() && y >= pos[3]+g_font[font].gheight*(i-(int)scroll)
               && y <= pos[3]+g_font[font].gheight*(i-(int)scroll+1))
			{
				selected = i;
				opened = false;
				if(changefunc != NULL)
					changefunc();
				return true;	// intercept mouse event
			}
		}
        
		// scroll bar?
		if(x >= pos[2]-square() && y >= pos[3]+5+scrollspace()*topratio() && x <= pos[2] &&
           y <= pos[3]+5+scrollspace()*bottomratio())
		{
			mousescroll = true;
			mousedowny = y;
			return true;	// intercept mouse event
		}
        
		// up button?
		if(x >= pos[2]-square() && y >= pos[1]+5 && x <= pos[2] && y <= pos[3]+5)
		{
			if(rowsshown() < MAX_OPTIONS_SHOWN)
			{
				opened = false;
				return true;
			}
            
			scroll--;
			if(scroll < 0)
				scroll = 0;
            
			return true;
		}
        
		// down button?
		if(x >= pos[2]-square() && y >= pos[3]+5+scrollspace() && x <= pos[2] && y <= pos[3]+5+scrollspace()+g_font[font].gheight)
		{
			scroll++;
			if(scroll+rowsshown() > options.size())
				scroll = options.size() - rowsshown();
            
			return true;
		}
        
		opened = false;
        
		return true;	// intercept mouse event
	}
    
	if(x >= pos[2]-square() && y >= pos[1]+5 && x <= pos[2] && y <= pos[1]+5+square())
	{
		opened = true;
		return true;
	}
    
	return false;
}

bool CWidget::DropDown_lbuttonup(float x, float y)
{
	if(mousescroll)
	{
		mousescroll = false;
		return true;	// intercept mouse event
	}
    
	return false;
}

void CView::draw()
{
	for(int i=0; i<widget.size(); i++)
		widget[i].draw();
    
	for(int i=widget.size()-1; i>=0; i--)
		widget[i].draw2();
}

void CGUI::draw()
{
    /*
	OrthoMode(0, 0, g_width, g_height);
    
	glActiveTextureARB(GL_TEXTURE2_ARB);
	glDisable(GL_TEXTURE_2D);
	glBindTexture(GL_TEXTURE_2D, NULL);
    
	glActiveTextureARB(GL_TEXTURE1_ARB);
	glDisable(GL_TEXTURE_2D);
	glBindTexture(GL_TEXTURE_2D, NULL);
    
	glActiveTextureARB(GL_TEXTURE0_ARB);
	glEnable(GL_TEXTURE_2D);
    
	glColor4f(1,1,1,1);
    */
	for(int i=0; i<view.size(); i++)
		if(view[i].opened)
			view[i].draw();
    /*
	PerspectiveMode();*/
}

void AssignMouseMove(void (*mouse)(float x, float y))
{
	g_GUI.assignMouseMove(mouse);
}

void AssignLButton(void (*down)(float x, float y), void (*up)(float x, float y))
{
	g_GUI.assignLButton(down, up);
}

void AddView(const char* name, int page)
{
	CView view(name, page);
	g_GUI.view.push_back(view);
}

void AddImage(const char* filepath, float left, float top, float right, float bottom, float r, float g, float b, float a)
{
	int last = g_GUI.view.size()-1;
	CWidget image;
	image.Image(filepath, left, top, right, bottom, r, g, b, a);
	g_GUI.view[last].widget.push_back(image);
}

void AddImage(CView* v, unsigned int tex, float left, float top, float right, float bottom, float r, float g, float b, float a)
{
	CWidget image;
	image.Image(tex, left, top, right, bottom, r, g, b, a);
	v->widget.push_back(image);
}

void AddLink(const char* t, int f, float left, float top, void (*click)())
{
	int last = g_GUI.view.size()-1;
	CWidget link;
	link.Link(t, f, left, top, click);
	g_GUI.view[last].widget.push_back(link);
}

void AddButton(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)())
{
	int last = g_GUI.view.size()-1;
	CWidget button;
	button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
	g_GUI.view[last].widget.push_back(button);
}

void AddButton(const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click2)(int p), int parm)
{
	int last = g_GUI.view.size()-1;
	CWidget button;
	button.Button(filepath, t, f, left, top, right, bottom, click2, parm);
	g_GUI.view[last].widget.push_back(button);
}

void AddButton(CView* v, const char* filepath, const char* t, int f, float left, float top, float right, float bottom, void (*click)(), void (*overf)(), void (*out)())
{
	CWidget button;
	button.Button(filepath, t, f, left, top, right, bottom, click, overf, out);
	v->widget.push_back(button);
}

void AddText(const char* n, const char* t, int f, float left, float top)
{
	int last = g_GUI.view.size()-1;
	CWidget text;
	text.Text(n, t, f, left, top);
	g_GUI.view[last].widget.push_back(text);
}

void AddText(CView* v, const char* n, const char* t, int f, float left, float top)
{
	CWidget text;
	text.Text(n, t, f, left, top);
	v->widget.push_back(text);
}

void AddText(const char* t, int f, float left, float top)
{
	int last = g_GUI.view.size()-1;
	CWidget text;
	text.Text(t, f, left, top);
	g_GUI.view[last].widget.push_back(text);
}

void AddDropDown(const char* n, int f, float left, float top, float width, void (*change)())
{
	int last = g_GUI.view.size()-1;
	CWidget dropdown;
	dropdown.DropDown(n, f, left, top, width, change);
	g_GUI.view[last].widget.push_back(dropdown);
}

void AddOption(const char* option)
{
	int last = g_GUI.view.size()-1;
	int lastdropdown = g_GUI.view[last].widget.size()-1;
	g_GUI.view[last].widget[lastdropdown].options.push_back(option);
}

void SelectOption()
{
	int last = g_GUI.view.size()-1;
	int lastdropdown = g_GUI.view[last].widget.size()-1;
	int lastoption = g_GUI.view[last].widget[lastdropdown].options.size()-1;
	CWidget* dropdown = &g_GUI.view[last].widget[lastdropdown];
	dropdown->selected = lastoption;
	dropdown->scroll = lastoption+1;
	if(dropdown->scroll+dropdown->rowsshown() > dropdown->options.size())
		dropdown->scroll = dropdown->options.size() - dropdown->rowsshown();
	if(dropdown->scroll < 0)
		dropdown->scroll = 0;
}

void AddDPad(const char* n, const char* texf, float left, float top, float right, float bottom, void (*dpadf)(float x, float y))
{
	int last = g_GUI.view.size()-1;
	CWidget dpad;
	dpad.DPad(n, texf, left, top, right, bottom, dpadf);
	g_GUI.view[last].widget.push_back(dpad);
}

void AddTextField(const char* placeholder, float left, float top, float width, float height, bool passw, int tag)
{
	/*
    CGRect frame = CGRectMake(left, top, width, height);
    UITextField* field = [[UITextField alloc] initWithFrame:frame];
    field.placeholder = placeholder;
    field.backgroundColor = [UIColor whiteColor];
    field.textColor = [UIColor blackColor];
    field.font = [UIFont systemFontOfSize:14.0f];
    field.borderStyle = UITextBorderStyleRoundedRect;
    field.clearButtonMode = UITextFieldViewModeWhileEditing;
    field.returnKeyType = UIReturnKeyDone;
    //field.textAlignment = UITextAlignmentLeft;
    field.contentVerticalAlignment = UIControlContentVerticalAlignmentCenter;
    field.tag = tag;
    field.autocapitalizationType = UITextAutocapitalizationTypeNone;
    field.secureTextEntry = passw;
    field.autocorrectionType = UITextAutocorrectionTypeNo;
    field.autocapitalizationType = UITextAutocapitalizationTypeNone;
    field.delegate = g_viewcontroller;
    [g_view addSubview:field];
    [field setHidden:true];
    
	int last = g_GUI.view.size()-1;
	g_GUI.view[last].textfield.push_back(field);*/
}

void AddTextBox(const char* n, const char* t, int f, float left, float top, float right, float bottom)
{
	int last = g_GUI.view.size()-1;
	CWidget textbox;
	textbox.TextBox(n, t, f, left, top, right, bottom);
	g_GUI.view[last].widget.push_back(textbox);
}

void CloseView(const char* name)
{
	for(int i=0; i<g_GUI.view.size(); i++)
		if(stricmp(g_GUI.view[i].name.c_str(), name) == 0)
			g_GUI.view[i].close();
}

void OpenSoleView(const char* name, int page)
{
	for(int i=0; i<g_GUI.view.size(); i++)
	{
		if(stricmp(g_GUI.view[i].name.c_str(), name) == 0 && g_GUI.view[i].page == page)
			g_GUI.view[i].open();
		else
			g_GUI.view[i].close();
	}
}

bool OpenAnotherView(const char* name, int page)
{
	for(int i=0; i<g_GUI.view.size(); i++)
	{
		if(stricmp(g_GUI.view[i].name.c_str(), name) == 0 && g_GUI.view[i].page == page)
		{
			g_GUI.view[i].open();
			return true;
		}
	}
    
	return false;
}

void NextPage(const char* name)
{
	int page = 0;
    
	for(int i=0; i<g_GUI.view.size(); i++)
	{
		if(stricmp(g_GUI.view[i].name.c_str(), name) == 0 && g_GUI.view[i].opened)
		{
			page = g_GUI.view[i].page;
			g_GUI.view[i].close();
			break;
		}
	}
    
	if(!OpenAnotherView(name, page+1))
		OpenAnotherView(name, 0);
}

void RedoGUI()
{
	//g_GUI.view.clear();
    
	float halign = g_width/2 - 80;
    
    float fieldwidth = 280;
    float buttonw = 120;
    float centerw = g_width/2;
    
    /*
	AddView("loading");
	//AddImage("gui\\bg.jpg", 0, 0, g_width, g_height);
	AddText("status", "", ARIAL16, halign, g_height/2);
    */
    
    AddView("logo");
    AddImage("logo", g_width/2 - g_height/2, 0, g_width/2 + g_height/2, g_height, 1, 1, 1, 0);
    
    AddView("main");
	AddImage("bg", 0, 0, g_width, g_height);
	//AddImage("logo", 0, 0, g_width, g_height);
	//AddImage("3levelsgreen2%", 0, 0, g_width, g_height);
    AddButton("buttonbg", "Play Story", MSGOTHIC16, centerw-buttonw/2, 100.0f, centerw+buttonw/2, 144.0f, &Click_GoToStory, NULL, NULL);
    //AddButton("buttonbg", "Play Online", MSGOTHIC16, centerw-buttonw/2, 140.0f, centerw+buttonw/2, 170.0f, &Click_GoToOnline, NULL, NULL);
    AddButton("buttonbg", "Credits", MSGOTHIC16, centerw-buttonw/2, 180.0f, centerw+buttonw/2, 224.0f, &Click_GoToCredits, NULL, NULL);
    
    AddView("online");
	AddImage("bg", 0, 0, g_width, g_height);
    AddButton("buttonbg", "Register", MSGOTHIC16, centerw-buttonw/2, 100.0f, centerw+buttonw/2, 130.0f, &Click_GoToRegister, NULL, NULL);
    AddButton("buttonbg", "Login", MSGOTHIC16, centerw-buttonw/2, 140.0f, centerw+buttonw/2, 170.0f, &Click_GoToLogin, NULL, NULL);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, 180.0f, centerw+buttonw/2, 210.0f, &Click_GoToMain, NULL, NULL);

	/*
    AddView("login");
	AddImage("bg", 0, 0, g_width, g_height);
    AddTextField(@"Username", centerw-fieldwidth/2, 70.0f, fieldwidth, 31.0f, false, USERNAME);
    AddTextField(@"Password", centerw-fieldwidth/2, 120.0f, fieldwidth, 31.0f, true, PASSWORD);
    AddButton("buttonbg", "Login", MSGOTHIC16, centerw-buttonw*3/5-buttonw/2, 170.0f, centerw-buttonw*3/5+buttonw/2, 200.0f, &Click_Login, NULL, NULL);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw+buttonw*3/5-buttonw/2, 170.0f, centerw+buttonw*3/5+buttonw/2, 200.0f, &Click_GoToOnline, NULL, NULL);
    */
	/*
    AddView("register");
	AddImage("bg", 0, 0, g_width, g_height);
    AddTextField(@"Username", centerw-fieldwidth/2, 20.0f, fieldwidth, 31.0f, false, REGUSERNAME);
    AddTextField(@"Email address", centerw-fieldwidth/2, 55.0f, fieldwidth, 31.0f, false, REGEMAIL);
    AddTextField(@"Password", centerw-fieldwidth/2, 90.0f, fieldwidth, 31.0f, true, REGPASSWORD);
    AddTextField(@"Confirm password", centerw-fieldwidth/2, 125.0f, fieldwidth, 31.0f, true, REGPASSWORD2);
    AddButton("buttonbg", "Register", MSGOTHIC16, centerw-buttonw*3/5-buttonw/2, 170.0f, centerw-buttonw*3/5+buttonw/2, 205.0f, &Click_Register, NULL, NULL);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw+buttonw*3/5-buttonw/2, 170.0f, centerw+buttonw*3/5+buttonw/2, 205.0f, &Click_GoToOnline, NULL, NULL);
    */
	AddView("credits");
	AddImage("bg", 0, 0, g_width, g_height);
	AddText("PROGRAMMING", MSGOTHIC16, halign - 100, g_height/2 - g_font[MSGOTHIC16].gheight*4);
	AddText("Denis Ivanov", MSGOTHIC16, halign - 100, g_height/2 - g_font[MSGOTHIC16].gheight*3);
	//AddText("Volodar", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight*2);
	AddText("MAPS", MSGOTHIC16, halign + 100, g_height/2 - g_font[MSGOTHIC16].gheight*4);
	AddText("Robert Jarrett", MSGOTHIC16, halign + 100, g_height/2 - g_font[MSGOTHIC16].gheight*3);
	AddText("MODELS", MSGOTHIC16, halign + 100, g_height/2 - g_font[MSGOTHIC16].gheight*1);
	AddText("Robert Jarrett", MSGOTHIC16, halign + 100, g_height/2 - g_font[MSGOTHIC16].gheight*0);
	AddText("Sergey Harchenko", MSGOTHIC16, halign + 100, g_height/2 + g_font[MSGOTHIC16].gheight*1);
	AddText("Denis Ivanov", MSGOTHIC16, halign + 100, g_height/2 + g_font[MSGOTHIC16].gheight*2);
	AddText("Andrei Shirokov", MSGOTHIC16, halign + 100, g_height/2 + g_font[MSGOTHIC16].gheight*3);
	AddText("STORY", MSGOTHIC16, halign - 100, g_height/2 - g_font[MSGOTHIC16].gheight*1);
	AddText("Ruslan Sabitov", MSGOTHIC16, halign - 100, g_height/2 - g_font[MSGOTHIC16].gheight*0);
	AddText("VIDEO", MSGOTHIC16, halign - 100, g_height/2 + g_font[MSGOTHIC16].gheight*2);
	AddText("Ruslan Sabitov", MSGOTHIC16, halign - 100, g_height/2 + g_font[MSGOTHIC16].gheight*3);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-54, centerw+buttonw/2, g_height-10, &Click_GoToMain, NULL, NULL);
    
    //float dpadsize = 128.0f * (float)g_height / 320;
    float dpadsize = 128.0f;
    
	AddView("play");
	AddText("chat0", "", MSGOTHIC16, 5, 5+0*g_font[MSGOTHIC16].gheight);
	AddText("chat1", "", MSGOTHIC16, 5, 5+1*g_font[MSGOTHIC16].gheight);
	AddText("chat2", "", MSGOTHIC16, 5, 5+2*g_font[MSGOTHIC16].gheight);
	AddText("chat3", "", MSGOTHIC16, 5, 5+3*g_font[MSGOTHIC16].gheight);
	AddText("chat4", "", MSGOTHIC16, 5, 5+4*g_font[MSGOTHIC16].gheight);
	AddText("chat5", "", MSGOTHIC16, 5, 5+5*g_font[MSGOTHIC16].gheight);
	AddText("score", "Score: 0", MSGOTHIC16, 5, 5+6*g_font[MSGOTHIC16].gheight);
	AddText("hp", "", MSGOTHIC16, 5, 5+7*g_font[MSGOTHIC16].gheight);
	AddText("stamina", "Stamina: 1.00 / 1.0", MSGOTHIC16, 5, 5+8*g_font[MSGOTHIC16].gheight);
	AddText("ammo", "", MSGOTHIC16, 5, 5+9*g_font[MSGOTHIC16].gheight);
    AddDPad("rotational", "rotational", g_width-dpadsize, g_height-dpadsize, g_width, g_height, &Rotational);
    AddDPad("movement", "rotational", 0, g_height-dpadsize, dpadsize, g_height, &Movement);
	AddImage("crosshairtop", g_width/2 - 16, g_height/2 - 16, g_width/2 + 16, g_height/2 + 16);
	AddImage("crosshairbottom", g_width/2 - 16, g_height/2 - 16, g_width/2 + 16, g_height/2 + 16);
	AddImage("crosshairleft", g_width/2 - 16, g_height/2 - 16, g_width/2 + 16, g_height/2 + 16);
	AddImage("crosshairright", g_width/2 - 16, g_height/2 - 16, g_width/2 + 16, g_height/2 + 16);
    
	AddView("switch view");
    //AddButton("buttonbg", "Switch View", MSGOTHIC16, g_width-buttonw, 0, g_width, 44, &Click_SwitchView, NULL, NULL);
    AddButton("buttonbg", "Switch View", MSGOTHIC16, 0, 0, buttonw, 44, &Click_SwitchView, NULL, NULL);
    
    AddView("shoot");
    //AddButton("buttonbg", "Shoot", MSGOTHIC16, centerw-buttonw/2, g_height-90, centerw+buttonw/2, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
    AddButton("buttonbg", "Shoot", MSGOTHIC16, g_width-dpadsize-10-buttonw, g_height-90, g_width-dpadsize-10, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("swing");
    AddButton("buttonbg", "Swing", MSGOTHIC16, g_width-dpadsize-10-buttonw, g_height-90, g_width-dpadsize-10, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("stab");
    AddButton("buttonbg", "Stab", MSGOTHIC16, g_width-dpadsize-10-buttonw, g_height-90, g_width-dpadsize-10, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("switch item");
    //AddButton("buttonbg", "Switch Item", MSGOTHIC16, g_width-buttonw, 54, g_width, 98, &Click_EquipNext, NULL, NULL);
    AddButton("buttonbg", "Switch Item", MSGOTHIC16, 0, 54, buttonw, 98, &Click_EquipNext, NULL, NULL);
    
    AddView("reload");
    AddButton("buttonbg", "Reload", MSGOTHIC16, g_width-buttonw, g_height-dpadsize-10-60, g_width, g_height-dpadsize-10, &Click_Reload, NULL, NULL);
    
    AddView("crouch");
    //AddButton("buttonbg", "Crouch", MSGOTHIC16, g_width-buttonw*2-10, 0, g_width-buttonw-10, 60, &Click_Crouch, NULL, NULL);
    AddButton("buttonbg", "Crouch", MSGOTHIC16, g_width-buttonw*3-20, 0, g_width-buttonw*2-20, 60,  &Click_Crouch, NULL, NULL);
    
    AddView("jump");
    //AddButton("buttonbg", "Jump", MSGOTHIC16, buttonw+10, 00, buttonw*2+10, 60, &Click_Jump, NULL, &Unclick_Jump);
    AddButton("buttonbg", "Jump", MSGOTHIC16, g_width-buttonw, 00, g_width, 60, &Click_Jump, NULL, &Unclick_Jump);
    
    AddView("run");
    //AddButton("buttonbg", "Run", MSGOTHIC16, buttonw*2+20, 00, buttonw*3+20, 60, &Click_Run, NULL, NULL);
    AddButton("buttonbg", "Run", MSGOTHIC16, g_width-buttonw*2-10, 0, g_width-buttonw-10, 60, &Click_Run, NULL, NULL);
    
	AddView("pick up");
	AddImage("mp5icon", g_width-256, g_height/2-128, g_width, g_height/2+128);
	AddText("+1", MSGOTHIC16, g_width-100, g_height/2+28);
    
	AddView("open door");
    AddButton("buttonbg", "Open", MSGOTHIC16, g_width/2 - 75, g_height/2 - 90, g_width/2 + 75, g_height/2 - 30, &Click_Action, NULL, NULL);
    
	AddView("close door");
    AddButton("buttonbg", "Close", MSGOTHIC16, g_width/2 - 75, g_height/2 - 90, g_width/2 + 75, g_height/2 - 30, &Click_Action, NULL, NULL);
    
	AddView("game over");
	AddText("Game Over", MSGOTHIC16, g_width/2 - 32, g_height/2 - 8);
    AddButton("buttonbg", "Try Again", MSGOTHIC16, g_width/2 - 75, g_height/2 + 50, g_width/2 + 75, g_height/2 + 94, &Click_Retry, NULL, NULL);
    
    AddView("message");
	AddImage("bg", 0, 0, g_width, g_height);
    AddTextBox("error", "Hosho was the world's first commissioned ship to be designed and built as an aircraft carrier, and the first aircraft carrier of the Imperial Japanese Navy (IJN). Commissioned in 1922, the ship was used for testing aircraft and equipment, and to evaluate operational methods and tactics. She provided valuable lessons and experience for the IJN in early carrier air operations.", MSGOTHIC16, 50, 100, g_width-50, g_height-100);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-45, centerw+buttonw/2, g_height-10, &Click_GoToMain, NULL, NULL);
    
    AddView("dialog");
	AddImage("bg", 0, 0, g_width, g_height);
    AddTextBox("dialog", "", MSGOTHIC16, 50, 100, g_width-50, g_height-100);
    AddButton("buttonbg", "Continue", MSGOTHIC16, centerw-20-buttonw, g_height-45, centerw-20, g_height-1, &Click_DialogContinue, NULL, NULL);
    AddButton("buttonbg", "Don't Show Any More", MSGOTHIC16, centerw, g_height-45, centerw+buttonw*1.5f, g_height-1, &Click_DontShow, NULL, NULL);
    
    AddView("status");
	AddImage("bg", 0, 0, g_width, g_height);
    AddText("status", "Status...", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight/2);
    
    AddView("status back");
	AddImage("bg", 0, 0, g_width, g_height);
    AddText("status", "Status", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight/2);
    AddButton("buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-45, centerw+buttonw/2, g_height-10, &Click_GoToMain, NULL, NULL);
    
	//AddView("quit");	//Ask the player if they want to quit the multiplayer game
	// TO DO
    
    //OpenSoleView("login");
    OpenSoleView("logo");
}