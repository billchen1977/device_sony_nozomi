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

/******************************************************************************
 *
 *  This is the interface file for FM call-out functions.
 *
 ******************************************************************************/
#ifndef BTA_FM_CO_H
#define BTA_FM_CO_H

#include "bta_fm_api.h"
#include "bta_rds_api.h"

/*******************************************************************************
**
** Function         bta_fm_co_init
**
** Description      This callout function is executed by FM when it is
**                  started by calling BTA_FmSetRDSMode() and RDS mode is turned
**                  on.  This function can be used by the phone to initialize
**                  RDS decoder or other platform dependent and RDS related purposes.
**
**
** Returns
**
*******************************************************************************/
extern tBTA_FM_STATUS bta_fm_co_init(tBTA_FM_RDS_B rds_mode);

/*******************************************************************************
**
** Function         bta_fm_co_rds_data
**
** Description      This function is called by FM when RDS data is ready.
**
** Parameter        p_data: RDS data in three bytes array, which includes 2 bytes
**                  RDS block data, and one byte control and correction indicator.
**
** Returns          void
**
*******************************************************************************/
extern tBTA_FM_STATUS bta_fm_co_rds_data(UINT8 * p_data, UINT16 len);

/*******************************************************************************
**
** Function         bta_fm_co_close
**
** Description      This callout function is executed by FM when it is
**                  started by calling BTA_FmSetRDSMode() and RDS mode is turned
**                  off.  This function can be used by the phone to reset RDS
**                  decoder.
**
**
** Returns
**
*******************************************************************************/
extern void bta_fm_co_close(void);

/*******************************************************************************
**
** Function         bta_fm_co_rds_reset
**
** Description      This function can be used by the phone to reset RDS
**                  decoder.
**
**
** Returns
**
*******************************************************************************/
extern void bta_fm_co_reset_rds_engine(void);

#endif /* BTA_FM_CO_H */

