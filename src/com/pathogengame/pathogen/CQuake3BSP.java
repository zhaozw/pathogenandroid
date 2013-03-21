package com.pathogengame.pathogen;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Vector;

import android.opengl.GLES20;
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

    CQuake3BSP()
    {
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

    boolean LoadBSP(String name, MainActivity act)
    {
    	mActivity = act;
        
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
            
    		CSortFace sortFace;
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
        
    	m_numOfBrushSides = lumps[kBrushSides].length / sizeof(int);
    	m_pBrushSides     = new tBSPBrushSide [m_numOfBrushSides];
        
    	fseek(fp, lumps[kBrushSides].offset, SEEK_SET);
    	fread(m_pBrushSides, m_numOfBrushSides, sizeof(tBSPBrushSide), fp);
        
    	m_numOfLeafBrushes = lumps[kLeafBrushes].length / sizeof(int);
    	m_pLeafBrushes     = new int [m_numOfLeafBrushes];
        
    	fseek(fp, lumps[kLeafBrushes].offset, SEEK_SET);
    	fread(m_pLeafBrushes, m_numOfLeafBrushes, sizeof(int), fp);
        
        fseek(fp, lumps[kModels].offset, SEEK_SET);
    	fread(m_pModels, m_numOfModels, sizeof(tBSPModel), fp);
        
    	for(i=0; i<m_numOfModels; i++)
    	{
    		float temp = m_pModels[i].mins.y;
    		m_pModels[i].mins.y = m_pModels[i].mins.z;
    		m_pModels[i].mins.z = -temp;
            
    		temp = m_pModels[i].maxs.y;
    		m_pModels[i].maxs.y = m_pModels[i].maxs.z;
    		m_pModels[i].maxs.z = -temp;
            
    		float tempmin = min(m_pModels[i].mins.z, m_pModels[i].maxs.z);
    		float tempmax = max(m_pModels[i].mins.z, m_pModels[i].maxs.z);
    		m_pModels[i].mins.z = tempmin;
    		m_pModels[i].maxs.z = tempmax;
    	}
        
    	m_bbox.min = m_pModels[0].mins;
    	m_bbox.max = m_pModels[0].maxs;
        
    	fseek(fp, lumps[kLightVolumes].offset, SEEK_SET);
    	fread(m_pLightVols, m_numOfLightVols, sizeof(tBSPLightVol), fp);
        
        num_lightvols.x = (unsigned int) (floorf(m_bbox.max.x/m_gridSize.x) - ceilf(m_bbox.min.x/m_gridSize.x) + 1);
    	num_lightvols.y = (unsigned int) (floorf(m_bbox.max.y/m_gridSize.y) - ceilf(m_bbox.min.y/m_gridSize.y) + 1);
    	num_lightvols.z = (unsigned int) (floorf(m_bbox.max.z/m_gridSize.z) - ceilf(m_bbox.min.z/m_gridSize.z) + 1);
        
    	fseek(fp, lumps[kEntities].offset, SEEK_SET);
    	char* entities = new char[ lumps[kEntities].length ];
    	fread(entities, lumps[kEntities].length, sizeof(char), fp);
    	fclose(fp);
        
    	ReadEntities(entities);
    	delete entities;
        
    	m_FacesDrawn.Resize(m_numOfFaces);
        
    	System.out.println(name + ".bsp");
        
    	return true;
    }
    
    void RenderLevel(const CVector3 &vPos);
    void SortFaces(const CVector3 &vPos);
    void RenderLevel2(const CVector3 &vPos);
    void RenderSky();

    CVector3 TraceRay(CVector3 vStart, CVector3 vEnd);
    CVector3 TraceSphere(CVector3 vStart, CVector3 vEnd, float radius, float maxStep);
    CVector3 TraceBox(CVector3 vStart, CVector3 vEnd, CVector3 vMin, CVector3 vMax, float maxStep);
	bool BreakFaces(CVector3 vStart, CVector3 vEnd);

    boolean IsOnGround()   {   return m_bGrounded; }
    boolean Collided()     {   return m_bCollided; }
    boolean Stuck()        {   return m_bStuck;    }
    boolean Ladder()		{   return m_bLadder;	}
	CVector3 CollisionNormal()		{ return m_vCollisionNormal;		}

    void Destroy(boolean delTex=true);
    
    int IsClusterVisible(int current, int test);
	int FindLeaf(const CVector3 &vPos);
	int FindCluster(const CVector3 &vPos);
	CVector3  LightVol(CVector3 vPos);

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

    CVector3 TryToStep(CVector3 vStart, CVector3 vEnd, float maxStep);

    CVector3 Trace(CVector3 vStart, CVector3 vEnd);
	void Break(CVector3 vStart, CVector3 vEnd);
	void BreakFace(int faceIndex, CVector3 vStart, CVector3 vEnd);

    void CheckNode(int nodeIndex, CVector3 vStart, CVector3 vEnd);
    void CheckBrush(tBSPBrush *pBrush, CVector3 vStart, CVector3 vEnd);
	void BreakNode(int nodeIndex, CVector3 vStart, CVector3 vEnd);
	void BreakBrush(int brushIndex, tBSPBrush *pBrush, CVector3 vStart, CVector3 vEnd);

    void RenderFace(int faceIndex);
    void RenderSkyFace(int faceIndex);
}
