

#include <string>
#include <sstream>
#include <iostream>

using namespace std;

#define MAX_CHARS	256

class CGlyph
{
public:
    int x;
    int y;
    int w;
    int h;
    
    CGlyph()
    {
        x = 0;
        y = 0;
        w = 0;
        h = 0;
    }
};

class CFont
{
public:
    unsigned int tex;
    float width;    //image width
    float height;   //image height
    float gheight;  //glyph height
    CGlyph glyph[MAX_CHARS];
    
	CFont()
	{
	}
};

enum FONT{MSGOTHIC16, MSGOTHIC16B, MSUIGOTHIC16, FONTS};
extern CFont g_font[FONTS];

void LoadFonts();
void DrawGlyph(unsigned int tex, float left, float top, float right, float bottom, float texleft, float textop, float texright, float texbottom);
//void DrawText(int font, float x, float y, const char* str);
void DrawText(int font, unsigned int vbo, int texlen);
void Text_fillvbo(int font, float x, float y, const char* str, unsigned int* vbo);
//void DrawShadowedText(int font, float x, float y, const char* str, float* color=NULL);
void ShadowedText_fillvbo(int font, float x, float y, const char* str, unsigned int* textvbo, unsigned int* textshadvbo);
//void DrawBoxShadText(int font, float startx, float starty, float width, float height, const char* str, float* color);
void DrawShadText(int font, unsigned int textshadvbo, unsigned int textvbo, int texlen, float* color=NULL);
void BoxShadText_fillvbo(int font, float startx, float starty, float width, float height, const char* str, unsigned int* textvbo, unsigned int* textshadvbo);