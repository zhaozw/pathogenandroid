

#include "main.h"
#include "font.h"
#include "image.h"
#include "logger.h"
#include "shader.h"
#include "model.h"

CFont g_font[FONTS];

int hexToInt(const char* s)
{
    int x;
    stringstream ss;
    ss << std::hex << s;
    ss >> x;
    return x;
}

int strToInt(const char *s)
{
    int x;
    istringstream iss(s);
    iss >> x;
    return x;
}

void FSub(const char* str, int& j, int& x, int& y, int& w, int& h)
{
    j = 0;
    
    int size = strlen(str);
    int i;
    
    for(i=0; i<size; i++)
        if(str[i] != ' ' && str[i] != '\n' && str[i] != '\r' && str[i] != '\t')
            break;
    
    if(str[i] == '"')
    {
        i++;    //skip over the "
        j = str[i];
        i++;    //skip over c
        i++;    //skip over the "
    }
    else
    {
        string hexstr = "";
        
        //if(str[i] == ' ')
         //   i++;
        
        for(; i<size; i++)
        {
            if(str[i] == ',')
                break;
            
            hexstr += str[i];
        }
        
        j = hexToInt(hexstr.c_str());
    }
    
    i++;    //skip over the ,
    
    string substr[4];
    
    for(int k=0; k<4; k++)
    {
        substr[k] = "";
        
        for(; i<size; i++)
        {
            if(str[i] == ',')
            {
                i++;
                break;
            }
            
            substr[k] += str[i];
        }
    }

    x = strToInt(substr[0].c_str());
    y = strToInt(substr[1].c_str());
    w = strToInt(substr[2].c_str());
    h = strToInt(substr[3].c_str());
}

void LoadFont(int id, const char* fontfile)
{
    CFont* f = &g_font[id];
	char texfile[128];
	char raw[64];
	StripPathExtension(fontfile, raw);
	sprintf(texfile, "fonts/%s", raw);
    f->tex = CreateTexture(texfile);
    f->width = g_texwidth;
    f->height = g_texheight;

	char fullfont[128];
	sprintf(fullfont, "fonts/%s.fnt", raw);
    
    //FILE* fp = fopen(fullfont, "r");
	CFile fp(fullfont);

    //if(!fp)
	//if(fp.fsize <= 0)
	if(!fp.mFile)
    {
        LOGE("Error loading font %s", fontfile);
        return;
    }
    
    //fseek(fp, 0, SEEK_END);
	//fp.seekend();
    //int size = fp.tell(); //ftell(fp);
    //rewind(fp);
	//fp.seek(0);
	//int size = fp.fsize;
	int size = fp.remain();
    
    //char* file = (char*)fp.data; //new char[size];
	char* file = new char[size+1];
    fp.read((void*)file, size);
	file[size] = '\0';
    //fclose(fp);
    
    //NSLog(@"%s", file);

	//LOGI("font %s size=%d", fontfile, size);
	//LOGI("%s", file);
    
    int i;
    string substr;
    int x, y, w, h, j;
    
    for(i = 0; i<size; i++)
    {
        substr = "";
        
        //skip whitespace
        for(; i<size; i++)
            if(file[i] != ' ' && file[i] != '\n' && file[i] != '\r' && file[i] != '\t')
                break;
        
        //write directive
        for(; i<size; i++)
        {
            if(file[i] == ' ' || file[i] == '\n' || file [i] == '\r' || file[i] == '\t' || file[i] == '=')
                break;
            
            substr += file[i];
        }
        
        i++;
        
        if(!stricmp(substr.c_str(), "[HGEFONT]"))
            continue;
        else if(!stricmp(substr.c_str(), "Bitmap"))
        {
            //skip the file name
            for(; i<size; i++)
                if(file[i] == ' ' || file[i] == '\n' || file [i] == '\r' || file[i] == '\t')
                    break;
            continue;
        }
        else if(!stricmp(substr.c_str(), "Char"))
        {
            substr = "";
            
            for(; i<size; i++)
            {
                if(file[i] == '\n' || file [i] == '\r')
                    break;
                
                substr += file[i];
            }
            
            FSub(substr.c_str(), j, x, y, w, h);
            
            CGlyph* g = &f->glyph[j];
            g->x = x;
            g->y = y;
            g->w = w;
            g->h = h;
        }
    }
    
    f->gheight = f->glyph['A'].h;
    
    delete [] file;
    LOGI("%s.fnt", fontfile);
}
/*
void DrawGlyph(unsigned int tex, float left, float top, float right, float bottom, float texleft, float textop, float texright, float texbottom)
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
        left, top,0,          texleft, textop,
        right, top,0,         texright, textop,
        right, bottom,0,      texright, texbottom,
        
        right, bottom,0,      texright, texbottom,
        left, bottom,0,       texleft, texbottom,
        left, top,0,          texleft, textop
    };

    glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_DYNAMIC_DRAW);
    
#ifndef USE_OMNI
    //glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    //glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
    glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif

    glDrawArrays(GL_TRIANGLES, 0, 6);
}
*/
void Glyph_fillvbo(float* va, float left, float top, float right, float bottom, float texleft, float textop, float texright, float texbottom)
{/*
    float vertices[] =
    {
        //posx, posy    texx, texy
        left, top,0,          texleft, textop,
        right, top,0,         texright, textop,
        right, bottom,0,      texright, texbottom,
        
        right, bottom,0,      texright, texbottom,
        left, bottom,0,       texleft, texbottom,
        left, top,0,          texleft, textop
    };*/

	va[0] = left;
	va[1] = top;
	va[2] = 0;
	va[3] = texleft;
	va[4] = textop;

	va[5] = right;
	va[6] = top;
	va[7] = 0;
	va[8] = texright;
	va[9] = textop;

	va[10] = right;
	va[11] = bottom;
	va[12] = 0;
	va[13] = texright;
	va[14] = texbottom;

	va[15] = right;
	va[16] = bottom;
	va[17] = 0;
	va[18] = texright;
	va[19] = texbottom;

	va[20] = left;
	va[21] = bottom;
	va[22] = 0;
	va[23] = texleft;
	va[24] = texbottom;

	va[25] = left;
	va[26] = top;
	va[27] = 0;
	va[28] = texleft;
	va[29] = textop;
}
/*
void DrawText(int font, float x, float y, const char* str)
{
    CFont* f = &g_font[font];
    
    int size = strlen(str);
    
    CGlyph* g;
    int left, top, right, bottom;
    float texleft, textop, texright, texbottom;
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w *g_scale;
    }
}*/

void DrawText(int font, unsigned int vbo, int texlen)
{
    CFont* f = &g_font[font];

	glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, f->tex);
#ifndef USE_OMNI
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif

	glBindBuffer(GL_ARRAY_BUFFER, vbo);
#ifndef USE_OMNI
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
	glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#endif

    glDrawArrays(GL_TRIANGLES, 0, texlen * 6);
}

void Text_fillvbo(int font, float x, float y, const char* str, unsigned int* vbo)
{
    CFont* f = &g_font[font];
    
    int size = strlen(str);
    
    CGlyph* g;
    int left, top, right, bottom;
    float texleft, textop, texright, texbottom;

	int vboindex = 0;
	int numverts = size * 3 * 2;
	const int vbobytes = numverts * 5 * 4;
	float* va = new float[ numverts * 5 ];
	const int vbostride = 5 * 6;
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        //DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
		Glyph_fillvbo(&(va[vboindex]), left, top, right, bottom, texleft, textop, texright, texbottom);
		vboindex += vbostride;
        x += g->w *g_scale;
    }

	glGenBuffers(1, vbo);
	glBindBuffer(GL_ARRAY_BUFFER, *vbo);
    glBufferData(GL_ARRAY_BUFFER, vbobytes, va, GL_STATIC_DRAW);

	delete [] va;
}
/*
void DrawShadowedText(int font, float x, float y, const char* str, float* color)
{
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], 0, 0, 0, 1);
#else
    glUniform4f(g_slots[OMNI][COLOR], 0, 0, 0, 1);
#endif
    DrawText(font, x+1*g_scale, y+1*g_scale, str);
    
    if(color == NULL)
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
#else
        glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
#endif
    else
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], color[0], color[1], color[2], color[3]);
#else
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
#endif
    DrawText(font, x, y, str);
    
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
#else
    glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
#endif
}*/

void ShadowedText_fillvbo(int font, float x, float y, const char* str, unsigned int* textvbo, unsigned int* textshadvbo)
{
    Text_fillvbo(font, x+1*g_scale, y+1*g_scale, str, textshadvbo);
    
    Text_fillvbo(font, x, y, str, textvbo);
}
/*
void DrawBoxShadText(int font, float startx, float starty, float width, float height, const char* str, float* color)
{
    CFont* f = &g_font[font];
    
    int size = strlen(str);
    
    CGlyph* g;
    CGlyph* g2;
    int left, top, right, bottom;
    float texleft, textop, texright, texbottom;
    int x = startx + 1*g_scale;
    int y = starty + 1*g_scale;
    int nexti = 0;  //next [i] to skip line
    int lastspace;
    int j, x1;
    
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], 0, 0, 0, 1);
#else
    glUniform4f(g_slots[OMNI][COLOR], 0, 0, 0, 1);
#endif
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx+1*g_scale;
                y += f->gheight*g_scale;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w *g_scale;
                
                if(str[j] == ' ' || str[j] == '\t')
                    lastspace = j;
                
                if(x1 > width)
                {
                    if(lastspace < 0)
                        continue;
                    
                    nexti = lastspace+1;
                    break;
                }
            }
        }
        
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w *g_scale;
    }
    
    nexti = 0;
    x = startx;
    y = starty;
    
    if(color == NULL)
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
#else
        glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
#endif
    else
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], color[0], color[1], color[2], color[3]);
#else
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
#endif

    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx;
                y += f->gheight *g_scale;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w *g_scale;
                
                if(str[j] == ' ' || str[j] == '\t')
                    lastspace = j;
                
                if(x1 > width)
                {
                    if(lastspace < 0)
                        continue;
                    
                    nexti = lastspace+1;
                    break;
                }
            }
        }
        
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w *g_scale;
    }
}*/

void DrawShadText(int font, unsigned int textshadvbo, unsigned int textvbo, int texlen, float* color)
{
    CFont* f = &g_font[font];

	glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, f->tex);
#ifndef USE_OMNI
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif
	
#ifndef USE_OMNI
    glUniform4f(g_slots[ORTHO][COLOR], 0, 0, 0, 1);
#else
    glUniform4f(g_slots[OMNI][COLOR], 0, 0, 0, 1);
#endif

	glBindBuffer(GL_ARRAY_BUFFER, textshadvbo);
#ifndef USE_OMNI
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
	glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#endif
    glDrawArrays(GL_TRIANGLES, 0, texlen * 6);
	//glDrawArrays(GL_TRIANGLES, 0, 6);

    if(color == NULL)
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
#else
        glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
#endif
    else
#ifndef USE_OMNI
        glUniform4f(g_slots[ORTHO][COLOR], color[0], color[1], color[2], color[3]);
#else
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
#endif
	
	glBindBuffer(GL_ARRAY_BUFFER, textvbo);
#ifndef USE_OMNI
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
	glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#endif
    glDrawArrays(GL_TRIANGLES, 0, texlen * 6);
	//glDrawArrays(GL_TRIANGLES, 0, 6);
}

void BoxShadText_fillvbo(int font, float startx, float starty, float width, float height, const char* str, unsigned int* textvbo, unsigned int* textshadvbo)
{
	CFont* f = &g_font[font];
    
    int size = strlen(str);
    
    CGlyph* g;
    CGlyph* g2;
    int left, top, right, bottom;
    float texleft, textop, texright, texbottom;
    int x = startx + 1*g_scale;
    int y = starty + 1*g_scale;
    int nexti = 0;  //next [i] to skip line
    int lastspace;
    int j, x1;

	int vboindex = 0;
	int numverts = size * 3 * 2;
	const int vbobytes = numverts * 5 * 4;
	float* va = new float[ numverts * 5 ];
	const int vbostride = 6 * 5;
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx+1*g_scale;
                y += f->gheight*g_scale;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w *g_scale;
                
                if(str[j] == ' ' || str[j] == '\t')
                    lastspace = j;
                
                if(x1 > width)
                {
                    if(lastspace < 0)
                        continue;
                    
                    nexti = lastspace+1;
                    break;
                }
            }
        }
        
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;

        //DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
		Glyph_fillvbo(&(va[vboindex]), left, top, right, bottom, texleft, textop, texright, texbottom);
		vboindex += vbostride;

        x += g->w *g_scale;
    }
    
	glGenBuffers(1, textshadvbo);
	glBindBuffer(GL_ARRAY_BUFFER, *textshadvbo);
    glBufferData(GL_ARRAY_BUFFER, vbobytes, va, GL_STATIC_DRAW);

	//LOGI("textshadvbo %u", *textshadvbo);

    nexti = 0;
    x = startx;
    y = starty;

	vboindex = 0;

    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx;
                y += f->gheight *g_scale;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w *g_scale;
                
                if(str[j] == ' ' || str[j] == '\t')
                    lastspace = j;
                
                if(x1 > width)
                {
                    if(lastspace < 0)
                        continue;
                    
                    nexti = lastspace+1;
                    break;
                }
            }
        }
        
        left = x;
        right = x + g->w *g_scale;
        top = y;
        bottom = y + g->h *g_scale;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;

        //DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
		Glyph_fillvbo(&(va[vboindex]), left, top, right, bottom, texleft, textop, texright, texbottom);
		vboindex += vbostride;

        x += g->w *g_scale;
    }
	
	glGenBuffers(1, textvbo);
	glBindBuffer(GL_ARRAY_BUFFER, *textvbo);
    glBufferData(GL_ARRAY_BUFFER, vbobytes, va, GL_STATIC_DRAW);

	//LOGI("textvbo %u", *textvbo);

	delete [] va;
}

void LoadFonts()
{
    LoadFont(MSGOTHIC16, "msgothic16");
    LoadFont(MSGOTHIC16B, "msgothic16b");
    LoadFont(MSUIGOTHIC16, "msuigothic16");
}
