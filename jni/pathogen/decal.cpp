

#include "main.h"
#include "decal.h"
#include "3dmath.h"
#include "image.h"
#include "gui.h"
#include "quake3bsp.h"
#include "shader.h"

CDecalType g_decalT[DECAL_TYPES];
CDecal g_decal[DECALS];
unsigned int g_decalVBO = 0;

void Decal(int type, const char* tex, float decay, float size)
{
	CDecalType* t = &g_decalT[type];
    
	t->tex = CreateTexture(tex);
	t->decay = decay;
	t->size = size;
}

void Decals()
{
	Decal(BLOODSPLAT, "effects/bloodsplat", 0.01f, 10.0f);
	Decal(BULLETHOLE, "effects/bullethole", 0.005f, 3.0f);
}

void UpdateDecals()
{
	CDecal* d;
	CDecalType* t;
    
	for(int i=0; i<DECALS; i++)
	{
		d = &g_decal[i];
        
		if(!d->on)
			continue;
        
		t = &g_decalT[d->type];
        
		d->life -= t->decay;
        
		if(d->life < 0.0f)
			d->on = false;
	}
}

void MakeDecalVBO()
{
	float vertices[DECALS*6*5];
	glGenBuffers(1, &g_decalVBO);
    glBindBuffer(GL_ARRAY_BUFFER, g_decalVBO);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float)*DECALS*6*5, vertices, GL_DYNAMIC_DRAW);
	//return;
	/*
	CDecal* d;
	for(int i=0; i<DECALS; i++)
    {
        d = &g_decal[i];
		DummyVBO(&d->vbo);
	}*/
}

void DelDecalVBO()
{
	glDeleteBuffers(1, &g_decalVBO);
	/*
	CDecal* d;
	for(int i=0; i<DECALS; i++)
    {
        d = &g_decal[i];

		if(d->vbo)
		{
			glDeleteBuffers(1, &d->vbo);
			d->vbo = 0;
		}
	}*/
}

void DrawDecals()
{
	//return;
	CDecal* d;
	CDecalType *t;
    
	float precolor[] = {1,1,1,1};
    
    if(g_reddening > 0.0f)
    {
		precolor[1] = 1.0f - g_reddening;
		precolor[2] = 1.0f - g_reddening;
	}
    
    float colorf[] = {1, 1, 1, 1};
	CVector3 colorv;
    
	int vbovert;
	glBindBuffer(GL_ARRAY_BUFFER, g_decalVBO);

	for(int i=0; i<DECALS; i++)
    {
        d = &g_decal[i];
        if(!d->on)
            continue;
        
        colorv = g_map.LightVol(d->lpos);
		colorf[0] = colorv.x;
		colorf[1] = precolor[1] * colorv.y;
		colorf[2] = precolor[2] * colorv.z;
		colorf[3] = d->life;
#ifndef USE_OMNI
        glUniform4f(g_slots[BILLBOARD][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
#else
		glUniform4f(g_slots[OMNI][COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
#endif

        t = &g_decalT[d->type];
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, t->tex);
#ifndef USE_OMNI
        glUniform1i(g_slots[BILLBOARD][TEXTURE], 0);
#else
        glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif
        
        float vertices[] =
        {
            //posx, posy posz   texx, texy
            d->a.x, d->a.y, d->a.z,          1, 0,
            d->b.x, d->b.y, d->b.z,          1, 1,
            d->c.x, d->c.y, d->c.z,          0, 1,
            
            d->c.x, d->c.y, d->c.z,          0, 1,
            d->d.x, d->d.y, d->d.z,          0, 0,
            d->a.x, d->a.y, d->a.z,          1, 0
        };
		
		vbovert = i * 6;
		//glBindBuffer(GL_ARRAY_BUFFER, d->vbo);
        //glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_DYNAMIC_DRAW);

		glBufferSubData(GL_ARRAY_BUFFER, vbovert * 5 * sizeof(float), 6 * 5 * sizeof(float), vertices);

#ifndef USE_OMNI
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
        glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
        glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
        glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
#endif

        //glDrawArrays(GL_TRIANGLES, 0, 6);
		glDrawArrays(GL_TRIANGLES, vbovert, 6);
    }
    
#ifndef USE_OMNI
    glUniform4f(g_slots[BILLBOARD][COLOR], precolor[0], precolor[1], precolor[2], precolor[3]);
#else
    glUniform4f(g_slots[OMNI][COLOR], precolor[0], precolor[1], precolor[2], precolor[3]);
#endif
}

int NewDecal()
{
	for(int i=0; i<DECALS; i++)
		if(!g_decal[i].on)
			return i;
    
	return -1;
}

void PlaceDecal(int type, CVector3 pos, CVector3 norm)
{
	int i = NewDecal();
	if(i < 0)
		return;
    
	CDecal* d = &g_decal[i];
	d->on = true;
	d->life = 1;
    d->type = type;
    
	CDecalType* t = &g_decalT[type];
    
	CVector3 vertical = Normalize(Cross(pos, norm));
	CVector3 horizontal = Normalize(Cross(vertical, norm));
    
	CVector3 vert = vertical*t->size;
	CVector3 horiz = horizontal*t->size;
    
    d->lpos = pos + norm * 0.1f;
	d->a = pos - horiz + vert;
	d->b = pos + horiz + vert;
	d->c = pos + horiz - vert;
	d->d = pos - horiz - vert;
}
