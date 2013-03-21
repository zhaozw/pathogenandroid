package com.pathogengame.pathogen;

public class Math3D 
{
	static float DEGTORAD(float a)		{	return (float)Math.PI * a / 180.0f;	}
	static float RADTODEG(float a)		{	return 180.0f * a / (float)Math.PI;	}

	static final float PI_ON_180 = (float)Math.PI/180.0f;
	
	static boolean Equals(CVector3 a, CVector3 b)
	{
		if(a.x == b.x && a.y == b.y && a.z == b.z)
			return true;
		
		return false;
	}
	
	static CVector3 Copy(CVector3 a)
	{
		return new CVector3(a.x, a.y, a.z);
	}
	
	static CVector3 Add(CVector3 a, CVector3 b)
	{
		return new CVector3(a.x+b.x, a.y+b.y, a.z+b.z);
	}
	
	static CVector3 Add(tVector3uc a, tVector3uc b)
	{
		return new CVector3(a.x + b.x, a.y + b.y, a.z + b.z);
	}

	static CVector3 Subtract(CVector3 a, CVector3 b)
	{
		return new CVector3(a.x-b.x, a.y-b.y, a.z-b.z);
	}

	static CVector3 Multiply(CVector3 a, float num)
	{
		return new CVector3(a.x*num, a.y*num, a.z*num);
	}

	static CVector3 Multiply(CVector3 a, CVector3 b)
	{
		return new CVector3(a.x*b.x, a.y*b.y, a.z*b.z);
	}

	static CVector3 Divide(CVector3 a, float num)
	{
		return new CVector3(a.x/num, a.y/num, a.z/num);
	}
	
	float Clip(float n, float lower, float upper)
	{
	    return Math.max(lower, Math.min(n, upper));
	}
	
	static float Magnitude(CVector3 vNormal)
	{
		return (float)Math.sqrt( (vNormal.x * vNormal.x) + (vNormal.y * vNormal.y) + (vNormal.z * vNormal.z) );
	}

	static float Magnitude2(CVector3 vNormal)
	{
		return (vNormal.x * vNormal.x) + (vNormal.y * vNormal.y) + (vNormal.z * vNormal.z);
	}
	
	static float Magnitude2(float dx, float dy)
	{
	    return dx*dx + dy*dy;
	}
	
	static CVector3 Normalize(CVector3 vNormal)
	{
		float magnitude = Magnitude(vNormal);
	    
		vNormal.x /= magnitude;
		vNormal.y /= magnitude;
		vNormal.z /= magnitude;
	    
		return vNormal;
	}
	
	static CVector3 Cross(CVector3 vVector1, CVector3 vVector2)
	{
		CVector3 vNormal = new CVector3();
	    
		vNormal.x = ((vVector1.y * vVector2.z) - (vVector1.z * vVector2.y));
		vNormal.y = ((vVector1.z * vVector2.x) - (vVector1.x * vVector2.z));
		vNormal.z = ((vVector1.x * vVector2.y) - (vVector1.y * vVector2.x));
	    
		return vNormal;
	}
	
	static CVector3 Vector(CVector3 vPoint1, CVector3 vPoint2)
	{
		CVector3 vVector = new CVector3();
	    
		vVector.x = vPoint1.x - vPoint2.x;
		vVector.y = vPoint1.y - vPoint2.y;
		vVector.z = vPoint1.z - vPoint2.z;
	    
		return vVector;
	}
	
	static float PlaneDistance(CVector3 Normal, CVector3 Point)
	{
		float distance = 0; // This variable holds the distance from the plane to the origin
	    
		// Use the plane equation to find the distance (Ax + By + Cz + D = 0)  We want to find D.
		// So, we come up with D = -(Ax + By + Cz)
		// Basically, the negated dot product of the normal of the plane and the point.
		distance = - ((Normal.x * Point.x) + (Normal.y * Point.y) + (Normal.z * Point.z));
	    
		return distance;
	}

	// Clockwise
	static CVector3 Normal(CVector3 vTriangle[])
	{
		CVector3 vVector1 = Vector(vTriangle[2], vTriangle[0]);
		CVector3 vVector2 = Vector(vTriangle[1], vTriangle[0]);
	    
		CVector3 vNormal = Cross(vVector1, vVector2);
	    
		vNormal = Normalize(vNormal);
	    
		return vNormal;
	}

	// Counter-clockwise
	static CVector3 Normal2(CVector3 vTriangle[])
	{
		CVector3 vVector1 = Vector(vTriangle[2], vTriangle[0]);
		CVector3 vVector2 = Vector(vTriangle[1], vTriangle[0]);
	    
		CVector3 vNormal = Cross(vVector2, vVector1);
	    
		vNormal = Normalize(vNormal);
	    
		return vNormal;
	}
	
	static float Dot(CVector3 vVector1, CVector3 vVector2)
	{
		return ( (vVector1.x * vVector2.x) + (vVector1.y * vVector2.y) + (vVector1.z * vVector2.z) );
	}

	CVector3 Rotate(CVector3 v, float rad, float x, float y, float z)
	{
		CVector3 newV = new CVector3();
		float cosTheta = (float)Math.cos(rad);
		float sinTheta = (float)Math.sin(rad);
	    
		newV.x  = (cosTheta + (1 - cosTheta) * x * x)		* v.x;
		newV.x += ((1 - cosTheta) * x * y - z * sinTheta)	* v.y;
		newV.x += ((1 - cosTheta) * x * z + y * sinTheta)	* v.z;
	    
		newV.y  = ((1 - cosTheta) * x * y + z * sinTheta)	* v.x;
		newV.y += (cosTheta + (1 - cosTheta) * y * y)		* v.y;
		newV.y += ((1 - cosTheta) * y * z - x * sinTheta)	* v.z;
	    
		newV.z  = ((1 - cosTheta) * x * z - y * sinTheta)	* v.x;
		newV.z += ((1 - cosTheta) * y * z + x * sinTheta)	* v.y;
		newV.z += (cosTheta + (1 - cosTheta) * z * z)		* v.z;
	    
		return newV;
	}
	
	CVector3 RotateAround(CVector3 v, CVector3 around, float rad, float x, float y, float z)
	{
		v = Subtract(v, around);
		v = Rotate(v, rad, x, y, z);
		v = Add(around, v);
		return v;
	}
	
	static CVector3 GetRay(int x, int y, MainActivity act)
	{
		float halfWidth = act.mWidth / 2.0f;
		float halfHeight = act.mHeight / 2.0f;
	    
		float ratioX = (x - halfWidth) / halfWidth;
	    
		float ratioY = -(y - halfHeight) / halfHeight;
	    
		CVector3 direction = Math3D.Normalize( Math3D.Subtract(act.mCamera.View(), act.mCamera.Position()) );
	    
		float Hnear = 2 * (float)Math.tan( Math3D.DEGTORAD(act.mFOV) / 2) * act.mNear;
		float Wnear = Hnear * (float)act.mWidth/(float)act.mHeight;
	    
		CVector3 directional = Math3D.Multiply(direction, act.mNear);
		CVector3 sideways = Math3D.Multiply(act.mCamera.Strafe(), ratioX * Wnear/2.0f);
		CVector3 vertical = Math3D.Multiply(act.mCamera.Up2(), ratioY * Hnear/2.0f);
		
		CVector3 total = Math3D.Add(directional, sideways);
		total = Math3D.Add(total, vertical);
		
		//return ( direction * act.mNear + act.mCamera.Strafe() * ratioX * Wnear/2.0f + act.mCamera.Up2() * ratioY * Hnear/2.0f );
		return total;
	}
	
	static float GetYaw(float dx, float dz)
	{
	    /*
	    float yaw = atan2(dx, dz);
	    
	    if(yaw < 0.0f)
	        yaw += DEGTORAD( 360.0f );
	    
		return yaw;*/
		return (float)Math.atan2(dx, dz);
	}
	
	static boolean WithinAngle(CCamera c, CVector3 p, float angle)
	{
		CVector3 d = Math3D.Subtract(p, c.Position());
		float yaw = GetYaw(d.x, d.z);
		float yaw2 = yaw - DEGTORAD(360.0f);
		float yaw3 = yaw + DEGTORAD(360.0f);
	    
		if(Math.abs(c.Yaw() - yaw) <= angle || Math.abs(c.Yaw() - yaw2) <= angle || Math.abs(c.Yaw() - yaw3) <= angle)
			return true;
	    
		return false;
	}
}
