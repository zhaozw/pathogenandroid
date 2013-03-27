

#include "video.h"
#include "main.h"
#include "gui.h"

// http://stackoverflow.com/questions/4725773/ffmpeg-on-android/4820282#4820282
// http://stackoverflow.com/questions/5000385/playing-audio-with-ffmpeg

void PlayIntro()
{
    g_mode = INTRO;
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
