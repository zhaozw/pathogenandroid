

#include "main.h"
#include "font.h"
#include "image.h"
#include "logger.h"
#include "shader.h"

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
	if(fp.fsize <= 0)
    {
        LOGE("Error loading font %s", fontfile);
        return;
    }
    
    //fseek(fp, 0, SEEK_END);
	fp.seekend();
    int size = fp.tell(); //ftell(fp);
    //rewind(fp);
	fp.seek(0);
    
    char* file = new char[size];
    fp.read((void*)file, size);
    //fclose(fp);
    
    //NSLog(@"%s", file);
    
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

void DrawGlyph(unsigned int tex, float left, float top, float right, float bottom, float texleft, float textop, float texright, float texbottom)
{
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
    
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
    
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

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
        right = x + g->w;
        top = y;
        bottom = y + g->h;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w;
    }
}

void DrawShadowedText(int font, float x, float y, const char* str, float* color)
{
    glUniform4f(g_slots[ORTHO][COLOR], 0, 0, 0, 1);
    DrawText(font, x+1, y+1, str);
    
    if(color == NULL)
        glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
    else
        glUniform4f(g_slots[ORTHO][COLOR], color[0], color[1], color[2], color[3]);
    DrawText(font, x, y, str);
    
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
}

void DrawBoxShadText(int font, float startx, float starty, float width, float height, const char* str, float* color)
{
    CFont* f = &g_font[font];
    
    int size = strlen(str);
    
    CGlyph* g;
    CGlyph* g2;
    int left, top, right, bottom;
    float texleft, textop, texright, texbottom;
    int x = startx + 1;
    int y = starty + 1;
    int nexti = 0;  //next [i] to skip line
    int lastspace;
    int j, x1;
    
    glUniform4f(g_slots[ORTHO][COLOR], 0, 0, 0, 1);
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx+1;
                y += f->gheight;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w;
                
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
        right = x + g->w;
        top = y;
        bottom = y + g->h;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w;
    }
    
    nexti = 0;
    x = startx;
    y = starty;
    
    if(color == NULL)
        glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
    else
        glUniform4f(g_slots[ORTHO][COLOR], color[0], color[1], color[2], color[3]);
    
    for(int i=0; i<size; i++)
    {
        g = &f->glyph[str[i]];
        
        if(i == nexti)
        {
            if(nexti != 0)
            {
                x = startx;
                y += f->gheight;
            }
            
            lastspace = -1;
            x1 = startx;
            
            for(j=i; j<size; j++)
            {
                g2 = &f->glyph[str[j]];
                x1 += g2->w;
                
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
        right = x + g->w;
        top = y;
        bottom = y + g->h;
        texleft = g->x / f->width;
        texright = (g->x + g->w) / f->width;
        textop = g->y / f->height;
        texbottom = (g->y + g->h) / f->height;
        DrawGlyph(f->tex, left, top, right, bottom, texleft, textop, texright, texbottom);
        x += g->w;
    }
}


void LoadFonts()
{
    LoadFont(MSGOTHIC16, "msgothic16");
    LoadFont(MSGOTHIC16B, "msgothic16b");
    LoadFont(MSUIGOTHIC16, "msuigothic16");
}