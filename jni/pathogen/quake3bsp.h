#ifndef QUAKE3BSP_H
#define QUAKE3BSP_H

#include <vector>
#include "3dmath.h"

using namespace std;

#define BSP_TEXTURES	128

#define FACE_POLYGON	1

#define TYPE_RAY		0
#define TYPE_SPHERE		1
#define TYPE_BOX		2

#define EPSILON			0.03125f

struct tVector3i
{
	int x, y, z;
};

struct tVector3ui
{
	unsigned int x, y, z;
};

struct tVector3uc
{
	unsigned char x, y, z;
    
	/*
     tVector3uc& operator=(CVector3 vVector)
     {
     x = vVector.x;
     y = vVector.y;
     z = vVector.z;
     return *this;
     }
     */
    
	CVector3 operator+(tVector3uc vVector)
	{
		return CVector3(vVector.x + x, vVector.y + y, vVector.z + z);
	}
};

struct tVector2uc
{
	unsigned char x, y;
};

struct tBSPHeader
{
    char strID[4];	// This should always be 'IBSP'
    int version;	// This should be 0x2e for Quake 3 files
};

struct tBSPLump
{
	int offset;
	int length;
};

struct tBSPVertex
{
    CVector3 vPosition;
    CVector2 vTextureCoord;
    CVector2 vLightmapCoord;
    CVector3 vNormal;
    byte color[4];
};

struct tBSPFace
{
    int textureID;				// The index into the texture array
    int effect;					// The index for the effects (or -1 = n/a)
    int type;					// 1=polygon, 2=patch, 3=mesh, 4=billboard
    int startVertIndex;			// The starting index into this face's first vertex
    int numOfVerts;				// The number of vertices for this face
    int startIndex;				// The starting index into the indices array for this face
    int numOfIndices;			// The number of indices for this face
    int lightmapID;				// The texture index for the lightmap
    int lMapCorner[2];			// The face's lightmap corner in the image
    int lMapSize[2];			// The size of the lightmap section
    CVector3 lMapPos;			// The 3D origin of lightmap.
    CVector3 lMapVecs[2];		// The 3D space for s and t unit vectors.
    CVector3 vNormal;			// The face normal.
    int size[2];				// The bezier patch dimensions.
};

struct tBSPTexture
{
    char strName[64];			// The name of the texture w/o the extension
    int flags;					// The surface flags (unknown)
    int textureType;			// The type of texture (solid, water, slime, etc..) (type & 1) = 1 (solid)
};

struct tBSPLightmap
{
    byte imageBits[128][128][3];   // The RGB data in a 128x128 image
};

struct tBSPNode
{
    int plane;					// The index into the planes array
    int front;					// The child index for the front node
    int back;					// The child index for the back node
    tVector3i min;				// The bounding box min position.
    tVector3i max;				// The bounding box max position.
};

// This stores a leaf (end node) in the BSP tree
struct tBSPLeaf
{
    int cluster;				// The visibility cluster
    int area;					// The area portal
    tVector3i min;				// The bounding box min position
    tVector3i max;				// The bounding box max position
    int leafface;				// The first index into the face array
    int numOfLeafFaces;			// The number of faces for this leaf
    int leafBrush;				// The first index for into the brushes
    int numOfLeafBrushes;		// The number of brushes for this leaf
};

// This stores a splitter plane in the BSP tree
struct tBSPPlane
{
    CVector3 vNormal;			// Plane normal.
    float d;					// The plane distance from origin
};

// This stores the cluster data for the PVS's
struct tBSPVisData
{
	int numOfClusters;			// The number of clusters
	int bytesPerCluster;		// The amount of bytes (8 bits) in the cluster's bitset
	byte *pBitsets;				// The array of bytes that holds the cluster bitsets
};

// This stores the brush data
struct tBSPBrush
{
	int brushSide;				// The starting brush side for the brush
	int numOfBrushSides;		// Number of brush sides for the brush
	int textureID;				// The texture index for the brush
};

// This stores the brush side data, which stores indices for the normal and texture ID
struct tBSPBrushSide
{
	int plane;					// The plane index
	int textureID;				// The texture index
};

struct tBSPLightVol
{
	tVector3uc ambient;
	tVector3uc directional;
	tVector2uc dir; //0=phi, 1=theta
};

struct tBSPModel
{
	CVector3 mins;
	CVector3 maxs;
	int firstFace;
	int numOfFaces;
	int firstBrush;
	int numOfBrushes;
};

// This is our lumps enumeration
enum eLumps
{
    kEntities = 0,				// Stores player/object positions, etc...
    kTextures,					// Stores texture information
    kPlanes,				    // Stores the splitting planes
    kNodes,						// Stores the BSP nodes
    kLeafs,						// Stores the leafs of the nodes
    kLeafFaces,					// Stores the leaf's indices into the faces
    kLeafBrushes,				// Stores the leaf's indices into the brushes
    kModels,					// Stores the info of world models
    kBrushes,					// Stores the brushes info (for collision)
    kBrushSides,				// Stores the brush surfaces info
    kVertices,					// Stores the level vertices
    kIndices,					// Stores the level indices
    kShaders,					// Stores the shader files (blending, anims..)
    kFaces,						// Stores the faces for the level
    kLightmaps,					// Stores the lightmaps for the level
    kLightVolumes,				// Stores extra world lighting information
    kVisData,					// Stores PVS and cluster info (visibility)
    kMaxLumps					// A constant to store the number of lumps
};

class CBitset
{
    
public:
    CBitset() : m_bits(0), m_size(0) {}
    
	~CBitset()
	{
		if(m_bits)
		{
			delete m_bits;
			m_bits = NULL;
		}
	}
    
	void Resize(int count)
	{
		m_size = count/32 + 1;
        
        if(m_bits)
		{
			delete m_bits;
			m_bits = 0;
		}
        
		m_bits = new unsigned int[m_size];
		ClearAll();
	}
    
	void Set(int i)
	{
		m_bits[i >> 5] |= (1 << (i & 31));
	}
    
	int On(int i)
	{
		return m_bits[i >> 5] & (1 << (i & 31 ));
	}
    
	void Clear(int i)
	{
		m_bits[i >> 5] &= ~(1 << (i & 31));
	}
    
	void ClearAll()
	{
		memset(m_bits, 0, sizeof(unsigned int) * m_size);
	}
    
private:
    
	unsigned int *m_bits;
	int m_size;
};

struct tBSPBBox
{
	CVector3 min;
	CVector3 max;
};

class CSortFace
{
public:
	CVector3 pos;
	int faceIndex;
	float dist;
};

class CQuake3BSP
{
private:
    GLuint* m_pVertexBuffers;
    GLuint* m_pIndexBuffers;
    
	CVector3 m_gridSize;
	tBSPBBox m_bbox;
	tVector3ui num_lightvols;
	vector<CSortFace> m_sortFaces;
    
    int m_numOfVerts;
	int m_numOfFaces;
	int m_numOfIndices;
	int m_numOfTextures;
	int m_numOfLightmaps;
	int m_numOfNodes;
	int m_numOfLeafs;
	int m_numOfLeafFaces;
	int m_numOfPlanes;
	int m_numOfBrushes;
	int m_numOfBrushSides;
	int m_numOfLeafBrushes;
	int m_numOfLightVols;
	int m_numOfModels;
    
	int m_traceType;
	float m_traceRatio;
	float m_traceRadius;
    
	bool m_bCollided;
	bool m_bGrounded;
	bool m_bTryStep;
	bool m_bStuck;
	bool m_bBroke;
    bool m_bLadder;
    
	CVector3 m_vTraceMins;
	CVector3 m_vTraceMaxs;
	CVector3 m_vExtents;
	CVector3 m_vCollisionNormal;
	CVector3 m_vStart;
	CVector3 m_vEnd;
    
	int* m_pIndices;
	tBSPVertex* m_pVerts;
	tBSPFace* m_pFaces;
	tBSPNode* m_pNodes;
	tBSPLeaf* m_pLeafs;
	tBSPPlane* m_pPlanes;
	int* m_pLeafFaces;
	tBSPVisData m_clusters;
	tBSPTexture* m_pTextures;
	tBSPLightmap *m_pLightmaps;
	tBSPBrush* m_pBrushes;
	tBSPBrushSide* m_pBrushSides;
	int* m_pLeafBrushes;
	tBSPLightVol* m_pLightVols;
	tBSPModel* m_pModels;
    
	bool* m_brokenFace;
	bool* m_brokenBrush;
	UINT* m_textures;
	UINT* m_lightmaps;
	bool* m_passable;
	bool* m_sky;
	bool* m_transparent;
	bool* m_water;
	bool* m_breakable;
	bool* m_ladder;
	bool* m_grate;
    
	CBitset m_FacesDrawn;

public:
    CQuake3BSP();
    ~CQuake3BSP();

    bool LoadBSP(const char* name);
    void RenderLevel(const CVector3 &vPos);
    void SortFaces(const CVector3 &vPos);
    void RenderLevel2(const CVector3 &vPos);
    void RenderSky();

    CVector3 TraceRay(CVector3 vStart, CVector3 vEnd);
    CVector3 TraceSphere(CVector3 vStart, CVector3 vEnd, float radius, float maxStep);
    CVector3 TraceBox(CVector3 vStart, CVector3 vEnd, CVector3 vMin, CVector3 vMax, float maxStep);
	bool BreakFaces(CVector3 vStart, CVector3 vEnd);

    bool IsOnGround()   {   return m_bGrounded; }
    bool Collided()     {   return m_bCollided; }
    bool Stuck()        {   return m_bStuck;    }
	bool Ladder()		{   return m_bLadder;	}
	CVector3 CollisionNormal()		{ return m_vCollisionNormal;		}

    void Destroy(bool delTex=true);
    
    int IsClusterVisible(int current, int test);
	int FindLeaf(const CVector3 &vPos);
	int FindCluster(const CVector3 &vPos);
	CVector3  LightVol(CVector3 vPos);
	
    void RenderFace(int faceIndex);
    void RenderSkyFace(int faceIndex);

private:
    void ChangeGamma(byte *pImage, int size, float factor);
    UINT CreateLightmapTexture(byte* pImageBits, int width, int height);

    CVector3 TryToStep(CVector3 vStart, CVector3 vEnd, float maxStep);

    CVector3 Trace(CVector3 vStart, CVector3 vEnd);
	void Break(CVector3 vStart, CVector3 vEnd);
	void BreakFace(int faceIndex, CVector3 vStart, CVector3 vEnd);

    void CheckNode(int nodeIndex, CVector3 vStart, CVector3 vEnd);
    void CheckBrush(tBSPBrush *pBrush, CVector3 vStart, CVector3 vEnd);
	void BreakNode(int nodeIndex, CVector3 vStart, CVector3 vEnd);
	void BreakBrush(int brushIndex, tBSPBrush *pBrush, CVector3 vStart, CVector3 vEnd);
};

extern bool g_dothis;

extern class CQuake3BSP g_map;

#endif