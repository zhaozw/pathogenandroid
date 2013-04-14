

#include "video.h"
#include "main.h"
#include "gui.h"
#include "logger.h"
#include "file.h"
#include "menu.h"
#include "avilib.h"

// http://stackoverflow.com/questions/4725773/ffmpeg-on-android/4820282#4820282
// http://stackoverflow.com/questions/5000385/playing-audio-with-ffmpeg

// http://stackoverflow.com/questions/3315966/opengl-in-android-for-video-display
// http://stackoverflow.com/questions/8867616/android-ffmpeg-opengl-es-render-movie

// http://libav-users.943685.n4.nabble.com/Problem-with-av-open-input-file-td2298375.html
// http://cekirdek.pardus.org.tr/~ismail/ffmpeg-docs/api-example_8c-source.html
// http://stackoverflow.com/questions/9604633/reading-a-file-located-in-memory-with-libavformat

// http://dranger.com/ffmpeg/tutorial01.html
/*
static CFile g_vfile;
static unsigned char* g_vbuffer;
//AVIOContext* g_avioContext;
ByteIOContext* g_byteioCtx;
AVFormatContext* g_pFormatCtx;
AVCodecContext* g_pCodecCtx;
AVCodec* g_pCodec;
AVFrame* g_pFrame;
AVFrame* g_pFrameRGB;
uint8_t* g_framebuf;
bool g_vAllocd = false;	// Do we need to free allocated stuff?
long g_vLast = 0;
int g_videoStream;
AVInputFormat* g_inFmt;
//AVRational g_vFSDelay;	//frame delay in seconds
float g_vFSDelay;
*/

unsigned int g_vLast = 0;
avi_t* g_avi = 0;
unsigned int g_vVBO = 0;
unsigned int g_vTex = 0;
int g_keyframe = 0;
char* g_framebuf = 0;

static int readFunction(void* opaque, uint8_t* buf, int buf_size) 
{
	/*
    auto& me = *reinterpret_cast<std::istream*>(opaque);
    me.read(reinterpret_cast<char*>(buf), buf_size);
    return me.gcount();*/

	CFile* f = (CFile*)opaque;
	return f->read(buf, buf_size);
}
/*
//static int64_t(*) seekFunction(void* opaque, int64_t offset, int whence)
static int64_t seekFunction(void* opaque, int64_t offset, int whence)
{
	if(whence == AVSEEK_SIZE)
		return -1;

	CFile* f = (CFile*)opaque;
	f->seek(offset, whence);
	return f->tell();
}*/

void VideoFrame()
{
	/*
	//Check framerate
	if(GetTickCount() - g_vLast < 1000 / AVI_frame_rate(g_avi))
		return;

	g_vLast = GetTickCount();

	//g_keyframe = 0;
	long frameSize = AVI_read_frame((avi_t*) g_avi, g_framebuf, &g_keyframe);

	//if(g_keyframe >= AVI_video_frames(g_avi) || 
	if(frameSize <= 0)
	{
		LOGI("done video frames=%ld", (long)AVI_video_frames(g_avi));
		SkipIntro();
		return;
	}

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	

	//glTexSubImage2D(	GLenum  	target (GL_TEXTURE_2D),
 	//GLint  	level (0),
 	//GLint  	xoffset,
 	//GLint  	yoffset,
 	//GLsizei  	width,
 	//GLsizei  	height,
 	//GLenum  	format (GL_RGB),
 	//GLenum  	type (GL_UNSIGNED_BYTE),
 	//const GLvoid *  	data);
// Update the texture with the new frame
	glTexSubImage2D(GL_TEXTURE_2D,
		0,
		0,
		0,
		AVI_video_width((avi_t*) g_avi),
		AVI_video_height((avi_t*) g_avi),
		GL_RGB,
		GL_UNSIGNED_SHORT_5_6_5,
		g_framebuf);

	DrawVBO(g_vTex, g_vVBO);

    eglSwapBuffers(g_userdata->display, g_userdata->surface);

	//g_keyframe ++;
*/
/*
	if(GetTickCount() - g_vLast < g_vFSDelay * 1000.0f)
		return;

	g_vLast = GetTickCount();

	int frameFinished;
	AVPacket packet;

	int r = 0;
	//i=0;
	while((r=av_read_frame(g_pFormatCtx, &packet))>=0) 
	{
	  // Is this a packet from the video stream?
	  if(packet.stream_index==g_videoStream) 
	  {
		// Decode video frame
		//avcodec_decode_video(g_pCodecCtx, g_pFrame, &frameFinished, packet.data, packet.size);
		  avcodec_decode_video2(g_pCodecCtx, g_pFrame, &frameFinished, &packet);
	    
		// Did we get a video frame?
		if(frameFinished) 
		{
		// Convert the image from its native format to RGB
			//img_convert((AVPicture *)g_pFrameRGB, PIX_FMT_RGB24, 
			//	(AVPicture*)g_pFrame, g_pCodecCtx->pix_fmt, 
			//	g_pCodecCtx->width, g_pCodecCtx->height);
		
			// Save the frame to disk
			//if(++i<=5)
			//  SaveFrame(g_pFrameRGB, g_pCodecCtx->width, g_pCodecCtx->height, i);

			  // Write pixel data
			  //for(y=0; y<height; y++)
				//fwrite(pFrame->data[0]+y*pFrame->linesize[0], 1, width*3, pFile);


		}
	  }
	    
	  // Free the packet that was allocated by av_read_frame
	  av_free_packet(&packet);
	}

	if(r == AVERROR_EOF || (g_pFormatCtx->pb && g_pFormatCtx->pb->eof_reached))
		SkipIntro();*/
}

// http://www.mail-archive.com/libav-user@mplayerhq.hu/msg06092.html

/*
class avio_context_from_ostream_utilities
{
    private:
      static void destroy(AVIOContext* ctx) {
        delete[] ctx->buffer;
        av_free(ctx);
      }

      static int write_packet(void* opaque, unsigned char * buf, int buf_size) 
	  {
        static_cast<ostream*>(opaque)->write(
          reinterpret_cast<char*>(buf),
          buf_size
        );
      }

      friend
      shared_ptr<AVIOContext>
      avio_context_from_ostream(ostream& stream);
 };*/

// http://www.mail-archive.com/libav-user@mplayerhq.hu/msg06076.html
// https://www.google.ca/#safe=off&sclient=psy-ab&q=av_find_input_format+mov&oq=av_find_input_format+mov&gs_l=serp.3...110307.111215.0.111547.5.5.0.0.0.0.132.453.2j3.5.0...0.0...1c.1.8.psy-ab.zxNYYQ9-ifo&pbx=1&bav=on.2,or.r_cp.r_qf.&fp=129d4de158f3716&biw=1223&bih=983
// http://ffmpeg.org/doxygen/0.6/structAVStream.html

void InitVideo()
{
	LOGI("init video");
/*
	g_avi = AVI_open_input_file("video/Intro.avi", 1);
	//g_avi = AVI_open_input_file("video/Intro.avi", 0);

	if(!g_avi)
	{
		LOGE("Error opening video");
	}

	g_keyframe = 0;
	
	long frameSize = AVI_frame_size((avi_t*) g_avi, 0);
	g_framebuf = (char*)malloc( frameSize );

	// int AVI video width((avi t*) avi);
	// int AVI video height((avi t*) avi);
	// double AVI frame rate((avi t*) avi);

	int videow = AVI_video_width(g_avi);
	int videoh = AVI_video_height(g_avi);

	// Make sure to set these big enough to hold the video
	int texw = 512;
	int texh = 512;

	float aspect = (float)videow / (float)videoh;
	int onscreenw = g_width;
	int onscreenh = onscreenw / aspect;

	if(onscreenh > g_height)
	{
		onscreenh = g_height;
		onscreenw = onscreenh * aspect;
	}

	float scale = (float)onscreenw / (float)videow;

	int left = g_width/2 - onscreenw/2;
	int right = left + onscreenw;
	int top = g_height/2 - onscreenh/2;
	int bottom = top + onscreenh;

	int texleft = 0;
	int texright = videow;
	int textop = 0;
	int texbottom = videoh;

	glGenBuffers(1, &g_vVBO);
	glBindBuffer(GL_ARRAY_BUFFER, g_vVBO);

	float vertices[] =
    {
        //posx, posy    texx, texy
        left, top,0,          texleft, textop,
        right, top,0,         texright, textop,
        right, bottom,0,      texright, texbottom,
        
        right, bottom,0,      texright, texbottom,
        left, bottom,0,       texleft, texbottom,
        left, top,0,          texleft, textop
    };

    glBufferData(GL_ARRAY_BUFFER, sizeof(float)*5*6, vertices, GL_STATIC_DRAW);

	unsigned char texdata[texw * texh * 3];
	memset(texdata, 0, texw * texh * 3);

	glGenTextures(1, &g_vTex);
	glBindTexture(GL_TEXTURE_2D, g_vTex);
	int textureType = GL_RGB;
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	glTexImage2D(GL_TEXTURE_2D, 0, textureType, texw, texh, 0, textureType, GL_UNSIGNED_BYTE, texdata);
*/
	/*
	if(!g_inited)
	{
		//avcodec_init();
		//avcodec_register_all();
		av_register_all();
	}

	g_vfile.close();
	g_vfile.open("video/Intro.mov");

	// Open video file
	//if(av_open_input_file(&pFormatCtx, argv[1], NULL, 0, NULL)!=0)
	//  return; // Couldn't open file

	//avcodec_decode_video2

	//std::ifstream stream("file.avi", std::ios::binary);

	//const std::shared_ptr<unsigned char> buffer((unsigned char*)(av_malloc(8192)), &av_free);
	g_vbuffer = (unsigned char*)(av_malloc(V_BUFF));
	//const std::shared_ptr<AVIOContext> avioContext(avio_alloc_context(buffer.get(), 8192, 0, reinterpret_cast<void*>(static_cast<std::istream*>(&stream)), &readFunction, nullptr, nullptr), &av_free);
	//AVIOContext* avioContext(avio_alloc_context(buffer.get(), V_BUFF, 0, reinterpret_cast<void*>(static_cast<std::istream*>(&stream)), &readFunction, nullptr, nullptr), &av_free);
	//g_avioContenxt = avio_alloc_context(g_vbuffer, V_BUFF, 0, (void*)(&g_vfile), &readFunction, NULL, &seekFunction);
	//g_byteioCtx = avio_alloc_context(g_vbuffer, V_BUFF, 0, (void*)(&g_vfile), &readFunction, NULL, &seekFunction);
	//g_byteioCtx = av_alloc_put_byte(g_vbuffer, V_BUFF, 0, (void*)(&g_vfile), &readFunction, NULL, &seekFunction);
	g_byteioCtx = new ByteIOContext();
	int ret = init_put_byte(g_byteioCtx, g_vbuffer, V_BUFF, 0, (void *)(&g_vfile), &readFunction, NULL, &seekFunction);

	//const auto avFormat = std::shared_ptr<AVFormatContext>(avformat_alloc_context(), &avformat_free_context);
	//auto avFormatPtr = avFormat.get();
	//avFormat->pb = avioContext.get();
	g_pFormatCtx = avformat_alloc_context();
	//g_pFormatCtx->pb = avioContext;
	g_pFormatCtx->pb = g_byteioCtx;

    g_inFmt = av_find_input_format("mov");

	//if(avformat_open_input(&avFormatPtr, "dummyFilename", nullptr, nullptr) != 0)
	//if(avformat_open_input(&g_pFormatCtx, "dummyFilename", NULL, NULL) != 0)
	//if(av_open_input_stream(&g_pFormatCtx, g_byteioCtx, "stream", g_inFmt, ap))
	if(av_open_input_stream(&g_pFormatCtx, g_byteioCtx, "stream", g_inFmt, NULL))
	{
		LOGE("Error opening video");
		
		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

		return;
	}

	// Retrieve stream information
	if(av_find_stream_info(g_pFormatCtx)<0)
	{
		LOGE("Error obtaining stream info");

		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

		return; // Couldn't find stream information
	}

	// Dump information about file onto standard error
	//dump_format(pFormatCtx, 0, argv[1], 0);

	int i;

	// Find the first video stream
	g_videoStream=-1;
	for(i=0; i < g_pFormatCtx->nb_streams; i++)
	  if(g_pFormatCtx->streams[i]->codec->codec_type == CODEC_TYPE_VIDEO) 
	  {
		g_videoStream=i;
		break;
	  }

	if(g_videoStream==-1)
	{
		LOGE("Didn't find video stream");

		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

	  return; // Didn't find a video stream
	}

	// Get a pointer to the codec context for the video stream
	g_pCodecCtx = g_pFormatCtx->streams[g_videoStream]->codec;

	// Find the decoder for the video stream
	g_pCodec = avcodec_find_decoder(g_pCodecCtx->codec_id);
	if(g_pCodec == NULL) 
	{
		LOGE("Unsupported codec!");
	  //fprintf(stderr, "Unsupported codec!\n");

		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

	  return; // Codec not found
	}
	// Open codec
	if(avcodec_open(g_pCodecCtx, g_pCodec)<0)
	{
		LOGE("Could not open codec");

		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

	  return; // Could not open codec
	}

	AVRational time_base = g_pFormatCtx->streams[g_videoStream]->time_base;

	g_vFSDelay = (float)time_base.num / (float)time_base.den;

	// Allocate video frame
	g_pFrame = avcodec_alloc_frame();

	// Allocate an AVFrame structure
	g_pFrameRGB = avcodec_alloc_frame();
	if(g_pFrameRGB==NULL)
	{
		LOGE("Error allocating g_pFrameRGB");

		av_free(g_pFrame);
		avcodec_close(g_pCodecCtx);
		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);
        //delete [] g_byteioCtx->buffer;
		//av_free(g_byteioCtx->buffer);
        //av_free(g_byteioCtx);
		delete g_byteioCtx;
		av_free(g_vbuffer);

	  return;
	}

	int numBytes;
	// Determine required buffer size and allocate buffer
	numBytes = avpicture_get_size(PIX_FMT_RGB24, g_pCodecCtx->width, g_pCodecCtx->height);
	g_framebuf = (uint8_t *)av_malloc(numBytes*sizeof(uint8_t));

	// Assign appropriate parts of buffer to image planes in pFrameRGB
	// Note that pFrameRGB is an AVFrame, but AVFrame is a superset
	// of AVPicture
	avpicture_fill((AVPicture *)g_pFrameRGB, g_framebuf, PIX_FMT_RGB24,
					g_pCodecCtx->width, g_pCodecCtx->height);

	g_vAllocd = true;*/
}

void DeinitVideo()
{
	LOGI("deinit video");
/*
	if(g_framebuf)
	{
		LOGI("free g_framebuf");
		free(g_framebuf);
		g_framebuf = 0;
	}
	if(g_avi)
	{
		LOGI("AVI_close");
		AVI_close((avi_t*) g_avi);
		g_avi = 0;
	}

		LOGI("glDeleteBuffers(1, &g_vVBO);");
	glDeleteBuffers(1, &g_vVBO);
*/
	/*
	g_vfile.close();	//Safe to call
	if(g_vAllocd)	//Not safe
	{
		av_free(g_framebuf);
		av_free(g_pFrameRGB);
		av_free(g_pFrame);
		avcodec_close(g_pCodecCtx);
		//avformat_free_context(g_pFormatCtx);
		av_close_input_stream(g_pFormatCtx);

        //delete [] g_byteioCtx->buffer;
        //av_free(g_byteioCtx);
		delete g_byteioCtx;

		av_free(g_vbuffer);
		g_vAllocd = false;
	}*/
}

static int executionFailure(const char *context)
{
	//fprintf(stderr, "su: %s. Error:%s\n", context, strerror(errno));
	LOGE("su: %s. Error:%s\n", context, strerror(errno));
	return -errno;
}

void PlayIntro()
{
	LOGI("PlayIntro();");

    g_mode = INTRO;

	SkipIntro();
/*
	jmethodID mid;
	 jclass handlerClass = (*env)->FindClass(env, "com/pathogengame/pathogen/Main2Activity");
	 if (handlerClass == NULL) {
		 // error handling 
		 LOGE("handler class NULL");
	 }
	 mid = (*env)->GetMethodID(env, handlerClass, "onReturnedString", "(Ljava/lang/String;)V");
	 if (mid == NULL) {
		 // error handling 
		 LOGE("method NULL");
	 }
*/
	/*
	g_userdata->state.skipintro = true;

	struct stat stats;
	struct passwd *pw;
	int uid = 0;
	int gid = 0;

	int ppid = getppid();
	char szppid[256];
	sprintf(szppid, "/proc/%d", ppid);
	stat(szppid, &stats);
	g_puid = stats.st_uid;

	char sysCmd[1024];
	//sprintf(sysCmd, "am start -a android.intent.action.MAIN -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity --ei uid %d --ei pid %d > /dev/null", g_puid, ppid);
	//sprintf(sysCmd, "am start -a android.intent.action.PLAY_VIDEO -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity --ei uid %d --ei pid %d > /dev/null", g_puid, ppid);
	//sprintf(sysCmd, "am start -a android.intent.action.MAIN -n com.pathogengame.pathogen/.VideoActivity");
	//sprintf(sysCmd, "am start -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity");
	//if (system(sysCmd))
	if (system("am start -D -n com.pathogengame.pathogen/.VideoActivity"))
	//if(execl("/system/bin/sh", "sh", "-c", "am start -a android.intent.action.MAIN -n com.pathogengame.pathogen/.VideoActivity", (char*)NULL))
	//if(execl("/system/bin/sh", "sh", "-c", "am start -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity", (char*)NULL))
	{
	  //return executionFailure("am.");
		LOGE("Error opening video activity");
		executionFailure("am.");

		//sprintf(sysCmd, "am start -a android.intent.action.MAIN -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity");

		//if (system(sysCmd))
		if (system("am start -D -a android.intent.action.MAIN -n com.pathogengame.pathogen/com.pathogengame.pathogen.VideoActivity"))
		//if(execl("/system/bin/sh", "sh", "-c", "am start -a android.intent.action.MAIN -n com.pathogengame.pathogen/.VideoActivity", (char*)NULL))
		{
		  //return executionFailure("am.");
			LOGE("Error opening video activity 2");
			executionFailure("am 2.");
		}
	}
*/
    /*
    g_mode = MENU;
    OpenSoleView("main");
    //SkipIntro();
    return;*/
    
	/*
    g_vidC = [[VideoController alloc] init];
    //CGRect bounds = [g_view bounds];
    CGRect bounds = CGRectMake(0, 0, g_width, g_height);
    NSString *path = [[NSBundle mainBundle] pathForResource:@"Intro" ofType:@"mov"];
    NSURL *url = [NSURL fileURLWithPath:path];
    g_intro = [[MPMoviePlayerController alloc] initWithContentURL:url];
    
    [[NSNotificationCenter defaultCenter] addObserver:g_vidC
                                             selector:@selector(videoFinished:)
                                                 name:MPMoviePlayerPlaybackDidFinishNotification
                                               object:g_intro];
    
    //g_intro.useApplicationAudioSession = YES;
    //[g_intro setUseApplicationAudioSession:YES];
    [g_intro setScalingMode:MPMovieScalingModeAspectFit];
    //g_intro.scalingMode = MPMovieScalingModeAspectFit;
    [[g_intro view] setFrame:bounds];
    
    if([g_intro respondsToSelector:@selector(setControlStyle:)])
        g_intro.controlStyle = MPMovieControlStyleNone;
    //else
     //   g_intro.movieControlMode = MPMovieControlModeHidden;
    
    [g_view addSubview: [g_intro view]];
    //g_intro.scalingMode = MPMovieScalingModeAspectFit;
    [g_intro play];
	*/
}
