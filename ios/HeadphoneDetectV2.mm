#import "HeadphoneDetectV2.h"
#import <AVFoundation/AVFoundation.h>

@implementation HeadphoneDetectV2
{
    BOOL hasListeners;
}
RCT_EXPORT_MODULE()

static NSString * const AUDIO_DEVICE_CHANGED_NOTIFICATION = @"AUDIO_DEVICE_CHANGED_NOTIFICATION";
static NSString * const IS_AUDIO_DEVICE_CONNECTED = @"isAudioDeviceConnected";

-(void) startObserving
{
    hasListeners = YES;
}

+ (BOOL) requiresMainQueueSetup
{
    return YES;
}

- (dispatch_queue_t) methodQueue
{
    return dispatch_get_main_queue();
}

-(void) stopObserving
{
    hasListeners = NO;
}


- (NSArray<NSString *> *) supportedEvents
{
    return @[AUDIO_DEVICE_CHANGED_NOTIFICATION];
}

- (instancetype) init
{
    if (self = [super init]) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(audioRouteChangeListenerCallback:) name:AVAudioSessionRouteChangeNotification object:nil];
    }
    return self;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeHeadphoneDetectV2SpecJSI>(params);
}

- (void) audioRouteChangeListenerCallback:(NSNotification*)notification
{
    if (hasListeners) { // Only send events if anyone is listening
        NSDictionary * res = [HeadphoneDetectV2 _isAudioDeviceConnected];
        [self sendEventWithName:AUDIO_DEVICE_CHANGED_NOTIFICATION
                           body: res
         ];
    }
}

+ (NSDictionary *) _isAudioDeviceConnected
{
    NSMutableDictionary *res = [
                                @{ @"audioJack": @NO, @"bluetooth": @NO }
                                mutableCopy
                                ];

    AVAudioSessionRouteDescription* route = [[AVAudioSession sharedInstance] currentRoute];

    for (AVAudioSessionPortDescription* desc in [route outputs]) {
        if ([[desc portType] isEqualToString:AVAudioSessionPortHeadphones]) {
            res[@"audioJack"] = @YES;
        }

        if (
            [[desc portType] isEqualToString:AVAudioSessionPortBluetoothA2DP] ||
            [[desc portType] isEqualToString:AVAudioSessionPortBluetoothHFP] ||
            [[desc portType] isEqualToString:AVAudioSessionPortBluetoothLE]
            ) {
            res[@"bluetooth"] = @YES;
        }
    }

    return res;
}

- (void)isAudioDeviceConnected:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  resolve([HeadphoneDetectV2 _isAudioDeviceConnected]);
}

@end
