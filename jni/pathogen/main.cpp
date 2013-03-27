

#include "jniapi.h"
#include "logger.h"
#include "main.h"
#include "shader.h"
#include "file.h"
#include "font.h"
#include "script.h"

//com.mamlambo.sample.ndk1
//AndroidNDK1SampleActivity

//static ANativeWindow *window = 0;
//static Renderer *renderer = 0;

float g_width;
float g_height;
float g_near = 1;
float g_far = 9000;
float g_fov = 90;

double g_FrameInterval;
GAMEMODE g_mode = LOGO;

float g_reddening = 0;
bool g_arrest = false;
int g_viewmode = FIRSTPERSON;
int g_score = 0;

zip* APKArchive;
char g_path[256];
char g_tempPath[256];
JNIEnv* g_env;
AAssetManager* g_amgr;
int g_width;
int g_height;
bool g_inited = false;


static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

unsigned int timeGetTime() 
{ 
	struct timeval now; 
	gettimeofday(&now, NULL); 
	return now.tv_usec/1000; 
} 
unsigned int GetTickCount()
{
	return timeGetTime();
}

void Deinit()
{
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

	g_entityType.clear();
}

void Reload()
{
	LoadShaders();
	    
	srand(GetTickCount());
    
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CW);
    
    LoadFonts();

	/*
	Entities();
	Items();
	Effects();
	Particles();
	Decals();
	RedoGUI();
	Keymap();
	LoadSounds();
	//InitNet();
	ScriptFuncs();
	*/
	//PlayIntro();

	g_inited = true;
}

JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_helloLog(JNIEnv * env, jobject jthis, jstring logThis)  
{  
	/*
    jboolean isCopy;  
    const char * szLogThis = (*env)->GetStringUTFChars(env, logThis, &isCopy);  
    __android_log_print(ANDROID_LOG_DEBUG, DEBUG_TAG, "NDK:LC: [%s]", szLogThis);  
    (*env)->ReleaseStringUTFChars(env, logThis, szLogThis);  */
} 

JNIEXPORT jstring Java_com_pathogengame_pathogen_MainActivity_getString(JNIEnv * env, jobject jthis, jint value1, jint value2)  
{  
	/*
    char *szFormat = "The sum of the two numbers is: %i";  
    char *szResult;  
    // add the two values  
    jlong sum = value1+value2;  
    // malloc room for the resulting string  
    szResult = malloc(sizeof(szFormat) + 20);  
    // standard sprintf  
    sprintf(szResult, szFormat, sum);  
    // get an object string  */
    //jstring result = (*env)->NewStringUTF(env, szResult); 
    jstring result = env->NewStringUTF("test");  
    // cleanup  
    //free(szResult);  
    return result;  
} 

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnCreate(JNIEnv* jenv, jobject obj)
{
    LOGI("nativeOnResume");
    //renderer = new Renderer();
    return;
}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnResume(JNIEnv* jenv, jobject obj)
{
    LOGI("nativeOnResume");
    //renderer->start();
    return;
}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnPause(JNIEnv* jenv, jobject obj)
{
    LOGI("nativeOnPause");
    //renderer->stop();
    return;
}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeOnStop(JNIEnv* jenv, jobject obj)
{
    LOGI("nativeOnStop");
    //delete renderer;
    //renderer = 0;
	Deinit();
    return;
}

/*
JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_nativeSetSurface(JNIEnv* jenv, jobject obj, jobject surface)
{
    if (surface != 0) {
        window = ANativeWindow_fromSurface(jenv, surface);
        LOG_INFO("Got window %p", window);
        renderer->setWindow(window);
    } else {
        LOG_INFO("Releasing window");
        ANativeWindow_release(window);
    }

    return;
}*/

bool Resize(int w, int h) 
{
    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);

    LOGI("setupGraphics(%d, %d)", w, h);
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

    return true;
}

void UpdateGameState()
{
    Unforward();
    Unback();
    Unleft();
    Unright();
    
    for(int i=0; i<g_touch.size(); i++)
    {
        CVector2i* touch = &g_touch[i];
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

/*
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
*/

void Update()
{
    if(g_mode == PLAY)
        UpdateGameState();
    if(g_mode == LOGO)
        UpdateLogo();
    
    //ResendPackets();
}

void Draw() 
{
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
    checkGlError("glDrawArrays");*/

	if(g_mode == PLAY)
    {
        float aspect = fabsf(g_width / g_height);
        GLKMatrix4 projection = GLKMatrix4MakePerspective(GLKMathDegreesToRadians(g_fov), aspect, g_near, g_far);
        
        GLKMatrix4 modelmat = GLKMatrix4MakeTranslation(0.0f, 0.0f, 0.0f);
        
        CVector3 viewvec = g_camera->View();
        CVector3 posvec = g_camera->Position();
        CVector3 posvec2 = g_camera->LookPos();
        CVector3 upvec = g_camera->UpVector();
        
        CMatrix viewmat = gluLookAt(posvec2.x, posvec2.y, posvec2.z,
                                    viewvec.x, viewvec.y, viewvec.z,
                                    upvec.x, upvec.y, upvec.z);
        
        CMatrix modelview;
        modelview.set(modelmat.m);
        modelview.postMultiply(viewmat);
        
        g_frustum.CalculateFrustum(projection.m, modelview.getMatrix());
        
        float color[] = {1,1,1,1};
        
        if(g_reddening > 0.0f)
        {
            color[1] = 1.0f - g_reddening;
            color[2] = 1.0f - g_reddening;
            
            g_reddening -= g_FrameInterval;
        }
        
        glUseProgram(g_program[SKY]);
        glUniformMatrix4fv(g_slots[SKY][PROJECTION], 1, 0, projection.m);
        //glUniformMatrix4fv(g_slots[SKY][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[SKY][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[SKY][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[SKY][POSITION]);
        glEnableVertexAttribArray(g_slots[SKY][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        //g_map.RenderSky();
        DrawSkyBox(posvec2);
        
        glUseProgram(g_program[MAP]);
        glUniformMatrix4fv(g_slots[MAP][PROJECTION], 1, 0, projection.m);
        glUniformMatrix4fv(g_slots[MAP][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[MAP][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MAP][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MAP][POSITION]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD2]);
        g_map.RenderLevel(posvec);
        
        glUseProgram(g_program[MODEL]);
        glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.m);
        //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MODEL][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
        glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
        SortEntities();
        DrawEntities(false);
        DrawEntities(true);
        
        glUseProgram(g_program[MAP]);
        glUniformMatrix4fv(g_slots[MAP][PROJECTION], 1, 0, projection.m);
        glUniformMatrix4fv(g_slots[MAP][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[MAP][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MAP][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MAP][POSITION]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD]);
        glEnableVertexAttribArray(g_slots[MAP][TEXCOORD2]);
        g_map.SortFaces(posvec);
        g_map.RenderLevel2(posvec);
        
        glUseProgram(g_program[MODEL]);
        glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.m);
        glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
        //glUniform4f(g_slots[MODEL][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
        glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        DrawDecals();
        
        glUseProgram(g_program[BILLBOARD]);
        glUniformMatrix4fv(g_slots[BILLBOARD][PROJECTION], 1, 0, projection.m);
        glUniformMatrix4fv(g_slots[BILLBOARD][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[BILLBOARD][VIEWMAT], 1, 0, viewmat.getMatrix());
        //glUniform3f(g_slots[BILLBOARD][CAMERAPOS], posvec.x, posvec.y, posvec.z);
        glUniform4f(g_slots[BILLBOARD][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][POSITION]);
        glEnableVertexAttribArray(g_slots[BILLBOARD][TEXCOORD]);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        SortBillboards();
        DrawBillboards();
        
        glUseProgram(g_program[MODEL]);
        glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.m);
        //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.m);
        glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
        glUniform4f(g_slots[MODEL][COLOR], color[0], color[1], color[2], color[3]);
        glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
        glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
        DrawHands();
    }
    
    glDisable(GL_DEPTH_TEST);
    glUseProgram(g_program[ORTHO]);
    glUniform1f(g_slots[ORTHO][WIDTH], (float)g_width);
    glUniform1f(g_slots[ORTHO][HEIGHT], (float)g_height);
    glUniform4f(g_slots[ORTHO][COLOR], 1, 1, 1, 1);
    glEnableVertexAttribArray(g_slots[ORTHO][POSITION]);
    glEnableVertexAttribArray(g_slots[ORTHO][TEXCOORD]);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    g_GUI.draw();
    //DrawShadowedText(MSGOTHIC16, 0, 0, "Hello world. My name is Denis.");
    glEnable(GL_DEPTH_TEST);
}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_SurfChang(JNIEnv * env, jobject obj,  jint width, jint height)
{
    Resize(width, height);
	Reload();
}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_SurfCreat(JNIEnv * env, jobject obj,  jint width, jint height)
{

}

JNIEXPORT void JNICALL Java_com_pathogengame_pathogen_MainActivity_Step(JNIEnv * env, jobject obj)
{
	Update();
    Draw();
}

static void loadAPK (const char* apkPath) 
{
  LOGI("Loading APK %s", apkPath);
  APKArchive = zip_open(apkPath, 0, NULL);
  if (APKArchive == NULL) 
  {
    LOGE("Error loading APK");
    return;
  }

  //Just for debug, print APK contents
  int numFiles = zip_get_num_files(APKArchive);
  for (int i=0; i<numFiles; i++) 
  {
    const char* name = zip_get_name(APKArchive, i, 0);
    if (name == NULL) 
	{
      LOGE("Error reading zip file name at index %i : %s", zip_strerror(APKArchive));
      return;
    }
    LOGI("File %i : %s\n", i, name);
  }
}

JNIEXPORT void Java_com_pathogengame_pathogen_MainActivity_nativeInit(JNIEnv * env, jclass cls, jstring apkPath, jstring tmpPath, jobject amgr) 
{
  const char* str;
  jboolean isCopy;
  str = env->GetStringUTFChars(apkPath, &isCopy);
  //loadAPK(str);
  strcpy(g_path, str);
  str = env->GetStringUTFChars(tmpPath, &isCopy);
  strcpy(g_tempPath, str);
  g_amgr = AAssetManager_fromJava(env, amgr);
  g_env = env;

  /*
  int width, height;
  texture = loadTextureFromPNG("assets/sprites/texture.png", width, height);

  printGLString("Version", GL_VERSION);
  printGLString("Vendor", GL_VENDOR);
  printGLString("Renderer", GL_RENDERER);
  printGLString("Extensions", GL_EXTENSIONS);

  glEnableClientState(GL_VERTEX_ARRAY);
  glEnableClientState(GL_TEXTURE_COORD_ARRAY);
  glEnable(GL_BLEND);
  glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
  glEnable(GL_TEXTURE_2D);
  glClearColor(1,0,0,0);
  glColor4f(1,1,1,1);
  glDisable(GL_DEPTH_TEST);
  glDisable(GL_CULL_FACE);*/
}

/*
- (void) touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event
{
    [self.view endEditing:YES];
    
    [super touchesBegan:touches withEvent:event];
    
    NSArray *allTouches = [touches allObjects];
    //UITouch *touch = [touches anyObject];
    //if([touch tapCount] < 2)
    
    int count = [allTouches count];
    CGPoint touch;
    
    for(int i=0; i<count; i++)
    {
        touch = [[allTouches objectAtIndex:i] locationInView:self.view];
        
        g_touch.push_back(touch);
        g_GUI.lbuttondown(touch.x, touch.y);
    }
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event
{
    [super touchesMoved:touches withEvent:event];
    
    NSArray *allTouches = [touches allObjects];
    int count = [allTouches count];
    CGPoint touch;
    CGPoint* t;
    int j;
    int nearest;
    float nearestD;
    float D;
    int k;
    vector<int> modified;
    bool found;
    
    for(int i=0; i<count; i++)
    {
        touch = [[allTouches objectAtIndex:i] locationInView:self.view];
        
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
            D = Magnitude2(t->x - touch.x, t->y - touch.y);
            
            if(D < nearestD)
                nearest = j;
        }
        
        if(nearest >= 0)
        {
            g_touch[nearest] = touch;
            modified.push_back(nearest);
        }
        else
        {
            g_touch.push_back(touch);
            modified.push_back(g_touch.size()-1);
        }
        
        g_GUI.mousemove(touch.x, touch.y);
    }
    
    g_GUI.touchcheck();
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event
{
    [super touchesEnded:touches withEvent:event];
    
    NSArray *allTouches = [touches allObjects];
    int count = [allTouches count];
    
    CGPoint touch;
    CGPoint* t;
    int j;
    int nearest;
    float nearestD;
    float D;
    
    for(int i=0; i<count; i++)
    {
        touch = [[allTouches objectAtIndex:i] locationInView:self.view];
        
        nearestD = 999999999.0;
        nearest = -1;
        for(j=0; j<g_touch.size(); j++)
        {
            t = &g_touch[j];
            D = Magnitude2(t->x - touch.x, t->y - touch.y);
            
            if(D < nearestD)
                nearest = j;
        }
        
        if(nearest >= 0)
            g_touch.erase( g_touch.begin() + nearest );
        
        g_GUI.lbuttonup(touch.x, touch.y);
    }
    
    g_GUI.touchcheck();
}
*/