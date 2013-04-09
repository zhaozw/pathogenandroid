

#include <vector>
#include "3DMath.h"

using namespace std;

class CBillboardType
{
public:
    char name[32];
    unsigned int tex;
};

extern vector<CBillboardType> g_billbT;

class CBillboard
{
public:
    bool on;
    int type;
    float size;
    CVector3 pos;
    float dist;
    int particle;
	bool nolightvol;
	//unsigned int vbo;
    
    CBillboard()
    {
        on = false;
        particle = -1;
		//vbo = 0;
    }
};

#define BILLBOARDS  512
extern CBillboard g_billb[BILLBOARDS];

extern unsigned int g_muzzle[4];

void Effects();
int NewBillboard(char* tex);
int Billboard(const char* name);
void SortBillboards();
void DrawBillboards();
void PlaceBillboard(const char* n, CVector3 pos, float size, int particle, bool nolightvol);
void PlaceBillboard(int type, CVector3 pos, float size, int particle, bool nolightvol);
void MakeBillbVBO();
void DelBillbVBO();