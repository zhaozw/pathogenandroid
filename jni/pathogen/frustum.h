

#include "3DMath.h"

class CFrustum
{
public:
	void CalculateFrustum(float* proj, float* modl);
	bool PointInFrustum(float x, float y, float z);
	bool SphereInFrustum(float x, float y, float z, float radius);
	bool CubeInFrustum(float x, float y, float z, float size);
	bool BoxInFrustum(float x, float y, float z, float sizeX, float sizeY, float sizeZ);
    
private:
	float m_Frustum[6][4];	// This holds the A B C and D values for each side of our frustum.
};

extern CFrustum g_frustum;