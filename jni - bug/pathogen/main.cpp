

#include "logger.h"
#include "main.h"
#include "shader.h"
#include "file.h"
#include "image.h"

float g_width;
float g_height;
bool g_quit = false;
AAssetManager* g_amgr;
unsigned int g_img = 0;

unsigned int g_arrayVBO = 0;

void MakeArrayVBO()
{

}

void DrawArray()
{
	/*
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, g_img);
    glUniform1i(g_slots[MODEL][TEXTURE], 0);

	float vertices[] =
	{
		//posx, posy posz  pad texx, texy
		0, 0, 0,        	0,	  0, 0,
		0, 0, 1,         	0,	 0, 1,
		1, 0, 1,         	0,	 1, 1,
		        
		1, 0, 1,        	0,	  1, 1,
		1, 0, 0,         	0,	 1, 0,
		0, 0, 0,         	0,	 0, 0
	};
      
	glVertexAttribPointer(g_slots[MODEL][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*6, &vertices[0]);
    glVertexAttribPointer(g_slots[MODEL][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*6, &vertices[4]);
        
    glDrawArrays(GL_TRIANGLES, 0, 6);
    */
}

void DrawImage(unsigned int tex, float left, float top, float right, float bottom)
{
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, tex);
    glUniform1i(g_slots[ORTHO][TEXTURE], 0);
	
    float vertices[] =
    {
        //posx, posy   pad	 texx, texy
        left, top,0,      0,    0, 0,
        right, top,0,     0,    1, 0,
        right, bottom,0,  0,    1, 1,
        
        right, bottom,0,  0,    1, 1,
        left, bottom,0,   0,    0, 1,
        left, top,0,      0,    0, 0
    };
    
    glVertexAttribPointer(g_slots[ORTHO][POSITION], 3, GL_FLOAT, GL_FALSE, sizeof(float)*6, &vertices[0]);
    glVertexAttribPointer(g_slots[ORTHO][TEXCOORD], 2, GL_FLOAT, GL_FALSE, sizeof(float)*6, &vertices[4]);

    glDrawArrays(GL_TRIANGLES, 0, 6);
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

    // Initialize GL state.
    glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_FASTEST);
	glEnable(GL_DEPTH_TEST);
	glDisable(GL_CULL_FACE);
	
	g_amgr = engine->app->activity->assetManager;
    
	LoadShaders();
	g_img = CreateTexture("models/human2.jpg");

	engine->animating = 1;

    return 0;
}

static void engine_draw_frame(struct engine* engine) 
{
    if (engine->display == NULL) 
        return;

	LOGI("start draw");

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    glUseProgram(g_program[MODEL]);
    //glUniformMatrix4fv(g_slots[MODEL][PROJECTION], 1, 0, projection.getMatrix());
    //glUniformMatrix4fv(g_slots[MODEL][MODELMAT], 1, 0, modelmat.getMatrix());
    //glUniformMatrix4fv(g_slots[MODEL][VIEWMAT], 1, 0, viewmat.getMatrix());
    glEnableVertexAttribArray(g_slots[MODEL][POSITION]);
    glEnableVertexAttribArray(g_slots[MODEL][TEXCOORD]);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    DrawArray();	// <-- Try commenting out this line

    glUseProgram(g_program[ORTHO]);
    glUniform1f(g_slots[ORTHO][WIDTH], (float)g_width);
    glUniform1f(g_slots[ORTHO][HEIGHT], (float)g_height);
    glEnableVertexAttribArray(g_slots[ORTHO][POSITION]);
    glEnableVertexAttribArray(g_slots[ORTHO][TEXCOORD]);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	DrawImage(g_img, 0, 0, 300, 300);

    eglSwapBuffers(engine->display, engine->surface);
}

// Tear down the EGL context currently associated with the display.
static void engine_term_display(struct engine* engine) 
{
	glDeleteTextures(1, &g_img);

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
static int32_t engine_handle_input(struct android_app* app, AInputEvent* event) 
{
    return 0;
}

// Process the next main command.
static void engine_handle_cmd(struct android_app* app, int32_t cmd) 
{
    struct engine* engine = (struct engine*)app->userData;
    switch (cmd) {
        case APP_CMD_SAVE_STATE:
            // The system has asked us to save our current state.  Do so.
            engine->app->savedState = malloc(sizeof(struct saved_state));
            *((struct saved_state*)engine->app->savedState) = engine->state;
            engine->app->savedStateSize = sizeof(struct saved_state);
            break;
        case APP_CMD_INIT_WINDOW:
			 // The window is being shown, get it ready.
            if (engine->app->window != NULL) 
			{
                engine_init_display(engine);
                //engine_draw_frame(engine);
            }
            break;
        case APP_CMD_TERM_WINDOW:
            // The window is being hidden or closed, clean it up.
            engine_term_display(engine);
			g_quit = true;
            break;
        case APP_CMD_GAINED_FOCUS:
			
            // When our app gains focus, we start monitoring the accelerometer.
            if (engine->accelerometerSensor != NULL) 
			{
                ASensorEventQueue_enableSensor(engine->sensorEventQueue, engine->accelerometerSensor);
                // We'd like to get 60 events per second (in us).
                ASensorEventQueue_setEventRate(engine->sensorEventQueue, engine->accelerometerSensor, (1000L/30)*1000);
            }
			
            break;
        case APP_CMD_LOST_FOCUS:
			
            // When our app loses focus, we stop monitoring the accelerometer.
            // This is to avoid consuming battery while not being used.
            if (engine->accelerometerSensor != NULL) {
                ASensorEventQueue_disableSensor(engine->sensorEventQueue,
                        engine->accelerometerSensor);
            }
            // Also stop animating.
            engine->animating = 0;
			
            engine_draw_frame(engine);
            break;
    }
}

// This is the main entry point of a native application that is using
// android_native_app_glue.  It runs in its own thread, with its own
// event loop for receiving input events and doing other things.
void android_main(struct android_app* state) 
{
    struct engine engine;

    // Make sure glue isn't stripped.
    app_dummy();

    memset(&engine, 0, sizeof(engine));
    state->userData = &engine;
    state->onAppCmd = engine_handle_cmd;
    state->onInputEvent = engine_handle_input;
    engine.app = state;

    // Prepare to monitor accelerometer
    engine.sensorManager = ASensorManager_getInstance();
    engine.accelerometerSensor = ASensorManager_getDefaultSensor(engine.sensorManager, ASENSOR_TYPE_ACCELEROMETER);
    engine.sensorEventQueue = ASensorManager_createEventQueue(engine.sensorManager, state->looper, LOOPER_ID_USER, NULL, NULL);

    if (state->savedState != NULL) 
	{
        // We are starting with a previous saved state; restore from it.
        engine.state = *(struct saved_state*)state->savedState;
    }

	g_amgr = state->activity->assetManager;

    // loop waiting for stuff to do.
    while (!g_quit) 
	{
        // Read all pending events.
        int ident;
        int events;
        struct android_poll_source* source;

        // If not animating, we will block forever waiting for events.
        // If animating, we loop until all events are read, then continue
        // to draw the next frame of animation.
        while ((ident=ALooper_pollAll(engine.animating ? 0 : -1, NULL, &events, (void**)&source)) >= 0) 
		{
            // Process this event.
            if (source != NULL) 
			{
                source->process(state, source);
            }

            // If a sensor has data, process it now.
            if (ident == LOOPER_ID_USER) {
                if (engine.accelerometerSensor != NULL) {
                    ASensorEvent event;
                    while (ASensorEventQueue_getEvents(engine.sensorEventQueue, &event, 1) > 0) {
                        //LOGI("accelerometer: x=%f y=%f z=%f", event.acceleration.x, event.acceleration.y, event.acceleration.z);
                    }
                }
            }

            // Check if we are exiting.
            if (state->destroyRequested != 0) 
			{
                engine_term_display(&engine);
                return;
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

			//LOGI("animate");
			// Drawing is throttled to the screen update rate, so there
			// is no need to do timing here.
			engine_draw_frame(&engine);
        }
    }

	ANativeActivity_finish(state->activity);
}
