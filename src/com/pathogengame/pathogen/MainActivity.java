package com.pathogengame.pathogen;

import android.opengl.*;
import android.app.Activity;
import android.view.WindowManager;
import android.content.Context;
import android.view.Display;
import android.view.Window;
import android.os.Bundle;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import android.content.res.AssetManager;
import android.view.Menu;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Vector;

import android.view.View;

public class MainActivity extends Activity 
{
	public static final float MIN_D = 1.0f;
	public static final float MAX_D = 9000.0f;
	public static final int TEXTURES = 128;
	
	public static final int MOV_THRESH = 15;
	
	public static final int FRAME_RATE = 30;
	public static final float FRAME_INTERVAL = (1000.0f / (float)FRAME_RATE);
	
	public static final float Z_FOV	= 45.0f;
	public static final float Z_TURN_RATE = 1.0f;
	public static final float RUN_DSTAMINA = (1.0f / 5.0f);	//5 seconds to use up 1 stamina
	public static final float Z_DAMAGE = 30.0f;
	public static final float MELEE_D = 50.0f;
	public static final float GRASP_D = 25.0f;
	public static final int Z_ATTACK_DELAY = 1000;
	public static final float INTERACTION_D = 60.0f;
	public static final float VISIBLE_LIGHT = (50.0f/255.0f);
	
	public static final float FRICTION = 3.0f;
	public static final float GRAVITY = 9.8f;
	
	enum GAMEMODE{LOGO, INTRO, MENU, CONNECTING, PLAY};
	GAMEMODE mMode = GAMEMODE.LOGO;
	
	enum VIEWMODE{FIRSTPERSON, THIRDPERSON};
	VIEWMODE mViewMode = VIEWMODE.THIRDPERSON;
	
    private MyGLSurfaceView mGLView;
    public MyGL20Renderer mRenderer;
    public CShader mShader[] = new CShader[ CShader.SHADERS ];
    public CFont mFont[] = new CFont[ CFont.FONTS ];
    public CGUI mGUI;

	private float[] mViewMatrix = new float[16];
	private float[] mProjMatrix = new float[16];
	private float[] mModelMatrix = new float[16];

	int mWidth = 1;
	int mHeight = 1;
	public float mRetinaScale = 1.0f;
	float mNear = 1;
	float mFar = 9000;
	float mFOV = 90;
	
	CTexture mTexture[] = new CTexture[TEXTURES];
	boolean mLastTexTransp = false;
	
	int mTexWidth;
	int mTexHeight;
	
	public static final int MODELS = 128;
	CModel mModel[] = new CModel[MODELS];
	
	CQuake3BSP mMap;
	int mScore = 0;
	boolean mArrest = false;
	CCamera mCamera;
	CFrustum mFrustum;
	
	long mTicks = 0;
	
	int mLocalP = 0;
	
	public static final int PLAYERS		= 32;
	CPlayer mPlayer[] = new CPlayer[PLAYERS];
	
	Vector<CEntityType> mEntityType;
	public static final int ENTITIES	= 256;
	CEntity mEntity[] = new CEntity[ENTITIES];
	
	CItemType mItemType[] = new CItemType[CItemType.ITEM_TYPES];

	Vector<CSpawn> g_sspawn = new Vector<CSpawn>(); //story spawn
	Vector<CSpawn> g_spawn = new Vector<CSpawn>();  //human spawn
	Vector<CSpawn> g_zspawn = new Vector<CSpawn>(); //zombie spawn
	
	CFuncPtr DialogContinue = null;
	boolean mShowDialog = true;
	
	int mLastEnt = -1;
	
	float mReddening = 0;

	Vector<CSound> mZDeathSnd = new Vector<CSound>();
	Vector<CSound> mZGraspSnd = new Vector<CSound>();
	Vector<CSound> mZPainSnd = new Vector<CSound>();
	CSound mDoorKnock = new CSound();
	CSound mStaticSound = new CSound();
	
	static final int SCRIPT_FUNCS = 10;
	CFuncPtr mScriptFunc[] = new CFuncPtr[SCRIPT_FUNCS];
	
	Vector<CBillboardType> mBillbT = new Vector<CBillboardType>();
	static final int BILLBOARDS = 512;
	CBillboard mBillb[] = new CBillboard[BILLBOARDS];
	
	int mMuzzle[] = new int[4];
	
	CParticleType mParticleType[] = new CParticleType[CParticleType.PARTICLE_TYPES];
	static final int PARTICLES = 256;
	CParticle mParticle[] = new CParticle[PARTICLES];
	
	CDecalType mDecalT[] = new CDecalType[CDecalType.DECAL_TYPES];
	static final int DECALS = 128;
	CDecal mDecal[] = new CDecal[DECALS];
	
	//int rotational;
	
	void FreeEntities()
	{
		CEntity e;
		for(int i=0; i<ENTITIES; i++)
		{
			e = mEntity[i];
			e.on = false;
		}
	}

	void FreePlayers(int ignore)
	{
		CPlayer p;
		for(int i=0; i<PLAYERS; i++)
		{
			if(i == ignore)
				continue;
	        
			p = mPlayer[i];
			p.on = false;
			p.items.clear();
		}
	}

	void FreeBillboards()
	{
		CBillboard b;
		for(int i=0; i<BILLBOARDS; i++)
		{
			b = mBillb[i];
			b.on = false;
		}
	}
	
	void UnloadMap()
	{
		//g_map.Destroy(false);
		mMap.Destroy(true);
		FreeBillboards();
		FreeEntities();
		FreePlayers(mLocalP);
		g_zspawn.clear();
		g_spawn.clear();
		g_sspawn.clear();		//For mobile version
		mMap.mFuncProxy.clear();
		mMap.mFuncMap.clear();
	    mShowDialog = true;
	}
	
	void Decal(int type, String tex, float decay, float size)
	{
		mDecalT[type] = new CDecalType();
		CDecalType t = mDecalT[type];
		
		t.tex = CreateTexture(tex, true);
		t.decay = decay;
		t.size = size;
	}
	
	void Decals()
	{
		Decal(CDecalType.BLOODSPLAT, "/effects/bloodsplat", 0.01f, 10.0f);
		Decal(CDecalType.BULLETHOLE, "/effects/bullethole", 0.005f, 3.0f);
	}
	
	void UpdateDecals()
	{
		CDecal d;
		CDecalType t;
	    
		for(int i=0; i<DECALS; i++)
		{
			d = mDecal[i];
	        
			if(!d.on)
				continue;
	        
			t = mDecalT[d.type];
	        
			d.life -= t.decay;
	        
			if(d.life < 0.0f)
				d.on = false;
		}
	}
	
	void DrawDecals()
	{
		CDecal d;
		CDecalType t;
	    
		float precolor[] = {1,1,1,1};
	    
	    if(mReddening > 0.0f)
	    {
			precolor[1] = 1.0f - mReddening;
			precolor[2] = 1.0f - mReddening;
		}
	    
	    float colorf[] = {1, 1, 1, 1};
		CVector3 colorv;
	    
        CShader s = mShader[CShader.BILLBOARD];
		
		for(int i=0; i<DECALS; i++)
	    {
	        d = mDecal[i];
	        if(!d.on)
	            continue;
	        
	        colorv = mMap.LightVol(d.lpos);
			colorf[0] = colorv.x;
			colorf[1] = precolor[1] * colorv.y;
			colorf[2] = precolor[2] * colorv.z;
			colorf[3] = d.life;
	        GLES20.glUniform4f(s.slot[CShader.COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
	        
	        t = mDecalT[d.type];
	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.tex);
	        GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	        
	        float vertices[] =
	        {
	            //posx, posy posz   texx, texy
	            d.a.x, d.a.y, d.a.z,          1, 0,
	            d.b.x, d.b.y, d.b.z,          1, 1,
	            d.c.x, d.c.y, d.c.z,          0, 1,
	            
	            d.c.x, d.c.y, d.c.z,          0, 1,
	            d.d.x, d.d.y, d.d.z,          0, 0,
	            d.a.x, d.a.y, d.a.z,          1, 0
	        };
	        
	        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, 4*5, 0*4);
	        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, 4*5, 3*4);
	        
	        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	    }
	    
		GLES20.glUniform4f(s.slot[CShader.COLOR], precolor[0], precolor[1], precolor[2], precolor[3]);
	}

	int NewDecal()
	{
		for(int i=0; i<DECALS; i++)
			if(!mDecal[i].on)
				return i;
	    
		return -1;
	}
	
	void PlaceDecal(int type, CVector3 pos, CVector3 norm)
	{
		int i = NewDecal();
		if(i < 0)
			return;
	    
		CDecal d = mDecal[i];
		d.on = true;
		d.life = 1;
	    d.type = type;
	    
		CDecalType t = mDecalT[type];
	    
		CVector3 vertical = Math3D.Normalize(Math3D.Cross(pos, norm));
		CVector3 horizontal = Math3D.Normalize(Math3D.Cross(vertical, norm));
	    
		CVector3 vert = Math3D.Multiply(vertical, t.size);
		CVector3 horiz = Math3D.Multiply(horizontal, t.size);
	    
	    d.lpos = Math3D.Add(pos, Math3D.Multiply(norm, 0.1f));
		d.a = Math3D.Add(Math3D.Subtract(pos, horiz), vert);
		d.b = Math3D.Add(Math3D.Add(pos, horiz), vert);
		d.c = Math3D.Subtract(Math3D.Add(pos, horiz), vert);
		d.d = Math3D.Subtract(Math3D.Subtract(pos, horiz), vert);
	}
	
	int NewParticle()
	{
		for(int i=0; i<PARTICLES; i++)
			if(!mParticle[i].on)
				return i;
	    
		return -1;
	}
	
	void Particle(int i, String texpath, int del, float dec, CVector3 minV, CVector3 maxV, CVector3 minA, CVector3 maxA, float minS, float maxS, CFuncPtr collision)
	{
		mParticleType[i] = new CParticleType();
		CParticleType t = mParticleType[i];
	    
		t.billbT = Billboard(texpath);
		t.delay = del;
		t.decay = dec;
		t.minvelocity = minV;
		t.velvariation = Math3D.Subtract(maxV, minV);
		t.minacceleration = minA;
		t.accelvariation = Math3D.Subtract(maxA, minA);
		t.minsize = minS/2.0f;
		t.sizevariation = (maxS-minS)/2.0f;
		t.collision = collision;
	}
	
	void EmitParticle(int type, CVector3 pos)
	{
		int i = NewParticle();
		if(i < 0)
			return;
	    
		CParticleType t = mParticleType[type];
		PlaceBillboard(t.billbT, pos, t.minsize, i, false);
	    
		CParticle p = mParticle[i];
	    
		p.on = true;
		p.life = 1;
		p.vel.x = t.minvelocity.x + t.velvariation.x * (float)(Math.random());
		p.vel.y = t.minvelocity.y + t.velvariation.y * (float)(Math.random());
		p.vel.z = t.minvelocity.z + t.velvariation.z * (float)(Math.random());
		p.type = type;
	}
	
	void Particles()
	{
		Particle(CParticleType.BLOODPART, "bloodpart", 500, 0.05f, new CVector3(-1.5f, -0.5f, -1.5f), new CVector3(1.5f, 1.8f, 1.5f), new CVector3(0, -0.1f, 0), new CVector3(0.0f, -0.3f, 0.0f), 5.0f, 10.0f, new Collision_BloodSplat(this));
	}

	void UpdateParticles()
	{
		CBillboard b;
		CParticle p;
	    
		for(int i=0; i<BILLBOARDS; i++)
		{
			b = mBillb[i];
	        
			if(!b.on)
				continue;
	        
			if(b.particle < 0)
				continue;
	        
			p = mParticle[b.particle];
	        
			p.Update(b);
		}
	}
	
	void Effects()
	{
		mMuzzle[0] = CreateTexture("effects/muzzle0", true);
		mMuzzle[1] = CreateTexture("effects/muzzle1", true);
		mMuzzle[2] = CreateTexture("effects/muzzle2", true);
		mMuzzle[3] = CreateTexture("effects/muzzle3", true);
	}
	
	int NewBillboard(String tex)
	{
	    CBillboardType t = new CBillboardType();
		String rawtex;
		rawtex = CFile.StripPathExtension(tex);
		t.name = "/billboards/" + rawtex;
	    t.tex = CreateTexture(t.name, true);
		mBillbT.add(t);
		return mBillbT.size() - 1;
	}
	
	int Billboard(String name)
	{
		String rawname;
		rawname = "/billboards/" + CFile.StripPathExtension(name);
	    
	    for(int i=0; i<mBillbT.size(); i++)
	    {
	        if(mBillbT.get(i).name.equalsIgnoreCase(rawname))
	            return i;
	    }
	    
	    return NewBillboard(rawname);
	}
	
	int NewBillboard()
	{
	    for(int i=0; i<BILLBOARDS; i++)
	        if(!mBillb[i].on)
	            return i;
	    
	    return -1;
	}
	
	void PlaceBillboard(String n, CVector3 pos, float size, int particle, boolean nolightvol)
	{
	    int type = Billboard(n);
	    if(type < 0)
	        return;
	    
	    PlaceBillboard(type, pos, size, particle, nolightvol);
	}
	
	void PlaceBillboard(int type, CVector3 pos, float size, int particle, boolean nolightvol)
	{
	    int i = NewBillboard();
	    if(i < 0)
	        return;
	    
	    CBillboard b = mBillb[i];
	    b.on = true;
	    b.type = type;
	    b.pos = pos;
	    b.size = size;
		b.particle = particle;
		b.nolightvol = nolightvol;
	}
	
	void SwitchBillboards(int i, int j)
	{
		CBillboard temp = new CBillboard();
		CBillboard ibb = mBillb[i];
		CBillboard jbb = mBillb[j];
		
		/*
	    boolean on;
	    int type;
	    float size;
	    CVector3 pos;
	    float dist;
	    int particle;
		boolean nolightvol;
		*/
		
		temp.on = ibb.on;
		temp.type = ibb.type;
		temp.size = ibb.size;
		temp.pos.x = ibb.pos.x;
		temp.pos.y = ibb.pos.y;
		temp.pos.z = ibb.pos.z;
		temp.dist = ibb.dist;
		temp.particle = ibb.particle;
		temp.nolightvol = ibb.nolightvol;
		
		ibb.on = jbb.on;
		ibb.type = jbb.type;
		ibb.size = jbb.size;
		ibb.pos.x = jbb.pos.x;
		ibb.pos.y = jbb.pos.y;
		ibb.pos.z = jbb.pos.z;
		ibb.dist = jbb.dist;
		ibb.particle = jbb.particle;
		ibb.nolightvol = jbb.nolightvol;

		jbb.on = temp.on;
		jbb.type = temp.type;
		jbb.size = temp.size;
		jbb.pos.x = temp.pos.x;
		jbb.pos.y = temp.pos.y;
		jbb.pos.z = temp.pos.z;
		jbb.dist = temp.dist;
		jbb.particle = temp.particle;
		jbb.nolightvol = temp.nolightvol;
	}
	
	void SortBillboards()
	{
	    CVector3 pos = mCamera.LookPos(this);
	    
		for(int i=0; i<BILLBOARDS; i++)
		{
			if(!mBillb[i].on)
				continue;
	        
			mBillb[i].dist = Math3D.Magnitude2(Math3D.Subtract(pos, mBillb[i].pos));
		}
	    
		CBillboard temp;
		int leftoff = 0;
		boolean backtracking = false;
	    
		for(int i=1; i<BILLBOARDS; i++)
		{
			if(!mBillb[i].on)
				continue;
	        
			if(i > 0)
			{
				if(mBillb[i].dist > mBillb[i-1].dist)
				{
					if(!backtracking)
					{
						leftoff = i;
						backtracking = true;
					}
					//temp = mBillb[i];
					//mBillb[i] = mBillb[i-1];
					//mBillb[i-1] = temp;
					SwitchBillboards(i, i-1);
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
	
	void DrawBillboards()
	{
	    CBillboard billb;
	    CBillboardType t;
	    float size;
	    
		CVector3 vertical = mCamera.Up2();
		CVector3 horizontal = mCamera.Strafe();
		CVector3 a, b, c, d;
		CVector3 vert, horiz;
	    
		CParticle part;
		CParticleType pT;
	    
	    float precolor[] = {1,1,1,1};
	    
	    if(mReddening > 0.0f)
	    {
			precolor[1] = 1.0f - mReddening;
			precolor[2] = 1.0f - mReddening;
		}
	    
		float colorf[] = {1, 1, 1, 1};
		CVector3 colorv;
		
		CShader s = mShader[CShader.BILLBOARD];
	    
	    for(int i=0; i<BILLBOARDS; i++)
	    {
	        billb = mBillb[i];
	        if(!billb.on)
	            continue;
	        
	        t = mBillbT.get(billb.type);
	        
	        if(billb.nolightvol)
				colorv = new CVector3(1, 1, 1);
			else
				colorv = mMap.LightVol(billb.pos);
	        
			colorf[0] = colorv.x;
			colorf[1] = precolor[1] * colorv.y;
			colorf[2] = precolor[2] * colorv.z;
	        GLES20.glUniform4f(s.slot[CShader.COLOR], colorf[0], colorf[1], colorf[2], colorf[3]);
	        
			if(billb.particle >= 0)
			{
				part = mParticle[billb.particle];
				pT = mParticleType[part.type];
				size = pT.minsize + pT.sizevariation*(1.0f - part.life);
			}
			else
				size = billb.size;
	        
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.tex);
			GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	        
			vert = Math3D.Multiply(vertical, size);
			horiz = Math3D.Multiply(horizontal, size);
	        
			a = Math3D.Add(Math3D.Subtract(billb.pos, horiz), vert);
			b = Math3D.Add(Math3D.Add(billb.pos, horiz), vert);
			c = Math3D.Subtract(Math3D.Add(billb.pos, horiz), vert);
			d = Math3D.Subtract(Math3D.Subtract(billb.pos, horiz), vert);
	        
	        float vertices[] =
	        {
	            //posx, posy posz   texx, texy
	            a.x, a.y, a.z,          1, 0,
	            b.x, b.y, b.z,          1, 1,
	            c.x, c.y, c.z,          0, 1,
	            
	            c.x, c.y, c.z,          0, 1,
	            d.x, d.y, d.z,          0, 0,
	            a.x, a.y, a.z,          1, 0
	        };
	        
	        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, 4*5, 0*4);
	        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, 4*5, 3*4);
	        
	        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
	    }
	    
	    GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
	    
		CEntity e;
		CPlayer p;
		CHold h;
		CItemType iT;
		size = 8.0f;
		vert = Math3D.Multiply(vertical, size);
		horiz = Math3D.Multiply(horizontal, size);
		CVector3 muzz;
		CCamera cam;
		CVector3 offset;
	    
		for(int i=0; i<ENTITIES; i++)
		{
			e = mEntity[i];
	        
			if(!e.on)
				continue;
	        
			if(e.controller < 0)
				continue;
	        
			p = mPlayer[e.controller];
	        
			if(p.equipped < 0)
				continue;
	        
			h = p.items.get(p.equipped);
			iT = mItemType[h.type];
	        
			if((e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_SHOTSHOULDER_S || e.frame[CEntity.BODY_UPPER].value > Animation.ANIM_SHOTSHOULDER_S+4) &&
	           (e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_SHOTGUNSHOT_S || e.frame[CEntity.BODY_UPPER].value > Animation.ANIM_SHOTGUNSHOT_S+4) &&
	           (e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_PISTOLSHOT_S || e.frame[CEntity.BODY_UPPER].value > Animation.ANIM_PISTOLSHOT_S+4))
				continue;
	        
			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mMuzzle[(int)Math.round(Math.random()*4)]);
			GLES20.glUniform1i(s.slot[CShader.TEXTURE], 0);
	        
			cam = e.camera;
	        
			if(p == mPlayer[mLocalP] && mViewMode == VIEWMODE.FIRSTPERSON)
				muzz = Math3D.Rotate(iT.front, -cam.Pitch(), 1, 0, 0);
			else
				muzz = Math3D.RotateAround(iT.front, new CVector3(0, CEntity.MID_HEIGHT_OFFSET, 0), -cam.Pitch(), 1, 0, 0);
	        
			muzz = Math3D.Add(cam.Position(), Math3D.Rotate(muzz, cam.Yaw(), 0, 1, 0));
	        
			a = Math3D.Add(Math3D.Subtract(muzz, horiz), vert);
			b = Math3D.Add(Math3D.Add(muzz, horiz), vert);
			c = Math3D.Subtract(Math3D.Add(muzz, horiz), vert);
			d = Math3D.Subtract(Math3D.Subtract(muzz, horiz), vert);
	        
	        float vertices[] =
	        {
	            //posx, posy posz   texx, texy
	            a.x, a.y, a.z,          1, 0,
	            b.x, b.y, b.z,          1, 1,
	            c.x, c.y, c.z,          0, 1,
	            
	            c.x, c.y, c.z,          0, 1,
	            d.x, d.y, d.z,          0, 0,
	            a.x, a.y, a.z,          1, 0
	        };
	        
	        GLES20.glVertexAttribPointer(s.slot[CShader.POSITION], 3, GLES20.GL_FLOAT, false, 4*5, 0*4);
	        GLES20.glVertexAttribPointer(s.slot[CShader.TEXCOORD], 2, GLES20.GL_FLOAT, false, 4*5, 3*4);
	        
	        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		}
	}
	
	void NoMove()
	{
		CPlayer p = mPlayer[mLocalP];
		p.forward = false;
		p.backward = false;
		p.left = false;
		p.right = false;
		p.run = false;
		p.jump = false;
	}
	
	void DoScriptFunc(int script)
	{
		//char msg[128];
		//sprintf(msg, "func_proxy #%d", script);
		//Chat(msg);
	    
		NoMove();
	    
		mScriptFunc[script].func();
	}
	
	void ScriptFuncs()
	{
		mScriptFunc[0] = new Script0(this);
		mScriptFunc[1] = new Script1(this);
		mScriptFunc[2] = new Script2(this);
		mScriptFunc[3] = new Script3(this);
		mScriptFunc[4] = new Script4(this);
		mScriptFunc[5] = new Script5(this);
		mScriptFunc[6] = new Script6(this);
		mScriptFunc[7] = new Script7(this);
		mScriptFunc[8] = new Script8(this);
		mScriptFunc[9] = new Script0(this);
	}
	
	boolean IsHuman(int type)
	{
		CEntityType t = mEntityType.get(type);
	    
		if(t.category == CEntity.HUMAN)
			return true;
	    
	    return false;
	}

	boolean IsZombie(int type)
	{
		CEntityType t = mEntityType.get(type);
	    
		if(t.category == CEntity.ZOMBIE)
			return true;
	    
	    return false;
	}
	
	/*int NewAI()
	{
	    CPlayer p;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        
	        if(p.on)
	            continue;
	        
	        p.ai = true;
	        p.on = true;
			p.target = -1;
			p.stamina = 1;
	        return i;
	    }
	    
	    return -1;
	}*/
	
	int NewAI(int activity)
	{
		CPlayer p;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
			p = mPlayer[i];
	        
	        if(p.on)
	            continue;
	        
	        p.ai = true;
			p.on = true;
			p.target = -1;
			p.stamina = 1;
			p.activity = activity;
	        p.hp = p.MaxHP();
	        return i;
	    }
	    
	    return -1;
	}
	
	int NewEntity()
	{
		for(int i=0; i<ENTITIES; i++)
			if(!mEntity[i].on)
				return i;
	    
		return -1;
	}
	
	boolean PlaceEntity(int type, int controller, float amount, float clip, CVector3 pos, float yaw, Integer ID, boolean nolightvol, int script)
	{
		int i = NewEntity();
		if(i < 0)
			return false;
	    
	    CEntity e = mEntity[i];

		e.on = true;
		e.frame[CEntity.BODY_LOWER].value = 0;
		e.frame[CEntity.BODY_UPPER].value = 0;
		e.type = type;
		e.controller = controller;
		e.amount = amount;
		e.clip = clip;
		e.state = CEntity.STATE_NONE;
		e.nolightvol = nolightvol;
		e.script = script;
	    
	    CCamera c = e.camera;
	    
		c.PositionCamera(pos.x, pos.y, pos.z, pos.x+1, pos.y, pos.z, 0, 1, 0);
		c.RotateView(Math3D.DEGTORAD(yaw), 0, 1, 0);
	    
		CEntityType t = mEntityType.get(type);
		e.cluster = mMap.FindCluster(Math3D.Add(c.Position(), t.vCenterOff));
	    
		CPlayer p = null;
		if(controller >= 0)
		{
			mPlayer[controller].entity = i;
			p = mPlayer[controller];
		}
	    
		if(ID != null)
			ID = i;
	    
		if(controller >= 0)
			p.goal = pos;
	    
	    return true;
	}
	
	int EntityID(String lmodel)
    {
        String raw = CFile.StripPathExtension(lmodel);
        for(int i=0; i<mEntityType.size(); i++)
            if(raw.equalsIgnoreCase(mEntityType.get(i).lmodel))
                return i;
        
        return -1;
    }
	
	boolean CheckEntitySound(Vector<CSound> vec, String file)
	{
		for(int i=0; i<(vec).size(); i++)
			if(!(vec).get(i).file.equalsIgnoreCase(file))
				return true;
	    
		return false;
	}

	void EntitySound(int category, String file)
	{
		CEntityType t = mEntityType.get(mLastEnt);
		Vector<CSound> vec = new Vector<CSound>();
	    
		if(category == CEntity.CLOSESND)
			vec = t.closeSound;
		else if(category == CEntity.OPENSND)
			vec = t.openSound;
	    
		String raw = CFile.StripPathExtension(file);
		String filepath = "/sounds/" + raw + ".wav";
	    
		if(CheckEntitySound(vec, filepath))
			return;
	    
		(vec).add(new CSound(this, raw));
	}
	
	int Entity(int category, String lowermodel, float animrate, int collider)
	{
		mEntityType.add(new CEntityType());
		int i = mEntityType.size() - 1;
		CEntityType t = mEntityType.get(i);
	    
		String raw = CFile.StripPathExtension(lowermodel);
		t.lmodel = raw;
		t.model[CEntity.BODY_LOWER] = LoadModel(raw, new CVector3(1,1,1));
		t.model[CEntity.BODY_UPPER] = -1;
	    
		t.category = category;
		ModelMinMax(t.model[CEntity.BODY_LOWER], t.vMin, t.vMax);
		t.maxStep = 15;
		t.speed = 200;
		t.jump = 0;
		t.crouch = 0;
		t.animrate = animrate;
		t.vCenterOff = Math3D.Divide(Math3D.Add(t.vMin, t.vMax), 2.0f);
		t.collider = collider;
	    
		mLastEnt = i;
	    
		return i;
	}

	void Entity(int category, int item, String lowermodel, String uppermodel, CVector3 scale, CVector3 translate, CVector3 vMin, CVector3 vMax, float maxStep, float speed, float jump, float crouch, float animrate)
	{
	    mEntityType.add(new CEntityType());
		int i = mEntityType.size() - 1;
		CEntityType t = mEntityType.get(i);
	    
	    t.lmodel = lowermodel;
	    
	    if(lowermodel.equals(""))
	        t.model[CEntity.BODY_LOWER] = -1;
	    else
	    {
	        String raw = CFile.StripPathExtension(lowermodel);
	        t.model[CEntity.BODY_LOWER] = LoadModel(raw, scale);
	    }
	    
	    if(uppermodel.equals(""))
	        t.model[CEntity.BODY_UPPER] = -1;
	    else
	    {
	        String raw = CFile.StripPathExtension(uppermodel);
	        t.model[CEntity.BODY_UPPER] = LoadModel(raw, scale);
	    }

		t.category = category;
		t.item = item;
	    t.vMin = vMin;
		t.vMax = vMax;
		t.maxStep = maxStep;
		t.speed = speed;
		t.jump = jump;
		t.crouch = crouch;
		t.animrate = animrate;
	    t.collider = -1;
	}
	
	int NewModel()
	{
	    for(int i=0; i<MODELS; i++)
	        if(!mModel[i].on)
	            return i;
	    
	    return -1;
	}

	int FindModel(String raw)
	{
		for(int i=0; i<MODELS; i++)
		{
			if(!mModel[i].on)
				continue;
	        
			if(mModel[i].name.equalsIgnoreCase(raw))
				return i;
		}
	    
		return -1;
	}

	int LoadModel(String name, CVector3 scale)
	{
		String raw = CFile.StripPathExtension(name);
		int i = FindModel(raw);
		if(i >= 0)
			return i;
	    
	    i = NewModel();
	    if(i < 0)
	        return -1;
	    
	    mModel[i].Load(raw, scale, this);
	    
	    return i;
	}

	void ModelMinMax(int model, CVector3 vMin, CVector3 vMax)
	{
		//(*vMin) = CVector3(0, 0, 0);
		//(*vMax) = CVector3(0, 0, 0);
		vMin.x = vMin.y = vMin.z = 0;
		vMax.x = vMax.y = vMax.z = 0;
	    
		CModel m = mModel[model];
		CVertexArray va;
		
		int v;
		for(int f=0; f<m.header.num_frames; f++)
	        //for(int f=0; f<1; f++)
		{
			va = m.vertexArrays[f];
			for(v=0; v<va.numverts; v++)
			{
				if(va.vertices[v].x < vMin.x)
					vMin.x = va.vertices[v].x;
				if(va.vertices[v].y < vMin.y)
					vMin.y = va.vertices[v].y;
				if(va.vertices[v].z < vMin.z)
					vMin.z = va.vertices[v].z;
				if(va.vertices[v].x > vMax.x)
					vMax.x = va.vertices[v].x;
				if(va.vertices[v].y > vMax.y)
					vMax.y = va.vertices[v].y;
				if(va.vertices[v].z > vMax.z)
					vMax.z = va.vertices[v].z;
			}
		}
	    
	    float maxextent = Math.max(Math.max(Math.abs(vMin.x), Math.abs(vMin.z)), Math.max(Math.abs(vMax.x), Math.abs(vMax.z)));
	    
		/*
	     vMin->x = min(vMin->x, vMin->z);
	     vMin->z = vMin->x;
	     vMax->x = max(vMax->x, vMax->z);
	     vMax->z = vMax->x;*/
		vMin.x = vMin.z = -maxextent;
		vMax.x = vMax.z = maxextent;
	}

	CVector3 ModelFront(int model, int from, int to)
	{
		CVector3 vFront = new CVector3(0, 0, -99999);
	    
		CModel m = mModel[model];
		CVertexArray va;
	    
		for(int f=from; f<to; f++)
		{
			va = m.vertexArrays[f];
			for(int v=0; v<va.numverts; v++)
			{
				if(va.vertices[v].z < vFront.z)
					continue;
	            
				vFront = va.vertices[v];
			}
		}
	    
		return Math3D.Copy(vFront);
	}
	
	boolean Unobstructed(CCamera zc, CVector3 pos, CEntity ignore1, CEntity ignore2)
	{
		CVector3 trace = mMap.TraceRay(zc.Position(), pos);
		if(!Math3D.Equals(trace, pos))
			return false;
	    
		CEntity e;
		CVector3 vLine[] = new CVector3[2];
		vLine[0] = Math3D.Copy(zc.Position());
		vLine[1] = Math3D.Copy(pos);
		int cluster = mMap.FindCluster(zc.Position());
	    CEntityType t;
	    
		for(int i=0; i<ENTITIES; i++)
		{
			e = mEntity[i];
	        
			if(!e.on)
				continue;
	        
	        /*
			if(e == ignore1)
				continue;
	        
			if(e == ignore2)
				continue;*/
	        
			if(!mMap.IsClusterVisible(cluster, e.cluster))
				continue;
	        
	        t = mEntityType.get(e.type);
	        
	        if(t.category != CEntity.DOOR)
	            continue;
	        
			trace = e.TraceRay(vLine);
	        
			if(Math3D.Equals(trace, vLine[1]))
				continue;
	        
			return false;
		}
	    
		return true;
	}

	boolean Visible(CCamera zc, CVector3 pos, CEntity ignore1, CEntity ignore2)
	{
		//check angle
		if(!Math3D.WithinAngle(zc, pos, Math3D.DEGTORAD(Z_FOV/2.0f)))
			return false;
	    
		//check obstruction
		if(!Unobstructed(zc, pos, ignore1, ignore2))
			return false;
	    
		return true;
	}

	boolean HumanVisible(CCamera zc, CEntity ze)
	{
	    CPlayer p;
	    CEntity e;
	    CCamera c;
	    
		for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        
	        if(!p.on)
	            continue;
	        
	        if(p.ai)
	            continue;
	        
	        if(p.entity < 0)
	            continue;
	        
			if(p.hp <= 0.0f)
				continue;
	        
	        e = mEntity[p.entity];
	        
	        if(!IsHuman(e.type))
	            continue;
	        
	        c = e.camera;
	        
			if(!Unobstructed(zc, c.Position(), e, ze))
				continue;
	        
			return true;
	    }
	    
		return false;
	}

	int NearestVisHuman(CCamera zc, CEntity ze)
	{
	    CPlayer p;
	    CEntity e;
	    CCamera c;
	    int nearest = -1;
	    float nearestD2 = 999999999.0f;
	    float D2;
		CVector3 trace;
		CVector3 light;
		CEntityType t;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        
	        if(!p.on)
	            continue;
	        
	        if(p.ai)
	            continue;
	        
	        if(p.entity < 0)
	            continue;
	        
			if(p.hp <= 0.0f)
				continue;
	        
	        e = mEntity[p.entity];
	        
	        if(!IsHuman(e.type))
	            continue;
	        
	        c = e.camera;
	        
	        D2 = Math3D.Magnitude2(Math3D.Subtract(c.Position(), zc.Position()));
	        
			//check distance
	        if(D2 > nearestD2)
	            continue;
	        
			if(!Visible(zc, c.Position(), e, ze))
				continue;
	        
			t = mEntityType.get(e.type);
			light = mMap.LightVol(Math3D.Add(c.Position(), t.vCenterOff));
	        
			//check lighting
			if(light.x < VISIBLE_LIGHT && light.y < VISIBLE_LIGHT && light.z < VISIBLE_LIGHT)
				continue;
	        
	        nearestD2 = D2;
	        nearest = i;
	    }
	    
	    return nearest;
	}

	void NewGoal(CPlayer p)
	{
	    CEntity e = mEntity[p.entity];
	    CCamera c = e.camera;
		p.goal = c.Position();
		p.goal.x += Math.random() * 1000.0 - 500.0; //rand()%1000 - 500;
		p.goal.z += Math.random() * 1000.0 - 500.0; //rand()%1000 - 500;
	}

	void NoAct(CPlayer p)
	{
		p.forward = false;
	}
	
	void UpdateTicks()
	{
		mTicks += (1000/FRAME_RATE);
	}
	
	long GetTickCount()
	{
		return mTicks;
	}

	void GameOver()
	{
	    mArrest = true;
	    mGUI.OpenAnotherView("game over", 0);
	    mViewMode = VIEWMODE.THIRDPERSON;
	    
	    mGUI.CloseView("shoot");
	    mGUI.CloseView("swing");
	    mGUI.CloseView("stab");
	    mGUI.CloseView("open door");
	    mGUI.CloseView("close door");
	    mGUI.CloseView("reload");
	    mGUI.CloseView("switch item");
	    mGUI.CloseView("switch view");
	    mGUI.CloseView("jump");
	    mGUI.CloseView("crouch");
	    mGUI.CloseView("run");
	}
	
	void Reddening()
	{
		mReddening = 1.0f;
		mGUI.RedoHP();
	}

	void Damage(CPlayer p, float damage, boolean shot)
	{
		p.hp -= damage;
	    
		if(p == mPlayer[mLocalP])
			Reddening();
	    
		if(p.hp <= 0.0f)
		{
			CEntity e = mEntity[p.entity];
	        
			if((int)(Math.round(Math.random()*2)) == 1)
			{
				e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_UDEATHFW_S;
				e.frame[CEntity.BODY_LOWER].value = Animation.ANIM_LDEATHFW_S;
			}
			else
			{
				e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_UDEATHBW_S;
				e.frame[CEntity.BODY_LOWER].value = Animation.ANIM_LDEATHBW_S;
			}
	        
			p.forward = false;
			p.backward = false;
			p.left = false;
			p.right = false;
			p.crouched = false;
			p.crouching = false;
			p.jump = false;
	        
			CCamera c = e.camera;
			c.Pitch(0);
	        
			if(p == mPlayer[mLocalP])
			{
				GameOver();
			}
	        
			if(IsZombie(e.type))
			{
	            mScore += 50;
	            mGUI.RedoScore();
	            
				if(e.script > 0)
				{
					DoScriptFunc(e.script);
					e.script = -1;
				}
	            
				p.ticksleft = 2 * FRAME_RATE;
	            
				if(mZDeathSnd.size() > 0)
					mZDeathSnd.get( (int)Math.round(Math.random()*mZDeathSnd.size()) ).Play();
			}
		}
		else if(shot)
			p.pain = true;
	}

	
	void Grasp(CPlayer zom, CEntity zE, CPlayer hum)
	{
		if(GetTickCount() - zom.last < Z_ATTACK_DELAY)
			return;
	    
		zom.last = GetTickCount();
		zE.frame[CEntity.BODY_UPPER].value = Animation.ANIM_ZGRASP_S;
		Damage(hum, Z_DAMAGE, false);
	    
		if(mZGraspSnd.size() > 0)
			mZGraspSnd.get( (int)Math.round(Math.random()*mZGraspSnd.size()) ).Play();
	}
	
	void UpdateAI(CPlayer p)
	{
	    if(p.entity < 0)
	        return;
	    
		if(p.activity == CPlayer.NOACT)
			return;
	    
	    CEntity e = mEntity[p.entity];
	    CCamera c = e.camera;
	    
		if(p.activity == CPlayer.ONSIGHT && !HumanVisible(c, e))
		{
			NoAct(p);
			return;
		}
	    
	    int target = p.target;
	    
		if(target >= 0)
		{
			CPlayer p2 = mPlayer[target];
			CEntity e2 = mEntity[p2.entity];
			CCamera c2 = e2.camera;
	        
			if(!Visible(c, c2.Position(), e2, e))
				target = -1;
		}
	    
		if(target < 0)
			target = NearestVisHuman(c, e);
	    
		p.target = target;
		float dyaw;
	    
	    //return;
	    
	    if(target < 0)
	    {
			CVector3 d = Math3D.Subtract(c.Position(), p.goal);
			d.y = 0;
			if(Math3D.Magnitude2(d) < 100)
				NewGoal(p);
	        
			if(Math3D.WithinAngle(c, p.goal, Math3D.DEGTORAD(Z_TURN_RATE*2.0f)))
				p.forward = true;
			else
				p.forward = false;
	        
			dyaw = Math3D.DYaw(c, p.goal);
	    }
		else
		{
			CPlayer p2 = mPlayer[target];
			CEntity e2 = mEntity[p2.entity];
			CCamera c2 = e2.camera;
	        
			p.goal = c2.Position();
			p.forward = true;
	        
			dyaw = Math3D.DYaw(c, c2.Position());
	        
			if(Visible(c, c2.Position(), e2, e) && Math3D.Magnitude2(Math3D.Subtract(c.Position(), c2.Position())) <= GRASP_D * GRASP_D)
				Grasp(p, e, p2);
		}
	    
		if(Math.abs(dyaw) < Math3D.DEGTORAD(Z_TURN_RATE*2.0f))
			c.View(p.goal);
		else if(dyaw < 0.0f)
			c.RotateView(-Math3D.DEGTORAD(Z_TURN_RATE), 0, 1, 0);
		else if(dyaw > 0.0f)
			c.RotateView(Math3D.DEGTORAD(Z_TURN_RATE), 0, 1, 0);
	}

	void UpdateAI()
	{
	    CPlayer p;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        
	        if(!p.on)
	            continue;
	        
	        if(!p.ai)
	            continue;
	        
			if(p.hp <= 0.0f)
				continue;
	        
	        UpdateAI(p);
	    }
	}

	void UpdateDead(CPlayer p)
	{
		CEntity e = mEntity[p.entity];
	    
		if(!IsZombie(e.type))
			return;
	    
		p.ticksleft --;
	    
		if(p.ticksleft <= 0)
		{
			e.on = false;
			p.on = false;
		}
	}
	
	void Shot(int player)
	{
		CPlayer p = mPlayer[player];
		CHold h = p.items.get(p.equipped);
		CItemType t = mItemType[h.type];
	    
		if(GetTickCount() - p.last < t.delay)
			return;
	    
		p.last = GetTickCount();
		h.clip -= 1.0f;
	    
		if(p == mPlayer[mLocalP])
			mGUI.RedoAmmo();
	    
		CEntity e = mEntity[p.entity];
	    
		if(t.ammo == CItemType.PRIMARYAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTSHOULDER_S;
		else if(t.ammo == CItemType.SECONDARYAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTGUNSHOT_S;
		else if(t.ammo == CItemType.TERTAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_PISTOLSHOT_S;
		else if(h.type == CItemType.BBAT)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_BATSWING_S;
		else if(h.type == CItemType.KNIFE)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_KNIFESTAB_S;
	    
		if(t.shotSound.size() > 0)
			t.shotSound.get( (int)Math.round(Math.random()*t.shotSound.size()) ).Play();
	    
		CCamera c = e.camera;
	    
		CVector3 d = Math3D.Normalize(Math3D.Subtract(c.View(), c.Position()));
		CVector3 horizontal = Math3D.Normalize(Math3D.Cross( new CVector3(0, 1, 0), d ));
		CVector3 vertical = Math3D.Normalize(Math3D.Cross( horizontal, d ));
		CVector3 vLine[] = new CVector3[2];
		vLine[0] = c.Position();
		CVector3 vert;
		CVector3 horiz;
		CVector3 o;
	    
		CEntity e2;
		int hit;
		CVector3 trace;
	    
		for(int s=0; s<t.split; s++)
		{
			horiz = Math3D.Multiply(horizontal, (float)(t.inacc * (Math.random()*1000-500)/500.0f));
			vert = Math3D.Multiply(vertical, (float)(t.inacc * (Math.random()*1000-500)/500.0f));
			vLine[1] = Math3D.Add(Math3D.Add(c.Position(), Math3D.Multiply(d, t.range)), Math3D.Add(horiz, vert));
	        
			if(mMap.BreakFaces(vLine[0], vLine[1]))
				continue;
	        
			o = vLine[1] = mMap.TraceRay(vLine[0], vLine[1]);
	        
			hit = -1;
	        
			for(int i=0; i<ENTITIES; i++)
			{
				e2 = mEntity[i];
	            
				if(!e2.on)
					continue;
	            
				if(!mMap.IsClusterVisible(e.cluster, e2.cluster))
					continue;
	            
				if(i == p.entity)
					continue;
	            
				trace = e2.TraceRay(vLine);
	            
				if(trace == vLine[1])
					continue;
	            
				hit = i;
				vLine[1] = trace;
			}
	        
			if(hit < 0)
			{
				if(mMap.Collided())
				{
					PlaceDecal(CDecalType.BULLETHOLE, vLine[1], mMap.CollisionNormal());
	                
					if(t.hitSound.size() > 0)
						t.hitSound.get( (int)Math.round(Math.random()*t.hitSound.size()) ).Play();
				}
	            
				continue;
			}

			if(t.hitSound.size() > 0)
				t.hitSound.get( (int)Math.round(Math.random()*t.hitSound.size()) ).Play();
	        
			e2 = mEntity[hit];
			c = e2.camera;
	        
			CVector3 v = c.Velocity();
			c.Velocity( Math3D.Add(v, Math3D.Multiply(d, 100.0f)) );
	        
			if(e2.controller < 0)
				return;
	        
			if(!IsZombie(e2.type))
				return;
	        
			CPlayer p2 = mPlayer[e2.controller];
	        
			if(p2.hp > 0.0f)
			{
				float damage = t.damage;
	            
				if(vLine[1].y >= c.Position().y + CEntity.HEAD_OFFSET)
					damage *= 100.0f;
	            
				Damage(p2, damage, true);
			}
	        
			for(int i=0; i<30; i++)
				EmitParticle(CParticleType.BLOODPART, vLine[1]);
	        
			// TO DO
		}
	}
	
	void EquipFrame(CPlayer p, int hold, CItemType t)
	{
		CEntity e = mEntity[p.entity];
	    
		if(t == null)
			e.frame[CEntity.BODY_UPPER].value = 0;
		else if(t.ammo == CItemType.PRIMARYAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTSHOULDER_E;
		else if(t.ammo == CItemType.SECONDARYAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTGUNSHOT_E;
		else if(t.ammo == CItemType.TERTAMMO)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_PISTOLSHOT_E;
		else if(p.items.get(hold).type == CItemType.BBAT)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_BATSWING_E;
		else if(p.items.get(hold).type == CItemType.KNIFE)
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_KNIFESTAB_E;
	}

	void DoneReload(int player)
	{
		CPlayer p = mPlayer[player];
		p.reload = false;
		CHold h = p.items.get(p.equipped);
		CItemType t = mItemType[h.type];
		EquipFrame(p, p.equipped, t);
	}
	
	void Equip(CPlayer p, int hold, CItemType t)
	{
		p.equipped = hold;
		EquipFrame(p, hold, t);
	    
		if(p == mPlayer[mLocalP])
			mGUI.RedoAmmo();
	}
	
	void EquipAny(CPlayer p)
	{
		CHold h;
		CItemType t;
	    
		for(int i=0; i<p.items.size(); i++)
		{
			h = p.items.get(i);
			t = mItemType[h.type];
	        
			if(!t.equip)
				continue;
	        
			p.equipped = i;
			EquipFrame(p, i, t);
			return;
		}
	    
		EquipFrame(p, -1, null);
	}

	boolean IsAmmo(int item)
	{
		if(item == CItemType.PRIMARYAMMO)
			return true;
	    
		if(item == CItemType.SECONDARYAMMO)
			return true;
	    
		if(item == CItemType.TERTAMMO)
			return true;
	    
		return false;
	}
	
	float SubtractItem(CPlayer p, int item, float amount)
	{
		CHold h = null;
		int i;
	    
		for(i=0; i<p.items.size(); i++)
		{
			if(p.items.get(i).type != item)
				continue;
	        
			h = p.items.get(i);
			break;
		}
	    
		if(h == null)
			return 0;
	    
		if(h.amount < amount)
			amount = h.amount;
	    
		h.amount -= amount;
	    
		if(h.amount <= 0.0f)
		{
			p.items.remove( i );
	        
			if(p.equipped > i)
				p.equipped--;
			else if(p.equipped == i)
				EquipAny(p);
		}
	    
		if(p == mPlayer[mLocalP] && IsAmmo(item))
			mGUI.RedoAmmo();
	    
		return amount;
	}
	
	boolean HasAmmo(CPlayer p, int ammo)
	{
		CHold h;
	    
		for(int i=0; i<p.items.size(); i++)
		{
			h = p.items.get(i);
	        
			if(h.type != ammo)
				continue;
	        
			if(h.amount < 1.0f)
				continue;
	        
			return true;
		}
	    
		return false;
	}

	void Reload(int player)
	{
		CPlayer p = mPlayer[player];
		CHold h = p.items.get(p.equipped);
		CItemType t = mItemType[h.type];
	    
		float amount = SubtractItem(p, t.ammo, Math.min(t.reloadrate, t.clip - h.clip));
		h = p.items.get(p.equipped); //item might have shifted
		h.clip += amount;
	    
		if(h.clip >= t.clip || !HasAmmo(p, t.ammo))	// Done reloading?
		{
			if(h.type == CItemType.MOSSBERG500)	// Need to cock gun?
			{
				CEntity e = mEntity[p.entity];
				e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTGUNCOCK_S;
	            
				if(t.cockSound.size() > 0)
					t.cockSound.get( (int)Math.round(Math.random()*t.cockSound.size()) ).Play();
			}
			else	// Assume aiming stance
				DoneReload(player);
		}
		else if(h.type == CItemType.MOSSBERG500)	// Continuing to reload shotgun?
		{
			CEntity e = mEntity[p.entity];
			e.frame[CEntity.BODY_UPPER].value = Animation.ANIM_SHOTGUNRELD_M;
	        
			if(t.reloadSound.size() > 0)
				t.reloadSound.get( (int)Math.round(Math.random()*t.reloadSound.size()) ).Play();
		}
	    
		if(p == mPlayer[mLocalP])
			mGUI.RedoAmmo();
	}

	void UpdatePlayers()
	{
	    CPlayer p;
		float maxhp;
		float maxstamina;
		CHold h;
		CItemType t;
		CEntity e;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        
	        if(!p.on)
	            continue;
	        
	        if(p.hp <= 0.0f)
			{
				UpdateDead(p);
				continue;
			}
	        
	        maxhp = p.MaxHP();
	        if(p.hp < maxhp)
	        {
	            p.hp += p.HPRegen() * FRAME_INTERVAL;
	            if(p.hp > maxhp)
	                p.hp = maxhp;
	            
	            if(p == mPlayer[mLocalP])
	                mGUI.RedoHP();
	        }
	        
			maxstamina = p.MaxStamina();
			if(p.run && !p.crouched)
			{
				p.stamina -= RUN_DSTAMINA * FRAME_INTERVAL;
				if(p.stamina < 0.0f)
				{
					p.stamina = 0.0f;
					p.run = false;
				}
	            
				if(p == mPlayer[mLocalP])
					mGUI.RedoStamina();
			}
			else if(p.stamina < maxstamina)
			{
				p.stamina += p.StaminaRegen() * FRAME_INTERVAL;
				if(p.stamina > maxstamina)
					p.stamina = maxstamina;
	            
				if(p == mPlayer[mLocalP])
					mGUI.RedoStamina();
			}
	        
			if(p.shoot && p.equipped >= 0)
			{
				h = p.items.get(p.equipped);
				t = mItemType[h.type];
				e = mEntity[p.entity];
	            
				if(h.clip >= 1.0f)
				{
					Shot(i);
				}
				else
					p.shoot = false;
			}
	    }
	}

	void Animate()
	{
	    CPlayer p;
	    CEntity e;
	    CEntityType t;
	    int leftright;
	    int forwardback;
		float animrate;
		CHold h;
		CItemType iT;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = mPlayer[i];
	        if(!p.on)
	            continue;
	        
	        if(p.entity < 0)
	            continue;
	        
	        leftright = 0;
	        forwardback = 0;
	        
	        e = mEntity[p.entity];
			t = mEntityType.get(e.type);
	        
	        if(p.forward)
	            forwardback ++;
	        if(p.backward)
	            forwardback --;
	        if(p.left)
	            leftright --;
	        if(p.right)
	            leftright ++;
	        
			animrate = t.animrate;
	        
			if(p.crouched)
				animrate /= 2.0f;
			else if(p.run && p.stamina > 0.0f)
				animrate *= 2.0f;
	        
			if(p.hp > 0.0f)
			{
				if(!p.crouched)
				{
					if(forwardback == 0 && leftright == 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALK_S, Animation.ANIM_WALK_S, true, animrate);
					else if(forwardback > 0 && leftright == 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALK_S, Animation.ANIM_WALK_E, true, animrate);
					else if(forwardback < 0 && leftright == 0)  CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALK_S, Animation.ANIM_WALK_E, true, animrate);
					else if(forwardback == 0 && leftright > 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_STRAFER_S, Animation.ANIM_STRAFER_E, true, animrate);
					else if(forwardback == 0 && leftright < 0)  CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_STRAFER_S, Animation.ANIM_STRAFER_E, true, animrate);
					else if(forwardback > 0 && leftright > 0)   CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALKFWR_S, Animation.ANIM_WALKFWR_E, true, animrate);
					else if(forwardback > 0 && leftright < 0)   CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALKFWL_S, Animation.ANIM_WALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright > 0)   CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALKFWL_S, Animation.ANIM_WALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright < 0)   CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_WALKFWR_S, Animation.ANIM_WALKFWR_E, true, animrate);
				}
				else
				{
					if(forwardback == 0 && leftright == 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALK_S, Animation.ANIM_CWALK_S, true, animrate);
					else if(forwardback > 0 && leftright == 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALK_S, Animation.ANIM_CWALK_E, true, animrate);
					else if(forwardback < 0 && leftright == 0)  CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALK_S, Animation.ANIM_CWALK_E, true, animrate);
					else if(forwardback == 0 && leftright > 0)  CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CSTRAFER_S, Animation.ANIM_CSTRAFER_E, true, animrate);
					else if(forwardback == 0 && leftright < 0)  CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CSTRAFER_S, Animation.ANIM_CSTRAFER_E, true, animrate);
					else if(forwardback > 0 && leftright > 0)   CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALKFWR_S, Animation.ANIM_CWALKFWR_E, true, animrate);
					else if(forwardback > 0 && leftright < 0)   CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALKFWL_S, Animation.ANIM_CWALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright > 0)   CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALKFWL_S, Animation.ANIM_CWALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright < 0)   CModel.PlayAnimationB(e.frame[CEntity.BODY_LOWER], Animation.ANIM_CWALKFWR_S, Animation.ANIM_CWALKFWR_E, true, animrate);
				}
			}
	        
			if(p.hp <= 0.0f)
			{
				if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_UDEATHFW_S && e.frame[CEntity.BODY_UPPER].value <= Animation.ANIM_UDEATHFW_E)
				{
					CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_UDEATHFW_S, Animation.ANIM_UDEATHFW_E, false, 1.0f);
					CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_LDEATHFW_S, Animation.ANIM_LDEATHFW_E, false, 1.0f);
				}
				else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_UDEATHBW_S && e.frame[CEntity.BODY_UPPER].value <= Animation.ANIM_UDEATHBW_E)
				{
					CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_UDEATHBW_S, Animation.ANIM_UDEATHBW_E, false, 1.0f);
					CModel.PlayAnimation(e.frame[CEntity.BODY_LOWER], Animation.ANIM_LDEATHBW_S, Animation.ANIM_LDEATHBW_E, false, 1.0f);
				}
			}
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_ZGRASP_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_ZGRASP_E)					CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_ZGRASP_S, Animation.ANIM_ZGRASP_E, false, 1.0f);
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_SHOTSHOULDER_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_SHOTSHOULDER_E)		CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_SHOTSHOULDER_S, Animation.ANIM_SHOTSHOULDER_E, false, Animation.ANIM_SHOTSHOULDER_R);
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_PISTOLSHOT_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_PISTOLSHOT_E)			CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_PISTOLSHOT_S, Animation.ANIM_PISTOLSHOT_E, false, 1.0f);
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_SHOTGUNSHOT_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_SHOTGUNSHOT_E)		CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_SHOTGUNSHOT_S, Animation.ANIM_SHOTGUNSHOT_E, false, Animation.ANIM_SHOTGUNSHOT_R);
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_BATSWING_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_BATSWING_E)				CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_BATSWING_S, Animation.ANIM_BATSWING_E, false, 1.0f);
			else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_KNIFESTAB_S && e.frame[CEntity.BODY_UPPER].value < Animation.ANIM_KNIFESTAB_E)			CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_KNIFESTAB_S, Animation.ANIM_KNIFESTAB_E, false, 1.0f);
			else if(p.reload)
			{
				h = p.items.get(p.equipped);
				iT = mItemType[h.type];
	            
				if(iT.ammo == CItemType.PRIMARYAMMO && CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_RIFLERELOAD_S, Animation.ANIM_RIFLERELOAD_E, false, Animation.ANIM_RIFLERELOAD_R))
					Reload(i);
				if(iT.ammo == CItemType.SECONDARYAMMO)
				{
					if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_SHOTGUNRELD_S && e.frame[CEntity.BODY_UPPER].value <= Animation.ANIM_SHOTGUNRELD_E)
					{
						if(CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_SHOTGUNRELD_S, Animation.ANIM_SHOTGUNRELD_E, false, Animation.ANIM_SHOTGUNRELD_R))
							Reload(i);
					}
					else if(e.frame[CEntity.BODY_UPPER].value >= Animation.ANIM_SHOTGUNCOCK_S && e.frame[CEntity.BODY_UPPER].value <= Animation.ANIM_SHOTGUNCOCK_E)
					{
						if(CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_SHOTGUNCOCK_S, Animation.ANIM_SHOTGUNCOCK_E, false, Animation.ANIM_SHOTGUNRELD_R))
							DoneReload(i);
					}
				}
				if(iT.ammo == CItemType.TERTAMMO && CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_PISTOLRLD_S, Animation.ANIM_PISTOLRLD_E, false, 1.0f))
					Reload(i);
			}
			else if(p.pain)
			{
				if(CModel.PlayAnimation(e.frame[CEntity.BODY_UPPER], Animation.ANIM_PAIN_S, Animation.ANIM_PAIN_E, false, 1.0f))
				{
					e.frame[CEntity.BODY_UPPER].value = 0;
					p.pain = false;
				}
			}
	    }
	}
	
	public void LoadFonts()
	{
		mFont[CFont.MSGOTHIC16] = new CFont(this, "fonts/msgothic16");
		mFont[CFont.MSGOTHIC16B] = new CFont(this, "fonts/msgothic16b");
		mFont[CFont.MSUIGOTHIC16] = new CFont(this, "fonts/msuigothic16");
	}
	
    public void Init()
    {
    	for(int i=0; i<TEXTURES; i++)
    		mTexture[i] = new CTexture();
    	
    	for(int i=0; i<MODELS; i++)
    		mModel[i] = new CModel();
    	
    	for(int i=0; i<ENTITIES; i++)
    		mEntity[i] = new CEntity(this);
    	
    	mFrustum = new CFrustum();
    	
    	for(int i=0; i<SCRIPT_FUNCS; i++)
    		mScriptFunc[i] = new CFuncPtr(this);
    	
    	for(int i=0; i<BILLBOARDS; i++)
    		mBillb[i] = new CBillboard();
    	
    	for(int i=0; i<PARTICLES; i++)
    		mParticle[i] = new CParticle(this);
    	
    	for(int i=0; i<DECALS; i++)
    		mDecal[i] = new CDecal();
    	
    	mRenderer.mTriangle = new Triangle();
    	//mRenderer.mTriangle.mTextureDataHandle = CreateTexture("textures/texture", true);
    	//rotational = CreateTexture("gui/rotational", true);
    	
    	mShader[CShader.MODEL] = new CShader(this, "model.vert", "model.frag");
    	mShader[CShader.MAP] = new CShader(this, "map.vert", "map.frag");
    	mShader[CShader.SKY] = new CShader(this, "sky.vert", "sky.frag");
    	mShader[CShader.ORTHO] = new CShader(this, "ortho.vert", "ortho.frag");
    	mShader[CShader.BILLBOARD] = new CShader(this, "billboard.vert", "billboard.frag");
    	
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    	Display display = wm.getDefaultDisplay();
    	mWidth = display.getWidth();
    	mHeight = display.getHeight();
    	
    	if(mHeight > 320)
    		mRetinaScale = 2.0f;
    	//mRetinaScale = Math.max(1, (int)(mHeight/320));
    	
    	System.out.println("w,h = " + mWidth + "," + mHeight);
    	System.out.println("sc = " + mRetinaScale);
    	
    	LoadFonts();
    	
    	mGUI = new CGUI(this);
    }
    
    public void Deinit()
    {
    	FreeTextures();
    }
    
    void UpdateGameState()
    {
    	UpdateTicks();
    	
    	Unforward();
        Unback();
        Unleft();
        Unright();
        
        for(int i=0; i<mTouch.size(); i++)
        {
            CGPoint* touch = &g_touch[i];
            mGUI.touchframe(touch.x, touch.y);
        }
        
        UpdateAI();
        UpdatePlayers();
        Animate();
        Physics();
        UpdateParticles();
        UpdateDecals();
        UpdateGUI();
        ProjectAction();
        UpdateObjects();
        CheckFuncs();
    }
    
    void Update()
    {
    	
    }
    
    public void Draw()
    {
    	Update();
    	
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //public static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ);

        /*
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        //Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mViewMatrix, 0);

        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        //Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        //Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
        
        Matrix.setRotateM(mModelMatrix, 0, 0, 0, 0, 1.0f);
        
        CShader s = mShader[CShader.MODEL];
        s.Use();
        GLES20.glUniformMatrix4fv(s.slot[CShader.PROJMAT], 1, false, mProjMatrix, 0);
        GLES20.glUniformMatrix4fv(s.slot[CShader.VIEWMAT], 1, false, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(s.slot[CShader.MODELMAT], 1, false, mModelMatrix, 0);
		GLES20.glEnableVertexAttribArray(s.slot[CShader.POSITION]);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.TEXCOORD]);
        mRenderer.mTriangle.draw(s);
        
        s = mShader[CShader.ORTHO];
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        s.Use();
        GLES20.glUniform1f(s.slot[CShader.WIDTH], (float)mWidth);
        GLES20.glUniform1f(s.slot[CShader.HEIGHT], (float)mHeight);
        GLES20.glUniform4f(s.slot[CShader.COLOR], 1, 1, 1, 1);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.POSITION]);
        GLES20.glEnableVertexAttribArray(s.slot[CShader.TEXCOORD]);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        mGUI.draw();
        //DrawShadowedText(MSGOTHIC16, 0, 0, "Hello world. My name is Denis.");
        //mGUI.DrawImage(rotational, 0, 0, mWidth, mHeight);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        */
        
        
    }
    
    public void Resize(int width, int height)
    {
    	mWidth = width;
    	mHeight = height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        //public static void frustumM(float[] m, int offset, float left, float right, float bottom, float top, float near, float far);
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, MIN_D, MAX_D);
    }
    
    int RandomHuman()
    {
    	Vector<Integer> h = new Vector<Integer>();
    	CEntityType t;
        
    	for(int i=0; i<mEntityType.size(); i++)
    	{
    		t = mEntityType.get(i);
            
    		if(t.category != CEntity.HUMAN)
    			continue;
            
    		h.add(new Integer(i));
    	}
        
    	return h.get( (int)Math.round(Math.random()*h.size()) );
    }

    int RandomZombie()
    {
    	Vector<Integer> z = new Vector<Integer>();
    	CEntityType t;
        
    	for(int i=0; i<mEntityType.size(); i++)
    	{
    		t = mEntityType.get(i);
            
    		if(t.category != CEntity.ZOMBIE)
    			continue;
            
    		z.add(new Integer(i));
    	}
        
    	return z.get( (int)Math.round(Math.random()*z.size()) );
    }
    
    void SpawnPlayer()
    {
    	CPlayer p = mPlayer[mLocalP];
    	p.on = true;
    	p.ai = false;
    	p.stamina = 1;
    	CSpawn spawn = mMap.mSSpawn.get(0);
    	Integer e = new Integer(0);
        
    	PlaceEntity(RandomHuman(), mLocalP, -1, -1, spawn.pos, spawn.angle, e, false, -1);
        
    	mCamera = mEntity[e.intValue()].camera;	
    }
    
    void SpawnZombies()
    {
    	for(int i=0; i<mMap.mZSpawn.size(); i++)
        {
            CSpawn spawn = mMap.mZSpawn.get(i);
            PlaceEntity(RandomZombie(), NewAI(spawn.activity), -1, -1, spawn.pos, spawn.angle, null, false, spawn.script);
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(  
                WindowManager.LayoutParams.FLAG_FULLSCREEN,  
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mGLView = new MyGLSurfaceView(this);
        mRenderer = new MyGL20Renderer();
        mRenderer.mActivity = this;
        mGLView.setRenderer(mRenderer);
        setContentView(mGLView);

        /*
    	AssetManager am = getAssets();
    	try
    	{
    		System.out.println(am.list("gui/"));
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}*/
    }
    
    @Override
    protected void onStop()
    {
    	Deinit();
    	finish();
    	super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    public int NewTexture()
    {
    	for(int i=0; i<TEXTURES; i++)
    		if(!mTexture[i].on)
    			return i;
    	
    	return -1;
    }
    
    public int FindTexture(String strFileName)
    {
    	for(int i=0; i<TEXTURES; i++)
    		if(mTexture[i].on && mTexture[i].file.equals(strFileName))
    		{
    			//mLastTexTransp = mTexture[i].transp;
    	    	mTexWidth = mTexture[i].width;
    	    	mTexHeight = mTexture[i].height;
    			//System.out.println("found " + mTexture[i].file + " = " + mTexture[i].tex[0]);
    			return mTexture[i].tex[0];
    		}
    	
    	return -1;
    }
    
    public String FindTextureExtension(String strFileName)
    {
    	AssetManager am = getAssets();
    	
    	String jpgpath = strFileName + ".jpg";
    	String pngpath = strFileName + ".png";
    	boolean found = true;
        InputStream is = null;
        
        try 
        {
        	is = am.open(jpgpath);
        } 
        catch (final IOException e) 
        {
            found = false;
        }
        
        if(found)
        {
        	mLastTexTransp = false;
        	return jpgpath;
        }
        found = true;
    	
        try 
        {
        	is = am.open(pngpath);
        } 
        catch (final IOException e) 
        {
            found = false;
        }
      
        if(found)
        {
        	mLastTexTransp = true;
        	return pngpath;
        }
        found = true;
    	
    	return strFileName;
    }
    
    public int CreateTexture(String strFileName, boolean search)
    {
    	int texture = 0;
    	strFileName = FindTextureExtension(strFileName);
    	
    	if(search && (texture = FindTexture(strFileName)) >= 0)
    		return texture;
    		
    	int i = NewTexture();
    	mTexture[i].Load(this, strFileName);
    	//mLastTexTransp = mTexture[i].transp;
    	
    	mTexWidth = mTexture[i].width;
    	mTexHeight = mTexture[i].height;
    	
    	//System.out.println("load " + strFileName + " = " + mTexture[i].tex[0]);
    	
    	return mTexture[i].tex[0];
    }
    
    public void FreeTextures()
    {
    	for(int i=0; i<TEXTURES; i++)
    	{
    		if(!mTexture[i].on)
    			continue;
    		
    		GLES20.glDeleteTextures(1, mTexture[i].tex, 0);
    	}
    }
    
    void FreeTexture(String name)
    {
    	//String nameStripped = CFile.StripExtension(name);
    	//String typed = FindTextureExtension(nameStripped);
    	String typed = FindTextureExtension(name);
        //NSString* fullName = [[NSString alloc] initWithFormat:@"%@.%@", nameStripped, type];
        
    	for(int i=0; i<TEXTURES; i++)
        {
            if(!mTexture[i].on)
                continue;
            
            //NSLog(@"Test %d", i);
            //NSLog(@"Testing texture %s / %s", filepath, [g_texture[i].filepath UTF8String]);
    		//if(g_texture[i].on && stricmp([g_texture[i].filepath UTF8String], filepath) == 0)
            if(typed.equalsIgnoreCase(mTexture[i].file))
    		{
                
                //NSLog(@"Freeing texture %s / %s", filepath, [g_texture[i].filepath UTF8String]);
                
    			mTexture[i].on = false;
    			IntBuffer ib = IntBuffer.allocate(1);
    			ib.put(mTexture[i].tex);
    			GLES20.glDeleteTextures(1, ib);
    			//g_log<<"Found texture "<<filepath<<" ("<<texture<<")"<<endl;
    			return;
    		}
        }
        
        //NSLog(@"Couldn't free texture %s / %@", filepath, fullName);
    }
    
    /** Called when the user clicks the Send button */
    public void sendMessage(View view) 
    {
    	//System.out.println("Welcome To My First Java Program ");
        // Do something in response to button
    	//Intent intent = new Intent(this, DisplayMessageActivity.class);
    	//EditText editText = (EditText) findViewById(R.id.edit_message);
    	//String message = editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
    	//System.out.println("1:" + message);
    	//startActivity(intent);
    }
}
