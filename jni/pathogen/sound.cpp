

#include "sound.h"
#include "main.h"
#include "gui.h"
#include "menu.h"
#include "item.h"

vector<CSound> g_zdeathSnd;
vector<CSound> g_zgraspSnd;
vector<CSound> g_zpainSnd;
CSound g_doorKnock;
CSound g_staticSound;

CSound::CSound(const char* f)
{
	/*
    StripPathExtension(f, file);
    NSString* nsfile = [NSString stringWithCString:file encoding:NSUTF8StringEncoding];
    NSBundle* bundle = [NSBundle mainBundle];
    NSString* soundPath = [bundle pathForResource:nsfile ofType:@"wav"];
    
    const char* strSoundPath = [soundPath UTF8String];
    
    FILE* fp = fopen(strSoundPath, "rb");
    
    if(!fp)
    {
        NSLog(@"Error loading sound %s", file);
        return;
    }
    
    fclose(fp);
    
    AudioServicesCreateSystemSoundID((__bridge CFURLRef)[NSURL fileURLWithPath:soundPath], &soundID);
    
    NSLog(@"%s.wav", file);
	*/
}

void CSound::Play()
{
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
    g_doorKnock = CSound("sounds/doorknock");
    g_staticSound = CSound("sounds/static");
}