
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNHeadphoneDetectV2Spec.h"

@interface HeadphoneDetectV2 : NSObject <NativeHeadphoneDetectV2Spec>
#else
#import <React/RCTBridgeModule.h>

@interface HeadphoneDetectV2 : NSObject <RCTBridgeModule>
#endif

@end
