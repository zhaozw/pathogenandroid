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
#ifndef USE_OMNI
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif

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

	GLubyte indices[] =
		{
			0, 1, 2, 3, 4, 5
		};
    
#ifndef USE_OMNI
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#else
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif

    //glDrawArrays(GL_TRIANGLES, 0, 6);
	glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, indices);
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
        length += g_font[f].glyph[t[i]].w * g_scale;
    tpos[0] = (left+right)/2.0f - length/2.0f;
    tpos[1] = (top+bottom)/2.0f - g_font[f].gheight*g_scale/2.0f;
	over = false;
	ldown = false;
	tex = CreateTexture(filepath);
	bgtex = CreateTexture("gui/buttonbg");
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
        length += g_font[f].glyph[t[i]].w * g_scale;
    tpos[0] = (left+right)/2.0f - length/2.0f;
    tpos[1] = (top+bottom)/2.0f - g_font[f].gheight*g_scale/2.0f;
	over = false;
	ldown = false;
	tex = CreateTexture(filepath);
	bgtex = CreateTexture("gui/buttonbg");
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
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], rgba[0], rgba[1], rgba[2], rgba[3]);
#else
    glUniform4f(g_slots[OMNI][COLOR], rgba[0], rgba[1], rgba[2], rgba[3]);
#endif
	DrawImage(tex, pos[0], pos[1], pos[2], pos[3]);
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
#else
    glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
#endif
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
    
	float halign = g_width/2 - 80*g_scale;
    
    float fieldwidth = 280*g_scale;
    float buttonw = 120*g_scale;
    float centerw = g_width/2;
    
    /*
	AddView("loading");
	//AddImage("gui\\bg.jpg", 0, 0, g_width, g_height);
	AddText("status", "", ARIAL16, halign, g_height/2);
    */
    
    AddView("logo");
    AddImage("gui/logo", g_width/2 - g_height/2, 0, g_width/2 + g_height/2, g_height, 1, 1, 1, 0);
    
    AddView("main");
	AddImage("gui/bg", 0, 0, g_width, g_height);
	//AddImage("logo", 0, 0, g_width, g_height);
	//AddImage("3levelsgreen2%", 0, 0, g_width, g_height);
    AddButton("gui/buttonbg", "Play Story", MSGOTHIC16, centerw-buttonw/2, 100.0f*g_scale, centerw+buttonw/2, 144.0f*g_scale, &Click_GoToStory, NULL, NULL);
    //AddButton("gui/buttonbg", "Play Online", MSGOTHIC16, centerw-buttonw/2, 140.0f*g_scale, centerw+buttonw/2, 170.0f*g_scale, &Click_GoToOnline, NULL, NULL);
    AddButton("gui/buttonbg", "Credits", MSGOTHIC16, centerw-buttonw/2, 180.0f*g_scale, centerw+buttonw/2, 224.0f*g_scale, &Click_GoToCredits, NULL, NULL);
    
    AddView("online");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddButton("gui/buttonbg", "Register", MSGOTHIC16, centerw-buttonw/2, 100.0f*g_scale, centerw+buttonw/2, 130.0f*g_scale, &Click_GoToRegister, NULL, NULL);
    AddButton("gui/buttonbg", "Login", MSGOTHIC16, centerw-buttonw/2, 140.0f*g_scale, centerw+buttonw/2, 170.0f*g_scale, &Click_GoToLogin, NULL, NULL);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, 180.0f*g_scale, centerw+buttonw/2, 210.0f*g_scale, &Click_GoToMain, NULL, NULL);

	/*
    AddView("login");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddTextField(@"Username", centerw-fieldwidth/2, 70.0f, fieldwidth, 31.0f, false, USERNAME);
    AddTextField(@"Password", centerw-fieldwidth/2, 120.0f, fieldwidth, 31.0f, true, PASSWORD);
    AddButton("gui/buttonbg", "Login", MSGOTHIC16, centerw-buttonw*3/5-buttonw/2, 170.0f, centerw-buttonw*3/5+buttonw/2, 200.0f, &Click_Login, NULL, NULL);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw+buttonw*3/5-buttonw/2, 170.0f, centerw+buttonw*3/5+buttonw/2, 200.0f, &Click_GoToOnline, NULL, NULL);
    */
	/*
    AddView("register");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddTextField(@"Username", centerw-fieldwidth/2, 20.0f, fieldwidth, 31.0f, false, REGUSERNAME);
    AddTextField(@"Email address", centerw-fieldwidth/2, 55.0f, fieldwidth, 31.0f, false, REGEMAIL);
    AddTextField(@"Password", centerw-fieldwidth/2, 90.0f, fieldwidth, 31.0f, true, REGPASSWORD);
    AddTextField(@"Confirm password", centerw-fieldwidth/2, 125.0f, fieldwidth, 31.0f, true, REGPASSWORD2);
    AddButton("gui/buttonbg", "Register", MSGOTHIC16, centerw-buttonw*3/5-buttonw/2, 170.0f, centerw-buttonw*3/5+buttonw/2, 205.0f, &Click_Register, NULL, NULL);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw+buttonw*3/5-buttonw/2, 170.0f, centerw+buttonw*3/5+buttonw/2, 205.0f, &Click_GoToOnline, NULL, NULL);
    */
	AddView("credits");
	AddImage("gui/bg", 0, 0, g_width, g_height);
	AddText("PROGRAMMING", MSGOTHIC16, halign - 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*4*g_scale);
	AddText("Denis Ivanov", MSGOTHIC16, halign - 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*3*g_scale);
	//AddText("Volodar", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight*2*g_scale);
	AddText("MAPS", MSGOTHIC16, halign + 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*4*g_scale);
	AddText("Robert Jarrett", MSGOTHIC16, halign + 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*3*g_scale);
	AddText("MODELS", MSGOTHIC16, halign + 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*1*g_scale);
	AddText("Robert Jarrett", MSGOTHIC16, halign + 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*0*g_scale);
	AddText("Sergey Harchenko", MSGOTHIC16, halign + 100*g_scale, g_height/2 + g_font[MSGOTHIC16].gheight*1*g_scale);
	AddText("Denis Ivanov", MSGOTHIC16, halign + 100*g_scale, g_height/2 + g_font[MSGOTHIC16].gheight*2*g_scale);
	AddText("Andrei Shirokov", MSGOTHIC16, halign + 100*g_scale, g_height/2 + g_font[MSGOTHIC16].gheight*3*g_scale);
	AddText("STORY", MSGOTHIC16, halign - 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*1*g_scale);
	AddText("Ruslan Sabitov", MSGOTHIC16, halign - 100*g_scale, g_height/2 - g_font[MSGOTHIC16].gheight*0*g_scale);
	AddText("VIDEO", MSGOTHIC16, halign - 100*g_scale, g_height/2 + g_font[MSGOTHIC16].gheight*2*g_scale);
	AddText("Ruslan Sabitov", MSGOTHIC16, halign - 100*g_scale, g_height/2 + g_font[MSGOTHIC16].gheight*3*g_scale);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-54*g_scale, centerw+buttonw/2, g_height-10*g_scale, &Click_GoToMain, NULL, NULL);
    
    //float dpadsize = 128.0f * (float)g_height / 320;
    float dpadsize = 128.0f*g_scale;
    
	AddView("play");
	AddText("chat0", "", MSGOTHIC16, 5*g_scale, 5*g_scale+0*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("chat1", "", MSGOTHIC16, 5*g_scale, 5*g_scale+1*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("chat2", "", MSGOTHIC16, 5*g_scale, 5*g_scale+2*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("chat3", "", MSGOTHIC16, 5*g_scale, 5*g_scale+3*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("chat4", "", MSGOTHIC16, 5*g_scale, 5*g_scale+4*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("chat5", "", MSGOTHIC16, 5*g_scale, 5*g_scale+5*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("score", "Score: 0", MSGOTHIC16, 5*g_scale, 5*g_scale+6*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("hp", "", MSGOTHIC16, 5*g_scale, 5*g_scale+7*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("stamina", "Stamina: 1.00 / 1.0", MSGOTHIC16, 5*g_scale, 5*g_scale+8*g_font[MSGOTHIC16].gheight*g_scale);
	AddText("ammo", "", MSGOTHIC16, 5*g_scale, 5*g_scale+9*g_font[MSGOTHIC16].gheight*g_scale);
    AddDPad("rotational", "gui/rotational", g_width-dpadsize, g_height-dpadsize, g_width, g_height, &Rotational);
    AddDPad("movement", "gui/rotational", 0, g_height-dpadsize, dpadsize, g_height, &Movement);
	AddImage("gui/crosshairtop", g_width/2 - 16*g_scale, g_height/2 - 16*g_scale, g_width/2 + 16*g_scale, g_height/2 + 16*g_scale);
	AddImage("gui/crosshairbottom", g_width/2 - 16*g_scale, g_height/2 - 16*g_scale, g_width/2 + 16*g_scale, g_height/2 + 16*g_scale);
	AddImage("gui/crosshairleft", g_width/2 - 16*g_scale, g_height/2 - 16*g_scale, g_width/2 + 16*g_scale, g_height/2 + 16*g_scale);
	AddImage("gui/crosshairright", g_width/2 - 16*g_scale, g_height/2 - 16*g_scale, g_width/2 + 16*g_scale, g_height/2 + 16*g_scale);
    
	AddView("switch view");
    //AddButton("gui/buttonbg", "Switch View", MSGOTHIC16, g_width-buttonw, 0, g_width, 44, &Click_SwitchView, NULL, NULL);
    AddButton("gui/buttonbg", "Switch View", MSGOTHIC16, 0*g_scale, 0*g_scale, buttonw, 44*g_scale, &Click_SwitchView, NULL, NULL);
    
    AddView("shoot");
    //AddButton("gui/buttonbg", "Shoot", MSGOTHIC16, centerw-buttonw/2, g_height-90, centerw+buttonw/2, g_height-30, &Click_Shoot, NULL, &Unclick_Shoot);
    AddButton("gui/buttonbg", "Shoot", MSGOTHIC16, g_width-dpadsize-10*g_scale-buttonw, g_height-90*g_scale, g_width-dpadsize-10, g_height-30*g_scale, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("swing");
    AddButton("gui/buttonbg", "Swing", MSGOTHIC16, g_width-dpadsize-10*g_scale-buttonw, g_height-90*g_scale, g_width-dpadsize-10*g_scale, g_height-30*g_scale, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("stab");
    AddButton("gui/buttonbg", "Stab", MSGOTHIC16, g_width-dpadsize-10*g_scale-buttonw, g_height-90*g_scale, g_width-dpadsize-10*g_scale, g_height-30*g_scale, &Click_Shoot, NULL, &Unclick_Shoot);
    
    AddView("switch item");
    //AddButton("gui/buttonbg", "Switch Item", MSGOTHIC16, g_width-buttonw, 54, g_width, 98, &Click_EquipNext, NULL, NULL);
    AddButton("gui/buttonbg", "Switch Item", MSGOTHIC16, 0*g_scale, 54*g_scale, buttonw, 98*g_scale, &Click_EquipNext, NULL, NULL);
    
    AddView("reload");
    AddButton("gui/buttonbg", "Reload", MSGOTHIC16, g_width-buttonw, g_height-dpadsize-10*g_scale-60*g_scale, g_width, g_height-dpadsize-10*g_scale, &Click_Reload, NULL, NULL);
    
    AddView("crouch");
    //AddButton("gui/buttonbg", "Crouch", MSGOTHIC16, g_width-buttonw*2-10, 0, g_width-buttonw-10, 60, &Click_Crouch, NULL, NULL);
    AddButton("gui/buttonbg", "Crouch", MSGOTHIC16, g_width-buttonw*3-20*g_scale, 0*g_scale, g_width-buttonw*2-20*g_scale, 60*g_scale,  &Click_Crouch, NULL, NULL);
    
    AddView("jump");
    //AddButton("gui/buttonbg", "Jump", MSGOTHIC16, buttonw+10, 00, buttonw*2+10, 60, &Click_Jump, NULL, &Unclick_Jump);
    AddButton("gui/buttonbg", "Jump", MSGOTHIC16, g_width-buttonw, 00*g_scale, g_width, 60*g_scale, &Click_Jump, NULL, &Unclick_Jump);
    
    AddView("run");
    //AddButton("gui/buttonbg", "Run", MSGOTHIC16, buttonw*2+20, 00, buttonw*3+20, 60, &Click_Run, NULL, NULL);
    AddButton("gui/buttonbg", "Run", MSGOTHIC16, g_width-buttonw*2-10*g_scale, 0*g_scale, g_width-buttonw-10*g_scale, 60*g_scale, &Click_Run, NULL, NULL);
    
	AddView("pick up");
	AddImage("gui/mp5icon", g_width-256*g_scale, g_height/2-128*g_scale, g_width, g_height/2+128*g_scale);
	AddText("+1", MSGOTHIC16, g_width-100*g_scale, g_height/2+28*g_scale);
    
	AddView("open door");
    AddButton("gui/buttonbg", "Open", MSGOTHIC16, g_width/2 - 75*g_scale, g_height/2 - 90*g_scale, g_width/2 + 75*g_scale, g_height/2 - 30*g_scale, &Click_Action, NULL, NULL);
    
	AddView("close door");
    AddButton("gui/buttonbg", "Close", MSGOTHIC16, g_width/2 - 75*g_scale, g_height/2 - 90*g_scale, g_width/2 + 75*g_scale, g_height/2 - 30*g_scale, &Click_Action, NULL, NULL);
    
	AddView("game over");
	AddText("Game Over", MSGOTHIC16, g_width/2 - 32*g_scale, g_height/2 - 8*g_scale);
    AddButton("gui/buttonbg", "Try Again", MSGOTHIC16, g_width/2 - 75*g_scale, g_height/2 + 50*g_scale, g_width/2 + 75*g_scale, g_height/2 + 94*g_scale, &Click_Retry, NULL, NULL);
    
    AddView("message");
	AddImage("gui/bg", 0*g_scale, 0*g_scale, g_width, g_height);
    AddTextBox("error", "Hosho was the world's first commissioned ship to be designed and built as an aircraft carrier, and the first aircraft carrier of the Imperial Japanese Navy (IJN). Commissioned in 1922, the ship was used for testing aircraft and equipment, and to evaluate operational methods and tactics. She provided valuable lessons and experience for the IJN in early carrier air operations.", MSGOTHIC16, 50, 100, g_width-50, g_height-100);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-45*g_scale, centerw+buttonw/2, g_height-10*g_scale, &Click_GoToMain, NULL, NULL);
    
    AddView("dialog");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddTextBox("dialog", "", MSGOTHIC16, 50*g_scale, 100*g_scale, g_width-50*g_scale, g_height-100*g_scale);
    AddButton("gui/buttonbg", "Continue", MSGOTHIC16, centerw-20*g_scale-buttonw, g_height-45*g_scale, centerw-20*g_scale, g_height-1, &Click_DialogContinue, NULL, NULL);
    AddButton("gui/buttonbg", "Don't Show Any More", MSGOTHIC16, centerw, g_height-45*g_scale, centerw+buttonw*1.5f, g_height-1, &Click_DontShow, NULL, NULL);
    
    AddView("status");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddText("status", "Status...", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight*g_scale/2);
    
    AddView("status back");
	AddImage("gui/bg", 0, 0, g_width, g_height);
    AddText("status", "Status", MSGOTHIC16, halign, g_height/2 - g_font[MSGOTHIC16].gheight*g_scale/2);
    AddButton("gui/buttonbg", "Back", MSGOTHIC16, centerw-buttonw/2, g_height-45*g_scale, centerw+buttonw/2, g_height-10*g_scale, &Click_GoToMain, NULL, NULL);
    
	//AddView("quit");	//Ask the player if they want to quit the multiplayer game
	// TO DO
    
    //OpenSoleView("login");
    OpenSoleView("logo");
}