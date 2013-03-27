

#include "skybox.h"
#include "main.h"
#include "3dmath.h"
#include "image.h"
#include "shader.h"

unsigned int g_front;
unsigned int g_left;
unsigned int g_right;
unsigned int g_bottom;
unsigned int g_top;
unsigned int g_back;

void LoadSkyBox(const char* basename)
{
	char front[128];
	char left[128];
	char right[128];
	char bottom[128];
	char top[128];
	char back[128];
    
	sprintf(front, "textures/%sft^", basename);
	sprintf(left, "textures/%slf^", basename);
	sprintf(right, "textures/%srt^", basename);
	sprintf(bottom, "textures/%sdn^", basename);
	sprintf(top, "textures/%sup^", basename);
	sprintf(back, "textures/%sbk^", basename);
    
	g_front = CreateTexture(front);
	g_left = CreateTexture(left);
	g_right = CreateTexture(right);
	g_bottom = CreateTexture(bottom);
	g_top = CreateTexture(top);
	g_back = CreateTexture(back);
}

void DrawQuad(unsigned int tex, CVector3 a, CVector2 ta, CVector3 b, CVector2 tb, CVector3 c, CVector2 tc, CVector3 d, CVector2 td)
{
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
    glUniform1i(g_slots[SKY][TEXTURE], 0);
    
    float vertices[] =
    {
        //posx, posy posz   texx, texy
        a.x, a.y, a.z,          ta.x, ta.y,
        b.x, b.y, b.z,          tb.x, tb.y,
        c.x, c.y, c.z,          tc.x, tc.y,
        
        c.x, c.y, c.z,          tc.x, tc.y,
        d.x, d.y, d.z,          td.x, td.y,
        a.x, a.y, a.z,          ta.x, ta.y
    };
    
    glVertexAttribPointer(g_slots[SKY][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[SKY][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
}

void DrawSkyBox(CVector3 pos)
{
    glDisable(GL_DEPTH_TEST);
    
    //GLKMatrix4 modelmat = GLKMatrix4MakeTranslation(pos.x, pos.y, pos.z);
    //glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.m);

	CMatrix modelmat;
	float trans[] = {pos.x, pos.y, pos.z};
	modelmat.setTranslation(trans);
	glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.getMatrix());
    
    DrawQuad(g_right,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1));
    
    DrawQuad(g_left,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    DrawQuad(g_front,
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1));
    
    DrawQuad(g_back,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    DrawQuad(g_bottom,
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    DrawQuad(g_top,
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TOP_1, SKY_TOP_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TOP_0, SKY_TOP_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TOP_0, SKY_TOP_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TOP_1, SKY_TOP_0));
    
    glEnable(GL_DEPTH_TEST);
}