/******************************************************************************
 *
 *  Copyright (C) 2003-2012 Broadcom Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

#include "bt_target.h"
#include "btm_api.h"
#include "btcore/include/module.h"
#include "device/include/controller.h"

void LogMsg_0(UINT32 trace_set_mask, const char *fmt_str) {
    LogMsg(trace_set_mask, fmt_str);
}

void LogMsg_1(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1) {

    LogMsg(trace_set_mask, fmt_str, p1);
}

void LogMsg_2(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1, UINT32 p2) {
    LogMsg(trace_set_mask, fmt_str, p1, p2);
}

void LogMsg_3(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1, UINT32 p2,
        UINT32 p3) {
    LogMsg(trace_set_mask, fmt_str, p1, p2, p3);
}

void LogMsg_4(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1, UINT32 p2,
        UINT32 p3, UINT32 p4) {
    LogMsg(trace_set_mask, fmt_str, p1, p2, p3, p4);
}

void LogMsg_5(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1, UINT32 p2,
        UINT32 p3, UINT32 p4, UINT32 p5) {
    LogMsg(trace_set_mask, fmt_str, p1, p2, p3, p4, p5);
}

void LogMsg_6(UINT32 trace_set_mask, const char *fmt_str, UINT32 p1, UINT32 p2,
        UINT32 p3, UINT32 p4, UINT32 p5, UINT32 p6) {
    LogMsg(trace_set_mask, fmt_str, p1, p2, p3, p4, p5, p6);
}

void GKI_sched_lock(void) {

}

void GKI_sched_unlock(void) {

}

tBTM_STATUS BTM_ReadLocalVersion (bt_version_t *p_vers) {
    if (module_start_up(get_module(CONTROLLER_MODULE))) {
        const controller_t *controller = controller_get_interface();

        *p_vers = *(controller->get_bt_version());
        return BTM_SUCCESS;
    }

    return BTM_ERR_PROCESSING;
}
