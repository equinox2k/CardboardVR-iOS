//
//  EyeParams.h
//  CardboardVR-iOS
//
//  Created by Peter Tribe on 2014-08-26.
//  Copyright (c) 2014 Peter Tribe. All rights reserved.
//

#ifndef __CardboardVR_iOS__EyeParams__
#define __CardboardVR_iOS__EyeParams__

#import <UIKit/UIKit.h>
#include "Viewport.h"
#include "FieldOfView.h"
#include "EyeTransform.h"

typedef enum
{
    EyeParamsEyeTypeMonocular = 0,
    EyeParamsEyeTypeLeft = 1,
    EyeParamsEyeTypeRight = 2
} EyeParamsEyeType;

class EyeParams
{
public:
    EyeParams(EyeParamsEyeType eye);
    ~EyeParams();
    EyeParamsEyeType getEye();
    Viewport* getViewport();
    FieldOfView* getFov();
    EyeTransform* getTransform();
private:
    EyeParamsEyeType eye;
    Viewport *viewport;
    FieldOfView *fov;
    EyeTransform *eyeTransform;
};

#endif