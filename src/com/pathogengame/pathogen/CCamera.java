package com.pathogengame.pathogen;

import com.pathogengame.pathogen.MainActivity.VIEWMODE;

public class CCamera 
{
	private CVector3 mPosition;					
	private CVector3 mView;			
	private CVector3 mUpVector;		
	private CVector3 mStrafe;		
	private CVector3 mVelocity;
	private CVector2 mOrientation;	// pitch, yaw
	private boolean mGrounded;
	float mLastRotX = 0.0f;
	
	CCamera()
	{
		mPosition = new CVector3();
		mView = new CVector3();
		mUpVector = new CVector3();
		mStrafe = new CVector3();
		mVelocity = new CVector3();
		mOrientation = new CVector2();
	}
	
	CVector3 Position() {	return Math3D.Copy(mPosition);		}
	CVector3 View()		{	return Math3D.Copy(mView);			}
	CVector3 UpVector() {	return Math3D.Copy(mUpVector);		}
	CVector3 Up2()
	{
        return Math3D.Normalize( Math3D.Cross( mStrafe, Math3D.Subtract(mView, mPosition) ) );
	}
	CVector3 Strafe()	{	return mStrafe;			}
	CVector3 Velocity() {	return mVelocity;		}
	float Yaw()			{	return mOrientation.y; 	}
	float Pitch()		{	return mOrientation.x; 	}
	boolean Grounded()	{	return mGrounded;		}
	
	void SetPosition(CVector3 vPosition)	{	mPosition = vPosition;	}
	void SetView(CVector3 vView)			{	mView = vView;			}
	void SetUpVector(CVector3 vUpVector)	{	mUpVector = vUpVector;	}
	void Pitch(float pitch)					{	mOrientation.x = pitch;	}
    
	void PositionCamera(float positionX, float positionY, float positionZ,
			 		    float viewX,     float viewY,     float viewZ,
						float upVectorX, float upVectorY, float upVectorZ)
	{
		CVector3 vPosition	= new CVector3(positionX, positionY, positionZ);
		CVector3 vView		= new CVector3(viewX, viewY, viewZ);
		CVector3 vUpVector	= new CVector3(upVectorX, upVectorY, upVectorZ);
	    
		mPosition = vPosition;
		mView     = vView;
		mUpVector = vUpVector;
	    
		CalcStrafe();
		CalcYaw();
		CalcPitch();
	}
	
	void View(CVector3 vView)
	{
		mView = Math3D.Copy(vView);
		CalcYaw();
	    CalcStrafe();
	}
	
	CVector3 LookPos(MainActivity act)
	{
		if(act.mViewMode == VIEWMODE.FIRSTPERSON)
	        return mPosition;
		else
		{
			CVector3 d = Math3D.Normalize(Math3D.Subtract(mView, mPosition));
			CVector3 vLine[] = new CVector3[2];
			vLine[0] = Math3D.Copy(mPosition);
			vLine[1] = Math3D.Subtract(mPosition, Math3D.Multiply(d, 64.0f));
	        
			vLine[1] = act.mMap.TraceRay(mPosition, vLine[1]);
	        
			CEntity e;
			int localE = act.mPlayer[act.mLocalP].entity;
	        CEntity lE = act.mEntity[localE];
	        
			for(int i=0; i<act.ENTITIES; i++)
			{
				e = act.mEntity[i];
	            
				if(!e.on)
					continue;
	            
	            if(!act.mMap.IsClusterVisible(e.cluster, lE.cluster) && !act.mMap.IsClusterVisible(lE.cluster, e.cluster))
	                continue;
	            
				if(localE == i)
					continue;
	            
				vLine[1] = e.TraceRay(vLine);
			}
	        
			return vLine[1];
		}
	}
	
	void Look()
	{
	    /*
		gluLookAt(m_vPosition.x, m_vPosition.y, m_vPosition.z,
				  m_vView.x,	 m_vView.y,     m_vView.z,
				  m_vUpVector.x, m_vUpVector.y, m_vUpVector.z);
	     */
	}
	
	void RotateAroundPoint(CVector3 vCenter, float X, float Y, float Z)
	{
		
	}
	
	void Strafe(float speed)
	{
		mVelocity.x += mStrafe.x * speed;
		mVelocity.z += mStrafe.z * speed;
	    //MoveTo(Position() + CVector3(m_vStrafe.x, 0, m_vStrafe.z) * speed * g_FrameInterval);
	}
	
	void Move(float speed)
	{
		CVector3 vVector = Math3D.Subtract(mView, mPosition);
	    
		vVector = Math3D.Normalize(vVector);
	    
		mVelocity.x += vVector.x * speed;
		mVelocity.z += vVector.z * speed;
	    
	    //MoveTo(Position() + CVector3(vVector.x, 0, vVector.z) * speed * g_FrameInterval);
	}
	
	void Rise(float speed)
	{
		CVector3 vVector = mUpVector;
	    
		vVector = Math3D.Normalize(vVector);
	    
		mVelocity.y += vVector.y * speed;
	}
	
	void Move(CVector3 delta)
	{
		mPosition = Math3D.Add(mPosition, delta);
		mView = Math3D.Add(mView, delta);
	}
	
	void MoveTo(CVector3 vPos)
	{
		CVector3 delta = Math3D.Subtract(vPos, mPosition);
		Move(delta);
		//m_vPosition = vPos;
	}
	
	void Velocity(CVector3 vVelocity) { mVelocity = vVelocity; }
	
	void Grounded(boolean bGrounded)
	{
		mGrounded = bGrounded;
	    
		if(bGrounded && mVelocity.y < 0.0f)
			mVelocity.y = 0.0f;
	}
	
	void Stop()
	{
		//m_vVelocity = new CVector3(0, 0, 0);
		mVelocity.x = 0;
		mVelocity.y = 0;
		mVelocity.z = 0;
	}
	
	void Step()
	{
		//CVector3 vNew = Math3D.Add(mPosition, Math3D.Multiply(mVelocity, g_FrameInterval));
		CVector3 vNew = Math3D.Add(mPosition, Math3D.Multiply(mVelocity, 1.0f/(float)MainActivity.FRAME_RATE));
		MoveTo(vNew);
	}
	
	void Friction()
	{
		mVelocity.x /= MainActivity.FRICTION;
		mVelocity.z /= MainActivity.FRICTION;
	}
	
	void CalcStrafe()
	{
		CVector3 vCross = Math3D.Cross(Math3D.Subtract(mView, mPosition), mUpVector);
		mStrafe = Math3D.Normalize(vCross);
	}
	
	void CalcYaw()
	{
		CVector3 d = Math3D.Subtract(mView, mPosition);
		mOrientation.y = Math3D.GetYaw(d.x, d.z);
	}
	
	void CalcPitch()
	{
		CVector3 d = Math3D.Subtract(mView, mPosition);
		float lateral = Math3D.Magnitude(new CVector3(d.x, 0, d.z));
		mOrientation.x = Math3D.RADTODEG( (float)Math.atan2(d.y, lateral) );
	}
	
	void LimitHVel(float limit)
	{
		CVector3 hVel = new CVector3(mVelocity.x, 0, mVelocity.z);
		float vVel = Math3D.Magnitude( hVel );
	    
		if(vVel <= limit)
			return;
	    
		hVel = Math3D.Divide(Math3D.Multiply(hVel, limit), vVel);
	    
		mVelocity.x = hVel.x;
		mVelocity.z = hVel.z;
	}
	
	void RotateView(float angle, float x, float y, float z)
	{
		CVector3 vNewView = new CVector3();
	    
		CVector3 vView = Math3D.Subtract(mView, mPosition);
	    
		float cosTheta = (float)Math.cos(angle);
		float sinTheta = (float)Math.sin(angle);
	    
		vNewView.x  = (cosTheta + (1 - cosTheta) * x * x)		* vView.x;
		vNewView.x += ((1 - cosTheta) * x * y - z * sinTheta)	* vView.y;
		vNewView.x += ((1 - cosTheta) * x * z + y * sinTheta)	* vView.z;
	    
		vNewView.y  = ((1 - cosTheta) * x * y + z * sinTheta)	* vView.x;
		vNewView.y += (cosTheta + (1 - cosTheta) * y * y)		* vView.y;
		vNewView.y += ((1 - cosTheta) * y * z - x * sinTheta)	* vView.z;
	    
		vNewView.z  = ((1 - cosTheta) * x * z - y * sinTheta)	* vView.x;
		vNewView.z += ((1 - cosTheta) * y * z + x * sinTheta)	* vView.y;
		vNewView.z += (cosTheta + (1 - cosTheta) * z * z)		* vView.z;
	    
		mView = Math3D.Add(mPosition, vNewView);
		CalcYaw();
	    CalcStrafe();
	}
	
	void SetViewByMouse(float dx, float dy)
	{
		// Get the direction the mouse moved in, but bring the number down to a reasonable amount
		float angleY = dx / 50.0f;
		float angleZ = dy / 50.0f;
	    
	 	mLastRotX = mOrientation.x; // We store off the currentRotX and will use it in when the angle is capped
	    
		// Here we keep track of the current rotation (for up and down) so that
		// we can restrict the camera from doing a full 360 loop.
		mOrientation.x += angleZ;
	    
		// If the current rotation (in radians) is greater than 1.0, we want to cap it.
		if(mOrientation.x > 1.0f)
		{
			mOrientation.x = 1.0f;
	        
			// Rotate by remaining angle if there is any
			if(mLastRotX != 1.0f)
			{
				// To find the axis we need to rotate around for up and down
				// movements, we need to get a perpendicular vector from the
				// camera's view vector and up vector.  This will be the axis.
				// Before using the axis, it's a good idea to normalize it first.
				CVector3 vAxis = Math3D.Cross(Math3D.Subtract(mView, mPosition), mUpVector);
				vAxis = Math3D.Normalize(vAxis);
	            
				// rotate the camera by the remaining angle (1.0f - lastRotX)
				RotateView( 1.0f - mLastRotX, vAxis.x, vAxis.y, vAxis.z);
			}
		}
		// Check if the rotation is below -1.0, if so we want to make sure it doesn't continue
		else if(mOrientation.x < -1.0f)
		{
			mOrientation.x = -1.0f;
	        
			// Rotate by the remaining angle if there is any
			if(mLastRotX != -1.0f)
			{
				// To find the axis we need to rotate around for up and down
				// movements, we need to get a perpendicular vector from the
				// camera's view vector and up vector.  This will be the axis.
				// Before using the axis, it's a good idea to normalize it first.
				CVector3 vAxis = Math3D.Cross(Math3D.Subtract(mView, mPosition), mUpVector);
				vAxis = Math3D.Normalize(vAxis);
	            
				// rotate the camera by ( -1.0f - lastRotX)
				RotateView( -1.0f - mLastRotX, vAxis.x, vAxis.y, vAxis.z);
			}
		}
		// Otherwise, we can rotate the view around our position
		else
		{
			// To find the axis we need to rotate around for up and down
			// movements, we need to get a perpendicular vector from the
			// camera's view vector and up vector.  This will be the axis.
			// Before using the axis, it's a good idea to normalize it first.
			CVector3 vAxis = Math3D.Cross(Math3D.Subtract(mView, mPosition), mUpVector);
			vAxis = Math3D.Normalize(vAxis);
	        
			// Rotate around our perpendicular axis
			RotateView(angleZ, vAxis.x, vAxis.y, vAxis.z);
		}
	    
		// Always rotate the camera around the y-axis
		RotateView(angleY, 0, 1, 0);
		CalcStrafe();
		CalcYaw();
		//CalcPitch();*/
	}
}
