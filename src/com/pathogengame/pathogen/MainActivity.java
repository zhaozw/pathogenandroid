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
import java.util.Vector;

import android.view.View;

public class MainActivity extends Activity 
{
	public static final float MIN_D = 1.0f;
	public static final float MAX_D = 9000.0f;
	public static final int TEXTURES = 128;
	
	public static final int MOV_THRESH = 15;
	
	public static final int FRAME_RATE = 30;
	
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
	
	CQuake3BSP mMap;
	int mScore = 0;
	boolean mArrest = false;
	CCamera mCamera;
	
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
	
	//int rotational;
	
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
	
	int NewAI()
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
	}
	
	boolean Unobstructed(CCamera zc, CVector3 pos, CEntity ignore1, CEntity ignore2)
	{
		CVector3 trace = mMap.TraceRay(zc.Position(), pos);
		if(!Math3D.Equals(trace, pos))
			return false;
	    
		CEntity e;
		CVector3 vLine[2];
		vLine[0] = zc->Position();
		vLine[1] = pos;
		int cluster = g_map.FindCluster(zc->Position());
	    CEntityType* t;
	    
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
	        
			if(!mMap.IsClusterVisible(cluster, e->cluster))
				continue;
	        
	        t = mEntityType[e.type];
	        
	        if(t.category != ENTITY.DOOR)
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
		if(!WithinAngle(zc, pos, Math3D.DEGTORAD(Z_FOV/2.0f)))
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
	        
	        D2 = Magnitude2(Math3D.Subtract(c.Position(), zc.Position()));
	        
			//check distance
	        if(D2 > nearestD2)
	            continue;
	        
			if(!Visible(zc, c.Position(), e, ze))
				continue;
	        
			t = mEntityType[e.type];
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

	void UpdateAI(CPlayer p)
	{
	    if(p.entity < 0)
	        return;
	    
		if(p.activity == ACTIVITY.NOACT)
			return;
	    
	    CEntity e = mEntity[p.entity];
	    CCamera c = e.camera;
	    
		if(p.activity == ACTIVITY.ONSIGHT && !HumanVisible(c, e))
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
	    
		p->target = target;
		float dyaw;
	    
	    //return;
	    
	    if(target < 0)
	    {
			CVector3 d = c->Position() - p->goal;
			d.y = 0;
			if(Magnitude2(d) < 100)
				NewGoal(p);
	        
			if(WithinAngle(c, p->goal, DEGTORAD(Z_TURN_RATE*2.0f)))
				p->forward = true;
			else
				p->forward = false;
	        
			dyaw = DYaw(c, p->goal);
	    }
		else
		{
			CPlayer* p2 = &g_player[target];
			CEntity* e2 = &g_entity[p2->entity];
			CCamera* c2 = &e2->camera;
	        
			p->goal = c2->Position();
			p->forward = true;
	        
			dyaw = DYaw(c, c2->Position());
	        
			if(Visible(c, c2->Position(), e2, e) && Magnitude2(c->Position() - c2->Position()) <= GRASP_D * GRASP_D)
				Grasp(p, e, p2);
		}
	    
		if(fabs(dyaw) < DEGTORAD(Z_TURN_RATE*2.0f))
			c->View(p->goal);
		else if(dyaw < 0.0f)
			c->RotateView(-DEGTORAD(Z_TURN_RATE), 0, 1, 0);
		else if(dyaw > 0.0f)
			c->RotateView(DEGTORAD(Z_TURN_RATE), 0, 1, 0);
	}

	void UpdateAI()
	{
	    CPlayer* p;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = &g_player[i];
	        
	        if(!p->on)
	            continue;
	        
	        if(!p->ai)
	            continue;
	        
			if(p->hp <= 0.0f)
				continue;
	        
	        UpdateAI(p);
	    }
	}

	void UpdateDead(CPlayer* p)
	{
		CEntity* e = &g_entity[p->entity];
	    
		if(!IsZombie(e->type))
			return;
	    
		p->ticksleft --;
	    
		if(p->ticksleft <= 0)
		{
			e->on = false;
			p->on = false;
		}
	}

	void UpdatePlayers()
	{
	    CPlayer* p;
		float maxhp;
		float maxstamina;
		CHold* h;
		CItemType* t;
		CEntity* e;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = &g_player[i];
	        
	        if(!p->on)
	            continue;
	        
	        if(p->hp <= 0.0f)
			{
				UpdateDead(p);
				continue;
			}
	        
	        maxhp = p->MaxHP();
	        if(p->hp < maxhp)
	        {
	            p->hp += p->HPRegen() * g_FrameInterval;
	            if(p->hp > maxhp)
	                p->hp = maxhp;
	            
	            if(p == &g_player[g_localP])
	                RedoHP();
	        }
	        
			maxstamina = p->MaxStamina();
			if(p->run && !p->crouched)
			{
				p->stamina -= RUN_DSTAMINA * g_FrameInterval;
				if(p->stamina < 0.0f)
				{
					p->stamina = 0.0f;
					p->run = false;
				}
	            
				if(p == &g_player[g_localP])
					RedoStamina();
			}
			else if(p->stamina < maxstamina)
			{
				p->stamina += p->StaminaRegen() * g_FrameInterval;
				if(p->stamina > maxstamina)
					p->stamina = maxstamina;
	            
				if(p == &g_player[g_localP])
					RedoStamina();
			}
	        
			if(p->shoot && p->equipped >= 0)
			{
				h = &p->items[p->equipped];
				t = &g_itemType[h->type];
				e = &g_entity[p->entity];
	            
				if(h->clip >= 1.0f)
				{
					Shot(i);
				}
				else
					p->shoot = false;
			}
	    }
	}

	void Animate()
	{
	    CPlayer* p;
	    CEntity* e;
	    CEntityType* t;
	    int leftright;
	    int forwardback;
		float animrate;
		CHold* h;
		CItemType* iT;
	    
	    for(int i=0; i<PLAYERS; i++)
	    {
	        p = &g_player[i];
	        if(!p->on)
	            continue;
	        
	        if(p->entity < 0)
	            continue;
	        
	        leftright = 0;
	        forwardback = 0;
	        
	        e = &g_entity[p->entity];
			t = &g_entityType[e->type];
	        
	        if(p->forward)
	            forwardback ++;
	        if(p->backward)
	            forwardback --;
	        if(p->left)
	            leftright --;
	        if(p->right)
	            leftright ++;
	        
			animrate = t->animrate;
	        
			if(p->crouched)
				animrate /= 2.0f;
			else if(p->run && p->stamina > 0.0f)
				animrate *= 2.0f;
	        
			if(p->hp > 0.0f)
			{
				if(!p->crouched)
				{
					if(forwardback == 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_S, true, animrate);
					else if(forwardback > 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_E, true, animrate);
					else if(forwardback < 0 && leftright == 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALK_S, ANIM_WALK_E, true, animrate);
					else if(forwardback == 0 && leftright > 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_STRAFER_S, ANIM_STRAFER_E, true, animrate);
					else if(forwardback == 0 && leftright < 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_STRAFER_S, ANIM_STRAFER_E, true, animrate);
					else if(forwardback > 0 && leftright > 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_WALKFWR_S, ANIM_WALKFWR_E, true, animrate);
					else if(forwardback > 0 && leftright < 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_WALKFWL_S, ANIM_WALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright > 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALKFWL_S, ANIM_WALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright < 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_WALKFWR_S, ANIM_WALKFWR_E, true, animrate);
				}
				else
				{
					if(forwardback == 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_S, true, animrate);
					else if(forwardback > 0 && leftright == 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_E, true, animrate);
					else if(forwardback < 0 && leftright == 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALK_S, ANIM_CWALK_E, true, animrate);
					else if(forwardback == 0 && leftright > 0)  PlayAnimation(e->frame[BODY_LOWER], ANIM_CSTRAFER_S, ANIM_CSTRAFER_E, true, animrate);
					else if(forwardback == 0 && leftright < 0)  PlayAnimationB(e->frame[BODY_LOWER], ANIM_CSTRAFER_S, ANIM_CSTRAFER_E, true, animrate);
					else if(forwardback > 0 && leftright > 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALKFWR_S, ANIM_CWALKFWR_E, true, animrate);
					else if(forwardback > 0 && leftright < 0)   PlayAnimation(e->frame[BODY_LOWER], ANIM_CWALKFWL_S, ANIM_CWALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright > 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALKFWL_S, ANIM_CWALKFWL_E, true, animrate);
					else if(forwardback < 0 && leftright < 0)   PlayAnimationB(e->frame[BODY_LOWER], ANIM_CWALKFWR_S, ANIM_CWALKFWR_E, true, animrate);
				}
			}
	        
			if(p->hp <= 0.0f)
			{
				if(e->frame[BODY_UPPER] >= ANIM_UDEATHFW_S && e->frame[BODY_UPPER] <= ANIM_UDEATHFW_E)
				{
					PlayAnimation(e->frame[BODY_UPPER], ANIM_UDEATHFW_S, ANIM_UDEATHFW_E, false, 1.0f);
					PlayAnimation(e->frame[BODY_LOWER], ANIM_LDEATHFW_S, ANIM_LDEATHFW_E, false, 1.0f);
				}
				else if(e->frame[BODY_UPPER] >= ANIM_UDEATHBW_S && e->frame[BODY_UPPER] <= ANIM_UDEATHBW_E)
				{
					PlayAnimation(e->frame[BODY_UPPER], ANIM_UDEATHBW_S, ANIM_UDEATHBW_E, false, 1.0f);
					PlayAnimation(e->frame[BODY_LOWER], ANIM_LDEATHBW_S, ANIM_LDEATHBW_E, false, 1.0f);
				}
			}
			else if(e->frame[BODY_UPPER] >= ANIM_ZGRASP_S && e->frame[BODY_UPPER] < ANIM_ZGRASP_E)					PlayAnimation(e->frame[BODY_UPPER], ANIM_ZGRASP_S, ANIM_ZGRASP_E, false, 1.0f);
			else if(e->frame[BODY_UPPER] >= ANIM_SHOTSHOULDER_S && e->frame[BODY_UPPER] < ANIM_SHOTSHOULDER_E)		PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTSHOULDER_S, ANIM_SHOTSHOULDER_E, false, ANIM_SHOTSHOULDER_R);
			else if(e->frame[BODY_UPPER] >= ANIM_PISTOLSHOT_S && e->frame[BODY_UPPER] < ANIM_PISTOLSHOT_E)			PlayAnimation(e->frame[BODY_UPPER], ANIM_PISTOLSHOT_S, ANIM_PISTOLSHOT_E, false, 1.0f);
			else if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNSHOT_S && e->frame[BODY_UPPER] < ANIM_SHOTGUNSHOT_E)		PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNSHOT_S, ANIM_SHOTGUNSHOT_E, false, ANIM_SHOTGUNSHOT_R);
			else if(e->frame[BODY_UPPER] >= ANIM_BATSWING_S && e->frame[BODY_UPPER] < ANIM_BATSWING_E)				PlayAnimation(e->frame[BODY_UPPER], ANIM_BATSWING_S, ANIM_BATSWING_E, false, 1.0f);
			else if(e->frame[BODY_UPPER] >= ANIM_KNIFESTAB_S && e->frame[BODY_UPPER] < ANIM_KNIFESTAB_E)			PlayAnimation(e->frame[BODY_UPPER], ANIM_KNIFESTAB_S, ANIM_KNIFESTAB_E, false, 1.0f);
			else if(p->reload)
			{
				h = &p->items[p->equipped];
				iT = &g_itemType[h->type];
	            
				if(iT->ammo == ITEM::PRIMARYAMMO && PlayAnimation(e->frame[BODY_UPPER], ANIM_RIFLERELOAD_S, ANIM_RIFLERELOAD_E, false, ANIM_RIFLERELOAD_R))
					Reload(i);
				if(iT->ammo == ITEM::SECONDARYAMMO)
				{
					if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNRELD_S && e->frame[BODY_UPPER] <= ANIM_SHOTGUNRELD_E)
					{
						if(PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNRELD_S, ANIM_SHOTGUNRELD_E, false, ANIM_SHOTGUNRELD_R))
							Reload(i);
					}
					else if(e->frame[BODY_UPPER] >= ANIM_SHOTGUNCOCK_S && e->frame[BODY_UPPER] <= ANIM_SHOTGUNCOCK_E)
					{
						if(PlayAnimation(e->frame[BODY_UPPER], ANIM_SHOTGUNCOCK_S, ANIM_SHOTGUNCOCK_E, false, ANIM_SHOTGUNRELD_R))
							DoneReload(i);
					}
				}
				if(iT->ammo == ITEM::TERTAMMO && PlayAnimation(e->frame[BODY_UPPER], ANIM_PISTOLRLD_S, ANIM_PISTOLRLD_E, false, 1.0f))
					Reload(i);
			}
			else if(p->pain)
			{
				if(PlayAnimation(e->frame[BODY_UPPER], ANIM_PAIN_S, ANIM_PAIN_E, false, 1.0f))
				{
					e->frame[BODY_UPPER] = 0;
					p->pain = false;
				}
			}
	    }
	}

	void GameOver()
	{
	    g_arrest = true;
	    mGUI.OpenAnotherView("game over");
	    g_viewmode = THIRDPERSON;
	    
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

	void Damage(CPlayer* p, float damage, bool shot)
	{
		p->hp -= damage;
	    
		if(p == &g_player[g_localP])
			Reddening();
	    
		if(p->hp <= 0.0f)
		{
			CEntity* e = &g_entity[p->entity];
	        
			if(rand()%2 == 1)
			{
				e->frame[BODY_UPPER] = ANIM_UDEATHFW_S;
				e->frame[BODY_LOWER] = ANIM_LDEATHFW_S;
			}
			else
			{
				e->frame[BODY_UPPER] = ANIM_UDEATHBW_S;
				e->frame[BODY_LOWER] = ANIM_LDEATHBW_S;
			}
	        
			p->forward = false;
			p->backward = false;
			p->left = false;
			p->right = false;
			p->crouched = false;
			p->crouching = false;
			p->jump = false;
	        
			CCamera* c = &e->camera;
			c->Pitch(0);
	        
			if(p == &g_player[g_localP])
			{
				GameOver();
			}
	        
			if(IsZombie(e->type))
			{
	            g_score += 50;
	            RedoScore();
	            
				if(e->script > 0)
				{
					DoScriptFunc(e->script);
					e->script = -1;
				}
	            
				p->ticksleft = 2 * FRAME_RATE;
	            
				if(g_zdeathSnd.size() > 0)
					g_zdeathSnd[ rand()%g_zdeathSnd.size() ].Play();
			}
		}
		else if(shot)
			p->pain = true;
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
    	
    	//if(mHeight > 320)
    	//	mRetinaScale = 2.0f;
    	mRetinaScale = Math.max(1, (int)(mHeight/320));
    	
    	System.out.println("w,h = " + mWidth + "," + mHeight);
    	System.out.println("sc = " + mRetinaScale);
    	
    	LoadFonts();
    	
    	mGUI = new CGUI(this);
    }
    
    public void Deinit()
    {
    	FreeTextures();
    }
    
    public void Draw()
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //public static void setLookAtM(float[] rm, int rmOffset, float eyeX, float eyeY, float eyeZ, float centerX, float centerY, float centerZ, float upX, float upY, float upZ);

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
    
    void SpawnPlayer()
    {
    	CPlayer* p = &g_player[g_localP];
    	p->on = true;
    	p->ai = false;
    	p->stamina = 1;
    	CSpawn spawn = g_sspawn[0];
    	int e;
        
    	PlaceEntity(RandomHuman(), g_localP, -1, -1, spawn.pos, spawn.angle, &e, false, -1);
        
    	g_camera = &g_entity[e].camera;	
    }
    
    void SpawnZombies()
    {
    	for(int i=0; i<g_zspawn.size(); i++)
        {
            CSpawn spawn = g_zspawn[i];
            PlaceEntity(RandomZombie(), NewAI(spawn.activity), -1, -1, spawn.pos, spawn.angle, NULL, false, spawn.script);
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
