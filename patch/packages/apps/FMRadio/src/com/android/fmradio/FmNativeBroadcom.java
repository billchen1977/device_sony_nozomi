/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.fmradio;

import android.content.Context;

import com.broadcom.fm.fmreceiver.FmProxy;
import com.broadcom.fm.fmreceiver.IFmReceiverEventHandler;
import com.broadcom.fm.fmreceiver.IFmProxyCallback;

import java.util.ArrayList;

/**
 * This class define FM native interface, will description FM native interface
 */
public class FmNativeBroadcom extends FmNative implements IFmProxyCallback, IFmReceiverEventHandler {
	private static final int TIMEOUT = 5000;

	private static final int RDS_ID_PTY_EVT  = 2;
	private static final int RDS_ID_PS_EVT   = 7;
	private static final int RDS_ID_PTYN_EVT = 8;
	private static final int RDS_ID_RT_EVT   = 9;

	private static final int RDS_EVT_PS = 1;
	private static final int RDS_EVT_RT = 2;

	private FmProxy mFmReceiver = null;

	private float mfrequency = 0.0f;
	private boolean mRdsOn = false;
	private int mRdsEvent = 0;
	private String mRdsProgramService;
    private String mRdsRadioText;

    public boolean openDev(Context ctx) {
        synchronized(this) {
        	if (mFmReceiver == null) {
        	    if (!FmProxy.getProxy(ctx, this)) {
        	        return false;
    	        }
    	        try {
                    this.wait(TIMEOUT);
                } catch (Exception e) {
        	        return false;
                }
                return (mFmReceiver != null);
    	    }
    	    return true;
    	}
    }

    public boolean closeDev() {
        synchronized(this) {
            if (mFmReceiver != null) {
                mFmReceiver.unregisterEventHandler();
                mFmReceiver.finish();
                mFmReceiver = null;
            }
    	    return true;
    	}
    }


    public boolean powerUp(float frequency) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return false;
    	    }
    	    if (mFmReceiver.turnOnRadio(FmProxy.FUNC_REGION_NA | FmProxy.FUNC_RBDS | FmProxy.FUNC_AF | FmProxy.FUNC_SOFTMUTE) != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    if (mFmReceiver.tuneRadio((int)(frequency * 100)) != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    if (mFmReceiver.setAudioPath(FmProxy.AUDIO_PATH_DIGITAL) != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    if (mFmReceiver.setFMVolume(255) != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    return true;
    	}
    }

    public boolean powerDown(int type) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return false;
    	    }
    	    if (mFmReceiver.turnOffRadio() != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    return true;
    	}
    }

    public boolean tune(float frequency) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return false;
    	    }
    	    if (mFmReceiver.tuneRadio((int)(frequency * 100)) != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    return mfrequency == frequency;
    	}
    }

    public float seek(float frequency, boolean isUp) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return 0f;
    	    }
    	    if (mFmReceiver.tuneRadio((int)(frequency * 100)) != FmProxy.STATUS_OK) {
        	    return 0f;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return 0f;
            }
            if (mfrequency != frequency) {
        	    return 0f;
    	    }
    	    if (mFmReceiver.seekStation(isUp ? FmProxy.SCAN_MODE_UP : FmProxy.SCAN_MODE_DOWN) != FmProxy.STATUS_OK) {
        	    return 0f;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return 0f;
            }
    	    return mfrequency == frequency ? 0f : mfrequency;
    	}
    }

    public short[] autoScan() {
        synchronized (this) {
    	    ArrayList<Short> freqList = new ArrayList<Short>();
    	    float frequency = FmUtils.computeFrequency(FmUtils.getLowestStation());

        	if (mFmReceiver == null) {
        	    return null;
    	    }
    	    if (mFmReceiver.tuneRadio((int)(frequency * 100)) != FmProxy.STATUS_OK) {
        	    return null;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return null;
            }
            while (true) {
            	if (mFmReceiver.seekStation(FmProxy.SCAN_MODE_UP) != FmProxy.STATUS_OK) {
                    return null;
                }
                try {
                    this.wait(TIMEOUT);
                } catch (Exception e) {
                    return null;
                }
                if ((mfrequency <= frequency) || (mfrequency >= FmUtils.computeFrequency(FmUtils.getHighestStation()))) {
                    break;
                }
                freqList.add((short)FmUtils.computeStation(mfrequency));
                frequency = mfrequency;
    	    }
    	    if (freqList.size() == 0) {
    	        return null;
    	    }
    	    short[] freqArray = new short[freqList.size()];
    	    for (int i = 0; i < freqList.size(); i++) {
    	    	freqArray[i] = freqList.get(i);
    	    }
    	    return freqArray;
    	}
    }

    public boolean stopScan() {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return false;
    	    }
    	    if (mFmReceiver.seekStationAbort() != FmProxy.STATUS_OK) {
        	    return false;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return false;
            }
    	    return true;
    	}
    }

    public int setRds(boolean rdson) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return 0;
    	    }
    	    if (mFmReceiver.setRdsMode(rdson ? FmProxy.RDS_MODE_DEFAULT_ON : FmProxy.RDS_MODE_OFF,
    	    		FmProxy.RDS_FEATURE_PS | FmProxy.RDS_FEATURE_RT,
    	    		FmProxy.AF_MODE_OFF, FmProxy.MIN_SIGNAL_STRENGTH_DEFAULT) != FmProxy.STATUS_OK) {
        	    return 0;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return 0;
            }
    	    return 1;
    	}
    }

    public short readRds() {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return 0;
    	    }
    	    if (mRdsOn) {
    	        if ((mRdsEvent & RDS_EVT_PS) != 0) {
    	        	mRdsEvent &= ~RDS_EVT_PS;
    	        	return 0x0008;
    	        }
    	        if ((mRdsEvent & RDS_EVT_RT) != 0) {
    	        	mRdsEvent &= ~RDS_EVT_RT;
    	        	return 0x0040;
    	        }
    	    }
    	    return 0;
    	}
    }

    public byte[] getPs() {
        synchronized (this) {
        	if ((mFmReceiver == null) || !mRdsOn) {
        	    return null;
    	    }
    	    try {
                return mRdsProgramService.getBytes("UTF8");
            } catch (Exception e) {
        	    return null;
            }
    	}
    }

    public byte[] getLrText() {
        synchronized (this) {
        	if ((mFmReceiver == null) || !mRdsOn || (mRdsRadioText == null)) {
        	    return null;
    	    }
    	    try {
                return mRdsRadioText.getBytes("UTF8");
            } catch (Exception e) {
        	    return null;
            }
    	}
    }

    public short activeAf() {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return 0;
    	    }
    	    if (mFmReceiver.setRdsMode(FmProxy.RDS_MODE_DEFAULT_ON,
    	    		FmProxy.RDS_FEATURE_PS | FmProxy.RDS_FEATURE_RT | FmProxy.RDS_FEATURE_TP | FmProxy.RDS_FEATURE_PTY | FmProxy.RDS_FEATURE_PTYN,
    	    		FmProxy.AF_MODE_ON, FmProxy.MIN_SIGNAL_STRENGTH_DEFAULT) != FmProxy.STATUS_OK) {
        	    return 0;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return 0;
            }
    	    return (short)FmUtils.computeStation(mfrequency);
    	}
    }

    public int setMute(boolean mute) {
        synchronized (this) {
        	if (mFmReceiver == null) {
        	    return 0;
    	    }
    	    if (mFmReceiver.muteAudio(mute) != FmProxy.STATUS_OK) {
        	    return 0;
    	    }
    	    try {
                this.wait(TIMEOUT);
            } catch (Exception e) {
        	    return 0;
            }
    	    return 1;
    	}
    }

    public int isRdsSupport() {
    	return 1;
    }

    public int switchAntenna(int antenna) {
    	return 2;
    }


    public void onProxyAvailable(Object ProxyObject) {
        synchronized (this) {
    	    mFmReceiver = (FmProxy)ProxyObject;
    	    mFmReceiver.registerEventHandler(this);
            this.notifyAll();
        }
    }

    public void onAudioModeEvent(int audioMode) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }


    public void onAudioPathEvent(int audioPath) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }

    public void onEstimateNoiseFloorLevelEvent(int nfl) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }

    public void onLiveAudioQualityEvent(int rssi, int snr) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }

    public void onRdsDataEvent(int rdsDataType, int rdsIndex,
            String rdsText) {
        synchronized (this) {
        	if (mRdsOn) {
                if (rdsDataType == RDS_ID_PS_EVT) {
                	mRdsEvent |= RDS_EVT_PS;
                	mRdsProgramService = rdsText;
                } else if (rdsDataType == RDS_ID_RT_EVT) {
                	mRdsEvent |= RDS_EVT_RT;
                	mRdsRadioText = rdsText;
                }
            }
        }
    }

    public void onRdsModeEvent(int rdsMode, int alternateFreqHopEnabled) {
        synchronized (this) {
        	mRdsOn = rdsMode != FmProxy.RDS_MODE_OFF;
    	    this.notifyAll();
        }
    }

    public void onSeekCompleteEvent(int freq, int rssi,
            int snr, boolean seeksuccess) {
        synchronized (this) {
        	if (seeksuccess)
        	    mfrequency = freq / 100f;
    	    this.notifyAll();
        }
    }

    public void onStatusEvent(int freq, int rssi, int snr, boolean radioIsOn,
            int rdsProgramType, String rdsProgramService,
            String rdsRadioText, String rdsProgramTypeName, boolean isMute) {
        synchronized (this) {
        	mfrequency = freq / 100f;
    	    this.notifyAll();
        }
    }

    public void onWorldRegionEvent(int worldRegion) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }

    public void onVolumeEvent(int status, int volume) {
        synchronized (this) {
    	    this.notifyAll();
        }
    }
}
