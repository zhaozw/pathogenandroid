
#ifndef MODEL_H
#define MODEL_H

#include "3dmath.h"
#include "entity.h"

extern int g_skydome;

class CVertexArray
{
public:
	int numverts;
	CVector3* vertices;
	CVector2* texcoords;
	CVector3* normals;
    
	CVertexArray()
	{
		numverts = 0;
	}
    
	void free()
	{
		if(numverts <= 0)
			return;
        
		delete [] vertices;
		delete [] texcoords;
		delete [] normals;
		numverts = 0;
	}
    
	~CVertexArray()
	{
		free();
	}
};

class CVertexArray2
{
public:
	CVector3 vertex;
	CVector2 texcoord;
	CVector3 normal;
};

typedef float vec3_t[3];

struct md2_header_t
{
    int ident;
    int version;
    
    int skinwidth;
    int skinheight;
    
    int framesize;
    
    int num_skins;
    int num_vertices;
    int num_st;
    int num_tris;
    int num_glcmds;
    int num_frames;
    
    int offset_skins;
    int offset_st;
    int offset_tris;
    int offset_frames;
    int offset_glcmds;
    int offset_end;
};

/* Texture name */
struct md2_skin_t
{
    char name[64];
};

/* Texture coords */
struct md2_texCoord_t
{
    short s;
    short t;
};

/* Triangle info */
struct md2_triangle_t
{
    unsigned short vertex[3];
    unsigned short st[3];
};

/* Compressed vertex */
struct md2_vertex_t
{
    unsigned char v[3];
    unsigned char normalIndex;
};

/* Model frame */
struct md2_frame_t
{
    vec3_t scale;
    vec3_t translate;
    char name[16];
    struct md2_vertex_t *verts;
};

/* GL command packet */
struct md2_glcmd_t
{
    float s;
    float t;
    int index;
};

/* MD2 model structure */
class CModel
{
public:
    bool on;
    struct md2_header_t header;
    
    struct md2_skin_t *skins;
    struct md2_texCoord_t *texcoords;
    struct md2_triangle_t *triangles;
    struct md2_frame_t *frames;
    int *glcmds;
    
    unsigned int tex_id;
    
    GLuint* vertexBuffers;
    CVertexArray* vertexArrays;
    int numverts;
    char name[64];
    
    bool transp;
    
    CModel()
    {
        on = false;
        transp = false;
    }
    ~CModel()
    {
        Free();
    }
    
    void Load(const char* n, CVector3 scale);
    void Free();
    void DrawSky(int frame, CVector3 pos);
    void Draw(int frame, CVector3 pos, float pitch, float yaw);
    void Draw2(int frame, CVector3 pos, float pitch, float yaw);    //used for drawing the upper part of characters
};

#define MODELS  128
extern CModel g_model[MODELS];

int LoadModel(const char* name, CVector3 scale);
void ModelMinMax(int model, CVector3* vMin, CVector3* vMax);
CVector3 ModelFront(int model, int from, int to);
bool PlayAnimation(float& frame, int first, int last, bool loop, float rate);
bool PlayAnimationB(float& frame, int first, int last, bool loop, float rate);    //Play animation backwards

#endif
