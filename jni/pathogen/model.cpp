#include "3dmath.h"
#include "model.h"
#include "main.h"
#include "image.h"
#include "file.h"
#include "logger.h"
#include "shader.h"

CModel g_model[MODELS];

void CModel::Load(const char* n, CVector3 scale)
{
    char raw[64];
	StripPathExtension(n, raw);
	char filepath[128];
	sprintf(filepath, "models/%s.md2", raw);
    
    //FILE* fp = fopen (filepath, "rb");
    CFile fp(filepath);

    //if(!fp)
	//if(fp.fsize <= 0)
	if(!fp.mFile)
    {
        //int ferr = ferror(fp);
        LOGE("Couldn't load model %s / %s", raw, filepath);
        return;
    }
    
    strcpy(name, raw);
    
    transp = false;
    for(int i=0; i<strlen(raw); i++)
    {
        if(raw[i] == '%')
            transp = true;
    }
    
    /* Read header */
    //fread (&header, 1, sizeof (struct md2_header_t), fp);
    fp.read((void*)&header, sizeof(struct md2_header_t));

    if ((header.ident != 844121161) ||
        (header.version != 8))
    {
        LOGE("Bad version or identifier for model %s", name);
        //fclose(fp);
		fp.close();
        return;
    }
    
    /* Memory allocations */
    skins = new md2_skin_t[ header.num_skins ];
    texcoords = new md2_texCoord_t[ header.num_st ];
    triangles = new md2_triangle_t[ header.num_tris ];
    frames = new md2_frame_t[ header.num_frames ];
    glcmds = new int[ header.num_glcmds ];

	//LOGI("%s skins%d,st%d,tris%d,frames%d", name, header.num_skins, header.num_st, header.num_tris, header.num_frames);
    
    /* Read model data */
    //fseek (fp, header.offset_skins, SEEK_SET);
    //fread (skins, sizeof (struct md2_skin_t), header.num_skins, fp);
	fp.seek(header.offset_skins);
	fp.read((void*)skins, sizeof (struct md2_skin_t) * header.num_skins);
    
    //fseek (fp, header.offset_st, SEEK_SET);
    //fread (texcoords, sizeof (struct md2_texCoord_t), header.num_st, fp);
	fp.seek(header.offset_st);
	fp.read((void*)texcoords, sizeof (struct md2_texCoord_t) * header.num_st);
    
    //fseek (fp, header.offset_tris, SEEK_SET);
    //fread (triangles, sizeof (struct md2_triangle_t), header.num_tris, fp);
	fp.seek(header.offset_tris);
	fp.read((void*)triangles, sizeof (struct md2_triangle_t) * header.num_tris);
    
    //fseek (fp, header.offset_glcmds, SEEK_SET);
    //fread (glcmds, sizeof (int), header.num_glcmds, fp);
	fp.seek(header.offset_glcmds);
	fp.read((void*)glcmds, sizeof (int) * header.num_glcmds);
    
    /* Read frames */
    //fseek (fp, header.offset_frames, SEEK_SET);
	fp.seek(header.offset_frames);
    for (int i = 0; i < header.num_frames; ++i)
    {
        /* Memory allocation for vertices of this frame */
        frames[i].verts = new md2_vertex_t[ header.num_vertices ];
        
        /* Read frame data */
        //fread (frames[i].scale, sizeof (vec3_t), 1, fp);
        //fread (frames[i].translate, sizeof (vec3_t), 1, fp);
        //fread (frames[i].name, sizeof (char), 16, fp);
        //fread (frames[i].verts, sizeof (struct md2_vertex_t), header.num_vertices, fp);
		
        fp.read ((void*)frames[i].scale, sizeof (vec3_t));
        fp.read ((void*)frames[i].translate, sizeof (vec3_t));
        fp.read ((void*)frames[i].name, sizeof (char)*16);
        fp.read ((void*)frames[i].verts, sizeof (struct md2_vertex_t)*header.num_vertices);
    }
    
    //fclose (fp);
	fp.close();
    on = true;
    
    char texn[64];
    StripPathExtension(skins[0].name, texn);
    //strcpy(skins[0].name, texn);
	sprintf(skins[0].name, "models/%s", texn);
    tex_id = CreateTexture(skins[0].name);
    
    if(g_lastTexTransp)
        transp = true;
    
    vertexBuffers = new GLuint[ header.num_frames ];
    numverts = header.num_tris * 3;
    
	struct md2_frame_t *pframe;
	struct md2_vertex_t *pvert;
	int j;
	int i;
	int index;
    
    CVertexArray2* va2 = new CVertexArray2[ numverts ];
    float temp;
    
	for(int f=0; f<header.num_frames; f++)
	{
		for (i = 0; i < header.num_tris; ++i)
		{
			for (j = 0; j < 3; ++j)
			{
				index = i*3 + j;
                
				pframe = &frames[f];
				pvert = &pframe->verts[triangles[i].vertex[j]];
                
                /*
                 // Reverse vertex order
                 if(j == 1)
                 index += 1;
                 else if(j == 2)
                 index -= 1;
                 */
                
				// Compute texture coordinates
				va2[index].texcoord.x = (float)texcoords[triangles[i].st[j]].s / header.skinwidth;
				va2[index].texcoord.y = (float)texcoords[triangles[i].st[j]].t / header.skinheight;
                
				// Normal vector
				//glNormal3fv (anorms_table[pvert->normalIndex]);
				//va[index].normal.x = anorms_table[pvert->normalIndex][0];
				//va[index].normal.y = anorms_table[pvert->normalIndex][2];
				//va[index].normal.z = -anorms_table[pvert->normalIndex][1];
                
				// Calculate vertex position
				va2[index].vertex.x = (pframe->scale[0] * pvert->v[0]) + pframe->translate[0];
				va2[index].vertex.y = (pframe->scale[2] * pvert->v[2]) + pframe->translate[2];
				va2[index].vertex.z = -((pframe->scale[1] * pvert->v[1]) + pframe->translate[1]);
                
				va2[index].vertex.x *= scale.x;
				va2[index].vertex.y *= scale.y;
				va2[index].vertex.z *= scale.z;
                
				//va2[index].vertex.x += translate.x;
				//va2[index].vertex.y += translate.y;
				//va2[index].vertex.z += translate.z;
                
                //Flip 90 degrees
				temp = va2[index].vertex.x;
				va2[index].vertex.x = -va2[index].vertex.z;
				va2[index].vertex.z = temp;
			}
		}
        
        for(int i=0; i<numverts; i+=3)
		{
			CVector3 normal;
			CVector3 tri[3];
			tri[0] = va2[i].vertex;
			tri[1] = va2[i+1].vertex;
			tri[2] = va2[i+2].vertex;
			normal = Normal(tri);
			va2[i].normal = normal;
			va2[i+1].normal = normal;
			va2[i+2].normal = normal;
		}
        
        glGenBuffers(1, &vertexBuffers[f]);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[f]);
        glBufferData(GL_ARRAY_BUFFER, sizeof(CVertexArray2)*numverts, va2, GL_STATIC_DRAW);
	}
    
    delete [] va2;
    
    CVertexArray* va = new CVertexArray[ header.num_frames ];
    
	for(int f=0; f<header.num_frames; f++)
	{
		va[f].numverts = numverts;
		va[f].vertices = new CVector3[ numverts ];
		va[f].texcoords = new CVector2[ numverts ];
		va[f].normals = new CVector3[ numverts ];
        
		for (i = 0; i < header.num_tris; ++i)
		{
			for (j = 0; j < 3; ++j)
			{
				index = i*3 + j;
                
				pframe = &frames[f];
				pvert = &pframe->verts[triangles[i].vertex[j]];
                
				// Reverse vertex order
				if(j == 1)
					index += 1;
				else if(j == 2)
					index -= 1;
                
				// Compute texture coordinates
				va[f].texcoords[index].x = (float)texcoords[triangles[i].st[j]].s / header.skinwidth;
				va[f].texcoords[index].y = (float)texcoords[triangles[i].st[j]].t / header.skinheight;
                
				// Normal vector
				//glNormal3fv (anorms_table[pvert->normalIndex]);
				//va[index].normal.x = anorms_table[pvert->normalIndex][0];
				//va[index].normal.y = anorms_table[pvert->normalIndex][2];
				//va[index].normal.z = -anorms_table[pvert->normalIndex][1];
                
				// Calculate vertex position
				va[f].vertices[index].x = (pframe->scale[0] * pvert->v[0]) + pframe->translate[0];
				va[f].vertices[index].y = (pframe->scale[2] * pvert->v[2]) + pframe->translate[2];
				va[f].vertices[index].z = -((pframe->scale[1] * pvert->v[1]) + pframe->translate[1]);
                
				va[f].vertices[index].x *= scale.x;
				va[f].vertices[index].y *= scale.y;
				va[f].vertices[index].z *= scale.z;
                
				//va[f].vertices[index].x += translate.x;
				//va[f].vertices[index].y += translate.y;
				//va[f].vertices[index].z += translate.z;
                
                //Flip 90 degrees
				temp = va[f].vertices[index].x;
				va[f].vertices[index].x = -va[f].vertices[index].z;
				va[f].vertices[index].z = temp;
			}
		}
        
        for(int i=0; i<numverts; i+=3)
		{
			CVector3 normal;
			CVector3 tri[3];
			tri[0] = va[f].vertices[i];
			tri[1] = va[f].vertices[i+1];
			tri[2] = va[f].vertices[i+2];
			normal = Normal2(tri);
			va[f].normals[i] = normal;
			va[f].normals[i+1] = normal;
			va[f].normals[i+2] = normal;
		}
	}
    
    vertexArrays = va;
    
    LOGI("%s.md2", n);
}

void CModel::DrawSky(int frame, CVector3 pos)
{
    CMatrix modelmat;
    modelmat.setTranslation((const float*)&pos);
    
    glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
    
    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[frame]);
    glVertexAttribPointer(g_slots[MODEL][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(CVertexArray2), (void*)offsetof(CVertexArray2,vertex));
    glVertexAttribPointer(g_slots[MODEL][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(CVertexArray), (void*)offsetof(CVertexArray2,texcoord));
    
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex_id);
    glUniform1i(g_slots[MODEL][TEXTURE], 0);
    
    glDrawArrays(GL_TRIANGLES, 0, numverts);
}

void CModel::Draw(int frame, CVector3 pos, float pitch, float yaw)
{
    if(!on)
    {
        //NSLog(@"draw null %@", name);
        return;
    }
    
    CMatrix modelmat;
    float radians[] = {pitch, yaw, 0};
    modelmat.setTranslation((const float*)&pos);
    CMatrix rotation;
    rotation.setRotationRadians(radians);
    modelmat.postMultiply(rotation);
    
    //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
    
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_2D, tex_id);
    //glUniform1i(g_slots[MODEL][TEXTURE], 0);

    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[frame]);
    //glVertexAttribPointer(g_slots[MODEL][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(CVertexArray2), (void*)offsetof(CVertexArray2,vertex));
    glVertexAttribPointer(g_slots[MODEL][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(CVertexArray2), (void*)offsetof(CVertexArray2,texcoord));
    
    //glDrawArrays(GL_TRIANGLES, 0, numverts);
}

//used for drawing the upper part of characters
void CModel::Draw2(int frame, CVector3 pos, float pitch, float yaw)
{
    CVector3 offset = pos + CVector3(0, MID_HEIGHT_OFFSET, 0);
    CMatrix modelmat;
    float radians[] = {pitch, yaw, 0};
    modelmat.setTranslation((const float*)&offset);
    CMatrix rotation;
    rotation.setRotationRadians(radians);
    modelmat.postMultiply(rotation);
    CMatrix translation;
    offset = CVector3(0, -MID_HEIGHT_OFFSET, 0);
    translation.setTranslation((const float*)&offset);
    modelmat.postMultiply(translation);
    
    glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
    
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex_id);
    glUniform1i(g_slots[MODEL][TEXTURE], 0);

    glBindBuffer(GL_ARRAY_BUFFER, vertexBuffers[frame]);
    glVertexAttribPointer(g_slots[MODEL][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(CVertexArray2), (void*)offsetof(CVertexArray2,vertex));
    glVertexAttribPointer(g_slots[MODEL][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(CVertexArray2), (void*)offsetof(CVertexArray2,texcoord));
    
    glDrawArrays(GL_TRIANGLES, 0, numverts);
}

void CModel::Free()
{
    glDeleteBuffers(header.num_frames, vertexBuffers);
	glDeleteTextures(1, &tex_id);
    
	for(int i=0; i<header.num_frames; i++)
		vertexArrays[i].free();
    
    if(skins)
    {
        delete [] skins;
        skins = NULL;
    }
    
    if(texcoords)
    {
        delete [] texcoords;
        texcoords = NULL;
    }

    if(triangles)
    {
        delete [] triangles;
        triangles = NULL;
    }
    
    if(glcmds)
    {
        delete [] glcmds;
        glcmds = NULL;
    }
    
    if(frames)
    {
        for (int i=0; i<header.num_frames; i++)
        {
            delete [] frames[i].verts;
            frames[i].verts = NULL;
        }
        
        delete [] frames;
        frames = NULL;
    }
}

int NewModel()
{
    for(int i=0; i<MODELS; i++)
        if(!g_model[i].on)
            return i;
    
    return -1;
}

int FindModel(const char* raw)
{
	for(int i=0; i<MODELS; i++)
	{
		if(!g_model[i].on)
			continue;
        
		if(stricmp(g_model[i].name, raw) == 0)
			return i;
	}
    
	return -1;
}

int LoadModel(const char* name, CVector3 scale)
{
	char raw[64];
	StripPathExtension(name, raw);
	int i = FindModel(raw);
	if(i >= 0)
		return i;
    
    i = NewModel();
    if(i < 0)
        return -1;
    
    g_model[i].Load(raw, scale);
    
    return i;
}

void ModelMinMax(int model, CVector3* vMin, CVector3* vMax)
{
	(*vMin) = CVector3(0, 0, 0);
	(*vMax) = CVector3(0, 0, 0);
    
	CModel* m = &g_model[model];
	CVertexArray* va;
    
	for(int f=0; f<m->header.num_frames; f++)
        //for(int f=0; f<1; f++)
	{
		va = &m->vertexArrays[f];
		for(int v=0; v<va->numverts; v++)
		{
			if(va->vertices[v].x < vMin->x)
				vMin->x = va->vertices[v].x;
			if(va->vertices[v].y < vMin->y)
				vMin->y = va->vertices[v].y;
			if(va->vertices[v].z < vMin->z)
				vMin->z = va->vertices[v].z;
			if(va->vertices[v].x > vMax->x)
				vMax->x = va->vertices[v].x;
			if(va->vertices[v].y > vMax->y)
				vMax->y = va->vertices[v].y;
			if(va->vertices[v].z > vMax->z)
				vMax->z = va->vertices[v].z;
		}
	}
    
    float maxextent = max(max(fabs(vMin->x), fabs(vMin->z)), max(fabs(vMax->x), fabs(vMax->z)));
    
	/*
     vMin->x = min(vMin->x, vMin->z);
     vMin->z = vMin->x;
     vMax->x = max(vMax->x, vMax->z);
     vMax->z = vMax->x;*/
	vMin->x = vMin->z = -maxextent;
	vMax->x = vMax->z = maxextent;

	//LOGI("%s maxex=%f", m->name, maxextent);
}

CVector3 ModelFront(int model, int from, int to)
{
	CVector3 vFront(0, 0, -99999);
    
	CModel* m = &g_model[model];
	CVertexArray* va;
    
	for(int f=from; f<to; f++)
	{
		va = &m->vertexArrays[f];
		for(int v=0; v<va->numverts; v++)
		{
			if(va->vertices[v].z < vFront.z)
				continue;
            
			vFront = va->vertices[v];
		}
	}
    
	return vFront;
}

bool PlayAnimation(float& frame, int first, int last, bool loop, float rate)
{
    if(frame < first || frame > last)
    {
        frame = first;
        return false;
    }
    
    frame += rate;
    
    if(frame > last)
    {
        if(loop)
            frame = first;
		else
			frame = last;
        
        return true;
    }
    
    return false;
}

//Play animation backwards
bool PlayAnimationB(float& frame, int first, int last, bool loop, float rate)
{
    if(frame < first || frame > last)
    {
        frame = last;
        return false;
    }
    
    frame -= rate;
    
    if(frame < first)
    {
        if(loop)
            frame = last;
		else
			frame = first;
        
        return true;
    }
    
    return false;
}