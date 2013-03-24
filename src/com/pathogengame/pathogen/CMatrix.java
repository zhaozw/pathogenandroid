package com.pathogengame.pathogen;

public class CMatrix 
{
    //	Matrix data, stored in column-major order
    float m_matrix[] = new float[16];
    
    CMatrix()
    {
    	loadIdentity();
    }
    
    void loadIdentity()
    {
    	//memset( m_matrix, 0, sizeof( float )*16 );
        for(int i=0; i<16; i++)
            m_matrix[i] = 0;
    	m_matrix[0] = m_matrix[5] = m_matrix[10] = m_matrix[15] = 1;
    }
    
    void set(float matrix[])
    {
    	//memcpy( m_matrix, matrix, sizeof( float )*16 );
        for(int i=0; i<16; i++)
            m_matrix[i] = matrix[i];
    }
    
    //	Post-multiply by another matrix.
    void postMultiply(CMatrix matrix)
    {
    	float newMatrix[] = new float[16];
    	float m1[] = m_matrix, m2[] = matrix.m_matrix;
        
    	/*
    	const float *m1 = m_matrix, *m2 = matrix.m_matrix;

    	newMatrix[0] = m1[0]*m2[0] + m1[4]*m2[1] + m1[8]*m2[2];
    	newMatrix[1] = m1[1]*m2[0] + m1[5]*m2[1] + m1[9]*m2[2];
    	newMatrix[2] = m1[2]*m2[0] + m1[6]*m2[1] + m1[10]*m2[2];
    	newMatrix[3] = 0;

    	newMatrix[4] = m1[0]*m2[4] + m1[4]*m2[5] + m1[8]*m2[6];
    	newMatrix[5] = m1[1]*m2[4] + m1[5]*m2[5] + m1[9]*m2[6];
    	newMatrix[6] = m1[2]*m2[4] + m1[6]*m2[5] + m1[10]*m2[6];
    	newMatrix[7] = 0;

    	newMatrix[8] = m1[0]*m2[8] + m1[4]*m2[9] + m1[8]*m2[10];
    	newMatrix[9] = m1[1]*m2[8] + m1[5]*m2[9] + m1[9]*m2[10];
    	newMatrix[10] = m1[2]*m2[8] + m1[6]*m2[9] + m1[10]*m2[10];
    	newMatrix[11] = 0;

    	newMatrix[12] = m1[0]*m2[12] + m1[4]*m2[13] + m1[8]*m2[14] + m1[12];
    	newMatrix[13] = m1[1]*m2[12] + m1[5]*m2[13] + m1[9]*m2[14] + m1[13];
    	newMatrix[14] = m1[2]*m2[12] + m1[6]*m2[13] + m1[10]*m2[14] + m1[14];
    	newMatrix[15] = 1;*/

    	float a[] = m_matrix, b[] = matrix.m_matrix;

        newMatrix[0]  = a[0] * b[0]  + a[4] * b[1]  + a[8] * b[2]   + a[12] * b[3];
        newMatrix[1]  = a[1] * b[0]  + a[5] * b[1]  + a[9] * b[2]   + a[13] * b[3];
        newMatrix[2]  = a[2] * b[0]  + a[6] * b[1]  + a[10] * b[2]  + a[14] * b[3];
        newMatrix[3]  = a[3] * b[0]  + a[7] * b[1]  + a[11] * b[2]  + a[15] * b[3];

        newMatrix[4]  = a[0] * b[4]  + a[4] * b[5]  + a[8] * b[6]   + a[12] * b[7];
        newMatrix[5]  = a[1] * b[4]  + a[5] * b[5]  + a[9] * b[6]   + a[13] * b[7];
        newMatrix[6]  = a[2] * b[4]  + a[6] * b[5]  + a[10] * b[6]  + a[14] * b[7];
        newMatrix[7]  = a[3] * b[4]  + a[7] * b[5]  + a[11] * b[6]  + a[15] * b[7];

        newMatrix[8]  = a[0] * b[8]  + a[4] * b[9]  + a[8] * b[10]  + a[12] * b[11];
        newMatrix[9]  = a[1] * b[8]  + a[5] * b[9]  + a[9] * b[10]  + a[13] * b[11];
        newMatrix[10] = a[2] * b[8]  + a[6] * b[9]  + a[10] * b[10] + a[14] * b[11];
        newMatrix[11] = a[3] * b[8]  + a[7] * b[9]  + a[11] * b[10] + a[15] * b[11];

        newMatrix[12] = a[0] * b[12] + a[4] * b[13] + a[8] * b[14]  + a[12] * b[15];
        newMatrix[13] = a[1] * b[12] + a[5] * b[13] + a[9] * b[14]  + a[13] * b[15];
        newMatrix[14] = a[2] * b[12] + a[6] * b[13] + a[10] * b[14] + a[14] * b[15];
        newMatrix[15] = a[3] * b[12] + a[7] * b[13] + a[11] * b[14] + a[15] * b[15];
        
    	set(newMatrix);
    }
    
    //	Set the translation of the current matrix. Will erase any previous values.
    void setTranslation(float translation[])
    {
    	m_matrix[12] = translation[0];
    	m_matrix[13] = translation[1];
    	m_matrix[14] = translation[2];
    }
    
    void setTranslation(CVector3 translation)
    {
    	m_matrix[12] = translation.x;
    	m_matrix[13] = translation.y;
    	m_matrix[14] = translation.z;
    }
    
    //	Set the inverse translation of the current matrix. Will erase any previous values.
    void setInverseTranslation(float translation[])
    {
    	m_matrix[12] = -translation[0];
    	m_matrix[13] = -translation[1];
    	m_matrix[14] = -translation[2];
    }
    
    //	Make a rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setRotationRadians(float angles[])
    {
    	double cr = Math.cos( angles[0] );
    	double sr = Math.sin( angles[0] );
    	double cp = Math.cos( angles[1] );
    	double sp = Math.sin( angles[1] );
    	double cy = Math.cos( angles[2] );
    	double sy = Math.sin( angles[2] );
        
    	m_matrix[0] = ( float )( cp*cy );
    	m_matrix[1] = ( float )( cp*sy );
    	m_matrix[2] = ( float )( -sp );
        
    	double srsp = sr*sp;
    	double crsp = cr*sp;
        
    	m_matrix[4] = ( float )( srsp*cy-cr*sy );
    	m_matrix[5] = ( float )( srsp*sy+cr*cy );
    	m_matrix[6] = ( float )( sr*cp );
        
    	m_matrix[8] = ( float )( crsp*cy+sr*sy );
    	m_matrix[9] = ( float )( crsp*sy-sr*cy );
    	m_matrix[10] = ( float )( cr*cp );
    }
    
    //	Make a rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setRotationDegrees(float angles[])
    {
    	float vec[] = new float[3];
    	vec[0] = ( float )( angles[0]*180.0/Math.PI );
    	vec[1] = ( float )( angles[1]*180.0/Math.PI );
    	vec[2] = ( float )( angles[2]*180.0/Math.PI );
    	setRotationRadians( vec );
    }
    
    //	Make a rotation matrix from a quaternion. The 4th row and column are unmodified.
    void setRotationQuaternion(CQuaternion quat)
    {
    	m_matrix[0] = ( float )( 1.0 - 2.0*quat.m_quat[1]*quat.m_quat[1] - 2.0*quat.m_quat[2]*quat.m_quat[2] );
    	m_matrix[1] = ( float )( 2.0*quat.m_quat[0]*quat.m_quat[1] + 2.0*quat.m_quat[3]*quat.m_quat[2] );
    	m_matrix[2] = ( float )( 2.0*quat.m_quat[0]*quat.m_quat[2] - 2.0*quat.m_quat[3]*quat.m_quat[1] );
        
    	m_matrix[4] = ( float )( 2.0*quat.m_quat[0]*quat.m_quat[1] - 2.0*quat.m_quat[3]*quat.m_quat[2] );
    	m_matrix[5] = ( float )( 1.0 - 2.0*quat.m_quat[0]*quat.m_quat[0] - 2.0*quat.m_quat[2]*quat.m_quat[2] );
    	m_matrix[6] = ( float )( 2.0*quat.m_quat[1]*quat.m_quat[2] + 2.0*quat.m_quat[3]*quat.m_quat[0] );
        
    	m_matrix[8] = ( float )( 2.0*quat.m_quat[0]*quat.m_quat[2] + 2.0*quat.m_quat[3]*quat.m_quat[1] );
    	m_matrix[9] = ( float )( 2.0*quat.m_quat[1]*quat.m_quat[2] - 2.0*quat.m_quat[3]*quat.m_quat[0] );
    	m_matrix[10] = ( float )( 1.0 - 2.0*quat.m_quat[0]*quat.m_quat[0] - 2.0*quat.m_quat[1]*quat.m_quat[1] );
    }
    
    //	Make an inverted rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setInverseRotationRadians(float angles[])
    {
    	double cr = Math.cos( angles[0] );
    	double sr = Math.sin( angles[0] );
    	double cp = Math.cos( angles[1] );
    	double sp = Math.sin( angles[1] );
    	double cy = Math.cos( angles[2] );
    	double sy = Math.sin( angles[2] );
        
    	m_matrix[0] = ( float )( cp*cy );
    	m_matrix[4] = ( float )( cp*sy );
    	m_matrix[8] = ( float )( -sp );
        
    	double srsp = sr*sp;
    	double crsp = cr*sp;
        
    	m_matrix[1] = ( float )( srsp*cy-cr*sy );
    	m_matrix[5] = ( float )( srsp*sy+cr*cy );
    	m_matrix[9] = ( float )( sr*cp );
        
    	m_matrix[2] = ( float )( crsp*cy+sr*sy );
    	m_matrix[6] = ( float )( crsp*sy-sr*cy );
    	m_matrix[10] = ( float )( cr*cp );
    }
    
    //	Make an inverted rotation matrix from Euler angles. The 4th row and column are unmodified.
    void setInverseRotationDegrees(float angles[])
    {
    	float vec[] = new float[3];
    	vec[0] = ( float )( angles[0]*180.0/Math.PI );
    	vec[1] = ( float )( angles[1]*180.0/Math.PI );
    	vec[2] = ( float )( angles[2]*180.0/Math.PI );
    	setInverseRotationRadians( vec );
    }
    
    float[] getMatrix() { return m_matrix; }
    
    //	Translate a vector by the inverse of the translation part of this matrix.
    void inverseTranslateVect(float pVect[])
    {
    	pVect[0] = pVect[0]-m_matrix[12];
    	pVect[1] = pVect[1]-m_matrix[13];
    	pVect[2] = pVect[2]-m_matrix[14];
    }
    
    //	Rotate a vector by the inverse of the rotation part of this matrix.
    void inverseRotateVect(float pVect[])
    {
    	float vec[] = new float[3];
        
    	vec[0] = pVect[0]*m_matrix[0]+pVect[1]*m_matrix[1]+pVect[2]*m_matrix[2];
    	vec[1] = pVect[0]*m_matrix[4]+pVect[1]*m_matrix[5]+pVect[2]*m_matrix[6];
    	vec[2] = pVect[0]*m_matrix[8]+pVect[1]*m_matrix[9]+pVect[2]*m_matrix[10];
        
    	//memcpy( pVect, vec, sizeof( float )*3 );
        for(int i=0; i<3; i++)
            pVect[i] = vec[i];
    }
}
