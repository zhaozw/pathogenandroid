

#ifndef SOUND_H
#define SOUND_H

#include <vector>

using namespace std;

class CSound
{
public:
	CSound() {}
	CSound(const char* f);
    
    char file[32];
    //SystemSoundID soundID;
	void Play();
};

extern vector<CSound> g_zdeathSnd;
extern vector<CSound> g_zgraspSnd;
extern vector<CSound> g_zpainSnd;

extern CSound g_doorKnock;
extern CSound g_staticSound;

//void SoundPath(const char* from, char* to);
void LoadSounds();

#endif