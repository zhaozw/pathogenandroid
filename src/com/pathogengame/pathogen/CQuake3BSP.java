package com.pathogengame.pathogen;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import android.opengl.GLES20;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CQuake3BSP 
{
	public static final int BSP_TEXTURES = 128;

	public static final int FACE_POLYGON = 1;

	public static final int TYPE_RAY = 0;
	public static final int TYPE_SPHERE	= 1;
	public static final int TYPE_BOX = 2;

	public static final float EPSILON = 0.03125f;
	
	// This is our lumps enumeration
	public static final int kEntities = 0;				// Stores player/object positions, etc...
	public static final int kTextures = 1;				// Stores texture information
	public static final int kPlanes = 2;			    // Stores the splitting planes
	public static final int kNodes = 3;					// Stores the BSP nodes
	public static final int kLeafs = 4;					// Stores the leafs of the nodes
	public static final int kLeafFaces = 5;				// Stores the leaf's indices into the faces
	public static final int kLeafBrushes = 6;			// Stores the leaf's indices into the brushes
	public static final int kModels = 7;				// Stores the info of world models
	public static final int kBrushes = 8;				// Stores the brushes info (for collision)
	public static final int kBrushSides = 9;			// Stores the brush surfaces info
	public static final int kVertices = 10;				// Stores the level vertices
	public static final int kIndices = 11;				// Stores the level indices
	public static final int kShaders = 12;				// Stores the shader files (blending, anims..)
	public static final int kFaces = 13;				// Stores the faces for the level
	public static final int kLightmaps = 14;			// Stores the lightmaps for the level
	public static final int kLightVolumes = 15;			// Stores extra world lighting information
	public static final int kVisData = 16;				// Stores PVS and cluster info (visibility)
	public static final int kMaxLumps = 17;				// A constant to store the number of lumps
	
	int m_pVertexBuffers[];
    int m_pIndexBuffers[];
    
	CVector3 m_gridSize;
	tBSPBBox m_bbox;
	tVector3ui num_lightvols;
	Vector<CSortFace> m_sortFaces = new Vector<CSortFace>();
    
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
    
	boolean m_bCollided;
	boolean m_bGrounded;
	boolean m_bTryStep;
	boolean m_bStuck;
	boolean m_bBroke;
	boolean m_bLadder;
    
	CVector3 m_vTraceMins;
	CVector3 m_vTraceMaxs;
	CVector3 m_vExtents;
	CVector3 m_vCollisionNormal;
	CVector3 m_vStart;
	CVector3 m_vEnd;
    
	int m_pIndices[];
	tBSPVertex m_pVerts[];
	tBSPFace m_pFaces[];
	tBSPNode m_pNodes[];
	tBSPLeaf m_pLeafs[];
	tBSPPlane m_pPlanes[];
	int m_pLeafFaces[];
	tBSPVisData m_clusters;
	tBSPTexture m_pTextures[];
	tBSPLightmap m_pLightmaps[];
	tBSPBrush m_pBrushes[];
	tBSPBrushSide m_pBrushSides[];
	int m_pLeafBrushes[];
	tBSPLightVol m_pLightVols[];
	tBSPModel m_pModels[];
    
	boolean m_brokenFace[];
	boolean m_brokenBrush[];
	int m_textures[];
	int m_lightmaps[];
	boolean m_passable[];
	boolean m_sky[];
	boolean m_transparent[];
	boolean m_water[];
	boolean m_breakable[];
	boolean m_ladder[];
	boolean m_grate[];
    
	CBitset m_FacesDrawn;
	
	MainActivity mActivity;

	Vector<CSpawn> mSpawn;
	Vector<CSpawn> mSSpawn;
	Vector<CSpawn> mZSpawn;
	
	Vector<CFuncMap> mFuncMap;
	Vector<CFuncProxy> mFuncProxy;
	
	//int mLastEnt = -1;
	
	public static final float SKYBOX_SIZE = 16.0f;

	public static final float SKY_TEX_0	= 0.002f;
	public static final float SKY_TEX_1	= 0.998f;

	public static final float SKY_TOP_0	= 0.002f;
	public static final float SKY_TOP_1	= 0.998f;

	int mFront;
	int mLeft;
	int mRight;
	int mBottom;
	int mTop;
	int mBack;

    CQuake3BSP(MainActivity act)
    {
    	mActivity = act;
    	
    	m_gridSize = new CVector3(64.0f, 128.0f, 64.0f);
        
        m_pVertexBuffers = null;
        m_pIndexBuffers = null;
        
        m_numOfVerts		= 0;
    	m_numOfFaces		= 0;
    	m_numOfIndices		= 0;
    	m_numOfTextures		= 0;
    	m_numOfLightmaps	= 0;
    	m_numOfNodes		= 0;
    	m_numOfLeafs		= 0;
    	m_numOfLeafFaces	= 0;
    	m_numOfPlanes		= 0;
    	m_numOfBrushes		= 0;
    	m_numOfBrushSides	= 0;
    	m_numOfLeafBrushes	= 0;
        
    	//m_traceRatio		= 0;
    	m_traceType			= 0;
    	m_traceRadius		= 0;
        
    	m_bCollided	= false;
    	m_bGrounded	= false;
    	m_bTryStep		= false;
        
    	m_vTraceMins = new CVector3(0, 0, 0);
    	m_vTraceMaxs = new CVector3(0, 0, 0);
    	m_vExtents   = new CVector3(0, 0, 0);
        
    	m_vCollisionNormal = new CVector3(0, 0, 0);
        
    	m_pVerts		 = null;
    	m_pFaces		 = null;
    	m_pIndices		 = null;
    	m_pNodes		 = null;
    	m_pLeafs		 = null;
    	m_pPlanes		 = null;
    	m_pLeafFaces	 = null;
        
    	//memset(&m_clusters, 0, sizeof(tBSPVisData));
    	m_clusters = null;
        
    	m_pBrushes       = null;
    	m_pBrushSides	 = null;
    	m_pTextures      = null;
    	m_pLightmaps	 = null;
    	m_pLeafBrushes	 = null;
    	m_pModels		 = null;
    	m_pLightVols	 = null;
        
    	m_brokenFace	 = null;
    	m_brokenBrush	 = null;
        
        m_numOfTextures = 0;
        m_pTextures     = null;
        m_textures      = null;
        m_passable      = null;
        m_sky           = null;
        m_transparent   = null;
        m_water         = null;
        m_breakable     = null;
        m_ladder        = null;
        m_grate         = null;
    }

    boolean LoadBSP(String name)
    {
    	int i = 0;
        
    	String raw = CFile.StripPathExtension(name);
        String fullBspPath = "/maps/" + raw + ".bsp";

        InputStream iS = CFile.GetInput(fullBspPath, mActivity);
        
        if(iS == null)
        {
        	System.out.println("Could not find BSP file " + raw);
        	return false;
        }
        
    	tBSPHeader header = new tBSPHeader();
    	tBSPLump lumps[] = new tBSPLump[kMaxLumps];
   
        //char strID[4];	// This should always be 'IBSP'
        //int version;	// This should be 0x2e for Quake 3 files
    	
    	int offset = 0;
    	byte bucket[] = CFile.ReadBytes(iS, offset, 4);
    	header.strID = "";
    	for(i=0; i<4; i++)
    	{
    		header.strID += (char)bucket[i];
    	}
    	
    	offset += 4;
    	header.version = CFile.ReadInt(iS, offset);
    	
    	if(!header.strID.equals("IBSP"))
    	{
    		System.out.println("BSP header not IBSP (" + header.strID + ")");
    		return false;
    	}
    	
    	if(header.version != 0x2e)
    	{
    		System.out.println("BSP version not 0x2e ("+header.version+")");
    		return false;
    	}
    	
    	offset += 4;
    	//int offset;
    	//int length;
    	
    	for(i=0;  i<kMaxLumps; i++)
    	{
    		lumps[i] = new tBSPLump();
    		lumps[i].offset = CFile.ReadInt(iS, offset + 0);
    		lumps[i].length = CFile.ReadInt(iS, offset + 4);
    		offset += 8;
    	}
    	
    	/*
    CVector3 vPosition;
    CVector2 vTextureCoord;
    CVector2 vLightmapCoord;
    CVector3 vNormal;
    byte color[4];
    	 */
        
    	m_numOfVerts = lumps[kVertices].length / (3*4 + 2*4 + 2*4 + 3*4 + 4);	//sizeof(tBSPVertex);
    	m_pVerts     = new tBSPVertex [m_numOfVerts];
    	
    	/*
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
    */
        
    	m_numOfFaces = lumps[kFaces].length / ((8+4)*4 + 3*4*3 + 2*4);	//sizeof(tBSPFace);
    	m_pFaces     = new tBSPFace [m_numOfFaces];
    	m_brokenFace = new boolean [m_numOfFaces];
        
        m_pVertexBuffers = new int [m_numOfFaces];
        m_pIndexBuffers = new int [m_numOfFaces];
        
    	m_numOfIndices = lumps[kIndices].length / 4; //sizeof(int);
    	m_pIndices     = new int [m_numOfIndices];
    	
    	/*
    char strName[64];			// The name of the texture w/o the extension
    int flags;					// The surface flags (unknown)
    int textureType;			// The type of texture (solid, water, slime, etc..) (type & 1) = 1 (solid)
    */
        
    	m_numOfTextures = lumps[kTextures].length / (64 + 4 + 4); //sizeof(tBSPTexture);
    	m_pTextures = new tBSPTexture [m_numOfTextures];
    	m_textures = new int [m_numOfTextures];
    	m_passable = new boolean [m_numOfTextures];
    	m_sky = new boolean [m_numOfTextures];
    	m_transparent = new boolean [m_numOfTextures];
    	m_water = new boolean [m_numOfTextures];
    	m_breakable = new boolean [m_numOfTextures];
    	m_ladder = new boolean [m_numOfTextures];
    	m_grate = new boolean [m_numOfTextures];
        
    	//byte imageBits[128][128][3];   // The RGB data in a 128x128 image
    	
    	m_numOfLightmaps = lumps[kLightmaps].length / (128 * 128 * 3);	//sizeof(tBSPLightmap);
    	m_pLightmaps = new tBSPLightmap [m_numOfLightmaps];
    	m_lightmaps = new int [m_numOfLightmaps];
    	
    	/*
    CVector3 mins;
	CVector3 maxs;
	int firstFace;
	int numOfFaces;
	int firstBrush;
	int numOfBrushes;
	*/
        
    	m_numOfModels = lumps[kModels].length / (3*4*2 + 4*4);	//sizeof(tBSPModel);
    	m_pModels = new tBSPModel [m_numOfModels];
        
    	/*
	tVector3uc ambient;
	tVector3uc directional;
	tVector2uc dir; //0=phi, 1=theta
	*/
    	
    	m_numOfLightVols = lumps[kLightVolumes].length / (1*3 + 1*3 + 1*2); //sizeof(tBSPLightVol);
    	m_pLightVols = new tBSPLightVol [m_numOfLightVols];
        
    	offset = lumps[kVertices].offset;
        
    	for(i = 0; i < m_numOfVerts; i++)
    	{
    		m_pVerts[i] = new tBSPVertex();
    		
    		/*
    CVector3 vPosition = new CVector3();
    CVector2 vTextureCoord = new CVector2();
    CVector2 vLightmapCoord = new CVector2();
    CVector3 vNormal = new CVector3();
    byte color[] = new byte[4];
    		 */

    		m_pVerts[i].vPosition.x = CFile.ReadFloat(iS, offset + 0*4);
    		m_pVerts[i].vPosition.y = CFile.ReadFloat(iS, offset + 1*4);
    		m_pVerts[i].vPosition.z = CFile.ReadFloat(iS, offset + 2*4);
    		m_pVerts[i].vTextureCoord.x = CFile.ReadFloat(iS, offset + 3*4);
    		m_pVerts[i].vTextureCoord.y = CFile.ReadFloat(iS, offset + 4*4);
    		m_pVerts[i].vLightmapCoord.x = CFile.ReadFloat(iS, offset + 5*4);
    		m_pVerts[i].vLightmapCoord.y = CFile.ReadFloat(iS, offset + 6*4);
    		m_pVerts[i].vNormal.x = CFile.ReadFloat(iS, offset + 7*4);
    		m_pVerts[i].vNormal.y = CFile.ReadFloat(iS, offset + 8*4);
    		m_pVerts[i].vNormal.z = CFile.ReadFloat(iS, offset + 9*4);
    		m_pVerts[i].color = CFile.ReadBytes(iS, offset + 10*4, 4);
            
    		float temp = m_pVerts[i].vPosition.y;
    		m_pVerts[i].vPosition.y = m_pVerts[i].vPosition.z;
    		m_pVerts[i].vPosition.z = -temp;
    		
    		offset += (3*4 + 2*4 + 2*4 + 3*4 + 4);
    	}
        
    	offset = lumps[kIndices].offset;
    	for(i=0; i<m_numOfIndices; i++)
    	{
    		m_pIndices[i] = CFile.ReadInt(iS, offset);
    		offset += 4;
    	}
        
    	offset = lumps[kFaces].offset;
    	//fread(m_pFaces, m_numOfFaces, sizeof(tBSPFace), fp);
    	for(i=0; i<m_numOfFaces; i++)
    	{
    		m_pFaces[i] = new tBSPFace();
    		
    		/*
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
    		 */

    		m_pFaces[i].textureID = CFile.ReadInt(iS, offset + 0*4);
    		m_pFaces[i].effect = CFile.ReadInt(iS, offset + 1*4);
    		m_pFaces[i].type = CFile.ReadInt(iS, offset + 2*4);
    		m_pFaces[i].startVertIndex = CFile.ReadInt(iS, offset + 3*4);
    		m_pFaces[i].numOfVerts = CFile.ReadInt(iS, offset + 4*4);
    		m_pFaces[i].startIndex = CFile.ReadInt(iS, offset + 5*4);
    		m_pFaces[i].numOfIndices = CFile.ReadInt(iS, offset + 6*4);
    		m_pFaces[i].lightmapID = CFile.ReadInt(iS, offset + 7*4);
    		m_pFaces[i].lMapCorner[0] = CFile.ReadInt(iS, offset + 8*4);
    		m_pFaces[i].lMapCorner[1] = CFile.ReadInt(iS, offset + 9*4);
    		m_pFaces[i].lMapSize[0] = CFile.ReadInt(iS, offset + 10*4);
    		m_pFaces[i].lMapSize[1] = CFile.ReadInt(iS, offset + 11*4);

    		m_pFaces[i].lMapPos.x = CFile.ReadFloat(iS, offset + 12*4 + 0*4);
    		m_pFaces[i].lMapPos.y = CFile.ReadFloat(iS, offset + 12*4 + 1*4);
    		m_pFaces[i].lMapPos.z = CFile.ReadFloat(iS, offset + 12*4 + 2*4);
    		m_pFaces[i].lMapVecs[0].x = CFile.ReadFloat(iS, offset + 12*4 + 3*4);
    		m_pFaces[i].lMapVecs[0].y = CFile.ReadFloat(iS, offset + 12*4 + 4*4);
    		m_pFaces[i].lMapVecs[0].z = CFile.ReadFloat(iS, offset + 12*4 + 5*4);
    		m_pFaces[i].lMapVecs[1].x = CFile.ReadFloat(iS, offset + 12*4 + 6*4);
    		m_pFaces[i].lMapVecs[1].y = CFile.ReadFloat(iS, offset + 12*4 + 7*4);
    		m_pFaces[i].lMapVecs[1].z = CFile.ReadFloat(iS, offset + 12*4 + 8*4);
    		m_pFaces[i].vNormal.x = CFile.ReadFloat(iS, offset + 12*4 + 9*4);
    		m_pFaces[i].vNormal.y = CFile.ReadFloat(iS, offset + 12*4 + 10*4);
    		m_pFaces[i].vNormal.z = CFile.ReadFloat(iS, offset + 12*4 + 11*4);

    		m_pFaces[i].size[0] = CFile.ReadInt(iS, offset + 12*4 + 12*4 + 0*4);
    		m_pFaces[i].size[1] = CFile.ReadInt(iS, offset + 12*4 + 12*4 + 1*4);
    		
    		offset += ((8+4)*4 + 3*4*3 + 2*4);
    	}
        
    	int j;
    	ByteBuffer bb;
        for(i = 0; i < m_numOfFaces; i++)
        {
            tBSPFace pFace = m_pFaces[i];
            
        	bb = ByteBuffer.allocateDirect( pFace.numOfVerts * (3*4 + 2*4 + 2*4 + 3*4 + 4) );
        	
        	for(j=0; j<pFace.numOfVerts; j++)
        	{
        		//fb.put(index, f);
        		//m_pVerts[pFace->startVertIndex + j]
        				
        				/*
    CVector3 vPosition = new CVector3();
    CVector2 vTextureCoord = new CVector2();
    CVector2 vLightmapCoord = new CVector2();
    CVector3 vNormal = new CVector3();
    byte color[] = new byte[4];
        				 */

    			//int vIndex = m_pIndices[pFace.startIndex + j] + pFace.startVertIndex;
    			
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vPosition.x );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vPosition.y );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vPosition.z );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vTextureCoord.x );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vTextureCoord.y );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vLightmapCoord.x );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vLightmapCoord.y );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vNormal.x );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vNormal.y );
        		bb.putFloat( m_pVerts[pFace.startVertIndex + j].vNormal.z );
        		bb.put( m_pVerts[pFace.startVertIndex + j].color[0] );
        		bb.put( m_pVerts[pFace.startVertIndex + j].color[1] );
        		bb.put( m_pVerts[pFace.startVertIndex + j].color[2] );
        		bb.put( m_pVerts[pFace.startVertIndex + j].color[3] );
        	}
            
            GLES20.glGenBuffers(1, m_pVertexBuffers, i);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_pVertexBuffers[i]);
            //GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, sizeof(tBSPVertex)*pFace->numOfVerts, &(m_pVerts[pFace->startVertIndex]), GLES20.GL_STATIC_DRAW);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, pFace.numOfVerts * (3*4 + 2*4 + 2*4 + 3*4 + 4), bb, GLES20.GL_STATIC_DRAW);
            
            bb = ByteBuffer.allocateDirect( pFace.numOfIndices * 4 );
            
            for(j=0; j<pFace.numOfIndices; j++)
            {
            	bb.putInt( m_pIndices[pFace.startIndex + j] );
            }
            
            GLES20.glGenBuffers(1, m_pIndexBuffers, i);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_pIndexBuffers[i]);
            //GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, sizeof(int)*pFace->numOfIndices, &(m_pIndices[pFace->startIndex]), GLES20.GL_STATIC_DRAW);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, pFace.numOfIndices * 4, bb, GLES20.GL_STATIC_DRAW);
        }
        
        offset = lumps[kTextures].offset;
        
    	//fread(m_pTextures, m_numOfTextures, sizeof(tBSPTexture), fp);
        for(i=0; i<m_numOfTextures; i++)
        {
        	m_pTextures[i] = new tBSPTexture();
        	
        	/*
    //char strName[64];			// The name of the texture w/o the extension
    //int flags;					// The surface flags (unknown)
    //int textureType;			// The type of texture (solid, water, slime, etc..) (type & 1) = 1 (solid)
	
	String strName;
	int flags;
	int textureType;
        	 */

        	bucket = CFile.ReadBytes(iS, offset + 0, 64);
        	String texname = "";
        	for(j=0; j<64; j++)
        	{
        		texname += (char)bucket[j];
        	}
        	
        	m_pTextures[i].strName = texname;
        	m_pTextures[i].flags = CFile.ReadInt(iS, offset + 64);
        	m_pTextures[i].textureType = CFile.ReadInt(iS, offset + 64 + 4);
        	
        	offset += (64 + 4 + 4);
        }
        
    	for(i = 0; i < m_numOfTextures; i++)
    	{
    		m_passable[i] = false;
    		m_sky[i] = false;
    		m_water[i] = false;
    		m_transparent[i] = false;
    		m_breakable[i] = false;
    		m_ladder[i] = false;
    		m_grate[i] = false;
    	}
        
    	for(i = 0; i < m_numOfTextures; i++)
    	{
	    	if(m_pTextures[i].strName.contains("~"))
	    		m_passable[i] = true;
	    	if(m_pTextures[i].strName.contains("^"))
	    		m_sky[i] = true;
	    	if(m_pTextures[i].strName.contains("$"))
	    		m_water[i] = true;
	    	if(m_pTextures[i].strName.contains("%"))
	    		m_transparent[i] = true;
	    	if(m_pTextures[i].strName.contains("!"))
	    		m_breakable[i] = true;
	    	if(m_pTextures[i].strName.contains("#"))
	    		m_ladder[i] = true;
	    	if(m_pTextures[i].strName.contains("`"))
	    		m_grate[i] = true;
            
            // Make sure to remove the ` symbol from final textures
            // because of CopyPNGFile error
            //string replacer(m_pTextures[i].strName);
            //replacer.erase (std::remove(replacer.begin(), replacer.end(), '`'), replacer.end());
            //strcpy(m_pTextures[i].strName, replacer.c_str());
            
            //FindTextureExtension(m_pTextures[i].strName);
            //m_textures[i] = CreateTexture(m_pTextures[i].strName, false);
            //NSLog(@"create %d %s", i, m_pTextures[i].strName);
            m_textures[i] = mActivity.CreateTexture(m_pTextures[i].strName, true);
    	}
        
        int vIndex;
    	for(int faceIndex=0; faceIndex<m_numOfFaces; faceIndex++)
    	{
    		m_brokenFace[faceIndex] = false;
            
    		if(m_pFaces[faceIndex].type != FACE_POLYGON) continue;
    		if(!m_transparent[m_pFaces[faceIndex].textureID]) continue;
            
    		CSortFace sortFace = new CSortFace();
    		sortFace.faceIndex = faceIndex;
    		CVector3 vPos = new CVector3(0, 0, 0);
            
    		//m_pFaces[faceIndex].
    		tBSPFace pFace = m_pFaces[faceIndex];
            
    		for(i=0; i<pFace.numOfIndices; i++)
    		{
    			vIndex = m_pIndices[pFace.startIndex+i] + pFace.startVertIndex;
    			vPos = Math3D.Add(Math3D.Divide(m_pVerts[vIndex].vPosition, (float)(i+1)), Math3D.Multiply(vPos, (float)i/(float)(i+1)));
    		}
    		//m_pVerts[pFace->startVertIndex].vPosition
    		//pFace->numOfIndices, GL_UNSIGNED_INT, &(m_pIndices[pFace->startIndex]
    		//vPos = m_pVerts[pFace->startVertIndex].vPosition;
    		sortFace.pos = vPos;
    		m_sortFaces.add(sortFace);
    	}
        
    	offset = lumps[kLightmaps].offset;
        
    	for(i = 0; i < m_numOfLightmaps ; i++)
    	{
    		m_pLightmaps[i] = new tBSPLightmap();
    		//fread(&m_pLightmaps[i], 1, sizeof(tBSPLightmap), fp);
    		
    		m_pLightmaps[i].imageBits = CFile.ReadBytes(iS, offset, 128*128*3);
            
            m_lightmaps[i] = CreateLightmapTexture(m_pLightmaps[i].imageBits, 128, 128);
            
            offset += (128*128*3);
    	}
        
    	/*
    int plane;					// The index into the planes array
    int front;					// The child index for the front node
    int back;					// The child index for the back node
    tVector3i min;				// The bounding box min position.
    tVector3i max;				// The bounding box max position.
    	 */
    	
    	m_numOfNodes = lumps[kNodes].length / (4*3 + 4*3*2); //sizeof(tBSPNode);
    	m_pNodes     = new tBSPNode [m_numOfNodes];
        
    	offset = lumps[kNodes].offset;
    	
    	for(i=0; i<m_numOfNodes; i++)
    	{
    		m_pNodes[i] = new tBSPNode();
    		
    		/*
    int plane;					// The index into the planes array
    int front;					// The child index for the front node
    int back;					// The child index for the back node
    tVector3i min;				// The bounding box min position.
    tVector3i max;				// The bounding box max position.
    */

    		m_pNodes[i].plane = CFile.ReadInt(iS, offset + 0*4);
    		m_pNodes[i].front = CFile.ReadInt(iS, offset + 1*4);
    		m_pNodes[i].back = CFile.ReadInt(iS, offset + 2*4);
    		m_pNodes[i].min.x = CFile.ReadInt(iS, offset + 3*4 + 0*4 + 0*4);
    		m_pNodes[i].min.y = CFile.ReadInt(iS, offset + 3*4 + 1*4 + 0*4);
    		m_pNodes[i].min.z = CFile.ReadInt(iS, offset + 3*4 + 2*4 + 0*4);
    		m_pNodes[i].max.x = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 0*4);
    		m_pNodes[i].max.y = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 1*4);
    		m_pNodes[i].max.z = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 2*4);
    		
    		offset += (4*3 + 4*3*2);
    	}
        
    	/*
    int cluster;				// The visibility cluster
    int area;					// The area portal
    tVector3i min;				// The bounding box min position
    tVector3i max;				// The bounding box max position
    int leafface;				// The first index into the face array
    int numOfLeafFaces;			// The number of faces for this leaf
    int leafBrush;				// The first index for into the brushes
    int numOfLeafBrushes;		// The number of brushes for this leaf
    */
    	
    	m_numOfLeafs = lumps[kLeafs].length / (2*4 + 2*3*4 + 4*4);	//sizeof(tBSPLeaf);
    	m_pLeafs     = new tBSPLeaf [m_numOfLeafs];
        
    	offset = lumps[kLeafs].offset;
    	//fread(m_pLeafs, m_numOfLeafs, sizeof(tBSPLeaf), fp);
    	for(i=0; i<m_numOfLeafs; i++)
    	{
    		m_pLeafs[i] = new tBSPLeaf();

    		m_pLeafs[i].cluster = CFile.ReadInt(iS, offset + 0*4);
    		m_pLeafs[i].area = CFile.ReadInt(iS, offset + 1*4);
    		m_pLeafs[i].min.x = CFile.ReadInt(iS, offset + 2*4 + 0*4);
    		m_pLeafs[i].min.y = CFile.ReadInt(iS, offset + 2*4 + 1*4);
    		m_pLeafs[i].min.z = CFile.ReadInt(iS, offset + 2*4 + 2*4);
    		m_pLeafs[i].max.x = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 0*4);
    		m_pLeafs[i].max.y = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 1*4);
    		m_pLeafs[i].max.z = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 2*4);
    		m_pLeafs[i].leafface = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 3*4 + 0*4);
    		m_pLeafs[i].numOfLeafFaces = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 3*4 + 1*4);
    		m_pLeafs[i].leafBrush = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 3*4 + 2*4);
    		m_pLeafs[i].numOfLeafBrushes = CFile.ReadInt(iS, offset + 2*4 + 3*4 + 3*4 + 3*4);
    		
    		offset += (2*4 + 2*3*4 + 4*4);
    	}
        
    	for(i = 0; i < m_numOfLeafs; i++)
    	{
    		int temp = m_pLeafs[i].min.y;
    		m_pLeafs[i].min.y = m_pLeafs[i].min.z;
    		m_pLeafs[i].min.z = -temp;
            
    		temp = m_pLeafs[i].max.y;
    		m_pLeafs[i].max.y = m_pLeafs[i].max.z;
    		m_pLeafs[i].max.z = -temp;
    	}
        
    	m_numOfLeafFaces = lumps[kLeafFaces].length / 4; //sizeof(int);
    	m_pLeafFaces     = new int [m_numOfLeafFaces];
        
    	offset = lumps[kLeafFaces].offset;
    	for(i=0; i<m_numOfLeafFaces; i++)
    	{
    		m_pLeafFaces[i] = CFile.ReadInt(iS, offset);
    		offset += 4;
    	}
        
    	/*
    CVector3 vNormal;			// Plane normal.
    float d;					// The plane distance from origin
    */
    	
    	m_numOfPlanes = lumps[kPlanes].length / (3*4 + 4);	//sizeof(tBSPPlane);
    	m_pPlanes     = new tBSPPlane [m_numOfPlanes];
        
    	offset = lumps[kPlanes].offset;
    	for(i=0; i<m_numOfPlanes; i++)
    	{
    		m_pPlanes[i] = new tBSPPlane();

    		m_pPlanes[i].vNormal.x = CFile.ReadFloat(iS, offset + 0*4);
    		m_pPlanes[i].vNormal.y = CFile.ReadFloat(iS, offset + 1*4);
    		m_pPlanes[i].vNormal.z = CFile.ReadFloat(iS, offset + 2*4);
    		m_pPlanes[i].d = CFile.ReadFloat(iS, offset + 3*4);
    		
    		offset += (3*4 + 4);
    	}
        
    	for(i = 0; i < m_numOfPlanes; i++)
    	{
    		float temp = m_pPlanes[i].vNormal.y;
    		m_pPlanes[i].vNormal.y = m_pPlanes[i].vNormal.z;
    		m_pPlanes[i].vNormal.z = -temp;
    	}
        
    	offset = lumps[kVisData].offset;
    	if(lumps[kVisData].length > 0)
    	{
    		m_clusters.numOfClusters = CFile.ReadInt(iS, offset + 0*4);
    		m_clusters.bytesPerCluster = CFile.ReadInt(iS, offset + 1*4);
            
    		int size = m_clusters.numOfClusters * m_clusters.bytesPerCluster;
    		//m_clusters.pBitsets = new byte [size];
            
    		//fread(m_clusters.pBitsets, 1, sizeof(byte) * size, fp);
    		m_clusters.pBitsets = CFile.ReadBytes(iS, offset + 2*4, size);
    	}
    	else
    		m_clusters.pBitsets = null;
        
    	m_numOfBrushes = lumps[kBrushes].length / (3*4); //sizeof(int);
    	m_pBrushes     = new tBSPBrush [m_numOfBrushes];
    	m_brokenBrush  = new boolean [m_numOfBrushes];
        
    	for(i=0; i<m_numOfBrushes; i++)
    		m_brokenBrush[i] = false;
        
    	offset = lumps[kBrushes].offset;
    	for(i=0; i<m_numOfBrushes; i++)
    	{
    		m_pBrushes[i] = new tBSPBrush();
        	//fread(m_pBrushes, m_numOfBrushes, sizeof(tBSPBrush), fp);
    		
    		/*
		    int brushSide;				// The starting brush side for the brush
			int numOfBrushSides;		// Number of brush sides for the brush
			int textureID;				// The texture index for the brush
			*/

    		m_pBrushes[i].brushSide = CFile.ReadInt(iS, offset + 0*4);
    		m_pBrushes[i].numOfBrushSides = CFile.ReadInt(iS, offset + 1*4);
    		m_pBrushes[i].textureID = CFile.ReadInt(iS, offset + 2*4);
    		
    		offset += (3*4);
    	}
        
    	/*
	int plane;					// The plane index
	int textureID;				// The texture index
	*/
    	
    	m_numOfBrushSides = lumps[kBrushSides].length / (4*2); //sizeof(int);
    	m_pBrushSides     = new tBSPBrushSide [m_numOfBrushSides];
        
    	offset = lumps[kBrushSides].offset;
    	for(i=0; i<m_numOfBrushSides; i++)
    	{
    		m_pBrushSides[i] = new tBSPBrushSide();

    		m_pBrushSides[i].plane = CFile.ReadInt(iS, offset + 0*4);
    		m_pBrushSides[i].textureID = CFile.ReadInt(iS, offset + 1*4);
    		
    		offset += (4*2);
    	}
        
    	m_numOfLeafBrushes = lumps[kLeafBrushes].length / 4; //sizeof(int);
    	m_pLeafBrushes     = new int [m_numOfLeafBrushes];
        
    	offset = lumps[kLeafBrushes].offset;
    	for(i=0; i<m_numOfLeafBrushes; i++)
    	{
    		m_pLeafBrushes[i] = CFile.ReadInt(iS, offset);
    		offset += 4;
    	}
        
    	offset = lumps[kModels].offset;
    	//fread(m_pModels, m_numOfModels, sizeof(tBSPModel), fp);
    	for(i=0; i<m_numOfModels; i++)
    	{
    		m_pModels[i] = new tBSPModel();
    		
    		/*
			CVector3 mins;
			CVector3 maxs;
			int firstFace;
			int numOfFaces;
			int firstBrush;
			int numOfBrushes;
			*/

    		m_pModels[i].mins.x = CFile.ReadFloat(iS, offset + 0*4);
    		m_pModels[i].mins.y = CFile.ReadFloat(iS, offset + 1*4);
    		m_pModels[i].mins.z = CFile.ReadFloat(iS, offset + 2*4);
    		m_pModels[i].maxs.x = CFile.ReadFloat(iS, offset + 3*4 + 0*4);
    		m_pModels[i].maxs.y = CFile.ReadFloat(iS, offset + 3*4 + 1*4);
    		m_pModels[i].maxs.z = CFile.ReadFloat(iS, offset + 3*4 + 2*4);
    		m_pModels[i].firstFace = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 0*4);
    		m_pModels[i].numOfFaces = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 1*4);
    		m_pModels[i].firstBrush = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 2*4);
    		m_pModels[i].numOfBrushes = CFile.ReadInt(iS, offset + 3*4 + 3*4 + 3*4);
    		
    		offset += (3*4*2 + 4*4);
    	}
        
    	for(i=0; i<m_numOfModels; i++)
    	{
    		float temp = m_pModels[i].mins.y;
    		m_pModels[i].mins.y = m_pModels[i].mins.z;
    		m_pModels[i].mins.z = -temp;
            
    		temp = m_pModels[i].maxs.y;
    		m_pModels[i].maxs.y = m_pModels[i].maxs.z;
    		m_pModels[i].maxs.z = -temp;
            
    		float tempmin = Math.min(m_pModels[i].mins.z, m_pModels[i].maxs.z);
    		float tempmax = Math.max(m_pModels[i].mins.z, m_pModels[i].maxs.z);
    		m_pModels[i].mins.z = tempmin;
    		m_pModels[i].maxs.z = tempmax;
    	}
        
    	m_bbox.min = m_pModels[0].mins;
    	m_bbox.max = m_pModels[0].maxs;
        
    	offset = lumps[kLightVolumes].offset;
    	//fread(m_pLightVols, m_numOfLightVols, sizeof(tBSPLightVol), fp);
    	for(i=0; i<m_numOfLightVols; i++)
    	{
    		m_pLightVols[i] = new tBSPLightVol();
    		/*
			tVector3uc ambient;
			tVector3uc directional;
			tVector2uc dir; //0=phi, 1=theta
			*/

    		m_pLightVols[i].ambient.x = CFile.ReadUByte(iS, offset + 0*1);
    		m_pLightVols[i].ambient.y = CFile.ReadUByte(iS, offset + 1*1);
    		m_pLightVols[i].ambient.z = CFile.ReadUByte(iS, offset + 2*1);
    		m_pLightVols[i].directional.x = CFile.ReadUByte(iS, offset + 3*1 + 0*1);
    		m_pLightVols[i].directional.y = CFile.ReadUByte(iS, offset + 3*1 + 1*1);
    		m_pLightVols[i].directional.z = CFile.ReadUByte(iS, offset + 3*1 + 2*1);
    		m_pLightVols[i].dir.x = CFile.ReadUByte(iS, offset + 3*1 + 3*1 + 0*1);
    		m_pLightVols[i].dir.y = CFile.ReadUByte(iS, offset + 3*1 + 3*1 + 1*1);
    		
    		offset += (1*3 + 1*3 + 1*2);
    	}
        
        num_lightvols.x = (long) (Math.floor(m_bbox.max.x/m_gridSize.x) - Math.ceil(m_bbox.min.x/m_gridSize.x) + 1);
    	num_lightvols.y = (long) (Math.floor(m_bbox.max.y/m_gridSize.y) - Math.ceil(m_bbox.min.y/m_gridSize.y) + 1);
    	num_lightvols.z = (long) (Math.floor(m_bbox.max.z/m_gridSize.z) - Math.ceil(m_bbox.min.z/m_gridSize.z) + 1);
        
    	//fseek(fp, lumps[kEntities].offset, SEEK_SET);
    	offset = lumps[kEntities].offset;
    	
    	byte entb[] = CFile.ReadBytes(iS, offset, lumps[kEntities].length);
    	//fread(entities, lumps[kEntities].length, sizeof(char), fp);
    	//fclose(fp);
    	
    	String entities = "";
    	for(i=0; i<lumps[kEntities].length; i++)
    	{
    		entities += (char)entb[i];
    	}
    	
    	try 
    	{
			iS.close();
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
        
    	ReadEntities(entities);
        
    	m_FacesDrawn.Resize(m_numOfFaces);
        
    	System.out.println(name + ".bsp");
        
    	return true;
    }
    
    void RenderLevel(CVector3 vPos)
    {
    	tBSPLeaf pLeaf;
    	int faceCount;
    	int faceIndex;
        
    	m_FacesDrawn.ClearAll();
        
    	int leafIndex = FindLeaf(vPos);
        
    	int cluster = m_pLeafs[leafIndex].cluster;
    	int i = m_numOfLeafs;
        
    	while(i > 0)
    	{
    		i--;
    		
    		pLeaf = m_pLeafs[i];
            
    		if(!IsClusterVisible(cluster, pLeaf.cluster))
    			continue;
            
    		if(mActivity.mFrustum.BoxInFrustum((float)pLeaf.min.x, (float)pLeaf.min.y, (float)pLeaf.min.z,
    		  	 				       (float)pLeaf.max.x, (float)pLeaf.max.y, (float)pLeaf.max.z))
    			continue;
            
    		faceCount = pLeaf.numOfLeafFaces;
            
    		while(faceCount > 0)
    		{
    			faceCount--;
    			
    			faceIndex = m_pLeafFaces[pLeaf.leafface + faceCount];
                
    			if(m_pFaces[faceIndex].type != FACE_POLYGON) continue;
                
    			if(m_sky[m_pFaces[faceIndex].textureID]) continue;
    			if(m_transparent[m_pFaces[faceIndex].textureID]) continue;
    			if(m_brokenFace[faceIndex]) continue;
                
    			if(m_FacesDrawn.On(faceIndex) >= 1)
    				continue;
                
    			m_FacesDrawn.Set(faceIndex);
    			RenderFace(faceIndex);
    		}
        }
        
        /*
    	glClientActiveTextureARB(GL_TEXTURE1_ARB);
    	glActiveTextureARB(GL_TEXTURE1_ARB);
    	glDisable(GL_TEXTURE_2D);
    	glClientActiveTextureARB(GL_TEXTURE0_ARB);
    	glActiveTextureARB(GL_TEXTURE0_ARB);*/
    }
    
    void SwitchSortFaces(int i, int j)
    {
    	CSortFace temp = new CSortFace();
    	CSortFace isf = m_sortFaces.get(i);
    	CSortFace jsf = m_sortFaces.get(j);
    	
    	temp.dist = isf.dist;
    	temp.faceIndex = isf.faceIndex;
    	temp.pos = Math3D.Copy(isf.pos);
    	
    	isf.dist = jsf.dist;
    	isf.faceIndex = jsf.faceIndex;
    	isf.pos = Math3D.Copy(jsf.pos);
    	
    	jsf.dist = temp.dist;
    	jsf.faceIndex = temp.faceIndex;
    	jsf.pos = Math3D.Copy(temp.pos);
    }
    
    void SortFaces(CVector3 vPos)
    {
    	tBSPLeaf pLeaf;
    	int faceCount;
    	int faceIndex;
        
    	int leafIndex = FindLeaf(vPos);
        
    	int cluster = m_pLeafs[leafIndex].cluster;
    	int i = m_numOfLeafs;
        
    	while(i > 0)
    	{
    		i--;
    		
    		pLeaf = m_pLeafs[i];
            
    		if(!IsClusterVisible(cluster, pLeaf.cluster))
    			continue;
            
    		if(!mActivity.mFrustum.BoxInFrustum((float)pLeaf.min.x, (float)pLeaf.min.y, (float)pLeaf.min.z,
    		  	 				       (float)pLeaf.max.x, (float)pLeaf.max.y, (float)pLeaf.max.z))
    			continue;
            
    		faceCount = pLeaf.numOfLeafFaces;
            
    		while(faceCount > 0)
    		{
    			faceCount--;
    			
    			faceIndex = m_pLeafFaces[pLeaf.leafface + faceCount];
                
    			if(m_pFaces[faceIndex].type != FACE_POLYGON) continue;
                
    			if(!m_transparent[m_pFaces[faceIndex].textureID]) continue;
                
    			//if(m_FacesDrawn.On(faceIndex))
    			//	continue;
                
    			m_FacesDrawn.Set(faceIndex);
    		}
    	}
        
    	CSortFace sf;
    	for(i=0; i<m_sortFaces.size(); i++)
    	{
    		sf = m_sortFaces.get(i);
    		faceIndex = sf.faceIndex;
            
    		if(m_FacesDrawn.On(faceIndex) < 1)
    			continue;
            
    		sf.dist = Math3D.Magnitude2(Math3D.Subtract(sf.pos, vPos));
    	}
        
    	CSortFace temp;
    	int leftoff = 0;
    	boolean backtracking = false;
        
    	for(i=1; i<m_sortFaces.size(); i++)
    	{
    		sf = m_sortFaces.get(i);
    		faceIndex = sf.faceIndex;
            
    		//if(!m_FacesDrawn.On(faceIndex))
    		//	continue;
            
    		if(i > 0)
    		{
    			if(sf.dist > sf.dist)
    			{
    				if(!backtracking)
    				{
    					leftoff = i;
    					backtracking = true;
    				}
    				//temp = m_sortFaces[i];
    				//m_sortFaces[i] = m_sortFaces[i-1];
    				//m_sortFaces[i-1] = temp;
    				SwitchSortFaces(i, i-1);
    				i-=2;
    			}
    			else
    			{
    				if(backtracking)
    				{
    					backtracking = false;
    					i = leftoff;
    				}
    			}
    		}
    		else
    			backtracking = false;
    	}
    }

    void RenderLevel2(CVector3 vPos)
    {
    	//glFrontFace(GL_CW);
    	int faceIndex;
        
        /*
        char msg[128];
        sprintf(msg, "sort faces %d", (int)m_sortFaces.size());
        Chat(msg);*/
        
    	CSortFace sf;
    	//for(int i=m_sortFaces.size()-1; i>=0; i--)
    	for(int i=0; i<m_sortFaces.size(); i++)
    	{
    		sf = m_sortFaces.get(i);
    		faceIndex = sf.faceIndex;
            
    		if(m_brokenFace[faceIndex]) continue;
            
    		if(m_FacesDrawn.On(faceIndex) < 1)
    			continue;
            
            RenderFace(faceIndex);
    	}
        
        /*
    	glClientActiveTextureARB(GL_TEXTURE1_ARB);
    	glActiveTextureARB(GL_TEXTURE1_ARB);
    	glDisable(GL_TEXTURE_2D);
    	glClientActiveTextureARB(GL_TEXTURE0_ARB);
    	glActiveTextureARB(GL_TEXTURE0_ARB);
    	glFrontFace(GL_CCW);*/
    }
    
    //void RenderSky();

    CVector3 TraceRay(CVector3 vStart, CVector3 vEnd)
    {
    	// We don't use this function, but we set it up to allow us to just check a
    	// ray with the BSP tree brushes.  We do so by setting the trace type to TYPE_RAY.
    	m_traceType = TYPE_RAY;
        
    	m_vStart = vStart;
    	m_vEnd = vEnd;
        
    	// Run the normal Trace() function with our start and end
    	// position and return a new position
    	return Trace(vStart, vEnd);
    }
    
    CVector3 TraceSphere(CVector3 vStart, CVector3 vEnd, float radius, float maxStep)
    {
    	m_traceType = TYPE_SPHERE;
    	m_bCollided = false;
        
    	m_vStart = vStart;
    	m_vEnd = vEnd;
        
    	m_bTryStep = false;
    	m_bGrounded = false;
        
    	m_traceRadius = radius;
        
    	CVector3 vNewPosition = Trace(vStart, vEnd);
        
    	if(m_bCollided && m_bTryStep)
    		vNewPosition = TryToStep(vNewPosition, vEnd, maxStep);
        
    	return vNewPosition;
    }
    
    CVector3 TraceBox(CVector3 vStart, CVector3 vEnd, CVector3 vMin, CVector3 vMax, float maxStep)
    {
    	m_traceType = TYPE_BOX;
    	m_vTraceMaxs = vMax;
    	m_vTraceMins = vMin;
    	m_bCollided = false;
        
    	m_vStart = vStart;
    	m_vEnd = vEnd;
        
    	m_bTryStep = false;
    	m_bGrounded = false;
    	m_bLadder = false;
        
    	// Grab the extend of our box (the largest size for each x, y, z axis)
    	m_vExtents = new CVector3(-m_vTraceMins.x > m_vTraceMaxs.x ? -m_vTraceMins.x : m_vTraceMaxs.x,
    						  -m_vTraceMins.y > m_vTraceMaxs.y ? -m_vTraceMins.y : m_vTraceMaxs.y,
    						  -m_vTraceMins.z > m_vTraceMaxs.z ? -m_vTraceMins.z : m_vTraceMaxs.z);
        
    	CVector3 vNewPosition = Trace(vStart, vEnd);
        
    	if(m_bCollided && m_bTryStep)
    		vNewPosition = TryToStep(vNewPosition, vEnd, maxStep);
        
    	m_bStuck = false;
        
    	vNewPosition = Trace(vNewPosition, vNewPosition);
        
    	if(m_bStuck)
    		return vStart;
        
    	return vNewPosition;
    }
    
	boolean BreakFaces(CVector3 vStart, CVector3 vEnd)
	{
		m_traceType = TYPE_RAY;
		m_bBroke = false;
	    
		m_vStart = vStart;
		m_vEnd = vEnd;
	    
		Break(vStart, vEnd);
	    
		int leafIndex = FindLeaf(vStart);
	    
		int cluster = m_pLeafs[leafIndex].cluster;
		int i = m_numOfLeafs;
		int faceCount;
		int faceIndex;
		tBSPLeaf pLeaf;
	    
		while(i > 0)
		{
			i--;
			pLeaf = m_pLeafs[i];
	        
			if(!IsClusterVisible(cluster, pLeaf.cluster))
				continue;
	        
			if(!mActivity.mFrustum.BoxInFrustum((float)pLeaf.min.x, (float)pLeaf.min.y, (float)pLeaf.min.z,
			  	 				       (float)pLeaf.max.x, (float)pLeaf.max.y, (float)pLeaf.max.z))
				continue;
	        
			faceCount = pLeaf.numOfLeafFaces;
	        
			while(faceCount > 0)
			{
				faceCount--;
				
				faceIndex = m_pLeafFaces[pLeaf.leafface + faceCount];
	            
				if(m_pFaces[faceIndex].type != FACE_POLYGON) continue;
	            
				if(!m_breakable[m_pFaces[faceIndex].textureID]) continue;
				if(m_brokenFace[faceIndex]) continue;
	            
				BreakFace(faceIndex, vStart, vEnd);
			}
		}
	    
		return m_bBroke;
	}

    boolean IsOnGround()   {   return m_bGrounded; }
    boolean Collided()     {   return m_bCollided; }
    boolean Stuck()        {   return m_bStuck;    }
    boolean Ladder()		{   return m_bLadder;	}
	CVector3 CollisionNormal()		{ return m_vCollisionNormal;		}

    void Destroy(boolean delTex)
    {
    	IntBuffer ib = IntBuffer.allocate(m_numOfFaces);
    	ib.put(m_pVertexBuffers);
        GLES20.glDeleteBuffers(m_numOfFaces, ib);
        ib = IntBuffer.allocate(m_numOfFaces);
    	ib.put(m_pIndexBuffers);
        GLES20.glDeleteBuffers(m_numOfFaces, ib);
        m_numOfFaces = 0;
        
        if(m_pVertexBuffers != null)
        {
            m_pVertexBuffers = null;
        }
        
        if(m_pIndexBuffers != null)
        {
            m_pIndexBuffers = null;
        }
        
    	if(m_brokenFace != null)
    	{
    		m_brokenFace = null;
    	}
        
    	if(m_brokenBrush != null)
    	{
    		m_brokenBrush = null;
    	}
        
    	if(m_pVerts != null)
    	{
    		m_pVerts = null;
    	}
        
    	if(m_pFaces != null)
    	{
    		m_pFaces = null;
    	}
        
    	if(m_pIndices != null)
    	{
    		m_pIndices = null;
    	}
        
    	if(m_pNodes != null)
    	{
    		m_pNodes = null;
    	}
        
    	if(m_pLeafs != null)
    	{
    		m_pLeafs = null;
    	}
        
    	if(m_pLeafFaces != null)
    	{
    		m_pLeafFaces = null;
    	}
        
    	if(m_pPlanes != null)
    	{
    		m_pPlanes = null;
    	}
        
    	if(m_clusters.pBitsets != null)
    	{
    		m_clusters.pBitsets = null;
    	}
        
    	if(m_pBrushes != null)
    	{
    		m_pBrushes = null;
    	}
        
    	if(m_pBrushSides != null)
    	{
    		m_pBrushSides = null;
    	}
        
    	if(m_pLeafBrushes != null)
    	{
    		m_pLeafBrushes = null;
    	}
        
        if(delTex && m_pTextures != null)
    	{
    		//glDeleteTextures(m_numOfTextures, m_textures);
            
    		for(int i=0; i<m_numOfTextures; i++)
    		{
    			mActivity.FreeTexture(m_pTextures[i].strName);
    		}
    	}
        
    	if(m_pTextures != null)
    	{
    		m_pTextures = null;
    	}
        
    	if(m_pLightmaps != null)
    	{
    		m_pLightmaps = null;
    	}
        
    	if(m_pModels != null)
    	{
    		m_pModels = null;
    	}
        
    	if(m_pLightVols != null)
    	{
    		m_pLightVols = null;
    	}
        
    	m_sortFaces.clear();
        
    	//glDeleteTextures(m_numOfTextures, m_textures);
        if(m_numOfLightmaps > 0)
        {
        	ib = IntBuffer.allocate(m_numOfLightmaps);
        	ib.put(m_lightmaps);
            GLES20.glDeleteTextures(m_numOfLightmaps, ib);
            //m_numOfTextures = 0;
            m_numOfLightmaps = 0;
        }
        
        if(m_textures != null)
    	{
    		m_textures = null;
    	}
        
    	if(m_lightmaps != null)
    	{
    		m_lightmaps = null;
    	}
        
    	if(m_passable != null)
    	{
    		m_passable = null;
    	}
        
    	if(m_sky != null)
    	{
    		m_sky = null;
    	}
        
    	if(m_transparent != null)
    	{
    		m_transparent = null;
    	}
        
    	if(m_water != null)
    	{
    		m_water = null;
    	}
        
    	if(m_breakable != null)
    	{
    		m_breakable = null;
    	}
        
    	if(m_ladder != null)
    	{
    		m_ladder = null;
    	}
        
    	if(m_grate != null)
    	{
    		m_grate = null;
    	}
    	
    	System.gc();
    }
    
    boolean IsClusterVisible(int current, int test)
    {
    	// Make sure we have valid memory and that the current cluster is > 0.
    	// If we don't have any memory or a negative cluster, return a visibility (1).
    	if(m_clusters.pBitsets == null || current < 0) return true;
        
    	// Use binary math to get the 8 bit visibility set for the current cluster
    	byte visSet = m_clusters.pBitsets[(current*m_clusters.bytesPerCluster) + (test / 8)];
        
    	// Now that we have our vector (bitset), do some bit shifting to find if
    	// the "test" cluster is visible from the "current" cluster, according to the bitset.
    	int result = visSet & (1 << ((test) & 7));
        
    	// Return the result ( either 1 (visible) or 0 (not visible) )
    	return result == 1;
    }
    
	int FindLeaf(CVector3 vPos)
	{
		int i = 0;
		float distance = 0.0f;
	    
		// Continue looping until we find a negative index
		while(i >= 0)
		{
			// Get the current node, then find the slitter plane from that
			// node's plane index.  Notice that we use a constant reference
			// to store the plane and node so we get some optimization.
			tBSPNode  node = m_pNodes[i];
			tBSPPlane plane = m_pPlanes[node.plane];
	        
			// Use the Plane Equation (Ax + by + Cz + D = 0) to find if the
			// camera is in front of or behind the current splitter plane.
	        distance =	plane.vNormal.x * vPos.x +
	        plane.vNormal.y * vPos.y +
	        plane.vNormal.z * vPos.z - plane.d;
	        
			// If the camera is in front of the plane
	        if(distance >= 0)
			{
				// Assign the current node to the node in front of itself
	            i = node.front;
	        }
			// Else if the camera is behind the plane
	        else
			{
				// Assign the current node to the node behind itself
	            i = node.back;
	        }
	    }
	    
		// Return the leaf index (same thing as saying:  return -(i + 1)).
	    return ~i;  // Binary operation
	}
	
	int FindCluster(CVector3 vPos)
	{
		int leaf = FindLeaf(vPos);
		return m_pLeafs[leaf].cluster;
	}
	
	CVector3 trilinear(float tx, float ty, float tz, CVector3 p[])
	{
		float tx2=1.0f-tx;
		float ty2=1.0f-ty;
		float tz2=1.0f-tz;
	    
		CVector3 np;
		np = Math3D.Add(
				Math3D.Add(
						Math3D.Add(
								Math3D.Multiply(p[0], (tx2*ty2*tz2)), 
								Math3D.Multiply(p[1], (tx*ty2*tz2))), 
						Math3D.Add(
								Math3D.Multiply(p[2], (tx2*ty2*tz)), 
								Math3D.Multiply(p[3], (tx*ty2*tz)))),
				Math3D.Add(
						Math3D.Add(
								Math3D.Multiply(p[4], (tx2*tz2*ty)), 
								Math3D.Multiply(p[5], (tx*ty*tz2))), 
						Math3D.Add(
								Math3D.Multiply(p[6], (tx2*ty*tz)), 
								Math3D.Multiply(p[7], (tx*ty*tz))))
				);
		return np;
	}

	CVector3 trilinear2(float tx, float ty, float tz, CVector3 p0, CVector3 p1, CVector3 p2, CVector3 p3, CVector3 p4, CVector3 p5, CVector3 p6, CVector3 p7)
	{
		float tx2=1.0f-tx;
		float ty2=1.0f-ty;
		float tz2=1.0f-tz;
	    
		CVector3 np;
		np = Math3D.Add(
				Math3D.Add(
						Math3D.Add(
								Math3D.Multiply(p0, (tx2*ty2*tz2)), 
								Math3D.Multiply(p1, (tx*ty2*tz2))), 
						Math3D.Add(
								Math3D.Multiply(p2, (tx2*ty2*tz)), 
								Math3D.Multiply(p3, (tx*ty2*tz)))),
				Math3D.Add(
						Math3D.Add(
								Math3D.Multiply(p4, (tx2*tz2*ty)), 
								Math3D.Multiply(p5, (tx*ty*tz2))), 
						Math3D.Add(
								Math3D.Multiply(p6, (tx2*ty*tz)), 
								Math3D.Multiply(p7, (tx*ty*tz))))
				);
		return np;
	}

	CVector3 LightVol(CVector3 vPos)
	{
		CVector3 color = new CVector3();
	    
		if(m_numOfLightVols <= 0)
		{
			color.x = 1;
			color.y = 1;
			color.z = 1;
	        
			return color;
		}
	    
		vPos.x = vPos.x - m_bbox.min.x;
		vPos.y = vPos.y - m_bbox.min.y;
		vPos.z = vPos.z - m_bbox.min.z;
	    
		float px = vPos.x / m_gridSize.x;
		float py = vPos.y / m_gridSize.y;
		float pz = vPos.z / m_gridSize.z;
	    
		long lx = (long)px;
		//unsigned int lx = num_lightvols.x - (unsigned int)px;
		//unsigned int ly = (unsigned int)(py-1);
		long ly = (long)(py);
		//unsigned int ly = num_lightvols.y - (unsigned int)(py);
		long lz = (long)(num_lightvols.z - pz);
		//unsigned int lz = (unsigned int)(num_lightvols.y - pz);
		//unsigned int lz = (unsigned int)pz;
	    
		//if(lx > num_lightvols.x - 2 || ly > num_lightvols.y - 2 || lz > num_lightvols.z - 2)
		if(lx > num_lightvols.x - 1 || ly > num_lightvols.y - 1 || lz > num_lightvols.z - 1
				|| lx < 0 || ly < 0 || lz < 0)
		{
			color.x = 0;
			color.y = 0;
			color.z = 0;
			return color;
		}
	    
		px = px - (float)Math.floor(px);
		py = py - (float)Math.floor(py);
		pz = pz - (float)Math.floor(pz);
	    
		int elem1 = (int)(ly*num_lightvols.z*num_lightvols.x + lz*num_lightvols.x + lx);
		int elem2 = (int)(elem1 - num_lightvols.x);
		int elem3 = (int)(elem2 + num_lightvols.x*num_lightvols.z);
		int elem4 = (int)(elem3 + num_lightvols.x);
	    
		CVector3 temp[] = new CVector3[8];
		temp[0] = Math3D.Add(m_pLightVols[elem1].ambient, m_pLightVols[elem1].directional);
		temp[1] = Math3D.Add(m_pLightVols[elem1+1].ambient, m_pLightVols[elem1+1].directional);
		temp[2] = Math3D.Add(m_pLightVols[elem2].ambient, m_pLightVols[elem2].directional);
		temp[3] = Math3D.Add(m_pLightVols[elem2+1].ambient, m_pLightVols[elem2+1].directional);
		temp[4] = Math3D.Add(m_pLightVols[elem4].ambient, m_pLightVols[elem4].directional);
		temp[5] = Math3D.Add(m_pLightVols[elem4+1].ambient, m_pLightVols[elem4+1].directional);
		temp[6] = Math3D.Add(m_pLightVols[elem3].ambient, m_pLightVols[elem3].directional);
		temp[7] = Math3D.Add(m_pLightVols[elem3+1].ambient, m_pLightVols[elem3+1].directional);
		color = Math3D.Divide(Math3D.VMin(255, trilinear(px, py, pz, temp)), 255.0f);
	    
		return color;
	}

    void ChangeGamma(byte pImage[], int size, float factor)
    {
    	for(int i = 0, offset = 0; i < size / 3; i++, offset += 3)
    	{
    		float scale = 1.0f, temp = 0.0f;
    		float r = 0, g = 0, b = 0;
            
    		r = (float)pImage[offset + 0];
    		g = (float)pImage[offset + 1];
    		b = (float)pImage[offset + 2];
            
    		r = r * factor / 255.0f;
    		g = g * factor / 255.0f;
    		b = b * factor / 255.0f;
            
    		if(r > 1.0f && (temp = (1.0f/r)) < scale) scale=temp;
    		if(g > 1.0f && (temp = (1.0f/g)) < scale) scale=temp;
    		if(b > 1.0f && (temp = (1.0f/b)) < scale) scale=temp;
            
    		scale*=255.0f;
    		r*=scale;	g*=scale;	b*=scale;
            
    		pImage[offset + 0] = (byte)r;
    		pImage[offset + 1] = (byte)g;
    		pImage[offset + 2] = (byte)b;
    	}
    }
    
    int CreateLightmapTexture(byte pImageBits[], int width, int height)
    {
        int texture[] = new int[1];
    	GLES20.glGenTextures(1, texture, 0);
        
    	GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1);
        
    	GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        
    	ChangeGamma(pImageBits, width*height*3, 10);
    	
    	ByteBuffer buffer = ByteBuffer.allocateDirect(width*height*3);
    	buffer.put(pImageBits);
        
    	GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGB, 128, 128, 0, GLES20.GL_RGB, GLES20.GL_UNSIGNED_BYTE, buffer);
    	//gluBuild2DMipmaps(GL_TEXTURE_2D, 3, width, height, GL_RGB, GL_UNSIGNED_BYTE, pImageBits);
        
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
    	GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
    	//GLES20.glTexEnvf(GLES20.GL_TEXTURE_ENV, GLES20.GL_TEXTURE_ENV_MODE, GLES20.GL_MODULATE);
        
        return texture[0];
    }

    CVector3 TryToStep(CVector3 vStart, CVector3 vEnd, float maxStep)
    {
    	// In this function we loop until we either found a reasonable height
    	// that we can step over, or find out that we can't step over anything.
    	// We check 10 times, each time increasing the step size to check for
    	// a collision.  If we don't collide, then we climb over the step.
        
    	// Go through and check different heights to step up
    	for(float height = 1.0f; height <= maxStep; height++)
            //float height = 15;
    	{
    		// Reset our variables for each loop interation
    		m_bCollided = false;
    		m_bTryStep = false;
            
    		// Here we add the current height to our y position of a new start and end.
    		// If these 2 new start and end positions are okay, we can step up.
    		CVector3 vStepStart = new CVector3(vStart.x, vStart.y + height, vStart.z);
    		CVector3 vStepEnd   = new CVector3(vEnd.x, vStart.y + height, vEnd.z);
            
    		// Test to see if the new position we are trying to step collides or not
    		CVector3 vStepPosition = Trace(vStepStart, vStepEnd);
            
    		// If we didn't collide, we can step!
    		if(!m_bCollided)
    		{
    			// Here we get the current view, then increase the y value by the current height.
    			// This makes it so when we are walking up the stairs, our view follows our step
    			// height and doesn't sag down as we walk up the stairs.
    			//CVector3 vNewView = g_camera->View();
    			//g_camera->SetView(CVector3(vNewView.x, vNewView.y + height, vNewView.z));
                
    			// Return the current position since we stepped up somewhere
    			return vStepPosition;
    		}
    	}
        
    	// If we can't step, then we just return the original position of the collision
    	return vStart;
    }

    CVector3 Trace(CVector3 vStart, CVector3 vEnd)
    {
    	// Initially we set our trace ratio to 1.0f, which means that we don't have
    	// a collision or intersection point, so we can move freely.
    	m_traceRatio = 1.0f;
        
    	// We start out with the first node (0), setting our start and end ratio to 0 and 1.
    	// We will recursively go through all of the nodes to see which brushes we should check.
        CheckNode(0, vStart, vEnd);
        
    	// If the traceRatio is STILL 1.0f, then we never collided and just return our end position
    	if(m_traceRatio == 1.0f)
    	{
    		return vEnd;
    	}
    	else	// Else COLLISION!!!!
    	{
    		// Set our new position to a position that is right up to the brush we collided with
    		CVector3 vNewPosition = Math3D.Add(vStart, Math3D.Multiply(Math3D.Subtract(vEnd, vStart), m_traceRatio));
            
            if(m_traceType == TYPE_RAY)
                return vNewPosition;
            
    		// Get the distance from the end point to the new position we just got
    		CVector3 vMove = Math3D.Subtract(vEnd, vNewPosition);
            
    		// Get the distance we need to travel backwards to the new slide position.
    		// This is the distance of course along the normal of the plane we collided with.
    		float distance = Math3D.Dot(vMove, m_vCollisionNormal);
            
    		// Get the new end position that we will end up (the slide position).
    		CVector3 vEndPosition = Math3D.Subtract(vEnd, Math3D.Multiply(m_vCollisionNormal, distance));
            
    		// Since we got a new position for our sliding vector, we need to check
    		// to make sure that new sliding position doesn't collide with anything else.
    		vNewPosition = Trace(vNewPosition, vEndPosition);
            
    		if(m_vCollisionNormal.y > 0.2f || m_bGrounded)
    			m_bGrounded = true;
    		else
    			m_bGrounded = false;
            
    		// Return the new position to be used by our camera (or player)
    		return vNewPosition;
    	}
    }
    
	void Break(CVector3 vStart, CVector3 vEnd)
	{
		// Initially we set our trace ratio to 1.0f, which means that we don't have
		// a collision or intersection point, so we can move freely.
		m_traceRatio = 1.0f;
	    
		// We start out with the first node (0), setting our start and end ratio to 0 and 1.
		// We will recursively go through all of the nodes to see which brushes we should check.
		BreakNode(0, vStart, vEnd);
	}
	
	void BreakFace(int faceIndex, CVector3 vStart, CVector3 vEnd)
	{
		int vIndex;
		tBSPFace pFace = m_pFaces[faceIndex];
		CVector3 vPoly[];
		vPoly = new CVector3[pFace.numOfIndices];
	    
		for(int i=0; i<pFace.numOfIndices; i++)
		{
			vIndex = m_pIndices[pFace.startIndex+i] + pFace.startVertIndex;
			vPoly[i] = m_pVerts[vIndex].vPosition;
		}
	    
		CVector3 vLine[] = new CVector3[2];
		vLine[0] = Math3D.Copy(vStart);
		vLine[1] = Math3D.Copy(vEnd);
	    
		if(Math3D.IntersectedPolygon(vPoly, vLine, pFace.numOfIndices, null))
		{
			m_bBroke = true;
			m_brokenFace[faceIndex] = true;
		}
	    
		//delete [] vPoly;
	}

    void CheckNode(int nodeIndex, CVector3 vStart, CVector3 vEnd)
    {
    	int brushIndex;
        
    	// Check if the next node is a leaf
    	if(nodeIndex < 0)
    	{
    		// If this node in the BSP is a leaf, we need to negate and add 1 to offset
    		// the real node index into the m_pLeafs[] array.  You could also do [~nodeIndex].
    		tBSPLeaf pLeaf = m_pLeafs[-(nodeIndex + 1)];
            
    		// We have a leaf, so let's go through all of the brushes for that leaf
    		for(int i = 0; i < pLeaf.numOfLeafBrushes; i++)
    		{
                brushIndex = m_pLeafBrushes[pLeaf.leafBrush + i];
                
    			// Get the current brush that we going to check
    			tBSPBrush pBrush = m_pBrushes[brushIndex];
                
    			if(m_passable[pBrush.textureID])
    				continue;
                
    			if(m_traceType == TYPE_RAY && m_grate[pBrush.textureID])
    				continue;
                
    			if(m_brokenBrush[brushIndex])
    				continue;
                
    			// Check if we have brush sides and the current brush is solid and collidable
    			if((pBrush.numOfBrushSides > 0) && ((m_pTextures[pBrush.textureID].textureType & 1) >= 1))
    			{
    				// Now we delve into the dark depths of the real calculations for collision.
    				// We can now check the movement vector against our brush planes.
    				CheckBrush(pBrush, vStart, vEnd);
    			}
    		}
            
    		// Since we found the brushes, we can go back up and stop recursing at this level
    		return;
    	}
        
    	// Grad the next node to work with and grab this node's plane data
    	tBSPNode pNode = m_pNodes[nodeIndex];
    	tBSPPlane pPlane = m_pPlanes[pNode.plane];
        
    	// Here we use the plane equation to find out where our initial start position is
    	// according the the node that we are checking.  We then grab the same info for the end pos.
    	float startDistance = Math3D.Dot(vStart, pPlane.vNormal) - pPlane.d;
    	float endDistance = Math3D.Dot(vEnd, pPlane.vNormal) - pPlane.d;
    	float offset = 0.0f;
        
    	// If we are doing sphere collision, include an offset for our collision tests below
    	if(m_traceType == TYPE_SPHERE)
    		offset = m_traceRadius;
        
    	// Here we check to see if we are working with a BOX or not
    	else if(m_traceType == TYPE_BOX)
    	{
    		// Get the distance our AABB is from the current splitter plane
    		offset = (float)(Math.abs( m_vExtents.x * pPlane.vNormal.x ) +
    						 Math.abs( m_vExtents.y * pPlane.vNormal.y ) +
                             Math.abs( m_vExtents.z * pPlane.vNormal.z ) );
    	}
        
    	// Here we check to see if the start and end point are both in front of the current node.
    	// If so, we want to check all of the nodes in front of this current splitter plane.
    	if(startDistance >= offset && endDistance >= offset)
    	{
    		// Traverse the BSP tree on all the nodes in front of this current splitter plane
    		CheckNode(pNode.front, vStart, vEnd);
    	}
    	// If both points are behind the current splitter plane, traverse down the back nodes
    	else if(startDistance < -offset && endDistance < -offset)
    	{
    		// Traverse the BSP tree on all the nodes in back of this current splitter plane
    		CheckNode(pNode.back, vStart, vEnd);
    	}
    	else
    	{
    		// If we get here, then our ray needs to be split in half to check the nodes
    		// on both sides of the current splitter plane.  Thus we create 2 ratios.
    		float Ratio1 = 1.0f, Ratio2 = 0.0f;	//, middleRatio = 0.0f;
    		CVector3 vMiddle;	// This stores the middle point for our split ray
            
    		// Start of the side as the front side to check
    		int side = pNode.front;
            
    		// Here we check to see if the start point is in back of the plane (negative)
    		if(startDistance < endDistance)
    		{
    			// Since the start position is in back, let's check the back nodes
    			side = pNode.back;
                
    			// Here we create 2 ratios that hold a distance from the start to the
    			// extent closest to the start (take into account a sphere and epsilon).
    			float inverseDistance = 1.0f / (startDistance - endDistance);
    			Ratio1 = (startDistance - offset - EPSILON) * inverseDistance;
    			Ratio2 = (startDistance + offset + EPSILON) * inverseDistance;
    		}
    		// Check if the starting point is greater than the end point (positive)
    		else if(startDistance > endDistance)
    		{
    			// This means that we are going to recurse down the front nodes first.
    			// We do the same thing as above and get 2 ratios for split ray.
    			float inverseDistance = 1.0f / (startDistance - endDistance);
    			Ratio1 = (startDistance + offset + EPSILON) * inverseDistance;
    			Ratio2 = (startDistance - offset - EPSILON) * inverseDistance;
    		}
            
    		// Make sure that we have valid numbers and not some weird float problems.
    		// This ensures that we have a value from 0 to 1 as a good ratio should be :)
    		if (Ratio1 < 0.0f) Ratio1 = 0.0f;
            else if (Ratio1 > 1.0f) Ratio1 = 1.0f;
            
            if (Ratio2 < 0.0f) Ratio2 = 0.0f;
            else if (Ratio2 > 1.0f) Ratio2 = 1.0f;
            
    		// Just like we do in the Trace() function, we find the desired middle
    		// point on the ray, but instead of a point we get a middleRatio percentage.
    		//middleRatio = startRatio + ((endRatio - startRatio) * Ratio1);
    		vMiddle = Math3D.Add(vStart, Math3D.Multiply(Math3D.Subtract(vEnd, vStart), Ratio1));
            
    		// Now we recurse on the current side with only the first half of the ray
    		CheckNode(side, vStart, vMiddle);
            
    		// Now we need to make a middle point and ratio for the other side of the node
    		//middleRatio = startRatio + ((endRatio - startRatio) * Ratio2);
    		vMiddle = Math3D.Add(vStart, Math3D.Multiply(Math3D.Subtract(vEnd, vStart), Ratio2));
            
    		// Depending on which side should go last, traverse the bsp with the
    		// other side of the split ray (movement vector).
    		if(side == pNode.back)
    			CheckNode(pNode.front, vMiddle, vEnd);
    		else
    			CheckNode(pNode.back, vMiddle, vEnd);
    	}
    }
    
    void CheckBrush(tBSPBrush pBrush, CVector3 vStart, CVector3 vEnd)
    {
    	float startRatio = -1.0f;		// Like in BrushCollision.htm, start a ratio at -1
        float endRatio = 1.0f;			// Set the end ratio to 1
        boolean startsOut = false;			// This tells us if we starting outside the brush
    	//CVector3 vCollisionNormal;
    	//tBSPPlane *pColPlane;
        
    	// Go through all of the brush sides and check collision against each plane
    	for(int i = 0; i < pBrush.numOfBrushSides; i++)
    	{
    		// Here we grab the current brush side and plane in this brush
    		tBSPBrushSide pBrushSide = m_pBrushSides[pBrush.brushSide + i];
    		tBSPPlane pPlane = m_pPlanes[pBrushSide.plane];
            
    		// Let's store a variable for the offset (like for sphere collision)
    		float offset = 0.0f;
            
    		// If we are testing sphere collision we need to add the sphere radius
    		if(m_traceType == TYPE_SPHERE)
    			offset = m_traceRadius;
            
    		// Test the start and end points against the current plane of the brush side.
    		// Notice that we add an offset to the distance from the origin, which makes
    		// our sphere collision work.
    		float startDistance = Math3D.Dot(m_vStart, pPlane.vNormal) - (pPlane.d + offset);
    		float endDistance = Math3D.Dot(m_vEnd, pPlane.vNormal) - (pPlane.d + offset);
            
    		// Store the offset that we will check against the plane
    		CVector3 vOffset = new CVector3(0, 0, 0);
            
    		// If we are using AABB collision
    		if(m_traceType == TYPE_BOX)
    		{
    			// Grab the closest corner (x, y, or z value) that is closest to the plane
                vOffset.x = (pPlane.vNormal.x < 0)	? m_vTraceMaxs.x : m_vTraceMins.x;
    			vOffset.y = (pPlane.vNormal.y < 0)	? m_vTraceMaxs.y : m_vTraceMins.y;
    			vOffset.z = (pPlane.vNormal.z < 0)	? m_vTraceMaxs.z : m_vTraceMins.z;
                
    			// Use the plane equation to grab the distance our start position is from the plane.
                startDistance = Math3D.Dot(Math3D.Add(vStart, vOffset), pPlane.vNormal) - pPlane.d;
                
    			// Get the distance our end position is from this current brush plane
                endDistance   = Math3D.Dot(Math3D.Add(vEnd, vOffset), pPlane.vNormal) - pPlane.d;
            }
            
    		// Make sure we start outside of the brush's volume
    		if(startDistance > 0)	startsOut = true;
            
    		// Stop checking since both the start and end position are in front of the plane
    		if(startDistance > 0 && endDistance > 0)
    			return;
            
    		// Continue on to the next brush side if both points are behind or on the plane
    		if(startDistance <= 0 && endDistance <= 0)
    			continue;
            
    		// If the distance of the start point is greater than the end point, we have a collision!
    		if(startDistance > endDistance)
    		{
    			// This gets a ratio from our starting point to the approximate collision spot
    			float Ratio1 = (startDistance - EPSILON) / (startDistance - endDistance);
                
    			// If this is the first time coming here, then this will always be true,
    			if(Ratio1 > startRatio)
    			{
    				// Set the startRatio (currently the closest collision distance from start)
    				startRatio = Ratio1;
    				//m_bCollided = true;		// Let us know we collided!	// BUG FIX - Denis
                    
    				// Store the normal of plane that we collided with for sliding calculations
    				///vCollisionNormal = pPlane->vNormal;
    				//pColPlane = pPlane;
                    
    				m_vCollisionNormal = Math3D.Copy(pPlane.vNormal);
                    
    				// This checks first tests if we actually moved along the x or z-axis,
    				// meaning that we went in a direction somewhere.  The next check makes
    				// sure that we don't always check to step every time we collide.  If
    				// the normal of the plane has a Y value of 1, that means it's just the
    				// flat ground and we don't need to check if we can step over it, it's flat!
    				if((vStart.x != vEnd.x || vStart.z != vEnd.z) && pPlane.vNormal.y != 1 && pPlane.vNormal.y >= 0.0f)
    				{
    					// We can try and step over the wall we collided with
    					m_bTryStep = true;
    				}
                    
    				// Here we make sure that we don't slide slowly down walls when we
    				// jump and collide into them.  We only want to say that we are on
    				// the ground if we actually have stopped from falling.  A wall wouldn't
    				// have a high y value for the normal, it would most likely be 0.
    				if(m_vCollisionNormal.y >= 0.2f)
    					m_bGrounded = true;
    			}
    		}
    		else
    		{
    			// Get the ratio of the current brush side for the endRatio
    			float Ratio = (startDistance + EPSILON) / (startDistance - endDistance);
                
    			// If the ratio is less than the current endRatio, assign a new endRatio.
    			// This will usually always be true when starting out.
    			if(Ratio < endRatio)
    				endRatio = Ratio;
    		}
    	}
        
    	m_bCollided = true;	// BUG FIX - Denis
        
    	if(m_ladder[pBrush.textureID])
    		m_bLadder = true;
        
    	/*
         m_vCollisionNormal = vCollisionNormal;
         
         // This checks first tests if we actually moved along the x or z-axis,
         // meaning that we went in a direction somewhere.  The next check makes
         // sure that we don't always check to step every time we collide.  If
         // the normal of the plane has a Y value of 1, that means it's just the
         // flat ground and we don't need to check if we can step over it, it's flat!
         if((vStart.x != vEnd.x || vStart.z != vEnd.z) && pColPlane->vNormal.y != 1 && pColPlane->vNormal.y >= 0.0f)
         {
         // We can try and step over the wall we collided with
         m_bTryStep = true;
         }
         
         // Here we make sure that we don't slide slowly down walls when we
         // jump and collide into them.  We only want to say that we are on
         // the ground if we actually have stopped from falling.  A wall wouldn't
         // have a high y value for the normal, it would most likely be 0.
         if(m_vCollisionNormal.y >= 0.2f)
         m_bGrounded = true;
         */
        
    	// If we didn't start outside of the brush we don't want to count this collision - return;
    	if(startsOut == false)
    	{
    		m_bStuck = true;
    		//UnstuckBrush(pBrush, m_vTrace);
    		return;
    	}
        
    	// If our startRatio is less than the endRatio there was a collision!!!
    	if(startRatio < endRatio)
    	{
    		// Make sure the startRatio moved from the start and check if the collision
    		// ratio we just got is less than the current ratio stored in m_traceRatio.
    		// We want the closest collision to our original starting position.
    		if(startRatio > -1 && startRatio < m_traceRatio)
                //if(startRatio < m_traceRatio)
    		{
    			// If the startRatio is less than 0, just set it to 0
    			//if(startRatio < 0)
    			//	startRatio = 0;
                
    			// Store the new ratio in our member variable for later
    			m_traceRatio = startRatio;
    		}
    	}
    }
    
	void BreakNode(int nodeIndex, CVector3 vStart, CVector3 vEnd)
	{
		int brushIndex;
	    
		// Check if the next node is a leaf
		if(nodeIndex < 0)
		{
			// If this node in the BSP is a leaf, we need to negate and add 1 to offset
			// the real node index into the m_pLeafs[] array.  You could also do [~nodeIndex].
			tBSPLeaf pLeaf = m_pLeafs[-(nodeIndex + 1)];
	        
			// We have a leaf, so let's go through all of the brushes for that leaf
			for(int i = 0; i < pLeaf.numOfLeafBrushes; i++)
			{
				brushIndex = m_pLeafBrushes[pLeaf.leafBrush + i];
	            
				// Get the current brush that we going to check
				tBSPBrush pBrush = m_pBrushes[brushIndex];
	            
				if(!m_breakable[pBrush.textureID])
					continue;
	            
				if(m_brokenBrush[brushIndex])
					continue;
	            
				// Check if we have brush sides and the current brush is solid and collidable
				if((pBrush.numOfBrushSides > 0) && ((m_pTextures[pBrush.textureID].textureType & 1) >= 1))
				{
					// Now we delve into the dark depths of the real calculations for collision.
					// We can now check the movement vector against our brush planes.
					BreakBrush(brushIndex, pBrush, vStart, vEnd);
				}
			}
	        
			// Since we found the brushes, we can go back up and stop recursing at this level
			return;
		}
	    
		// Grad the next node to work with and grab this node's plane data
		tBSPNode pNode = m_pNodes[nodeIndex];
		tBSPPlane pPlane = m_pPlanes[pNode.plane];
	    
		// Here we use the plane equation to find out where our initial start position is
		// according the the node that we are checking.  We then grab the same info for the end pos.
		float startDistance = Math3D.Dot(vStart, pPlane.vNormal) - pPlane.d;
		float endDistance = Math3D.Dot(vEnd, pPlane.vNormal) - pPlane.d;
		float offset = 0.0f;
	    
		// If we are doing sphere collision, include an offset for our collision tests below
		if(m_traceType == TYPE_SPHERE)
			offset = m_traceRadius;
	    
		// Here we check to see if we are working with a BOX or not
		else if(m_traceType == TYPE_BOX)
		{
			// Get the distance our AABB is from the current splitter plane
			offset = (float)(Math.abs( m_vExtents.x * pPlane.vNormal.x ) +
							 Math.abs( m_vExtents.y * pPlane.vNormal.y ) +
	                         Math.abs( m_vExtents.z * pPlane.vNormal.z ) );
		}
	    
		// Here we check to see if the start and end point are both in front of the current node.
		// If so, we want to check all of the nodes in front of this current splitter plane.
		if(startDistance >= offset && endDistance >= offset)
		{
			// Traverse the BSP tree on all the nodes in front of this current splitter plane
			BreakNode(pNode.front, vStart, vEnd);
		}
		// If both points are behind the current splitter plane, traverse down the back nodes
		else if(startDistance < -offset && endDistance < -offset)
		{
			// Traverse the BSP tree on all the nodes in back of this current splitter plane
			BreakNode(pNode.back, vStart, vEnd);
		}
		else
		{
			// If we get here, then our ray needs to be split in half to check the nodes
			// on both sides of the current splitter plane.  Thus we create 2 ratios.
			float Ratio1 = 1.0f, Ratio2 = 0.0f;	//, middleRatio = 0.0f;
			CVector3 vMiddle;	// This stores the middle point for our split ray
	        
			// Start of the side as the front side to check
			int side = pNode.front;
	        
			// Here we check to see if the start point is in back of the plane (negative)
			if(startDistance < endDistance)
			{
				// Since the start position is in back, let's check the back nodes
				side = pNode.back;
	            
				// Here we create 2 ratios that hold a distance from the start to the
				// extent closest to the start (take into account a sphere and epsilon).
				float inverseDistance = 1.0f / (startDistance - endDistance);
				Ratio1 = (startDistance - offset - EPSILON) * inverseDistance;
				Ratio2 = (startDistance + offset + EPSILON) * inverseDistance;
			}
			// Check if the starting point is greater than the end point (positive)
			else if(startDistance > endDistance)
			{
				// This means that we are going to recurse down the front nodes first.
				// We do the same thing as above and get 2 ratios for split ray.
				float inverseDistance = 1.0f / (startDistance - endDistance);
				Ratio1 = (startDistance + offset + EPSILON) * inverseDistance;
				Ratio2 = (startDistance - offset - EPSILON) * inverseDistance;
			}
	        
			// Make sure that we have valid numbers and not some weird float problems.
			// This ensures that we have a value from 0 to 1 as a good ratio should be :)
			if (Ratio1 < 0.0f) Ratio1 = 0.0f;
	        else if (Ratio1 > 1.0f) Ratio1 = 1.0f;
	        
	        if (Ratio2 < 0.0f) Ratio2 = 0.0f;
	        else if (Ratio2 > 1.0f) Ratio2 = 1.0f;
	        
			// Just like we do in the Trace() function, we find the desired middle
			// point on the ray, but instead of a point we get a middleRatio percentage.
			//middleRatio = startRatio + ((endRatio - startRatio) * Ratio1);
			vMiddle = Math3D.Add(vStart, Math3D.Multiply(Math3D.Subtract(vEnd, vStart), Ratio1));
	        
			// Now we recurse on the current side with only the first half of the ray
			BreakNode(side, vStart, vMiddle);
	        
			// Now we need to make a middle point and ratio for the other side of the node
			//middleRatio = startRatio + ((endRatio - startRatio) * Ratio2);
			vMiddle = Math3D.Add(vStart, Math3D.Multiply(Math3D.Subtract(vEnd, vStart), Ratio2));
	        
			// Depending on which side should go last, traverse the bsp with the
			// other side of the split ray (movement vector).
			if(side == pNode.back)
				BreakNode(pNode.front, vMiddle, vEnd);
			else
				BreakNode(pNode.back, vMiddle, vEnd);
		}
	}
	
	void BreakBrush(int brushIndex, tBSPBrush pBrush, CVector3 vStart, CVector3 vEnd)
	{
		float startRatio = -1.0f;		// Like in BrushCollision.htm, start a ratio at -1
	    float endRatio = 1.0f;			// Set the end ratio to 1
	    boolean startsOut = false;			// This tells us if we starting outside the brush
		//CVector3 vCollisionNormal;
		//tBSPPlane *pColPlane;
	    
		// Go through all of the brush sides and check collision against each plane
		for(int i = 0; i < pBrush.numOfBrushSides; i++)
		{
			// Here we grab the current brush side and plane in this brush
			tBSPBrushSide pBrushSide = m_pBrushSides[pBrush.brushSide + i];
			tBSPPlane pPlane = m_pPlanes[pBrushSide.plane];
	        
			// Let's store a variable for the offset (like for sphere collision)
			float offset = 0.0f;
	        
			// If we are testing sphere collision we need to add the sphere radius
			if(m_traceType == TYPE_SPHERE)
				offset = m_traceRadius;
	        
			// Test the start and end points against the current plane of the brush side.
			// Notice that we add an offset to the distance from the origin, which makes
			// our sphere collision work.
			//float startDistance = Dot(vStart, pPlane->vNormal) - (pPlane->d + offset);
			//float endDistance = Dot(vEnd, pPlane->vNormal) - (pPlane->d + offset);
			float startDistance = Math3D.Dot(m_vStart, pPlane.vNormal) - (pPlane.d + offset);
			float endDistance = Math3D.Dot(m_vEnd, pPlane.vNormal) - (pPlane.d + offset);
	        
			// Store the offset that we will check against the plane
			CVector3 vOffset = new CVector3(0, 0, 0);
	        
			// If we are using AABB collision
			if(m_traceType == TYPE_BOX)
			{
				// Grab the closest corner (x, y, or z value) that is closest to the plane
	            vOffset.x = (pPlane.vNormal.x < 0)	? m_vTraceMaxs.x : m_vTraceMins.x;
				vOffset.y = (pPlane.vNormal.y < 0)	? m_vTraceMaxs.y : m_vTraceMins.y;
				vOffset.z = (pPlane.vNormal.z < 0)	? m_vTraceMaxs.z : m_vTraceMins.z;
	            
				// Use the plane equation to grab the distance our start position is from the plane.
	            //startDistance = Dot(vStart + vOffset, pPlane->vNormal) - pPlane->d;
	            startDistance = Math3D.Dot(Math3D.Add(vStart, vOffset), pPlane.vNormal) - pPlane.d;
	            
				// Get the distance our end position is from this current brush plane
	            //endDistance   = Dot(vEnd + vOffset, pPlane->vNormal) - pPlane->d;
	            endDistance   = Math3D.Dot(Math3D.Add(vEnd, vOffset), pPlane.vNormal) - pPlane.d;
	        }
	        
			// Make sure we start outside of the brush's volume
			if(startDistance > 0)	startsOut = true;
	        
			// Stop checking since both the start and end position are in front of the plane
			if(startDistance > 0 && endDistance > 0)
				return;
	        
			// Continue on to the next brush side if both points are behind or on the plane
			if(startDistance <= 0 && endDistance <= 0)
				continue;
	        
			// If the distance of the start point is greater than the end point, we have a collision!
			if(startDistance > endDistance)
			{
				// This gets a ratio from our starting point to the approximate collision spot
				float Ratio1 = (startDistance - EPSILON) / (startDistance - endDistance);
				//float Ratio1 = startDistance / (startDistance - endDistance);
	            
				// If this is the first time coming here, then this will always be true,
				if(Ratio1 > startRatio)
				{
					// Set the startRatio (currently the closest collision distance from start)
					startRatio = Ratio1;
				}
			}
			else
			{
				// Get the ratio of the current brush side for the endRatio
				float Ratio = (startDistance + EPSILON) / (startDistance - endDistance);
	            
				// If the ratio is less than the current endRatio, assign a new endRatio.
				// This will usually always be true when starting out.
				if(Ratio < endRatio)
					endRatio = Ratio;
			}
		}
	    
		m_bBroke = true;
		m_brokenBrush[brushIndex] = true;
	    
		/*
	     m_vCollisionNormal = vCollisionNormal;
	     
	     // This checks first tests if we actually moved along the x or z-axis,
	     // meaning that we went in a direction somewhere.  The next check makes
	     // sure that we don't always check to step every time we collide.  If
	     // the normal of the plane has a Y value of 1, that means it's just the
	     // flat ground and we don't need to check if we can step over it, it's flat!
	     if((vStart.x != vEnd.x || vStart.z != vEnd.z) && pColPlane->vNormal.y != 1 && pColPlane->vNormal.y >= 0.0f)
	     {
	     // We can try and step over the wall we collided with
	     m_bTryStep = true;
	     }
	     
	     // Here we make sure that we don't slide slowly down walls when we
	     // jump and collide into them.  We only want to say that we are on
	     // the ground if we actually have stopped from falling.  A wall wouldn't
	     // have a high y value for the normal, it would most likely be 0.
	     if(m_vCollisionNormal.y >= 0.2f)
	     m_bGrounded = true;
	     */
	    
		// If our startRatio is less than the endRatio there was a collision!!!
		if(startRatio < endRatio)
		{
			// Make sure the startRatio moved from the start and check if the collision
			// ratio we just got is less than the current ratio stored in m_traceRatio.
			// We want the closest collision to our original starting position.
			if(startRatio > -1 && startRatio < m_traceRatio)
	            //if(startRatio > -1 && startRatio < relativeRatio)
	            //if(startRatio < m_traceRatio)
			{
				// If the startRatio is less than 0, just set it to 0
				//if(startRatio < 0)
				//	startRatio = 0;
	            
				// Store the new ratio in our member variable for later
				m_traceRatio = startRatio;
			}
		}
	}

    void RenderFace(int faceIndex)
    {
    	tBSPFace pFace = m_pFaces[faceIndex];
        
    	CShader s = mActivity.mShader[CShader.MAP];
    	
    	/*
	    CVector3 vPosition;
	    CVector2 vTextureCoord;
	    CVector2 vLightmapCoord;
	    CVector3 vNormal;
	    byte color[4];
	    */
    	
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, m_pVertexBuffers[faceIndex]);
        //GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, sizeof(tBSPVertex), (void*)offsetof(tBSPVertex,vPosition));
        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, (3*4 + 2*4 + 2*4 + 3*4 + 4), 0);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_textures[pFace.textureID]);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);

        //GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, sizeof(tBSPVertex), (void*)offsetof(tBSPVertex,vTextureCoord));
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, (3*4 + 2*4 + 2*4 + 3*4 + 4), 3*4);
        
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, m_lightmaps[pFace.lightmapID]);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE2], 1);

        //GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD2], 2, GLES20.GL_FLOAT, false, sizeof(tBSPVertex), (void*)offsetof(tBSPVertex,vLightmapCoord));
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD2], 2, GLES20.GL_FLOAT, false, (3*4 + 2*4 + 2*4 + 3*4 + 4), 3*4 + 2*4);
        
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, m_pIndexBuffers[faceIndex]);
        //GLES20.glDrawElements(GLES20.GL_TRIANGLES, pFace.numOfIndices, GLES20.GL_UNSIGNED_INT, (void*)0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, pFace.numOfIndices, GLES20.GL_UNSIGNED_INT, 0);
    }
    
    //void RenderSkyFace(int faceIndex);
    
    CVector3 ParseCVector3(String str)
    {
		CVector3 pos = new CVector3();
		String values[] = str.split(" ");
		pos.x = Float.parseFloat(values[0]);
		pos.y = Float.parseFloat(values[1]);
		pos.z = Float.parseFloat(values[2]);
		return pos;
    }
    
    static void Swizzle(CVector3 v)
    {
    	float temp = v.z;
    	v.z = -v.y;
    	v.y = temp;
    }

    static void Swizzle(CVector3 vmin, CVector3 vmax)
    {
    	float temp = vmin.z;
    	vmin.z = -vmin.y;
    	vmin.y = temp;
        
    	temp = vmax.z;
    	vmax.z = -vmax.y;
    	vmax.y = temp;
        
    	float tempmin = Math.min(vmin.z, vmax.z);
    	float tempmax = Math.max(vmin.z, vmax.z);
    	vmin.z = tempmin;
    	vmax.z = tempmax;
    }
    
    void ReadEntity(String classname, String origin, String angle, String model, String size, String type, String sky, String count, String clip, String collider,
    		String opensound, String closesound, String activity, String nolightvol, String bbmin, String bbmax, String map, String script)
	{
		boolean nolvol = false;
		
		if(nolightvol.equalsIgnoreCase("true"))
			nolvol = true;
		
		int scrpt = -1;
		if(!script.equalsIgnoreCase(""))
			scrpt = Integer.parseInt(script);
		
		if(classname.equalsIgnoreCase("info_player_start"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			pos.y += 50.0f - 27.5f + 4.0f;
			float ang = Float.parseFloat(angle);
			CSpawn spawn = new CSpawn(pos, ang, -1, -1);
			mSSpawn.add(spawn);
		}
		if(classname.equalsIgnoreCase("info_player_deathmatch"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			pos.y += 50.0f - 27.5f + 4.0f;
			float ang;
			ang = Float.parseFloat(angle);
			CSpawn spawn = new CSpawn(pos, ang, -1, -1);
			mSpawn.add(spawn);
		}
		else if(classname.equalsIgnoreCase("info_player_zombie"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			pos.y += 50.0f - 27.5f + 4.0f;
			float ang;
			ang = Float.parseFloat(angle);
			//ang += DEGTORAD(180);
			int act = CPlayer.ONSIGHT;
		    
			if(activity.equalsIgnoreCase("none"))
				act = CPlayer.NOACT;
			else if(activity.equalsIgnoreCase("onsight"))
				act = CPlayer.ONSIGHT;
		    
			CSpawn spawn = new CSpawn(pos, ang, act, scrpt);
			mZSpawn.add(spawn);
		}
		
		else if(classname.equalsIgnoreCase("func_map"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
		    
			CVector3 vmin = ParseCVector3(bbmin);
			CVector3 vmax = ParseCVector3(bbmax);
			Swizzle(vmin, vmax);
		    
			CFuncMap funcmap = new CFuncMap(pos, vmin, vmax, map);
			mFuncMap.add(funcmap);
		}
		else if(classname.equalsIgnoreCase("func_proxy"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
		    
			CVector3 vmin = ParseCVector3(bbmin);
			CVector3 vmax = ParseCVector3(bbmax);
			Swizzle(vmin, vmax);
		    
			CFuncProxy funcproxy = new CFuncProxy(pos, vmin, vmax, scrpt);
			mFuncProxy.add(funcproxy);
		}
		else if(classname.equalsIgnoreCase("_entity"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			int eid = mActivity.EntityID(model);
		    
			if(eid < 0)
				eid = mActivity.Entity(CEntity.NOCAT, model, 1, -1);
		    
			float amt = 1;
		    
			mActivity.PlaceEntity(eid, -1, amt, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("weapon_mp5"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			float clp = 30;
			if(!clip.equalsIgnoreCase(""))
				clp = Float.parseFloat(clip);
		    
			int eid = mActivity.EntityID("mp5");
		    
			mActivity.PlaceEntity(eid, -1, 1, clp, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("weapon_mossberg500"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			float clp = 6;
			if(!clip.equalsIgnoreCase(""))
				clp = Float.parseFloat(clip);
		    
			int eid = mActivity.EntityID("mossberg500");
		    
			mActivity.PlaceEntity(eid, -1, 1, clp, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("weapon_m1911"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			float clp = 7;
			if(!clip.equalsIgnoreCase(""))
				clp = Float.parseFloat(clip);
		    
			int eid = mActivity.EntityID("m1911");
		    
			mActivity.PlaceEntity(eid, -1, 1, clp, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("weapon_bat"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
			float clp = 0;
		    
			int eid = mActivity.EntityID("bbat");
		    
			mActivity.PlaceEntity(eid, -1, 1, clp, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("weapon_knife"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
			float clp = 0;
		    
			int eid = mActivity.EntityID("knife");
		    
			mActivity.PlaceEntity(eid, -1, 1, clp, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("ammo_primary"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
			float amt = Float.parseFloat(count);
		    
			int eid = mActivity.EntityID("ammo1");
		    
			//if(eid < 0)
			//	eid = Entity(ENTITY::ITEM, "mp5", 1);
		    
			mActivity.PlaceEntity(eid, -1, amt, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("ammo_secondary"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
			float amt = Float.parseFloat(count);
		    
			int eid = mActivity.EntityID("ammo2");
		    
			//if(eid < 0)
			//	eid = Entity(ENTITY::ITEM, "mp5", 1);
		    
			mActivity.PlaceEntity(eid, -1, amt, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("ammo_tertiary"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
			float amt = Float.parseFloat(count);
		    
			int eid = mActivity.EntityID("ammo3");
		    
			//if(eid < 0)
			//	eid = Entity(ENTITY::ITEM, "mp5", 1);
		    
			mActivity.PlaceEntity(eid, -1, amt, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("fixed_entity"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			int eid = mActivity.EntityID(model);
		    
			if(eid < 0)
			{
				int coli = -1;
		        
				if(!collider.equalsIgnoreCase(""))
					coli = mActivity.LoadModel(collider, new CVector3(1,1,1));
		        
				eid = mActivity.Entity(CEntity.FIXEDENT, model, 1, coli);
			}
		    
			mActivity.PlaceEntity(eid, -1, -1, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("func_door"))
		{
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
			float ang = Float.parseFloat(angle);
		    
			int eid = mActivity.EntityID(model);
		    
			if(eid < 0)
				eid = mActivity.Entity(CEntity.DOOR, model, 1, -1);
		        //eid = Entity(ENTITY::FIXEDENT, model.c_str(), 1, -1);
		    
			mActivity.mLastEnt = eid;
		    
			if(!opensound.equalsIgnoreCase(""))
				mActivity.EntitySound(CEntity.OPENSND, opensound);
		    
			if(!closesound.equalsIgnoreCase(""))
				mActivity.EntitySound(CEntity.CLOSESND, closesound);
		    
			mActivity.PlaceEntity(eid, -1, -1, -1, pos, ang, null, nolvol, scrpt);
		}
		else if(classname.equalsIgnoreCase("_billboard"))
		{
		    //void PlaceBillboard(const char* name, CVector3 pos, float size)
			CVector3 pos = ParseCVector3(origin);
			Swizzle(pos);
		
		    float sizef = Float.parseFloat(size);
		    
		    mActivity.PlaceBillboard(type, pos, sizef, -1, nolvol);
		}
		else if(classname.equalsIgnoreCase("worldspawn"))
		{
			if(!sky.equalsIgnoreCase(""))
				LoadSkyBox(sky);
		}
	}
    
    void ReadEntities(String str)
    {
    	mSpawn = new Vector<CSpawn>();
    	mSSpawn = new Vector<CSpawn>();
    	mZSpawn = new Vector<CSpawn>();
    	mFuncMap = new Vector<CFuncMap>();
    	mFuncProxy = new Vector<CFuncProxy>();
        
    	String classname;
    	String origin;
    	String angle;
    	String model;
    	String size;
    	String type;
    	String sky;
    	String count;
    	String clip;
    	String collider;
    	String opensound;
    	String closesound;
    	String activity;
    	String nolightvol;
    	String bbmin;
    	String bbmax;
    	String map;
    	String script;
    	String var;
    	String val;
        
    	int len = str.length();
        
    	for(int i=0; i<len; i++)
    	{
    		classname = "";
    		angle = "";
    		origin = "";
    		model = "";
    		size = "";
    		type = "";
    		sky = "";
    		count = "";
    		clip = "";
    		collider = "";
    		opensound = "";
    		closesound = "";
    		activity = "";
    		nolightvol = "";
    		bbmin = "";
    		bbmax = "";
    		script = "";
    		map = "";
            
    		for(; i<len; i++)
    			if(str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n' && str.charAt(i) != '\r')
    				break;
            
    		for(; i<len; i++)
    			if(str.charAt(i) == '{')
    			{
    				i++;
    				break;
    			}
            
    		for(; i<len; i++)
    			if(str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n' && str.charAt(i) != '\r')
    				break;
            
    		while(str.charAt(i) != '}' && i < len)
    		{
    			var = "";
    			val = "";
                
    			for(; i<len; i++)
    				if(str.charAt(i) == '"')
    				{
    					i++;
    					break;
    				}
                
    			for(; i<len; i++)
    			{
    				if(str.charAt(i) == '"')
    				{
    					i++;
    					break;
    				}
                    
    				var = var + str.charAt(i);
    			}
                
    			for(; i<len; i++)
    				if(str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n' && str.charAt(i) != '\r')
    					break;
                
    			for(; i<len; i++)
    				if(str.charAt(i) == '"')
    				{
    					i++;
    					break;
    				}
                
    			for(; i<len; i++)
    			{
    				if(str.charAt(i) == '"')
    				{
    					i++;
    					break;
    				}
                    
    				val = val + str.charAt(i);
    			}
                
    			if(var.equalsIgnoreCase("classname"))
    				classname = val;
    			else if(var.equalsIgnoreCase("origin"))
    				origin = val;
    			else if(var.equalsIgnoreCase("angle"))
    				angle = val;
    			else if(var.equalsIgnoreCase("model"))
    				model = val;
    			else if(var.equalsIgnoreCase("size"))
    				size = val;
    			else if(var.equalsIgnoreCase("type"))
    				type = val;
    			else if(var.equalsIgnoreCase("sky"))
    				sky = val;
    			else if(var.equalsIgnoreCase("count"))
    				count = val;
    			else if(var.equalsIgnoreCase("clip"))
    				clip = val;
    			else if(var.equalsIgnoreCase("collider"))
    				collider = val;
    			else if(var.equalsIgnoreCase("opensound"))
    				opensound = val;
    			else if(var.equalsIgnoreCase("closesound"))
    				closesound = val;
    			else if(var.equalsIgnoreCase("activity"))
    				activity = val;
    			else if(var.equalsIgnoreCase("nolightvol"))
    				nolightvol = val;
    			else if(var.equalsIgnoreCase("min"))
    				bbmin = val;
    			else if(var.equalsIgnoreCase("max"))
    				bbmax = val;
    			else if(var.equalsIgnoreCase("map"))
    				map = val;
    			else if(var.equalsIgnoreCase("script"))
    				script = val;
                
    			for(; i<len; i++)
    				if(str.charAt(i) != ' ' && str.charAt(i) != '\t' && str.charAt(i) != '\n' && str.charAt(i) != '\r')
    					break;
    		}
            
    		ReadEntity(classname, origin, angle, model, size, type, sky, count, clip, collider, opensound, closesound, activity, nolightvol, bbmin, bbmax, map, script);
    	}
    }
    
    void LoadSkyBox(String basename)
    {
    	String front;
    	String left;
    	String right;
    	String bottom;
    	String top;
    	String back;
        
    	front = "/textures/" + basename + "ft^";
    	left = "/textures/" + basename + "lf^";
    	right = "/textures/" + basename + "rt^";
    	bottom = "/textures/" + basename + "dn^";
    	top = "/textures/" + basename + "up^";
    	back = "/textures/" + basename + "bk^";
        
    	mFront = mActivity.CreateTexture(front, true);
    	mLeft = mActivity.CreateTexture(left, true);
    	mRight = mActivity.CreateTexture(right, true);
    	mBottom = mActivity.CreateTexture(bottom, true);
    	mTop = mActivity.CreateTexture(top, true);
    	mBack = mActivity.CreateTexture(back, true);
    }
    
    void DrawQuad(int tex, CVector3 a, CVector2 ta, CVector3 b, CVector2 tb, CVector3 c, CVector2 tc, CVector3 d, CVector2 td)
    {
    	CShader s = mActivity.mShader[CShader.SKY];
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex);
        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
        
        float vertices[] =
        {
            //posx, posy posz   texx, texy
            a.x, a.y, a.z,          ta.x, ta.y,
            b.x, b.y, b.z,          tb.x, tb.y,
            c.x, c.y, c.z,          tc.x, tc.y,
            
            c.x, c.y, c.z,          tc.x, tc.y,
            d.x, d.y, d.z,          td.x, td.y,
            a.x, a.y, a.z,          ta.x, ta.y
        };
        
        FloatBuffer fb = FloatBuffer.allocate( (3 + 2) * 6 );
        fb.put(vertices);
        
        fb.position(0);
        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, (3+2)*4, fb);
        fb.position(3);
        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, (3+2)*4, fb);
        //GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, (3+2)*4, 0*4);
        //GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, (3+2)*4, 3*4);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }
    
    void DrawSkyBox(CVector3 pos)
    {
    	GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        //GLKMatrix4 modelmat = GLKMatrix4MakeTranslation(pos.x, pos.y, pos.z);
    	CMatrix modelmat = new CMatrix();
    	modelmat.setTranslation(pos);
    	CShader s = mActivity.mShader[CShader.SKY];
    	FloatBuffer fb = FloatBuffer.allocate( 16 );
    	fb.put(modelmat.getMatrix());
    	GLES20.glUniformMatrix4fv(s.slot[CShader.MODELMAT], 1, false, fb);
        
        DrawQuad(mRight,
                 new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_0),
                 new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_0),
                 new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_1),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_1));
        
        DrawQuad(mLeft,
        		new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_0),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_1),
                 new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_1),
                 new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_0));
        
        DrawQuad(mFront,
        		new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_0),
        		new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_0),
        		new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_1),
        		new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_1));
        
        DrawQuad(mBack,
        		new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_0),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_1),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_1),
                 new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_0));
        
        DrawQuad(mBottom,
        		new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_0),
        		new CVector3(SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_1, SKY_TEX_1),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_1),
                 new CVector3(-SKYBOX_SIZE, -SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TEX_0, SKY_TEX_0));
        
        DrawQuad(mTop,
        		new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TOP_1, SKY_TOP_1),
        		new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, -SKYBOX_SIZE), new CVector2(SKY_TOP_0, SKY_TOP_1),
        		new CVector3(-SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TOP_0, SKY_TOP_0),
        		new CVector3(SKYBOX_SIZE, SKYBOX_SIZE, SKYBOX_SIZE), new CVector2(SKY_TOP_1, SKY_TOP_0));
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    
}
