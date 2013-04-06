

#include "skybox.h"
#include "main.h"
#include "3dmath.h"
#include "image.h"
#include "shader.h"
#include "logger.h"

unsigned int g_front;
unsigned int g_left;
unsigned int g_right;
unsigned int g_bottom;
unsigned int g_top;
unsigned int g_back;
unsigned int g_skyVBO = 0;

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

/*
void DrawQuad(unsigned int tex, CVector3 a, CVector2 ta, CVector3 b, CVector2 tb, CVector3 c, CVector2 tc, CVector3 d, CVector2 td)
{
	//LOGI("drawq 0");

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
#ifndef USE_OMNI
    glUniform1i(g_slots[SKY][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif
    
	
	//LOGI("drawq 1");
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
	//LOGI("drawq 2");

	//glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_DYNAMIC_DRAW);
    
#ifndef USE_OMNI
    //glVertexAttribPointer(g_slots[SKY][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    //glVertexAttribPointer(g_slots[SKY][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    glVertexAttribPointer(g_slots[SKY][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[SKY][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
    glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif

		//checkGlError("in DrawSkyBox");

	//LOGI("drawq 3");
    glDrawArrays(GL_TRIANGLES, 0, 6);
}*/

void DrawQuad(int first, unsigned int tex)
{
	//LOGI("drawq 0");

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
#ifndef USE_OMNI
    glUniform1i(g_slots[SKY][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif
    
#ifndef USE_OMNI
    //glVertexAttribPointer(g_slots[SKY][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    //glVertexAttribPointer(g_slots[SKY][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    glVertexAttribPointer(g_slots[SKY][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[SKY][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
    glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif

		//checkGlError("in DrawSkyBox");

	//LOGI("drawq 3");
    glDrawArrays(GL_TRIANGLES, first, 6);
}

void Quad_fillvbo(float* va, CVector3 a, CVector2 ta, CVector3 b, CVector2 tb, CVector3 c, CVector2 tc, CVector3 d, CVector2 td)
{/*
    float vertices[] =
    {
        //posx, posy posz   texx, texy
        a.x, a.y, a.z,          ta.x, ta.y,
        b.x, b.y, b.z,          tb.x, tb.y,
        c.x, c.y, c.z,          tc.x, tc.y,
        
        c.x, c.y, c.z,          tc.x, tc.y,
        d.x, d.y, d.z,          td.x, td.y,
        a.x, a.y, a.z,          ta.x, ta.y
    };*/

	va[0] = a.x;
	va[1] = a.y;
	va[2] = a.z;
	va[3] = ta.x;
	va[4] = ta.y;

	va[5] = b.x;
	va[6] = b.y;
	va[7] = b.z;
	va[8] = tb.x;
	va[9] = tb.y;

	va[10] = c.x;
	va[11] = c.y;
	va[12] = c.z;
	va[13] = tc.x;
	va[14] = tc.y;

	va[15] = c.x;
	va[16] = c.y;
	va[17] = c.z;
	va[18] = tc.x;
	va[19] = tc.y;

	va[20] = d.x;
	va[21] = d.y;
	va[22] = d.z;
	va[23] = td.x;
	va[24] = td.y;

	va[25] = a.x;
	va[26] = a.y;
	va[27] = a.z;
	va[28] = ta.x;
	va[29] = ta.y;
}

void DelSky()
{
	if(g_skyVBO)
	{
		glDeleteBuffers(1, &g_skyVBO);
		g_skyVBO = 0;
	}
}

void MakeSky()
{
	//return;

	float va[6 * 6 * 5];

    Quad_fillvbo(&(va[0*6*5]),	//g_right,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1));
    
    Quad_fillvbo(&(va[1*6*5]),	//g_left,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    Quad_fillvbo(&(va[2*6*5]),	//g_front,
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1));
    
    Quad_fillvbo(&(va[3*6*5]),	//g_back,
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    Quad_fillvbo(&(va[4*6*5]),	//g_bottom,
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_0),
             CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_1, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_1),
             CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TEX_0, SKY_TEX_0));
    
    Quad_fillvbo(&(va[5*6*5]),	//g_top,
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TOP_1, SKY_TOP_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), CVector2(SKY_TOP_0, SKY_TOP_1),
             CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TOP_0, SKY_TOP_0),
             CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), CVector2(SKY_TOP_1, SKY_TOP_0));
	
	glGenBuffers(1, &g_skyVBO);
	glBindBuffer(GL_ARRAY_BUFFER, g_skyVBO);
    glBufferData(GL_ARRAY_BUFFER, 6 * 6 * 5 * sizeof(float), va, GL_STATIC_DRAW);
}

void DrawSkyBox(CVector3 pos)
{
	//return;

	//LOGI("skybox 0");
    glDisable(GL_DEPTH_TEST);
    
    //GLKMatrix4 modelmat = GLKMatrix4MakeTranslation(pos.x, pos.y, pos.z);
    //glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.m);

	CMatrix modelmat;
	float trans[] = {pos.x, pos.y, pos.z};
	modelmat.setTranslation(trans);
#ifndef USE_OMNI
	glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.getMatrix());
#else
	glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
#endif

	glBindBuffer(GL_ARRAY_BUFFER, g_skyVBO);
	DrawQuad(6*0, g_right);
	DrawQuad(6*1, g_left);
	DrawQuad(6*2, g_front);
	DrawQuad(6*3, g_back);
	DrawQuad(6*4, g_bottom);
	DrawQuad(6*5, g_top);

	/*
	//LOGI("skybox 1");
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
    */

    glEnable(GL_DEPTH_TEST);
}
