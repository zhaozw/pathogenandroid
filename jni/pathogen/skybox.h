
#define SKYBOX_SIZE	16.0f

#define SKY_TEX_0	0.002f
#define SKY_TEX_1	0.998f

#define SKY_TOP_0	0.002f
#define SKY_TOP_1	0.998f

extern unsigned int g_front;
extern unsigned int g_left;
extern unsigned int g_right;
extern unsigned int g_bottom;
extern unsigned int g_top;
extern unsigned int g_back;

struct CVector3;

void MakeSky();
void DelSky();
void DrawSkyBox(CVector3 pos);
void LoadSkyBox(const char* basename);