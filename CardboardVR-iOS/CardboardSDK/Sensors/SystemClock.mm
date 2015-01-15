//
//  SystemClock.mm
//  CardboardVR-iOS
//
//  Created by Peter Tribe on 2015-01-14.
//  Copyright (c) 2015 Peter Tribe. All rights reserved.
//

#include "SystemClock.h"
#include <mach/clock.h>
#include <mach/mach.h>

long SystemClock::nanoTime()
{
    clock_serv_t cclock;
    mach_timespec_t mts;
    host_get_clock_service(mach_host_self(), SYSTEM_CLOCK, &cclock);
    clock_get_time(cclock, &mts);
    mach_port_deallocate(mach_task_self(), cclock);
    return (mts.tv_sec * 1000000000) + mts.tv_nsec;
}