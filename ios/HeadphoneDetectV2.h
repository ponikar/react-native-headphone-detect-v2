
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHeadphoneDetectV2Spec.h"
@interface HeadphoneDetectV2 : NSObject <NativeHeadphoneDetectV2Spec>
#else

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface HeadphoneDetectV2 : RCTEventEmitter <RCTBridgeModule>
#endif

@end
