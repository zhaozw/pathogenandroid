

#ifndef SOUND_H
#define SOUND_H

#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <vector>

extern "C" {
//#include <avilib.h>
//#include <wavlib.h>
}

using namespace std;

#define ARRAY_LEN(a) (sizeof(a) / sizeof(a[0]))

class CSound;

class CChannel
{
public:
	SLObjectItf bqPlayerObject;
	SLPlayItf bqPlayerPlay;
	//SLSeekItf bqPlayerSeek;
	//SLMuteSoloItf bqPlayerMuteSolo;
	SLVolumeItf bqPlayerVolume;
    SLAndroidSimpleBufferQueueItf bqBufferQueue;

	// pointer and size of the next player buffer to enqueue, and number of remaining buffers
	//short *firstBuffer;
	int position;
	//short *nextBuffer;
	unsigned char *nextBuffer;
	unsigned int nextSize;
	//int nextCount;
	//int soundID;
	CSound* sound;

	CChannel()
	{	
		bqPlayerObject = NULL;
		//soundID = -1;
		sound = NULL;
	}
	void Create();
	void Destroy();
};

#define CHANNELS	4
extern CChannel g_channel[CHANNELS];

#define SBC_AUDIO_OUT_BUFFER_SIZE 512	//256
//#define SBC_AUDIO_OUT_CHANNELS 8
//#define SBC_AUDIO_OUT_SAMPLE_RATE 11025

class CSound
{
public:
	int channels;
	int rate;
	int bits;
	//short* data;
	unsigned char* data;
	int datalen;
    char file[32];
    //SystemSoundID soundID;
	//int* refcount;

	CSound(const CSound& copy);
	CSound() 
	{
		data = NULL;
		datalen = 0;

		//refcount = new int;
		//refcount = 1;
	}
	CSound(const char* f);
	~CSound();

	void Play();
	void channelplay(CChannel* c);
	bool load(const char* filename);

	//bool CreateAssetAudioPlayer(const char* filename);
};

extern vector<CSound> g_zdeathSnd;
extern vector<CSound> g_zgraspSnd;
extern vector<CSound> g_zpainSnd;

extern CSound g_doorKnock;
extern CSound g_staticSound;

//void SoundPath(const char* from, char* to);
void LoadSounds();
void InitSound();
void DeinitSound();

#endif