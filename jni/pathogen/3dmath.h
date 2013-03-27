

#ifndef MATH3D_H
#define MATH3D_H

#include "main.h"

#define DEGTORAD(a)		(M_PI * a / 180)
#define RADTODEG(a)		(180 * a / M_PI)

#ifndef PI_ON_180
#	define PI_ON_180	(M_PI/180.0)
#endif

#define CAMERA_SPEED	200.0f

struct CVector2i
{
	int x, y;

	CVector2i() {}
	
	CVector2i(int X, int Y)
	{
		x = X; y = Y;
	}
};

struct CVector2
{
	float x, y;
    
	CVector2() {}
    
	CVector2(float X, float Y)
	{
		x = X; y = Y;
	}
};

class CMatrix;

struct CVector3
{
public:
	CVector3() {}
    
	CVector3(float X, float Y, float Z)
	{
		x = X; y = Y; z = Z;
	}
    
	CVector3(const float* values)
	{
		Set(values);
	}
    
	bool operator==(CVector3 vVector)
	{
		if(x == vVector.x && y == vVector.y && z == vVector.z)
			return true;
        
		return false;
	}
    
	bool operator!=(CVector3 vVector)
	{
		if(x == vVector.x && y == vVector.y && z == vVector.z)
			return false;
        
		return true;
	}
    
	CVector3 operator+(CVector3 vVector)
	{
		return CVector3(vVector.x + x, vVector.y + y, vVector.z + z);
	}
    
	CVector3 operator-(CVector3 vVector)
	{
		return CVector3(x - vVector.x, y - vVector.y, z - vVector.z);
	}
    
	CVector3 operator*(float num)
	{
		return CVector3(x * num, y * num, z * num);
	}
    
	CVector3 operator*(CVector3 v)
	{
		return CVector3(x * v.x, y * v.y, z * v.z);
	}
    
	CVector3 operator/(float num)
	{
		return CVector3(x / num, y / num, z / num);
	}
    
	inline void Set( const float *values )
	{
		x = values[0];
		y = values[1];
		z = values[2];
	}
    
	void Transform( CMatrix& m );
	void Transform3( CMatrix& m );
    
	float x, y, z;
};

struct CTriangle
{
    CVector3 a;
    CVector3 b;
    CVector3 c;
};

class CCamera;

CVector3 VMin(float minf, CVector3 v);
float Magnitude(CVector3 vNormal);
float Magnitude2(CVector3 vNormal);
float Magnitude2(float dx, float dy);
CVector3 Normalize(CVector3 vNormal);
CVector3 Cross(CVector3 vVector1, CVector3 vVector2);
float Dot(CVector3 vVector1, CVector3 vVector2);
CVector3 Vector(CVector3 vPoint1, CVector3 vPoint2);
float PlaneDistance(CVector3 Normal, CVector3 Point);
CVector3 Normal(CVector3 vTriangle[]);
CVector3 Normal2(CVector3 vTriangle[]);
bool IntersectedPlane(CVector3 vPoly[], CVector3 vLine[], CVector3 &vNormal, float &originDistance);
bool WithinAngle(CCamera* c, CVector3 p, float angle);
float DYaw(CCamera* c, CVector3 p);
double AngleBetweenVectors(CVector3 Vector1, CVector3 Vector2);
CVector3 IntersectionPoint(CVector3 vNormal, CVector3 vLine[], double distance);
bool InsidePolygon(CVector3 vIntersection, CVector3 Poly[], long verticeCount);
bool IntersectedPolygon(CVector3 vPoly[], CVector3 vLine[], int verticeCount, CVector3* vIntersection=NULL);
CVector3 GetRay(int x, int y);
CVector3 Rotate(CVector3 v, float rad, float x, float y, float z);
CVector3 RotateAround(CVector3 v, CVector3 around, float rad, float x, float y, float z);
bool PointBehindPlane(CVector3 p, CVector3 normal, float dist);
bool TriBoxOverlap(CVector3 vPos, CVector3 vMin, CVector3 vMax, CTriangle tri);
bool TriBoxOverlap(CVector3 vCenter, CVector3 vRadius, CTriangle tri);
bool TriBoxOverlap2(CVector3 vScaleDown, CVector3 vCenter, CTriangle tri);
float Clip(float n, float lower, float upper);
float GetYaw(float dx, float dz);
CMatrix gluLookAt2(float eyex, float eyey, float eyez,
               float centerx, float centery, float centerz,
               float upx, float upy, float upz);
CMatrix BuildPerspProjMat(float fov, float aspect, float znear, float zfar);

class CQuaternion
{
public:
    CQuaternion( const float *angles ) { fromAngles( angles ); }
    CQuaternion( const CQuaternion& q1, CQuaternion& q2, float interp ) { slerp( q1, q2, interp ); }
    
    void fromAngles( const float *angles ); // (x,y,z) Euler rotation angles.
    
    /*
     Set values from an interpolation between two other quaternions.
     This will also modify the second quaternion if it is backwards.
     q1, q2	The quaternions to interpolate between
     interp	A value from 0.0-1.0 indicating the linear interpolation parameter.
     */
    void slerp( const CQuaternion& q1, CQuaternion& q2, float interp );
    
    float operator[]( int index ) const { return m_quat[index]; }
    void inverse();
    
private:
    float m_quat[4];
};

class CMatrix
{
public:
    CMatrix();
    ~CMatrix();
    void loadIdentity();
    void set( const float *matrix );
    
    //	Post-multiply by another matrix.
    void postMultiply( const CMatrix& matrix );
    
    //	Set the translation of the current matrix. Will erase any previous values.
    void setTranslation( const float *translation );
    
    //	Set the inverse translation of the current matrix. Will erase any previous values.
    void setInverseTranslation( const float *translation );
    
    //	Make a rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setRotationRadians( const float *angles );
    
    //	Make a rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setRotationDegrees( const float *angles );
    
    //	Make a rotation matrix from a quaternion. The 4th row and column are unmodified.
    void setRotationQuaternion( const CQuaternion& quat );
    
    //	Make an inverted rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setInverseRotationRadians( const float *angles );
    
    //	Make an inverted rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setInverseRotationDegrees( const float *angles );
    
    float* getMatrix() { return m_matrix; }
    
    //	Translate a vector by the inverse of the translation part of this matrix.
    void inverseTranslateVect( float *pVect );
    
    //	Rotate a vector by the inverse of the rotation part of this matrix.
    void inverseRotateVect( float *pVect );
    
private:
    //	Matrix data, stored in column-major order
    float m_matrix[16];
};

class CCamera
{
public:
	CCamera();
    
	CVector3 Position() {	return m_vPosition;		}
	CVector3 View()		{	return m_vView;			}
	CVector3 UpVector() {	return m_vUpVector;		}
	CVector3 Up2()
	{
        return Normalize( Cross( m_vStrafe, m_vView - m_vPosition ) );
	}
	CVector3 Strafe()	{	return m_vStrafe;		}
	CVector3 Velocity() {	return m_vVelocity;		}
	float Yaw()			{	return m_vOrientation.y; }
	float Pitch()		{	return m_vOrientation.x; }
	bool Grounded()		{	return m_bGrounded;		}
    
	void SetPosition(CVector3 vPosition)	{	m_vPosition = vPosition;	}
	void SetView(CVector3 vView)			{	m_vView = vView;			}
	void SetUpVector(CVector3 vUpVector)	{	m_vUpVector = vUpVector;	}
	void Pitch(float pitch)					{	m_vOrientation.x = pitch;	}
    
	void PositionCamera(float positionX, float positionY, float positionZ,
			 		    float viewX,     float viewY,     float viewZ,
						float upVectorX, float upVectorY, float upVectorZ);
	void RotateView(float angle, float X, float Y, float Z);
	void View(CVector3 vView);
	void SetViewByMouse(float dx, float dy);
	CVector3 LookPos();
	void Look();
	void RotateAroundPoint(CVector3 vCenter, float X, float Y, float Z);
	void Strafe(float speed);
	void Move(float speed);
	void Rise(float speed);
	void Move(CVector3 delta);
	void MoveTo(CVector3 vPos);
	void Velocity(CVector3 vVelocity) { m_vVelocity = vVelocity; }
	void Grounded(bool bGrounded);
	void LimitHVel(float vLimit);
	void Stop();
	void CalcStrafe();
	void CalcYaw();
	void CalcPitch();
	void Step();
	void Friction();
    
private:
	CVector3 m_vPosition;					
	CVector3 m_vView;			
	CVector3 m_vUpVector;		
	CVector3 m_vStrafe;		
	CVector3 m_vVelocity;
	CVector2 m_vOrientation;	// pitch, yaw
	bool m_bGrounded;
};									

extern CCamera* g_camera;

#endif