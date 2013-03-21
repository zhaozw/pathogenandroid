package com.pathogengame.pathogen;

public class CQuaternion
{
	float m_quat[] = new float[4];
	
    CQuaternion(float angles[]) { fromAngles( angles ); }
    CQuaternion( CQuaternion q1, CQuaternion q2, float interp ) { slerp( q1, q2, interp ); }
    
    // (x,y,z) Euler rotation angles.
    void fromAngles(float angles[])
    {
    	float angle;
    	double sr, sp, sy, cr, cp, cy;
        
    	angle = angles[2]*0.5f;
    	sy = Math.sin( angle );
    	cy = Math.cos( angle );
    	angle = angles[1]*0.5f;
    	sp = Math.sin( angle );
    	cp = Math.cos( angle );
    	angle = angles[0]*0.5f;
    	sr = Math.sin( angle );
    	cr = Math.cos( angle );
        
    	double crcp = cr*cp;
    	double srsp = sr*sp;
        
    	m_quat[0] = ( float )( sr*cp*cy-cr*sp*sy );
    	m_quat[1] = ( float )( cr*sp*cy+sr*cp*sy );
    	m_quat[2] = ( float )( crcp*sy-srsp*cy );
    	m_quat[3] = ( float )( crcp*cy+srsp*sy );
    }
    
    /*
     Set values from an interpolation between two other quaternions.
     This will also modify the second quaternion if it is backwards.
     q1, q2	The quaternions to interpolate between
     interp	A value from 0.0-1.0 indicating the linear interpolation parameter.
     */
    void slerp(CQuaternion q1, CQuaternion q2, float interp)
    {
    	// Decide if one of the quaternions is backwards
    	int i;
    	float a = 0, b = 0;
    	for ( i = 0; i < 4; i++ )
    	{
    		a += ( q1.m_quat[i]-q2.m_quat[i] )*( q1.m_quat[i]-q2.m_quat[i] );
    		b += ( q1.m_quat[i]+q2.m_quat[i] )*( q1.m_quat[i]+q2.m_quat[i] );
    	}
    	if ( a > b )
    		q2.inverse();
        
    	float cosom = q1.m_quat[0]*q2.m_quat[0]+q1.m_quat[1]*q2.m_quat[1]+q1.m_quat[2]*q2.m_quat[2]+q1.m_quat[3]*q2.m_quat[3];
    	double sclq1, sclq2;
        
    	if (( 1.0+cosom ) > 0.00000001 )
    	{
    		if (( 1.0-cosom ) > 0.00000001 )
    		{
    			double omega = Math.acos( cosom );
    			double sinom = Math.sin( omega );
    			sclq1 = Math.sin(( 1.0-interp )*omega )/sinom;
    			sclq2 = Math.sin( interp*omega )/sinom;
    		}
    		else
    		{
    			sclq1 = 1.0-interp;
    			sclq2 = interp;
    		}
    		for ( i = 0; i < 4; i++ )
    			m_quat[i] = ( float )( sclq1*q1.m_quat[i]+sclq2*q2.m_quat[i] );
    	}
    	else
    	{
    		m_quat[0] = -q1.m_quat[1];
    		m_quat[1] = q1.m_quat[0];
    		m_quat[2] = -q1.m_quat[3];
    		m_quat[3] = q1.m_quat[2];
            
    		sclq1 = Math.sin(( 1.0-interp )*0.5*Math.PI );
    		sclq2 = Math.sin( interp*0.5*Math.PI );
    		for ( i = 0; i < 3; i++ )
    			m_quat[i] = ( float )( sclq1*q1.m_quat[i]+sclq2*m_quat[i] );
    	}
    }
    
    //float operator[]( int index ) const { return m_quat[index]; }
    void inverse()
    {
    	m_quat[0] = -m_quat[0];
    	m_quat[1] = -m_quat[1];
    	m_quat[2] = -m_quat[2];
    	m_quat[3] = -m_quat[3];
    }
};
