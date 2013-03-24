package com.pathogengame.pathogen;

public class Math3D 
{
	/* this version of SIGN3 shows some numerical instability, and is improved
	 * by using the uncommented macro that follows, and a different test with it */
	//#ifdef OLD_TEST
	//#define SIGN3( A ) (((A).x<0)?4:0 | ((A).y<0)?2:0 | ((A).z<0)?1:0)
	//#else
	
	static final float EPS = 0.00005f; //10e-5;
	
	static int SIGN3( CVector3 A ) 
	{
		//return ((A.x < EPS) ? 4 : 0 | (A.x > -EPS) ? 32 : 0 |
		//(A.y < EPS) ? 2 : 0 | (A.y > -EPS) ? 16 : 0 |
		//(A.z < EPS) ? 1 : 0 | (A.z > -EPS) ? 8 : 0);
		
		return (((A.x < EPS) ? 4 : 0) | ((A.x > -EPS) ? 32 : 0) |
		((A.y < EPS) ? 2 : 0) | ((A.y > -EPS) ? 16 : 0) |
		((A.z < EPS) ? 1 : 0) | ((A.z > -EPS) ? 8 : 0));
	}
	
	static void CROSS( CVector3 A, CVector3 B, CVector3 C ) 
	{
		C.x =  A.y * B.z - A.z * B.y;
		C.y = -A.x * B.z + A.z * B.x;
		C.z =  A.x * B.y - A.y * B.x;
	}
	static void SUB( CVector3 A, CVector3 B, CVector3 C ) 
	{
		C.x =  A.x - B.x;
		C.y =  A.y - B.y;
		C.z =  A.z - B.z;
	}
	
	static float LERP( float A, float B, float C)
	{
		return (B+A*(C-B));
	}
	
	static float MIN3(float a, float b, float c)
	{
		return (((a<b)&&(a<c)) ? a : ((b<c) ? b : c));
	}
	
	static float MAX3(float a, float b, float c)
	{
		return (((a>b)&&(a>c)) ? (a) : ((b>c) ? b : c));
	}
	
	static final int INSIDE = 0;
	static final int OUTSIDE = 1;
	
	/*typedef struct{
	    CVector3 a;
	    CVector3 b;
	    CVector3 c;
	} Triangle3;
	 */
	
	// Which of the six face-plane(s) is point P outside of?
	static long face_plane(CVector3 p)
	{
	    long outcode;
	    outcode = 0;
	    if (p.x >  .5) outcode |= 0x01;
	    if (p.x < -.5) outcode |= 0x02;
	    if (p.y >  .5) outcode |= 0x04;
	    if (p.y < -.5) outcode |= 0x08;
	    if (p.z >  .5) outcode |= 0x10;
	    if (p.z < -.5) outcode |= 0x20;
	    return(outcode);
	}
	
	// Which of the twelve edge plane(s) is point P outside of? 
	static long bevel_2d(CVector3 p)
	{
	    long outcode;
	    outcode = 0;
	    if ( p.x + p.y > 1.0) outcode |= 0x001;
	    if ( p.x - p.y > 1.0) outcode |= 0x002;
	    if (-p.x + p.y > 1.0) outcode |= 0x004;
	    if (-p.x - p.y > 1.0) outcode |= 0x008;
	    if ( p.x + p.z > 1.0) outcode |= 0x010;
	    if ( p.x - p.z > 1.0) outcode |= 0x020;
	    if (-p.x + p.z > 1.0) outcode |= 0x040;
	    if (-p.x - p.z > 1.0) outcode |= 0x080;
	    if ( p.y + p.z > 1.0) outcode |= 0x100;
	    if ( p.y - p.z > 1.0) outcode |= 0x200;
	    if (-p.y + p.z > 1.0) outcode |= 0x400;
	    if (-p.y - p.z > 1.0) outcode |= 0x800;
	    return(outcode);
	}
	
	// Which of the eight corner plane(s) is point P outside of? 
	static long bevel_3d(CVector3 p)
	{
	    long outcode;
	    outcode = 0;
	    if (( p.x + p.y + p.z) > 1.5) outcode |= 0x01;
	    if (( p.x + p.y - p.z) > 1.5) outcode |= 0x02;
	    if (( p.x - p.y + p.z) > 1.5) outcode |= 0x04;
	    if (( p.x - p.y - p.z) > 1.5) outcode |= 0x08;
	    if ((-p.x + p.y + p.z) > 1.5) outcode |= 0x10;
	    if ((-p.x + p.y - p.z) > 1.5) outcode |= 0x20;
	    if ((-p.x - p.y + p.z) > 1.5) outcode |= 0x40;
	    if ((-p.x - p.y - p.z) > 1.5) outcode |= 0x80;
	    return(outcode);
	}
	
	// Test the point "alpha" of the way from P1 to P2 
	// See if it is on a face of the cube			  
	// Consider only faces in "mask"				   
	static long check_point(CVector3 p1, CVector3 p2, float alpha, long mask)
	{
	    CVector3 plane_point = new CVector3();
	    plane_point.x = LERP(alpha, p1.x, p2.x);
	    plane_point.y = LERP(alpha, p1.y, p2.y);
	    plane_point.z = LERP(alpha, p1.z, p2.z);
	    return(face_plane(plane_point) & mask);
	}
	
	// Compute intersection of P1 --> P2 line segment with face planes 
	// Then test intersection point to see if it is on cube face	   
	// Consider only face planes in "outcode_diff"					 
	// Note: Zero bits in "outcode_diff" means face line is outside of 
	static long check_line(CVector3 p1, CVector3 p2, long outcode_diff)
	{
	    if ((0x01 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)(( .5-p1.x)/(p2.x-p1.x)), (long)(0x3e)) == INSIDE) return(INSIDE);
	    if ((0x02 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)((-.5-p1.x)/(p2.x-p1.x)), (long)(0x3d)) == INSIDE) return(INSIDE);
	    if ((0x04 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)(( .5-p1.y)/(p2.y-p1.y)), (long)(0x3b)) == INSIDE) return(INSIDE);
	    if ((0x08 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)((-.5-p1.y)/(p2.y-p1.y)), (long)(0x37)) == INSIDE) return(INSIDE);
	    if ((0x10 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)(( .5-p1.z)/(p2.z-p1.z)), (long)(0x2f)) == INSIDE) return(INSIDE);
	    if ((0x20 & outcode_diff) != 0)
	        if (check_point(p1, p2, (float)((-.5-p1.z)/(p2.z-p1.z)), (long)(0x1f)) == INSIDE) return(INSIDE);
	    return(OUTSIDE);
	}

	// Test if 3D point is inside 3D triangle 
	static long point_triangle_intersection(CVector3 p, CTriangle t)
	{
	    long sign12, sign23, sign31;
	    CVector3 vect12 = new CVector3(), 
	    		vect23 = new CVector3(), 
	    	    vect31 = new CVector3(), 
	    	    vect1h = new CVector3(), 
	    	    vect2h = new CVector3(), 
	    	    vect3h = new CVector3();
	    CVector3 cross12_1p = new CVector3(), 
	    		cross23_2p = new CVector3(), 
	    	    cross31_3p = new CVector3();
	    // First, a quick bounding-box test:							   
	    // If P is outside triangle bbox, there cannot be an intersection. 
	    if (p.x > MAX3(t.a.x, t.b.x, t.c.x)) return(OUTSIDE);
	    if (p.y > MAX3(t.a.y, t.b.y, t.c.y)) return(OUTSIDE);
	    if (p.z > MAX3(t.a.z, t.b.z, t.c.z)) return(OUTSIDE);
	    if (p.x < MIN3(t.a.x, t.b.x, t.c.x)) return(OUTSIDE);
	    if (p.y < MIN3(t.a.y, t.b.y, t.c.y)) return(OUTSIDE);
	    if (p.z < MIN3(t.a.z, t.b.z, t.c.z)) return(OUTSIDE);
	    // For each triangle side, make a vector out of it by subtracting vertexes; 
	    // make another vector from one vertex to point P.						  
	    // The crossproduct of these two vectors is orthogonal to both and the	  
	    // signs of its X,Y,Z components indicate whether P was to the inside or    
	    // to the outside of this triangle side.								    
	    SUB(t.a, t.b, vect12);
	    SUB(t.a,    p, vect1h);
	    CROSS(vect12, vect1h, cross12_1p);
	    sign12 = SIGN3(cross12_1p);	  // Extract X,Y,Z signs as 0..7 or 0...63 integer 
	    SUB(t.b, t.c, vect23);
	    SUB(t.b,    p, vect2h);
	    CROSS(vect23, vect2h, cross23_2p);
	    sign23 = SIGN3(cross23_2p);
	    SUB(t.c, t.a, vect31);
	    SUB(t.c,    p, vect3h);
	    CROSS(vect31, vect3h, cross31_3p);
	    sign31 = SIGN3(cross31_3p);
	    // If all three crossproduct vectors agree in their component signs,  
	    // then the point must be inside all three.						   
	    // P cannot be OUTSIDE all three sides simultaneously.			    
	    // this is the old test; with the revised SIGN3() macro, the test
	    // needs to be revised. 
	//#ifdef OLD_TEST
	//    if ((sign12 == sign23) && (sign23 == sign31))
	//        return(INSIDE);
	//    else
	//        return(OUTSIDE);
	//#else
	    return ((sign12 & sign23 & sign31) == 0) ? OUTSIDE : INSIDE;
	//#endif
	}
	/*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . */
	/**********************************************/
	/* This is the main algorithm procedure.	  */
	/* Triangle t is compared with a unit cube,   */
	/* centered on the origin.				    */
	/* It returns INSIDE (0) or OUTSIDE(1) if t   */
	/* intersects or does not intersect the cube. */
	/**********************************************/
	static long t_c_intersection(CTriangle t)
	{
	    long v1_test,v2_test,v3_test;
	    float d, denom;
	    CVector3 vect12 = new CVector3(),
	    		vect13 = new CVector3(),
	    		norm = new CVector3();
	    CVector3 hitpp = new CVector3(),
	    		hitpn = new CVector3(),
	    		hitnp = new CVector3(),
	    		hitnn = new CVector3();
	    /* First compare all three vertexes with all six face-planes */
	    /* If any vertex is inside the cube, return immediately!	 */
	    if ((v1_test = face_plane(t.a)) == INSIDE) return(INSIDE);
	    if ((v2_test = face_plane(t.b)) == INSIDE) return(INSIDE);
	    if ((v3_test = face_plane(t.c)) == INSIDE) return(INSIDE);
	    /* If all three vertexes were outside of one or more face-planes, */
	    /* return immediately with a trivial rejection!				   */
	    if ((v1_test & v2_test & v3_test) != 0) return(OUTSIDE);
	    /* Now do the same trivial rejection test for the 12 edge planes */
	    v1_test |= bevel_2d(t.a) << 8;
	    v2_test |= bevel_2d(t.b) << 8;
	    v3_test |= bevel_2d(t.c) << 8;
	    if ((v1_test & v2_test & v3_test) != 0) return(OUTSIDE);
	    /* Now do the same trivial rejection test for the 8 corner planes */
	    v1_test |= bevel_3d(t.a) << 24;
	    v2_test |= bevel_3d(t.b) << 24;
	    v3_test |= bevel_3d(t.c) << 24;
	    if ((v1_test & v2_test & v3_test) != 0) return(OUTSIDE);
	    /* If vertex 1 and 2, as a pair, cannot be trivially rejected */
	    /* by the above tests, then see if the v1-->v2 triangle edge  */
	    /* intersects the cube.  Do the same for v1-->v3 and v2-->v3. */
	    /* Pass to the intersection algorithm the "OR" of the outcode */
	    /* bits, so that only those cube faces which are spanned by   */
	    /* each triangle edge need be tested.						 */
	    if ((v1_test & v2_test) == 0)
	        if (check_line(t.a,t.b,v1_test|v2_test) == INSIDE) return(INSIDE);
	    if ((v1_test & v3_test) == 0)
	        if (check_line(t.a,t.c,v1_test|v3_test) == INSIDE) return(INSIDE);
	    if ((v2_test & v3_test) == 0)
	        if (check_line(t.b,t.c,v2_test|v3_test) == INSIDE) return(INSIDE);
	    /* By now, we know that the triangle is not off to any side,	 */
	    /* and that its sides do not penetrate the cube.  We must now    */
	    /* test for the cube intersecting the interior of the triangle.  */
	    /* We do this by looking for intersections between the cube	  */
	    /* diagonals and the triangle...first finding the intersection   */
	    /* of the four diagonals with the plane of the triangle, and	 */
	    /* then if that intersection is inside the cube, pursuing	    */
	    /* whether the intersection point is inside the triangle itself. */
	    /* To find plane of the triangle, first perform crossproduct on  */
	    /* two triangle side vectors to compute the normal vector.	   */
	    
	    SUB(t.a,t.b,vect12);
	    SUB(t.a,t.c,vect13);
	    CROSS(vect12,vect13,norm);
	    /* The normal vector "norm" X,Y,Z components are the coefficients */
	    /* of the triangles AX + BY + CZ + D = 0 plane equation.  If we   */
	    /* solve the plane equation for X=Y=Z (a diagonal), we get	    */
	    /* -D/(A+B+C) as a metric of the distance from cube center to the */
	    /* diagonal/plane intersection.  If this is between -0.5 and 0.5, */
	    /* the intersection is inside the cube.  If so, we continue by    */
	    /* doing a point/triangle intersection.						   */
	    /* Do this for all four diagonals.							    */
	    d = norm.x * t.a.x + norm.y * t.a.y + norm.z * t.a.z;
	    /* if one of the diagonals is parallel to the plane, the other will intersect the plane */
	    if(Math.abs(denom=(norm.x + norm.y + norm.z))>EPS)
	    /* skip parallel diagonals to the plane; division by 0 can occur */
	    {
	        hitpp.x = hitpp.y = hitpp.z = d / denom;
	        if (Math.abs(hitpp.x) <= 0.5)
	            if (point_triangle_intersection(hitpp,t) == INSIDE) return(INSIDE);
	    }
	    if(Math.abs(denom=(norm.x + norm.y - norm.z))>EPS)
	    {
	        hitpn.z = -(hitpn.x = hitpn.y = d / denom);
	        if (Math.abs(hitpn.x) <= 0.5)
	            if (point_triangle_intersection(hitpn,t) == INSIDE) return(INSIDE);
	    }
	    if(Math.abs(denom=(norm.x - norm.y + norm.z))>EPS)
	    {
	        hitnp.y = -(hitnp.x = hitnp.z = d / denom);
	        if (Math.abs(hitnp.x) <= 0.5)
	            if (point_triangle_intersection(hitnp,t) == INSIDE) return(INSIDE);
	    }
	    if(Math.abs(denom=(norm.x - norm.y - norm.z))>EPS)
	    {
	        hitnn.y = hitnn.z = -(hitnn.x = d / denom);
	        if (Math.abs(hitnn.x) <= 0.5)
	            if (point_triangle_intersection(hitnn,t) == INSIDE) return(INSIDE);
	    }
	    
	    /* No edge touched the cube; no cube diagonal touched the triangle. */
	    /* We're done...there was no intersection.						  */
	    return(OUTSIDE);
	}

	static boolean TriBoxOverlap(CVector3 vPos, CVector3 vMin, CVector3 vMax, CTriangle tri)
	{//CVector3 center = c->Position() + (vMin + t->vMax)/2.0f;
		CVector3 center;
		center = Divide(Add(vPos, Add(vMax, vMin)), 2.0f);
		CVector3 halfsize;
		halfsize = Divide(Subtract(vMax, vMin), 2.0f);
		CVector3 scaledown = new CVector3();
		scaledown.x = 1.0f / halfsize.x;
		scaledown.y = 1.0f / halfsize.y;
		scaledown.z = 1.0f / halfsize.z;
		tri.a = Multiply(Subtract(tri.a, center), scaledown);
		tri.b = Multiply(Subtract(tri.b, center), scaledown);
		tri.c = Multiply(Subtract(tri.c, center), scaledown);
	    
		if(t_c_intersection(tri) == INSIDE)
			return true;
	    
		return false;
	}

	static boolean TriBoxOverlap(CVector3 vCenter, CVector3 vRadius, CTriangle tri)
	{
		CVector3 scaledown = new CVector3();
		scaledown.x = 1.0f / vRadius.x;
		scaledown.y = 1.0f / vRadius.y;
		scaledown.z = 1.0f / vRadius.z;
		tri.a = Multiply(Subtract(tri.a, vCenter), scaledown);
		tri.b = Multiply(Subtract(tri.b, vCenter), scaledown);
		tri.c = Multiply(Subtract(tri.c, vCenter), scaledown);
	    
		if(t_c_intersection(tri) == INSIDE)
			return true;
	    
		return false;
	}


	static boolean TriBoxOverlap2(CVector3 vScaleDown, CVector3 vCenter, CTriangle tri)
	{
		tri.a = Multiply(Subtract(tri.a, vCenter), vScaleDown);
		tri.b = Multiply(Subtract(tri.b, vCenter), vScaleDown);
		tri.c = Multiply(Subtract(tri.c, vCenter), vScaleDown);
	    
		if(t_c_intersection(tri) == INSIDE)
			return true;
	    
		return false;
	}
	
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

	static CVector3 Rotate(CVector3 v, float rad, float x, float y, float z)
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
	
	static CVector3 RotateAround(CVector3 v, CVector3 around, float rad, float x, float y, float z)
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
	
	static CVector3 VMin(float minf, CVector3 v)
	{
		v.x = Math.min(minf, v.x);
		v.y = Math.min(minf, v.y);
		v.z = Math.min(minf, v.z);
		return v;
	}
	
	static boolean IntersectedPlane(CVector3 vPoly[], CVector3 vLine[], CVector3 vNormal, float originDistance)
	{
		float distance1=0, distance2=0;						// The distances from the 2 points of the line from the plane
	    
		CVector3 vTemp = Normal(vPoly);
		vNormal.x = vTemp.x;
		vNormal.y = vTemp.y;
		vNormal.z = vTemp.z;
	    
		// Let's find the distance our plane is from the origin.  We can find this value
		// from the normal to the plane (polygon) and any point that lies on that plane (Any vertice)
		originDistance = PlaneDistance(vNormal, vPoly[0]);
	    
		// Get the distance from point1 from the plane using: Ax + By + Cz + D = (The distance from the plane)
	    
		distance1 = ((vNormal.x * vLine[0].x)  +					// Ax +
			         (vNormal.y * vLine[0].y)  +					// Bx +
					 (vNormal.z * vLine[0].z)) + originDistance;	// Cz + D
	    
		// Get the distance from point2 from the plane using Ax + By + Cz + D = (The distance from the plane)
	    
		distance2 = ((vNormal.x * vLine[1].x)  +					// Ax +
			         (vNormal.y * vLine[1].y)  +					// Bx +
					 (vNormal.z * vLine[1].z)) + originDistance;	// Cz + D
	    
		// Now that we have 2 distances from the plane, if we times them together we either
		// get a positive or negative number.  If it's a negative number, that means we collided!
		// This is because the 2 points must be on either side of the plane (IE. -1 * 1 = -1).
	    
		if(distance1 * distance2 >= 0)			// Check to see if both point's distances are both negative or both positive
	        return false;
	    
		return true;
	}
	
	static double AngleBetweenVectors(CVector3 Vector1, CVector3 Vector2)
	{
		float dotProduct = Dot(Vector1, Vector2);
		float vectorsMagnitude = Magnitude(Vector1) * Magnitude(Vector2) ;
		double angle = Math.acos( dotProduct / vectorsMagnitude );
	    
		if(Double.isNaN(angle))
			return 0;
	    
		return( angle );
	}
	
	static CVector3 IntersectionPoint(CVector3 vNormal, CVector3 vLine[], double distance)
	{
		CVector3 vPoint = new CVector3();
		CVector3 vLineDir;
		double Numerator = 0.0, Denominator = 0.0, dist = 0.0;
	    
		// Here comes the confusing part.  We need to find the 3D point that is actually
		// on the plane.  Here are some steps to do that:
	    
		// 1)  First we need to get the vector of our line, Then normalize it so it's a length of 1
		vLineDir = Vector(vLine[1], vLine[0]);		// Get the Vector of the line
		vLineDir = Normalize(vLineDir);				// Normalize the lines vector
	    
	    
		// 2) Use the plane equation (distance = Ax + By + Cz + D) to find the distance from one of our points to the plane.
		//    Here I just chose a arbitrary point as the point to find that distance.  You notice we negate that
		//    distance.  We negate the distance because we want to eventually go BACKWARDS from our point to the plane.
		//    By doing this is will basically bring us back to the plane to find our intersection point.
		Numerator = - (vNormal.x * vLine[0].x +		// Use the plane equation with the normal and the line
					   vNormal.y * vLine[0].y +
					   vNormal.z * vLine[0].z + distance);
	    
		// 3) If we take the dot product between our line vector and the normal of the polygon,
		//    this will give us the cosine of the angle between the 2 (since they are both normalized - length 1).
		//    We will then divide our Numerator by this value to find the offset towards the plane from our arbitrary point.
		Denominator = Dot(vNormal, vLineDir);		// Get the dot product of the line's vector and the normal of the plane
	    
		// Since we are using division, we need to make sure we don't get a divide by zero error
		// If we do get a 0, that means that there are INFINATE points because the the line is
		// on the plane (the normal is perpendicular to the line - (Normal.Vector = 0)).
		// In this case, we should just return any point on the line.
	    
		if( Denominator == 0.0)						// Check so we don't divide by zero
			return vLine[0];						// Return an arbitrary point on the line
	    
		// We divide the (distance from the point to the plane) by (the dot product)
		// to get the distance (dist) that we need to move from our arbitrary point.  We need
		// to then times this distance (dist) by our line's vector (direction).  When you times
		// a scalar (single number) by a vector you move along that vector.  That is what we are
		// doing.  We are moving from our arbitrary point we chose from the line BACK to the plane
		// along the lines vector.  It seems logical to just get the numerator, which is the distance
		// from the point to the line, and then just move back that much along the line's vector.
		// Well, the distance from the plane means the SHORTEST distance.  What about in the case that
		// the line is almost parallel with the polygon, but doesn't actually intersect it until half
		// way down the line's length.  The distance from the plane is short, but the distance from
		// the actual intersection point is pretty long.  If we divide the distance by the dot product
		// of our line vector and the normal of the plane, we get the correct length.  Cool huh?
	    
		dist = Numerator / Denominator;				// Divide to get the multiplying (percentage) factor
	    
		// Now, like we said above, we times the dist by the vector, then add our arbitrary point.
		// This essentially moves the point along the vector to a certain distance.  This now gives
		// us the intersection point.  Yay!
	    
		vPoint.x = (float)(vLine[0].x + (vLineDir.x * dist));
		vPoint.y = (float)(vLine[0].y + (vLineDir.y * dist));
		vPoint.z = (float)(vLine[0].z + (vLineDir.z * dist));
	    
		return vPoint;								// Return the intersection point
	}
	
	static final double MATCH_FACTOR = 0.9999;		// Used to cover up the error in floating point
	
	static boolean InsidePolygon(CVector3 vIntersection, CVector3 Poly[], int verticeCount)
	{
		//const double MATCH_FACTOR = 0.9999;		// Used to cover up the error in floating point
		double Angle = 0.0;						// Initialize the angle
		CVector3 vA, vB;						// Create temp vectors
	    
		// Just because we intersected the plane, doesn't mean we were anywhere near the polygon.
		// This functions checks our intersection point to make sure it is inside of the polygon.
		// This is another tough function to grasp at first, but let me try and explain.
		// It's a brilliant method really, what it does is create triangles within the polygon
		// from the intersection point.  It then adds up the inner angle of each of those triangles.
		// If the angles together add up to 360 degrees (or 2 * PI in radians) then we are inside!
		// If the angle is under that value, we must be outside of polygon.  To further
		// understand why this works, take a pencil and draw a perfect triangle.  Draw a dot in
		// the middle of the triangle.  Now, from that dot, draw a line to each of the vertices.
		// Now, we have 3 triangles within that triangle right?  Now, we know that if we add up
		// all of the angles in a triangle we get 180° right?  Well, that is kinda what we are doing,
		// but the inverse of that.  Say your triangle is an equilateral triangle, so add up the angles
		// and you will get 180° degree angles.  60 + 60 + 60 is 360°.
	    
		for (int i = 0; i < verticeCount; i++)		// Go in a circle to each vertex and get the angle between
		{
			vA = Vector(Poly[i], vIntersection);	// Subtract the intersection point from the current vertex
	        // Subtract the point from the next vertex
			vB = Vector(Poly[(i + 1) % verticeCount], vIntersection);
	        
			Angle += AngleBetweenVectors(vA, vB);	// Find the angle between the 2 vectors and add them all up as we go along
		}
	    
		// Now that we have the total angles added up, we need to check if they add up to 360 degrees.
		// Since we are using the dot product, we are working in radians, so we check if the angles
		// equals 2*PI.  We defined PI in 3DMath.h.  You will notice that we use a MATCH_FACTOR
		// in conjunction with our desired degree.  This is because of the inaccuracy when working
		// with floating point numbers.  It usually won't always be perfectly 2 * PI, so we need
		// to use a little twiddling.  I use .9999, but you can change this to fit your own desired accuracy.
	    
		if(Angle >= (MATCH_FACTOR * (2.0 * Math.PI)) )	// If the angle is greater than 2 PI, (360 degrees)
			return true;							// The point is inside of the polygon
	    
		return false;								// If you get here, it obviously wasn't inside the polygon, so Return FALSE
	}
	
	static boolean IntersectedPolygon(CVector3 vPoly[], CVector3 vLine[], int verticeCount, CVector3 vIntersection)
	{
		CVector3 vNormal = new CVector3();// = {0};
		float originDistance = 0;
	    
		// First we check to see if our line intersected the plane.  If this isn't true
		// there is no need to go on, so return false immediately.
		// We pass in address of vNormal and originDistance so we only calculate it once
	    
	    // Reference   // Reference
		if(!IntersectedPlane(vPoly, vLine,   vNormal,   originDistance))
			return false;
	    
		// Now that we have our normal and distance passed back from IntersectedPlane(),
		// we can use it to calculate the intersection point.  The intersection point
		// is the point that actually is ON the plane.  It is between the line.  We need
		// this point test next, if we are inside the polygon.  To get the I-Point, we
		// give our function the normal of the plan, the points of the line, and the originDistance.
	    
		CVector3 vTemp = IntersectionPoint(vNormal, vLine, originDistance);
	    
		// Now that we have the intersection point, we need to test if it's inside the polygon.
		// To do this, we pass in :
		// (our intersection point, the polygon, and the number of vertices our polygon has)
	    
		if(InsidePolygon(vTemp, vPoly, verticeCount))
		{
			if(vIntersection != null)
			{
				//vIntersection = vTemp;
				vIntersection.x = vTemp.x;
				vIntersection.y = vTemp.y;
				vIntersection.z = vTemp.z;
			}
	        
			return true;
		}
	    
		return false;
	}
	
	static float DYaw(CCamera c, CVector3 p)
	{
		CVector3 d = Subtract(p, c.Position());
		float yaw = GetYaw(d.x, d.z);
		float yaw2 = yaw - DEGTORAD(360.0f);
		float yaw3 = yaw + DEGTORAD(360.0f);
	    
		float dyaw = yaw - c.Yaw();
		float dyaw2 = yaw2 - c.Yaw();
		float dyaw3 = yaw3 - c.Yaw();
	    
		float mindyaw = dyaw;
	    
		if(Math.abs(dyaw2) < Math.abs(mindyaw))
			mindyaw = dyaw2;
	    
		if(Math.abs(dyaw3) < Math.abs(mindyaw))
			mindyaw = dyaw3;
	    
		return mindyaw;
	}
}
