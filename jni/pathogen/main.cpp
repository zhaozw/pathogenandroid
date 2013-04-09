

#include "jniapi.h"
#include "logger.h"
#include "main.h"
#include "shader.h"
#include "file.h"
#include "font.h"
#include "script.h"
#include "quake3bsp.h"
#include "3dmath.h"
#include "entity.h"
#include "player.h"
//#include "gluLookAt.h"
#include "gui.h"
#include "physics.h"
//#include "ortho.h"
#include "frustum.h"
#include "model.h"
#include "menu.h"
#include "image.h"
#include "billboard.h"
//#include "Net.h"
//#include "SendPackets.h"
//#include "ReadPackets.h"
#include "video.h"
#include "particle.h"
#include "decal.h"
#include "skybox.h"

//com.mamlambo.sample.ndk1
//AndroidNDK1SampleActivity

//static ANativeWindow *window = 0;
//static Renderer *renderer = 0;

float g_width;
float g_height;
float g_near = 1;
float g_far = 9000;
float g_fov = 90;
float g_scale;

double g_FrameInterval;
GAMEMODE g_mode = LOADING;

struct engine* g_engine;
float g_reddening = 0;
bool g_arrest = false;
int g_viewmode = FIRSTPERSON;
int g_score = 0;
bool g_quit = false;

//zip* APKArchive;
//char g_path[256];
//char g_tempPath[256];
//JNIEnv* g_env;
AAssetManager* g_amgr;
vector<CTouch> g_touch;
bool g_inited = false;
//long g_tick = 0;
//unsigned int g_img = 0;
//unsigned int g_VBO = 0;

void DummyVBO(unsigned int* vbo, GLenum usage)
{
	//LOGI("make v");
	float vertices[] =
    {
        //posx, posy    texx, texy
        10, 12,0,          0, 0,
        300, 12,0,         1, 0,
        300, 312,0,      1, 1,
        
        300, 312,0,      1, 1,
        10, 312,0,       0, 1,
        10, 12,0,          0, 0
    };

	//glGenBuffers(1, &g_VBO);
    //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
	glGenBuffers(1, vbo);
    glBindBuffer(GL_ARRAY_BUFFER, *vbo);
    glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, usage);
}
/*
void DeleteVBO()
{
	glDeleteBuffers(1, &g_VBO);
}*/

void DrawVBO()
{
	//LOGI("draw v");

    glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_2D, g_img);
    glBindTexture(GL_TEXTURE_2D, g_GUI.view[0].widget[0].tex);
#ifndef USE_OMNI
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
#else
    glUniform1i(g_slots[OMNI][TEXTURE], 0);
#endif

	//LOGI("gvbo %u", g_VBO);
	//LOGI("draw vbo %u", g_GUI.view[0].widget[0].vbo);

    //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
    glBindBuffer(GL_ARRAY_BUFFER, g_GUI.view[0].widget[0].vbo);
#ifndef USE_OMNI
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#else
	glVertexAttribPointer(g_slots[OMNI][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*0));
    glVertexAttribPointer(g_slots[OMNI][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, (void*)(sizeof(float)*3));
#endif

    glDrawArrays(GL_TRIANGLES, 0, 6);
}


static void printGLString(const char *name, GLenum s) 
{
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

void checkGlError(const char* op) 
{
    for (GLint error = glGetError(); error; error = glGetError()) 
	{
        //LOGI("after %s() glError (0x%x)\n", op, error);
        LOGI("after %s() glGetError (%d)\n", op, error);
    }
}

unsigned int timeGetTime() 
{ 
	
	struct timeval now; 
	gettimeofday(&now, NULL); 
	return (now.tv_sec * 1000) + (now.tv_usec / 1000);
	
	//return g_tick;
} 
unsigned int GetTickCount()
{
	return timeGetTime();
	//return g_tick;
}

long long GetTickCount2()
{	
	struct timeval now; 
	gettimeofday(&now, NULL); 
	//return now.tv_usec/1000; 
	return (now.tv_sec * 1000) + (now.tv_usec / 1000);
}

void Deinit()
{
	g_GUI.view.clear();

	DeinitSound();

	g_GUI.delvbo();

	DelSky();
	DelBillbVBO();
	DelDecalVBO();

	//DeleteVBO();
    //ClearPackets();
    //g_map.Destroy(true);

	UnloadMap();
    
    for(int i=0; i<MODELS; i++)
    {
        if(!g_model[i].on)
            continue;
        
        g_model[i].Free();
    }
    
    FreeTextures();
    
    for(int i=0; i<SHADERS; i++)
    {
        if (!g_program[i])
            continue;
        
        glDeleteProgram(g_program[i]);
        g_program[i] = 0;
    }

	g_billbT.clear();
	g_entityType.clear();
	g_zdeathSnd.clear();
	g_zgraspSnd.clear();
}

void LoadingScreen()
{
	GAMEMODE prevmode = g_mode;
	OpenAnotherView("loading");
	g_mode = LOADING;

	Draw();
    eglSwapBuffers(g_engine->display, g_engine->surface);
	Draw();
    eglSwapBuffers(g_engine->display, g_engine->surface);

	CloseView("loading");
	g_mode = prevmode;
}

void Reload(struct engine* engine)
{
	g_engine = engine;
	g_quit = false;
	Deinit();

	srand(GetTickCount());
    
	glClearColor(0, 0, 0, 1);
    glEnable(GL_DEPTH_TEST);
    //glEnable(GL_TEXTURE_2D);
    //glActiveTexture(GL_TEXTURE1);
    //glEnable(GL_TEXTURE_2D);
    //glActiveTexture(GL_TEXTURE0);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CW);
	glDisable(GL_CULL_FACE);
    
	LOGI("Load shaders...");
	LoadShaders();

	LOGI("Load fonts...");
    LoadFonts();

	LOGI("RedoGUI();....");
	RedoGUI();

	LoadingScreen();

	LOGI("Init sound...");
	InitSound();

	LOGI("LoadSounds();....");
	LoadSounds();
	
	//g_img = CreateTexture("models/human2.jpg");
	//MakeVBO();

	LOGI("Entities();....");
	Entities();

	LOGI("Items();....");
	Items();

	LOGI("Effects();....");
	Effects();
	
	LOGI("Particles();....");
	Particles();
	
	LOGI("Decals();....");
	Decals();
	
	//LOGI("RedoGUI();....");
	//RedoGUI();

	//MakeVBO(&g_GUI.view[0].widget[0].vbo);
	//g_GUI.view[0].widget[0].pos[0] = 10;
	//g_GUI.view[0].widget[0].pos[1] = 10;
	//g_GUI.view[0].widget[0].pos[2] = 300;
	//g_GUI.view[0].widget[0].pos[3] = 300;
	//LOGE("pos %f, %f, %f, %f", g_GUI.view[0].widget[0].pos[0], g_GUI.view[0].widget[0].pos[1],
	//	g_GUI.view[0].widget[0].pos[2], g_GUI.view[0].widget[0].pos[3]);
	//LOGE("before %u", g_GUI.view[0].widget[0].vbo);
	//g_GUI.view[0].widget[0].Image_fillVBO();
	
	LOGI("Keymap();....");
	Keymap();

	//InitNet();
	
	LOGI("ScriptFuncs();....");
	ScriptFuncs();
	//PlayIntro();

	MakeSky();
	MakeBillbVBO();
	MakeDecalVBO();

	g_inited = true;

	//Click_GoToStory();

	g_stage = 0;
	OpenSoleView("logo");
	g_mode = LOGO;
}

bool Resize(int w, int h) 
{
    //printGLString("Version", GL_VERSION);
    //printGLString("Vendor", GL_VENDOR);
    //printGLString("Renderer", GL_RENDERER);
   // printGLString("Extensions", GL_EXTENSIONS);

    //LOGI("setupGraphics(%d, %d)", w, h);
	/*
    gProgram = createProgram(gVertexShader, gFragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return false;
    }
    gvPositionHandle = glGetAttribLocation(gProgram, "vPosition");
    checkGlError("glGetAttribLocation");
    LOGI("glGetAttribLocation(\"vPosition\") = %d\n",
            gvPositionHandle);
			*/

    glViewport(0, 0, w, h);
    checkGlError("glViewport");

	g_width = w;
	g_height = h;
	LOGI("w,h = %d,%d", w, h);
	RedoGUI();

    return true;
}

void UpdateGameState()
{
	//return;

    Unforward();
    Unback();
    Unleft();
    Unright();
    
    for(int i=0; i<g_touch.size(); i++)
    {
        CTouch* touch = &g_touch[i];
		if(!touch->on)
			continue;

        g_GUI.touchframe(touch->x, touch->y);
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


void CalculateFrameRate()
{
	static double framesPerSecond   = 0.0f;		// This will store our fps
    static double lastTime			= 0.0f;		// This will hold the time from the last frame

	static double frameTime = 0.0f;				// This stores the last frame's time

	// Get the current time in seconds
    double currentTime = timeGetTime() * 0.001f;				


/////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *

	// We added a small value to the frame interval to account for some video
	// cards (Radeon's) with fast computers falling through the floor without it.

	// Here we store the elapsed time between the current and last frame,
	// then keep the current frame in our static variable for the next frame.
 	g_FrameInterval = currentTime - frameTime + 0.005f;

/////// * /////////// * /////////// * NEW * /////// * /////////// * /////////// *


	frameTime = currentTime;

	// Increase the frame counter
    ++framesPerSecond;

	// Now we want to subtract the current time by the last time that was stored
	// to see if the time elapsed has been over a second, which means we found our FPS.
    if( currentTime - lastTime > 1.0f )
	{
		char msg[128];
		sprintf(msg, "FPS: %f, %fs", (float)framesPerSecond, (float)(currentTime - lastTime)/(float)framesPerSecond);
		g_GUI.getview("chat")->gettext("fps")->text = msg;
		g_GUI.getview("chat")->gettext("fps")->fillvbo();

		// Here we set the lastTime to the currentTime
	    lastTime = currentTime;

		// Copy the frames per second into a string to display in the window title bar
		//sprintf(strFrameRate, "Current Frames Per Second: %d", int(framesPerSecond));

		// Set the window title bar to our string
		//SetWindowText(g_hWnd, strFrameRate);

		// Reset the frames per second
        framesPerSecond = 0;
    }
}

/*
void UpdateTicks()
{
	g_tick += 1000.0f * FRAME_INTERVAL;
}
*/
void Update()
{
	//LOGI("start Update()");

	//UpdateTicks();

    if(g_mode == PLAY)
        UpdateGameState();
    if(g_mode == LOGO)
        UpdateLogo();
    
    //ResendPackets();
	
	//LOGI("end Update()");
}

/*
void DrawBImage(unsigned int tex, float left, float top, float right, float bottom)
{
    //glActiveTexture(GL_TEXTURE0);
    //glBindTexture(GL_TEXTURE_2D, tex);
    //glUniform1i(g_slots[ORTHO][TEXTURE], 0);
    
    float vertices[] =
    {
        //posx, posy    texx, texy
        left, top,0,          0, 0,
        right, top,0,         1, 0,
        right, bottom,0,      1, 1,
        
        right, bottom,0,      1, 1,
        left, bottom,0,       0, 1,
        left, top,0,          0, 0
    };
    
    //glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
    //glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
	
        glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[0]);
        glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, &vertices[3]);
    
    glDrawArrays(GL_TRIANGLES, 0, 6);
}*/

void Draw() 
{
	//LOGI("start Draw()");
/*
    static float grey;
    grey += 0.01f;
    if (grey > 1.0f) 
	{
        grey = 0.0f;
    }
    glClearColor(grey, grey, grey, 1.0f);
    checkGlError("glClearColor");
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    checkGlError("glClear");

/*
    glUseProgram(gProgram);
    checkGlError("glUseProgram");

    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
    checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gvPositionHandle);
    checkGlError("glEnableVertexAttribArray");
    glDrawArrays(GL_TRIANGLES, 0, 3);
    checkGlError("glDrawArrays");
*/
    //glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	
	//glClearColor((float)(rand()%255)/255.0f, (float)(rand()%255)/255.0f, (float)(rand()%255)/255.0f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	//glDisable(GL_DEPTH_TEST);
#ifdef USE_OMNI
    glUseProgram(g_program[OMNI]);
#endif
	
/*
	glUseProgram(g_program[ORTHO]);
    glUniform1f(g_slots[ORTHO][WIDTH], (float)g_width);
    glUniform1f(g_slots[ORTHO][HEIGHT], (float)g_height);
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
    glEnableVertexAttribArray(g_slots[ORTHO][POSITION]);
    glEnableVertexAttribArray(g_slots[ORTHO][TEXCOORD]);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	glActiveTexture(GL_TEXTURE0);
	//glEnable(GL_TEXTURE_2D);
	//glEnableClientState(GL_VERTEX_ARRAY);
	DrawImage(CreateTexture("models/human2"), 0, 0, 100, 100);
	if(g_mode != PLAY)
    g_GUI.draw();
*/
	//static bool didonce = false;

	//if(g_mode == PLAY && !didonce)
	if(g_mode == PLAY)
    {
	//	didonce = true;

		//glEnable(GL_DEPTH_TEST);

        float aspect = fabsf(g_width / g_height);
		CMatrix projection = BuildPerspProjMat(g_fov, aspect, g_near, g_far);
        //GLKMatrix4 projection = GLKMatrix4MakePerspective(GLKMathDegreesToRadians(g_fov), aspect, g_near, g_far);
        
        //GLKMatrix4 modelmat = GLKMatrix4MakeTranslation(0.0f, 0.0f, 0.0f);
		CMatrix modelmat;
		float translation[] = {0, 0, 0};
        modelmat.setTranslation(translation);

	//LOGI("start g_cam->");
        CVector3 viewvec = g_camera->View();
        CVector3 posvec = g_camera->Position();
        CVector3 posvec2 = g_camera->LookPos();
        CVector3 upvec = g_camera->UpVector();
	//LOGI("end g_cam->");

        CMatrix viewmat = gluLookAt2(posvec2.x, posvec2.y, posvec2.z,
                                    viewvec.x, viewvec.y, viewvec.z,
                                    upvec.x, upvec.y, upvec.z);

/*
        CMatrix viewmat = gluLookAt2(0, 0, 0,
                                    0, 0, 1,
                                    0, 1, 0);*/

        
        CMatrix modelview;
        modelview.set(modelmat.getMatrix());
        modelview.postMultiply(viewmat);
        
	//LOGI("start frus");
        g_frustum.CalculateFrustum(projection.getMatrix(), modelview.getMatrix());
	//LOGI("end frus");


        float color[] = {1,1,1,1};
        
        if(g_reddening > 0.0f)
        {
            color[1] = 1.0f - g_reddening;
            color[2] = 1.0f - g_reddening;
            
            g_reddening -= FRAME_INTERVAL;
        }

/*
		float color[4];
		color[0] = 1;
		color[1] = 1;
		color[2] = 1;
		color[3] = 1;*/
	//LOGI("end redenning");
#ifndef USE_OMNI
	//LOGI("start DrawSkyBox()");
        glUseProgram(g_program[SKY]);
        glUniformMatrix4fv(g_slots[SKY][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[SKY][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[SKY][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[SKY][POSITION]);
        glEnableVertexAttribArray(g_slots[SKY][TEXCOORD]);
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
        //g_map.RenderSky();
        DrawSkyBox(posvec2);
		//DrawSkyBox(CVector3(0,0,0));
        //checkGlError("DrawSkyBox");
        //glDisableVertexAttribArray(g_slots[SKY][POSITION]);
        //glDisableVertexAttribArray(g_slots[SKY][TEXCOORD]);
#else
	//LOGI("start DrawSkyBox()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MODEL);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
	//LOGI("start DrawSkyBox() 1");
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	//LOGI("it DrawSkyBox()");
        DrawSkyBox(posvec2);
		//checkGlError("DrawSkyBox");
#endif
	
#ifndef USE_OMNI
	//LOGI("start RenderLevel()");
        glUseProgram(g_program[MAP]);
        glUniformMatrix4fv(g_slots[MAP][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[MAP][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[MAP][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MAP][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MAP][POSITION]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD2]);
        g_map.RenderLevel(posvec);
		//g_map.RenderFace(0);
		//checkGlError("RenderLevel");
        //g_map.SortFaces(posvec);
        //g_map.RenderLevel2(posvec);
		//checkGlError("RenderLevl");
#else
	//LOGI("start RenderLevel()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MAP);
        //glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        //glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        //glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD2]);
        g_map.RenderLevel(posvec);
		//checkGlError("RenderLevl");
		glDisableVertexAttribArray(g_slots[OMNI][TEXCOORD2]);
#endif
	
#ifndef USE_OMNI
	//LOGI("start DrawEntities()");
        glUseProgram(g_program[MODEL]);
        glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MODEL][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
        glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		//glActiveTexture(GL_TEXTURE0);
		//glEnable(GL_TEXTURE_2D);
        SortEntities();
        DrawEntities(false);
        DrawEntities(true);
		//g_model[0].Draw(0, CVector3(0,0,0), 0, 0);
        //glDisableVertexAttribArray(g_slots[MODEL][POSITION]);
        //glDisableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
		//checkGlError("DrawEntities");
		//DrawVBO();
	
        //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		//DrawDecals();
		//checkGlError("DrawDec");
        //SortBillboards();
        //DrawBillboards();
		//checkGlError("DrawBillb");
#else
	//LOGI("start DrawEntities()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MODEL);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        SortEntities();
        DrawEntities(false);
        DrawEntities(true);
		//checkGlError("DrawEnts");
#endif
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, NULL);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, NULL);

#ifndef USE_OMNI
	//LOGI("start RenderLevel2()");
        glUseProgram(g_program[MAP]);
        glUniformMatrix4fv(g_slots[MAP][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[MAP][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[MAP][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MAP][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MAP][POSITION]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD2]);
        g_map.SortFaces(posvec);
        g_map.RenderLevel2(posvec);
		//glActiveTexture(GL_TEXTURE0);
		//glDisableVertexAttribArray(g_slots[MAP][TEXCOORD2]);
		//checkGlError("RenderLevel2");
#else
	//LOGI("start RenderLevel2()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MAP);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD2]);
        g_map.SortFaces(posvec);
        g_map.RenderLevel2(posvec);
		//checkGlError("RenderLevl2");
		glDisableVertexAttribArray(g_slots[OMNI][TEXCOORD2]);
#endif

#ifndef USE_OMNI
	//LOGI("start DrawDecals()");
        glUseProgram(g_program[BILLBOARD]);
        glUniformMatrix4fv(g_slots[BILLBOARD][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[BILLBOARD][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[BILLBOARD][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[BILLBOARD][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][POSITION]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][TEXCOORD]);
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
		//glEnableClientState(GL_VERTEX_ARRAY);
        DrawDecals();
		//glDisableClientState(GL_VERTEX_ARRAY);
		//checkGlError("DrawDecals");
#else
	//LOGI("start DrawDecals()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MODEL);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        DrawDecals();
		//checkGlError("DrawDecals");
#endif

#ifndef USE_OMNI
	//LOGI("start DrawBillboards()");
        glUseProgram(g_program[BILLBOARD]);
        glUniformMatrix4fv(g_slots[BILLBOARD][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[BILLBOARD][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[BILLBOARD][VIEWMAT], 1, 0, viewmat.getMatrix());
        //glUniform3f(g_slots[BILLBOARD][CAMERAPOS], posvec.x, posvec.y, posvec.z);
        glUniform4f(g_slots[BILLBOARD][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][POSITION]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][TEXCOORD]);
        //glBindBuffer(GL_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
        SortBillboards();
        DrawBillboards();
        //glDisableVertexAttribArray(g_slots[BILLBOARD][POSITION]);
        //glDisableVertexAttribArray(g_slots[BILLBOARD][TEXCOORD]);
    //glUniform1f(g_slots[BILLBOARD][WIDTH], (float)g_width);
    //glUniform1f(g_slots[BILLBOARD][HEIGHT], (float)g_height);
    //glUniform4f(g_slots[BILLBOARD][COLOR], 1, 1, 1, 1);
    //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	//glActiveTexture(GL_TEXTURE0);
	//glEnable(GL_TEXTURE_2D);
	//glEnableClientState(GL_VERTEX_ARRAY);
	//DrawBImage(CreateTexture("models/human2"), 100, 100, 200, 200);
		//checkGlError("DrawBillb");
		//checkGlError("DrawBillb");
#else
	//LOGI("start DrawBillboards()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MODEL);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
		glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        SortBillboards();
        DrawBillboards();
		//checkGlError("DrawBillbs");
#endif

#ifndef USE_OMNI
	//LOGI("start DrawHands()");
        //glVertexAttribPointer(g_slots[BILLBOARD][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*5, NULL);
        //glVertexAttribPointer(g_slots[BILLBOARD][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*5, NULL);
		//glDisableClientState(GL_VERTEX_ARRAY);
        glUseProgram(g_program[MODEL]);
        glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.getMatrix());
        //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MODEL][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
        glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
        DrawHands();
		//checkGlError("DrawHands");		
#else
	//LOGI("start DrawHands()");
		glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_MODEL);
        glUniformMatrix4fv(g_slots[OMNI][PROJECTION], 1, 0, projection.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][MODELMAT], 1, 0, modelmat.getMatrix());
        glUniformMatrix4fv(g_slots[OMNI][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[OMNI][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
        glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
        DrawHands();
		//checkGlError("DrawHands");
#endif

		//glFinish();
    }
	//checkGlError("start Draw");

	//glUseProgram(g_program[BILLBOARD]);
    //glUniform1f(g_slots[BILLBOARD][WIDTH], (float)g_width);
    //glUniform1f(g_slots[BILLBOARD][HEIGHT], (float)g_height);
    //glUniform4f(g_slots[BILLBOARD][COLOR], 1, 1, 1, 1);
    //glEnableVertexAttribArray(g_slots[BILLBOARD][POSITION]);
    //glEnableVertexAttribArray(g_slots[BILLBOARD][TEXCOORD]);
    //glBindBuffer(GL_ARRAY_BUFFER, 0);
    ////glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	////glActiveTexture(GL_TEXTURE0);
	////glEnable(GL_TEXTURE_2D);
	////glEnableClientState(GL_VERTEX_ARRAY);
	//DrawBImage(NULL, 100, 100, 200, 200);
	
#ifndef USE_OMNI
	//LOGI("start DrawGUI()");
	//if(g_mode != PLAY)
	glDisable(GL_DEPTH_TEST);
    glUseProgram(g_program[ORTHO]);
    glUniform1f(g_slots[ORTHO][WIDTH], (float)g_width);
    glUniform1f(g_slots[ORTHO][HEIGHT], (float)g_height);
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
    glEnableVertexAttribArray(g_slots[ORTHO][POSITION]);
    glEnableVertexAttribArray(g_slots[ORTHO][TEXCOORD]);
    //glBindBuffer(GL_ARRAY_BUFFER, 0);
    //glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    //glBindBuffer(GL_ARRAY_BUFFER, g_VBO);
	//glActiveTexture(GL_TEXTURE0);
	//glEnable(GL_TEXTURE_2D);
	//glEnableClientState(GL_VERTEX_ARRAY);
	//DrawImage(CreateTexture("models/human2"), 0, 0, 100, 100);
	//DrawImage(g_img, 0, 0, 300, 300);
	//if(g_mode != PLAY)
    g_GUI.draw();
	//DrawVBO();
    //DrawShadowedText(MSGOTHIC16, 0, 0, "Hello world. My name is Denis.");
	//void DrawImage(unsigned int tex, float left, float top, float right, float bottom)
	//glDisableClientState(GL_VERTEX_ARRAY);
	//checkGlError("GUI Draw");
#else
	//LOGI("start DrawGUI()");
	glDisable(GL_DEPTH_TEST);
    //glUseProgram(g_program[OMNI]);
    glUniform1i(g_slots[OMNI][SHADERMODE], SHMODE_ORTHO);
    glUniform1f(g_slots[OMNI][WIDTH], (float)g_width);
    glUniform1f(g_slots[OMNI][HEIGHT], (float)g_height);
    glUniform4f(g_slots[OMNI][COLOR], 1, 1, 1, 1);
    glEnableVertexAttribArray(g_slots[OMNI][POSITION]);
    glEnableVertexAttribArray(g_slots[OMNI][TEXCOORD]);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	//DrawImage(CreateTexture("models/human2"), 0, 0, 100, 100);
	//if(g_mode != PLAY)
    g_GUI.draw();
	//checkGlError("DrawGUI");
#endif
    glEnable(GL_DEPTH_TEST);
	
	//glFinish();

	//checkGlError("Draw");
}


// Initialize an EGL context for the current display.
static int engine_init_display(struct engine* engine) 
{
    // initialize OpenGL ES and EGL

    // Here specify the attributes of the desired configuration.
    // Below, we select an EGLConfig with at least 8 bits per color
    // component compatible with on-screen windows
    const EGLint attribs[] = 
	{
			EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
			EGL_DEPTH_SIZE, 24,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_RED_SIZE, 8,
            EGL_NONE
    };

    EGLint w, h, dummy, format;
    EGLint numConfigs;
    EGLConfig config;
    EGLSurface surface;
    EGLContext context;

    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);

    eglInitialize(display, 0, 0);

    /* Here, the application chooses the configuration it desires. In this
     * sample, we have a very simplified selection process, where we pick
     * the first EGLConfig that matches our criteria */
    eglChooseConfig(display, attribs, &config, 1, &numConfigs);

    /* EGL_NATIVE_VISUAL_ID is an attribute of the EGLConfig that is
     * guaranteed to be accepted by ANativeWindow_setBuffersGeometry().
     * As soon as we picked a EGLConfig, we can safely reconfigure the
     * ANativeWindow buffers to match, using EGL_NATIVE_VISUAL_ID. */
    eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format);

    ANativeWindow_setBuffersGeometry(engine->app->window, 0, 0, format);

    surface = eglCreateWindowSurface(display, config, engine->app->window, NULL);

	EGLint contextAttrs[] = {
		 EGL_CONTEXT_CLIENT_VERSION, 2,
		 EGL_NONE
	};

    context = eglCreateContext(display, config, NULL, contextAttrs);

    if (eglMakeCurrent(display, surface, surface, context) == EGL_FALSE) 
	{
        LOGW("Unable to eglMakeCurrent");
        return -1;
    }

    eglQuerySurface(display, surface, EGL_WIDTH, &w);
    eglQuerySurface(display, surface, EGL_HEIGHT, &h);

    engine->display = display;
    engine->context = context;
    engine->surface = surface;
    engine->width = w;
    engine->height = h;
    engine->state.angle = 0;

	g_width = w;
	g_height = h;

	LOGI("w,h = %d,%d", w, h);

	g_scale = 1;
	if(h > 320)
		g_scale = 2;

    // Initialize GL state.
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
    //glEnable(GL_CULL_FACE);
    //glShadeModel(GL_SMOOTH);
    //glDisable(GL_DEPTH_TEST);
	glEnable(GL_DEPTH_TEST);
	glDisable(GL_CULL_FACE);
	
	g_amgr = engine->app->activity->assetManager;

    return 0;
}

bool AnimateNextFrame(int desiredFrameRate)
{
	/*
	static double lastTime = GetTickCount2() * 0.001f;
	static double elapsedTime = 0.0f;

	double currentTime = GetTickCount2() * 0.001f; // Get the time (milliseconds = seconds * .001)
	double deltaTime = currentTime - lastTime; // Get the slice of time
	double desiredFPS = 1.0 / desiredFrameRate; // Store 1 / desiredFrameRate

	elapsedTime += deltaTime; // Add to the elapsed time
	lastTime = currentTime; // Update lastTime

	// Check if the time since we last checked is greater than our desiredFPS
	if( elapsedTime > desiredFPS )
	{
		elapsedTime -= desiredFPS; // Adjust the elapsed time

		// Return true, to animate the next frame of animation
		return true;
	}

	// We don't animate right now.
	return false;*/
/*
	static long long lastTime = GetTickCount2();
	static int elapsedTime = 0;

	long long currentTime = GetTickCount2();
	int deltaTime = currentTime - lastTime;
	
	int desiredFPMS = 1000/desiredFrameRate;
	
	elapsedTime += deltaTime; // Add to the elapsed time
	lastTime = currentTime; // Update lastTime

	if(elapsedTime > desiredFPMS)
	{
		elapsedTime -= desiredFPMS;
		return true;
	}

	//LOGI("deltaTime %d, elapsedTime %d", deltaTime, elapsedTime);
	//LOGI("not anim %lld", elapsedTime);

	return false;
	*/

	static long long lastTime = GetTickCount2();
	long long currentTime = GetTickCount2();
	int desiredFPMS = 1000/desiredFrameRate;
	int deltaTime = currentTime - lastTime;
	
	if(deltaTime > desiredFPMS)
	{
		g_FrameInterval = (float)deltaTime/1000.0f + 0.005f;
		lastTime = currentTime;
		return true;
	}

	return false;
}

/**
 * Just the current frame in the display.
 */
static void engine_draw_frame(struct engine* engine) 
{
    if (engine->display == NULL) 
	{
        // No display.
        return;
    }
	
	//g_amgr = engine->app->activity->assetManager;

	Update();

    // Just fill the screen with a color.
    //glClearColor(((float)engine->state.x)/engine->width, engine->state.angle, ((float)engine->state.y)/engine->height, 1);
    //glClear(GL_COLOR_BUFFER_BIT);

	Draw();

    eglSwapBuffers(engine->display, engine->surface);
}

/**
 * Tear down the EGL context currently associated with the display.
 */
static void engine_term_display(struct engine* engine) 
{
	g_quit = true;
	Deinit();

    if (engine->display != EGL_NO_DISPLAY) 
	{
        eglMakeCurrent(engine->display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (engine->context != EGL_NO_CONTEXT) 
		{
            eglDestroyContext(engine->display, engine->context);
        }
        if (engine->surface != EGL_NO_SURFACE) 
		{
            eglDestroySurface(engine->display, engine->surface);
        }
        eglTerminate(engine->display);
    }

    engine->animating = 0;
    engine->display = EGL_NO_DISPLAY;
    engine->context = EGL_NO_CONTEXT;
    engine->surface = EGL_NO_SURFACE;
}

// Process the next input event.
// http://stackoverflow.com/questions/12500825/android-ndk-multitouch
// http://mobilepearls.com/labs/native-android-api/include/android/input.h
// http://stackoverflow.com/questions/13707664/keyboard-input-in-android-ndk-using-nativeactivity
// https://bitbucket.org/runhello/jumpcore/src/2641f6910f3386d74e3401a16a32f4888631cf35/android/jumpcore-eclipse/jni/gl_code.cpp
// http://android-developers.blogspot.ca/2010/06/making-sense-of-multitouch.html
static int32_t engine_handle_input(struct android_app* app, AInputEvent* event) 
{
	//LOGI("engine handle input");

    struct engine* engine = (struct engine*)app->userData;

	unsigned int pindex = (AMotionEvent_getAction(event) & AMOTION_EVENT_ACTION_POINTER_INDEX_MASK) >> AMOTION_EVENT_ACTION_POINTER_INDEX_SHIFT;
	//CTouch* touch;
	CTouch newt;
    CTouch* t;

    int j;
    int nearest;
    float nearestD;
    float D;
    int k;
    vector<int> modified;
    bool found;

    switch(AInputEvent_getType(event))
	{
		case AINPUT_EVENT_TYPE_MOTION:
			{
				//LOGI("motion %d,%d", (int)AMotionEvent_getX(event, 0), (int)AMotionEvent_getY(event, 0));
				engine->animating = 1;
				engine->state.x = AMotionEvent_getX(event, 0);
				engine->state.y = AMotionEvent_getY(event, 0);

				switch(AMotionEvent_getAction(event) & AMOTION_EVENT_ACTION_MASK)
				{
				case AMOTION_EVENT_ACTION_DOWN:
				case AMOTION_EVENT_ACTION_POINTER_DOWN:
					{
						newt = CTouch(AMotionEvent_getX(event, pindex),  AMotionEvent_getY(event, pindex));
						g_touch.push_back(newt);
						g_GUI.lbuttondown(newt.x, newt.y);

					}break;
				case AMOTION_EVENT_ACTION_CANCEL:
				case AMOTION_EVENT_ACTION_UP:
				case AMOTION_EVENT_ACTION_POINTER_UP:
					{
						newt = CTouch(AMotionEvent_getX(event, pindex),  AMotionEvent_getY(event, pindex));
						nearestD = 999999999.0;
						nearest = -1;
						for(j=0; j<g_touch.size(); j++)
						{
							t = &g_touch[j];
							D = Magnitude2(t->x - newt.x, t->y - newt.y);
				            
							if(D < nearestD)
								nearest = j;
						}
				        
						if(nearest >= 0)
							g_touch.erase( g_touch.begin() + nearest );
				        
						g_GUI.lbuttonup(newt.x, newt.y);
						
						g_GUI.touchcheck();
					}break;
				case AMOTION_EVENT_ACTION_MOVE :
					{
						newt = CTouch(AMotionEvent_getX(event, pindex),  AMotionEvent_getY(event, pindex));
        
						nearestD = 999999999.0;
						nearest = -1;
						for(j=0; j<g_touch.size(); j++)
						{
							found = false;
							for(k=0; k<modified.size(); k++)
							{
								if(modified[k] != j)
									continue;
				                
								found = true;
								break;
							}
							if(found)
								continue;
				            
							t = &g_touch[j];
							D = Magnitude2(t->x - newt.x, t->y - newt.y);
				            
							if(D < nearestD)
								nearest = j;
						}
				        
						if(nearest >= 0)
						{
							g_touch[nearest] = newt;
							modified.push_back(nearest);
						}
						else
						{
							g_touch.push_back(newt);
							modified.push_back(g_touch.size()-1);
						}
				        
						g_GUI.mousemove(newt.x, newt.y);
						g_GUI.touchcheck();
					}break;
				default:
					break;
				}
			}break;
		default: 
			{
				//LOGI("Event #%d pointer#%d/%d", (int)AInputEvent_getType(event), (int)pindex, (int)g_touch.size());
			}break;
	}

	//LOGI("repeat_count = %d", (int)AKeyEvent_getRepeatCount(event));

		//switch(AKeyEvent_getAction(event))
	
    return 0;
}

/**
 * Process the next main command.
 */
static void engine_handle_cmd(struct android_app* app, int32_t cmd) 
{
	//LOGI("engine handle cmd %d", (int)cmd);

    struct engine* engine = (struct engine*)app->userData;
    switch (cmd) {
        case APP_CMD_SAVE_STATE:
            // The system has asked us to save our current state.  Do so.
            engine->app->savedState = malloc(sizeof(struct saved_state));
            *((struct saved_state*)engine->app->savedState) = engine->state;
            engine->app->savedStateSize = sizeof(struct saved_state);
			LOGI("save state");
            break;
        case APP_CMD_INIT_WINDOW:
			// http://stackoverflow.com/questions/5352910/how-to-use-native-activity-can-it-be-combined-with-traditional-activity
            // The window is being shown, get it ready.
            if (engine->app->window != NULL) 
			{
                engine_init_display(engine);
                //engine_draw_frame(engine);
            }
			LOGI("init win");
            break;
        case APP_CMD_TERM_WINDOW:
            // The window is being hidden or closed, clean it up.
            engine_term_display(engine);
			LOGI("term win");
            break;
        case APP_CMD_GAINED_FOCUS:
			
            // When our app gains focus, we start monitoring the accelerometer.
            if (engine->accelerometerSensor != NULL) 
			{
                //ASensorEventQueue_enableSensor(engine->sensorEventQueue, engine->accelerometerSensor);
                // We'd like to get 60 events per second (in us).
                //ASensorEventQueue_setEventRate(engine->sensorEventQueue, engine->accelerometerSensor, (1000L/30)*1000);
            }

			Reload(engine);
			engine->animating = 1;

			LOGI("gained focus");
            break;
        case APP_CMD_LOST_FOCUS:
			
            // When our app loses focus, we stop monitoring the accelerometer.
            // This is to avoid consuming battery while not being used.
            if (engine->accelerometerSensor != NULL) {
                //ASensorEventQueue_disableSensor(engine->sensorEventQueue, engine->accelerometerSensor);
            }
            // Also stop animating.
            engine->animating = 0;
			
            //engine_draw_frame(engine);
			Deinit();
			LOGI("lost focus");
            break;
    }
}

/**
 * This is the main entry point of a native application that is using
 * android_native_app_glue.  It runs in its own thread, with its own
 * event loop for receiving input events and doing other things.
 */
void android_main(struct android_app* state) 
{
	//LOGI("ANDROID MAIN");

    struct engine engine;

    // Make sure glue isn't stripped.
    app_dummy();

    memset(&engine, 0, sizeof(engine));
    state->userData = &engine;
    state->onAppCmd = engine_handle_cmd;
    state->onInputEvent = engine_handle_input;
    engine.app = state;

    // Prepare to monitor accelerometer
    //engine.sensorManager = ASensorManager_getInstance();
    //engine.accelerometerSensor = ASensorManager_getDefaultSensor(engine.sensorManager, ASENSOR_TYPE_ACCELEROMETER);
    //engine.sensorEventQueue = ASensorManager_createEventQueue(engine.sensorManager, state->looper, LOOPER_ID_USER, NULL, NULL);

    if (state->savedState != NULL) 
	{
        // We are starting with a previous saved state; restore from it.
        engine.state = *(struct saved_state*)state->savedState;
    }

	g_amgr = state->activity->assetManager;

    // loop waiting for stuff to do.
    //while (!g_quit) 
	while(true)
	{
        // Read all pending events.
        int ident;
        int events;
        struct android_poll_source* source;

        // If not animating, we will block forever waiting for events.
        // If animating, we loop until all events are read, then continue
        // to draw the next frame of animation.
        while ((ident=ALooper_pollAll(engine.animating ? 0 : -1, NULL, &events, (void**)&source)) >= 0) 
        //while ((ident=ALooper_pollAll(0, NULL, &events, (void**)&source)) >= 0) 
		{
            // Process this event.
            if (source != NULL) 
			{
                source->process(state, source);
            }

            // If a sensor has data, process it now.
            if (ident == LOOPER_ID_USER) {
                if (engine.accelerometerSensor != NULL) 
				{
                    ASensorEvent event;
                    //while (ASensorEventQueue_getEvents(engine.sensorEventQueue, &event, 1) > 0) 
					{
                        //LOGI("accelerometer: x=%f y=%f z=%f", event.acceleration.x, event.acceleration.y, event.acceleration.z);
                    }
                }
            }

            // Check if we are exiting.
            if (state->destroyRequested != 0) 
			{
                engine_term_display(&engine);
				Deinit();
                return;
				//break;
            }
        }

        if (engine.animating) 
		{
            // Done with events; draw next animation frame.
            engine.state.angle += .01f;
            if (engine.state.angle > 1) 
			{
                engine.state.angle = 0;
            }

			if(AnimateNextFrame(FRAME_RATE))
			{
				//LOGI("animate");
				// Drawing is throttled to the screen update rate, so there
				// is no need to do timing here.
				engine_draw_frame(&engine);
			}
        }
    }

	//ANativeActivity_finish(state->activity);
}
