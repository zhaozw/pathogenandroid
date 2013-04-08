

#include "sound.h"
#include "main.h"
#include "gui.h"
#include "menu.h"
#include "item.h"
#include "file.h"
#include "wavlib.h"

// engine interfaces
static SLObjectItf engineObject = NULL;
static SLEngineItf engineEngine;

// output mix interfaces
static SLObjectItf outputMixObject = NULL;
static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;

// buffer queue player interfaces
//static SLObjectItf bqPlayerObject = NULL;
//static SLPlayItf bqPlayerPlay;
//static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
//static SLEffectSendItf bqPlayerEffectSend;
//static SLMuteSoloItf bqPlayerMuteSolo;
//static SLVolumeItf bqPlayerVolume;

// aux effect on the output mix, used by the buffer queue player
static const SLEnvironmentalReverbSettings reverbSettings =
    SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;

// file descriptor player interfaces
//static SLObjectItf fdPlayerObject = NULL;
//static SLPlayItf fdPlayerPlay;
//static SLSeekItf fdPlayerSeek;
//static SLMuteSoloItf fdPlayerMuteSolo;
//static SLVolumeItf fdPlayerVolume;

vector<CSound> g_zdeathSnd;
vector<CSound> g_zgraspSnd;
vector<CSound> g_zpainSnd;
CSound g_doorKnock;
CSound g_staticSound;
CChannel g_channel[CHANNELS];

// create the engine and output mix objects
void InitSound()
{
    SLresult result;

    // create engine
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;

    // realize the engine
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;

    // get the engine interface, which is needed in order to create other objects
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;

    // create output mix, with environmental reverb specified as a non-required interface
    const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
    const SLboolean req[1] = {SL_BOOLEAN_FALSE};
    result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;

    // realize the output mix
    result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;

    // get the environmental reverb interface
    // this could fail if the environmental reverb effect is not available,
    // either because the feature is not present, excessive CPU load, or
    // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
    result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
            &outputMixEnvironmentalReverb);
    if (SL_RESULT_SUCCESS == result) 
	{
        result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(outputMixEnvironmentalReverb, &reverbSettings);
        //(void)result;
    }
    // ignore unsuccessful result codes for environmental reverb, as it is optional for this example

	for(int i=0; i<CHANNELS; i++)
	{
		g_channel[i].Create();
	}
}

// shut down the native audio system
void DeinitSound()
{/*
    // destroy buffer queue audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL) 
	{
        (*bqPlayerObject)->Destroy(bqPlayerObject);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        bqPlayerBufferQueue = NULL;
        bqPlayerEffectSend = NULL;
        bqPlayerMuteSolo = NULL;
        bqPlayerVolume = NULL;
    }*/

	for(int i=0; i<CHANNELS; i++)
	{
		g_channel[i].Destroy();
	}

    // destroy output mix object, and invalidate all associated interfaces
    if (outputMixObject != NULL) 
	{
        (*outputMixObject)->Destroy(outputMixObject);
        outputMixObject = NULL;
        outputMixEnvironmentalReverb = NULL;
    }

    // destroy engine object, and invalidate all associated interfaces
    if (engineObject != NULL) 
	{
        (*engineObject)->Destroy(engineObject);
        engineObject = NULL;
        engineEngine = NULL;
    }

}

void CChannel::Destroy()
{
	// destroy file descriptor audio player object, and invalidate all associated interfaces
    if (bqPlayerObject != NULL) 
	{
        (*bqPlayerObject)->Destroy(bqPlayerObject);
		//DestroyObject(bqPlayerObject);
		//FreePlayerBuffer(bqBufferQueue);
        bqPlayerObject = NULL;
        bqPlayerPlay = NULL;
        //bqPlayerSeek = NULL;
        //bqPlayerMuteSolo = NULL;
		//bqPlayerVolume = NULL;
    }
}

// this callback handler is called every time a buffer finishes playing
void bqPlayerCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
{
	//LOGI("playercallb");

    //assert(bq == bqPlayerBufferQueue);
    //assert(NULL == context);
	CChannel* c = (CChannel*)context;
	
	//LOGI("playercallb 1");

	CSound* s = c->sound;

	//LOGI("playercallb 2");

	c->nextSize = min(SBC_AUDIO_OUT_BUFFER_SIZE, s->datalen - c->position);

	//LOGI("enqueue %d", c->nextSize);

    // for streaming playback, replace this test by logic to find and fill the next buffer
    if (c->nextBuffer != NULL && c->nextSize > 0) 
	{
		c->position += c->nextSize;
		c->nextBuffer = &(s->data[c->position]);

        SLresult result;
        // enqueue another buffer
        result = (*(c->bqBufferQueue))->Enqueue(c->bqBufferQueue, c->nextBuffer, c->nextSize);
        // the most likely other result is SL_RESULT_BUFFER_INSUFFICIENT,
        // which for this code example would indicate a programming error
        //assert(SL_RESULT_SUCCESS == result);
        //(void)result;
    }
}

// http://audioprograming.wordpress.com/2012/03/03/android-audio-streaming-with-opensl-es-and-the-ndk/
void CChannel::Create()
{
	SLDataLocator_AndroidSimpleBufferQueue loc_bufq =
                           {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
	SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8,
               SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
               SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
	SLDataSource audioSrc = {&loc_bufq, &format_pcm};

	SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
	SLDataSink audioSnk = {&loc_outmix, NULL};

	//const SLInterfaceID ids1[] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
	const SLInterfaceID ids1[] = {SL_IID_BUFFERQUEUE};
	const SLboolean req1[] = {SL_BOOLEAN_TRUE};
	SLresult result = (*engineEngine)->CreateAudioPlayer(engineEngine,
                    &(bqPlayerObject), &audioSrc, &audioSnk,
                     1, ids1, req1);
	//...
	result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);

	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, 
                                 SL_IID_PLAY,&(bqPlayerPlay));

	//result = (*bqPlayerObject)->GetInterface(bqPlayerObject,
    //   SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &(bqBufferQueue));
	
	result = (*bqPlayerObject)->GetInterface(bqPlayerObject,
       SL_IID_BUFFERQUEUE, &(bqBufferQueue));

	//result = (*p->bqPlayerBufferQueue)->RegisterCallback(p->bqPlayerBufferQueue, bqPlayerCallback, p);
    result = (*bqBufferQueue)->RegisterCallback(bqBufferQueue, bqPlayerCallback, this);

	/*
    // configure audio source
    SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
    SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8,
        SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
        SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
    SLDataSource audioSrc = {&loc_bufq, &format_pcm};
	
    // configure audio sink
	SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};

    // create audio player
    const SLInterfaceID ids[3] = {SL_IID_EFFECTSEND,
		//SL_IID_SEEK, SL_IID_MUTESOLO, 
		SL_IID_VOLUME, SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
    const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
    result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc, &audioSnk,
            3, ids, req);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("CreateAudioPlayer() error %s", filename);
	}

    // realize the player
    result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("Realize() error %s", filename);
	}

	result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &(sample->fdBufferQueue));
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("GetInterface(SL_IID_ANDROIDSIMPLEBUFFERQUEUE) error %s", filename);
	}

    // get the play interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_PLAY, &fdPlayerPlay);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("GetInterface(SL_IID_PLAY) error %s", filename);
	}

    // get the seek interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_SEEK, &fdPlayerSeek);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("GetInterface(SL_IID_SEEK) error %s", filename);
	}

    // get the mute/solo interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_MUTESOLO, &fdPlayerMuteSolo);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("GetInterface(SL_IID_MUTESOLO) error %s", filename);
	}

    // get the volume interface
    result = (*fdPlayerObject)->GetInterface(fdPlayerObject, SL_IID_VOLUME, &fdPlayerVolume);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("GetInterface(SL_IID_VOLUME) error %s", filename);
	}

    // enable whole file looping
    //result = (*fdPlayerSeek)->SetLoop(fdPlayerSeek, SL_BOOLEAN_TRUE, 0, SL_TIME_UNKNOWN);
    result = (*fdPlayerSeek)->SetLoop(fdPlayerSeek, SL_BOOLEAN_FALSE, 0, SL_TIME_UNKNOWN);
    //assert(SL_RESULT_SUCCESS == result);
    //(void)result;
	//if(result != SL_RESULT_SUCCESS)
	{
		//LOGE("SetLoop() error %s", filename);
	}
*/
}

WAV OpenWaveFile(const char* fileName)
{
	WAVError error = WAV_SUCCESS;
	WAV wav = 0;
	// Open the WAVE file
	//wav = wav_open(fileName, WAV_READ, &error);
	wav = wav_open(fileName, &error);
/*
    AAsset* asset = AAssetManager_open(g_amgr, fileName, AASSET_MODE_UNKNOWN);

    // the asset might not be found
    if (NULL == asset) 
	{
		LOGE("OpenWaveFile %s error 1", fileName);
        return wav;
    }

    // open asset as file descriptor
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    //assert(0 <= fd);
    AAsset_close(asset);

	wav = wav_fdopen(fd, WAV_READ, &error);
*/
	// Check error
	if(wav == 0)
	{
		LOGE("OpenWaveFile %s error 2", fileName);
		return wav;
	}
	
	return wav;
}

void CloseWaveFile(WAV wav)
{
	if (wav != 0)
	{
		wav_close(wav);
	}
}

// https://www.google.ca/search?hl=en&as_q=wav+tutorial&as_epq=opensl&as_oq=&as_eq=&as_nlo=&as_nhi=&lr=&cr=&as_qdr=all&as_sitesearch=&as_occt=any&safe=images&tbs=&as_filetype=&as_rights=
// http://sbcgamesdev.blogspot.ca/2013/02/simple-streaming-audio-mixer-for.html
// http://stackoverflow.com/questions/10770979/is-it-possible-to-get-a-byte-buffer-directly-from-an-audio-asset-in-opensl-es-f
// create asset audio player
bool CSound::load(const char* filename)
{
	//LOGI("CSound::load(%s) 1", filename);

	WAV wav = OpenWaveFile(filename);

	//LOGI("CSound::load(%s) 2", filename);
	if(wav == 0)
		return false;
	//LOGI("CSound::load(%s) 3", filename);

	// Calculate the buffer size
	datalen = wav_get_channels(wav) * wav_get_rate(wav) * wav_get_bits(wav);
	// Initialize buffer
	data = new unsigned char[datalen];
	if(data == 0)
	{
		return false;
	}
	//LOGI("CSound::load(%s) 4", filename);

	channels = wav_get_channels(wav);	//channel cout
	rate = wav_get_rate(wav) * 1000;	//samples per second in millihertz
	bits = wav_get_bits(wav);	//bits per sample/container size

	//LOGI("CSound::load(%s) 5", filename);
	unsigned char* newbuf;
	unsigned char* oldbuf;
	unsigned char tempbuf[SBC_AUDIO_OUT_BUFFER_SIZE];
	int totalsize = 0;
	ssize_t readSize = 0;

	//LOGI("CSound::load(%s) 6", filename);
	do{
	//LOGI("CSound::load(%s) 7", filename);
		readSize = wav_read_data(wav, tempbuf, SBC_AUDIO_OUT_BUFFER_SIZE);

	//LOGI("CSound::load(%s) 7.1", filename);
		if(readSize > 0)
		{
			newbuf = new unsigned char[totalsize + readSize];

			if(totalsize > 0)
			{
				memcpy(newbuf, oldbuf, totalsize);
				delete [] oldbuf;
			}
	//LOGI("CSound::load(%s) 8", filename);

			memcpy(&(newbuf[totalsize]), tempbuf, readSize);

			oldbuf = newbuf;		
			totalsize += readSize;
		}
	}while(readSize > 0);

	//LOGI("CSound::load(%s) 8", filename);
	datalen = totalsize;
	data = newbuf;
	//LOGI("CSound::load(%s) 9", filename);

	CloseWaveFile(wav);

	/*
    SLresult result;

    // use asset manager to open asset by filename
    //assert(NULL != g_amgr);
    AAsset* asset = AAssetManager_open(g_amgr, filename, AASSET_MODE_UNKNOWN);

    // the asset might not be found
    if (NULL == asset) 
	{
        return false;
    }

    // open asset as file descriptor
    off_t start, length;
    int fd = AAsset_openFileDescriptor(asset, &start, &length);
    //assert(0 <= fd);
    AAsset_close(asset);

    // configure audio source
    SLDataLocator_AndroidFD loc_fd = {SL_DATALOCATOR_ANDROIDFD, fd, start, length};
    SLDataFormat_MIME format_mime = {SL_DATAFORMAT_MIME, NULL, SL_CONTAINERTYPE_UNSPECIFIED};
    SLDataSource audioSrc = {&loc_fd, &format_mime};

    // configure audio sink
    SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
    SLDataSink audioSnk = {&loc_outmix, NULL};*/

    return true;
}

void CSound::channelplay(CChannel* c)
{
	c->Destroy();

	// PCM data source format
	SLDataFormat_PCM dataSourceFormat = {
	SL_DATAFORMAT_PCM, // format type
	channels, // channel count
	rate, // samples per second in millihertz
	bits, // bits per sample
	bits, // container size
	SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
	//SL_SPEAKER_FRONT_CENTER, // channel mask
	SL_BYTEORDER_LITTLEENDIAN // endianness
	};

	// Android simple buffer queue locator for the data source
	SLDataLocator_AndroidSimpleBufferQueue dataSourceLocator = {
	SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, // locator type
	1 // buffer count
	};

	// Data source is a simple buffer queue with PCM format
	SLDataSource dataSource = {
	&dataSourceLocator, // data locator
	&dataSourceFormat // data format
	};
	// Output mix locator for data sink
	SLDataLocator_OutputMix dataSinkLocator = {
	SL_DATALOCATOR_OUTPUTMIX, // locator type
	outputMixObject // output mix
	};
	// Data sink is an output mix
	SLDataSink dataSink = {
	&dataSinkLocator, // locator
	0 // format
	};
	// Interfaces that are requested
	SLInterfaceID interfaceIds[] = {
	SL_IID_BUFFERQUEUE
	};
	// Required interfaces. If the required interfaces
	// are not available the request will fail
	SLboolean requiredInterfaces[] = {
	SL_BOOLEAN_TRUE, // for SL IID BUFFERQUEUE
	};

	// Create audio player object
	SLresult result = (*engineEngine)->CreateAudioPlayer(
	engineEngine,
	&(c->bqPlayerObject),
	&dataSource,
	&dataSink,
	ARRAY_LEN(interfaceIds),
	interfaceIds,
	requiredInterfaces);

	result = (*(c->bqPlayerObject))->Realize(c->bqPlayerObject, SL_BOOLEAN_FALSE);

	//LOGI("SL_IID_PLAY");
	result = (*(c->bqPlayerObject))->GetInterface(c->bqPlayerObject, 
                                 SL_IID_PLAY, &(c->bqPlayerPlay));

	//LOGI("SL_IID_BUFFERQUEUE");
	result = (*(c->bqPlayerObject))->GetInterface(c->bqPlayerObject, SL_IID_BUFFERQUEUE, &(c->bqBufferQueue));

	//result = (*(c->bqPlayerObject))->GetInterface(c->bqPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE, &(c->bqBufferQueue));

	//LOGI("RegCall");
	//result = (*p->bqPlayerBufferQueue)->RegisterCallback(p->bqPlayerBufferQueue, bqPlayerCallback, p);
    result = (*(c->bqBufferQueue))->RegisterCallback(c->bqBufferQueue, bqPlayerCallback, c);

	c->sound = this;
	c->nextBuffer = data;
	c->nextSize = min(datalen, SBC_AUDIO_OUT_BUFFER_SIZE);
	c->position = 0;

	//LOGI("setplaystate");
	result = (*(c->bqPlayerPlay))->SetPlayState(c->bqPlayerPlay, SL_PLAYSTATE_PLAYING);

	//LOGI("enqueue");
	result = (*(c->bqBufferQueue))->Enqueue(c->bqBufferQueue, c->nextBuffer, c->nextSize);
}

CSound::CSound(const CSound& copy)
{
	channels = copy.channels;
	rate = copy.rate;
	bits = copy.bits;
	datalen = copy.datalen;
	strncpy(file, copy.file, 32);
	if(copy.data)
	{
		//LOGI("copy.data %d %s", datalen, file);
		data = new unsigned char[datalen];
		memcpy(data, copy.data, datalen);
	}
}

CSound::CSound(const char* f)
{
	//refcount = new int;
	//refcount = 1;

	data = NULL;

	StripPathExtension(f, file);
	char full[64];
	sprintf(full, "sounds/%s.wav", file);

	if(load(full))
		LOGI("%s.wav", file);
	else
		LOGE("Error loading sound %s.wav", file);
}

CSound::~CSound()
{
	//LOGI("CSound dest");

	//(*refcount)--;

	//if((*refcount) <= 0)
	{
		if(data)
		{
			delete [] data;
			data = NULL;
		}
	}
}

void CSound::Play()
{
	SLresult result;
	SLuint32 state;

	CChannel* c;
	for(int i=0; i<CHANNELS; i++)
	{
		c = &g_channel[i];
		result = (*(c->bqPlayerPlay))->GetPlayState(c->bqPlayerPlay, &state);

		if(result != SL_RESULT_SUCCESS)
		{
			LOGE("GetPlayState() error %s", file);
		}

		if(state != SL_PLAYSTATE_STOPPED)
			continue;

		// Play using this player
		//.. set up buffer
		channelplay(c);
        //result = (*c->bqPlayerBufferQueue)->Enqueue(c->bqPlayerBufferQueue, c->nextBuffer, c->nextSize);
		//result = (*(c->bqPlayerPlay))->SetPlayState(p->bqPlayerPlay, SL_PLAYSTATE_PLAYING);

		return;
		/*
		if(state != SL_PLAYSTATE_STOPPED)
		{
			result = (*fdPlayerPlay)->SetPlayState(fdPlayerPlay, SL_PLAYSTATE_STOPPED);

			if(result != SL_RESULT_SUCCESS)
			{
				LOGE("SetPlayState(SL_PLAYSTATE_STOPPED) error %s", file);
			}
		}
		/*
		result = (*fdPlayerSeek)->SetPosition(fdPlayerSeek, 0, SL_SEEKMODE_FAST);
		//result = (*fdPlayerSeek)->SetPosition(fdPlayerSeek, 0, SL_SEEKMODE_ACCURATE);

		if(result != SL_RESULT_SUCCESS)
		{
			LOGE("SetPosition(0) error %s", file);
		}*/
	}

	int farthest = -1;
	int farthestID = -1;
	
	for(int i=0; i<CHANNELS; i++)
	{
		c = &g_channel[i];

		if(c->position > farthest)
		{
			farthest = c->position;
			farthestID = i;
		}
	}

	if(farthestID < 0)
		return;

	// Player using this player
	c = &g_channel[farthestID];
	//.. set up buffer
	channelplay(c);
    //result = (*c->bqPlayerBufferQueue)->Enqueue(c->bqPlayerBufferQueue, c->nextBuffer, c->nextSize);
	//result = (*(c->bqPlayerPlay))->SetPlayState(p->bqPlayerPlay, SL_PLAYSTATE_PLAYING);

	//result = (*fdPlayerVolume)->SetVolumeLevel(fdPlayerVolume, SL_MILLIBEL_MAX);
	//SLmillibel 
	
	//if(result != SL_RESULT_SUCCESS)
	{
	//	LOGE("SetVolumeLevel() error %s", file);
	}
/*
	result = (*fdPlayerPlay)->SetPlayState(fdPlayerPlay, SL_PLAYSTATE_PLAYING);
	
	if(result != SL_RESULT_SUCCESS)
	{
		LOGE("SetPlayState(SL_PLAYSTATE_PLAYING) error %s", file);
	}
*/
    //NSLog(@"%s", file);
    //return;
    //AudioServicesPlaySystemSound(soundID);
	//PlaySound(filepath, NULL, SND_ASYNC | SND_FILENAME | SND_NODEFAULT);
}

void LoadSounds()
{
	g_zdeathSnd.push_back(CSound("sounds/creature_snarl1.wav"));
	g_zgraspSnd.push_back(CSound("sounds/creature_snarl2.wav"));
	//g_zpainSnd.push_back(CSound("sounds\\zpain.wav"));
    g_doorKnock.load("sounds/doorknock");
    //g_doorKnock.load("sounds/door-1-open.wav");
    g_staticSound.load("sounds/static");
}