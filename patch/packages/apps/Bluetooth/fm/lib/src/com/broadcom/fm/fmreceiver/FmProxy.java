/******************************************************************************
 *
 *  Copyright (C) 2009-2012 Broadcom Corporation
 *
 *  This program is the proprietary software of Broadcom Corporation and/or its
 *  licensors, and may only be used, duplicated, modified or distributed
 *  pursuant to the terms and conditions of a separate, written license
 *  agreement executed between you and Broadcom (an "Authorized License").
 *  Except as set forth in an Authorized License, Broadcom grants no license
 *  (express or implied), right to use, or waiver of any kind with respect to
 *  the Software, and Broadcom expressly reserves all rights in and to the
 *  Software and all intellectual property rights therein.
 *  IF YOU HAVE NO AUTHORIZED LICENSE, THEN YOU HAVE NO RIGHT TO USE THIS
 *  SOFTWARE IN ANY WAY, AND SHOULD IMMEDIATELY NOTIFY BROADCOM AND DISCONTINUE
 *  ALL USE OF THE SOFTWARE.
 *
 *  Except as expressly set forth in the Authorized License,
 *
 *  1.     This program, including its structure, sequence and organization,
 *         constitutes the valuable trade secrets of Broadcom, and you shall
 *         use all reasonable efforts to protect the confidentiality thereof,
 *         and to use this information only in connection with your use of
 *         Broadcom integrated circuit products.
 *
 *  2.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, THE SOFTWARE IS PROVIDED
 *         "AS IS" AND WITH ALL FAULTS AND BROADCOM MAKES NO PROMISES,
 *         REPRESENTATIONS OR WARRANTIES, EITHER EXPRESS, IMPLIED, STATUTORY,
 *         OR OTHERWISE, WITH RESPECT TO THE SOFTWARE.  BROADCOM SPECIFICALLY
 *         DISCLAIMS ANY AND ALL IMPLIED WARRANTIES OF TITLE, MERCHANTABILITY,
 *         NONINFRINGEMENT, FITNESS FOR A PARTICULAR PURPOSE, LACK OF VIRUSES,
 *         ACCURACY OR COMPLETENESS, QUIET ENJOYMENT, QUIET POSSESSION OR
 *         CORRESPONDENCE TO DESCRIPTION. YOU ASSUME THE ENTIRE RISK ARISING
 *         OUT OF USE OR PERFORMANCE OF THE SOFTWARE.
 *
 *  3.     TO THE MAXIMUM EXTENT PERMITTED BY LAW, IN NO EVENT SHALL BROADCOM
 *         OR ITS LICENSORS BE LIABLE FOR
 *         (i)   CONSEQUENTIAL, INCIDENTAL, SPECIAL, INDIRECT, OR EXEMPLARY
 *               DAMAGES WHATSOEVER ARISING OUT OF OR IN ANY WAY RELATING TO
 *               YOUR USE OF OR INABILITY TO USE THE SOFTWARE EVEN IF BROADCOM
 *               HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES; OR
 *         (ii)  ANY AMOUNT IN EXCESS OF THE AMOUNT ACTUALLY PAID FOR THE
 *               SOFTWARE ITSELF OR U.S. $1, WHICHEVER IS GREATER. THESE
 *               LIMITATIONS SHALL APPLY NOTWITHSTANDING ANY FAILURE OF
 *               ESSENTIAL PURPOSE OF ANY LIMITED REMEDY.
 *
 *****************************************************************************/


package com.broadcom.fm.fmreceiver;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.content.Context;

import com.broadcom.fm.fmreceiver.IFmReceiverCallback;
import com.broadcom.fm.fmreceiver.IFmReceiverService;
import com.broadcom.fm.fmreceiver.IFmReceiverEventHandler;
import com.broadcom.fm.fmreceiver.IFmProxyCallback;

import android.content.ComponentName;
import android.content.ServiceConnection;

/**
 * FmProxy is the Java API entry point to issue commands to FM receiver
 * hardware. After a command is issued one or more FmReceiverEvents will be
 * issued to the requested client application handler. The application must
 * implement the {@link IFmReceiverEventHandler} interface to receive the
 * results of requested operations.
 * <p>
 * PUBLIC FM PROXY API
 * <p>
 * An FmProxy object acts as a proxy to the FmService/FmTransmitterService etc
 * <p>
 * Usage:
 * <p>
 * First create a reference to the FM Proxy system service:
 * <p>
 * <code> FmProxy mFmPRoxu = (FmProxy) FmProxy.getProxy(); </code>
 * <p>
 * Then register as an event handler:
 * <p>
 * <code> mFmReceiver.registerEventHandler(this); </code>
 * <p>
 * The application should then call the turnOnRadio() function and wait for a
 * confirmation status event before calling further functions.
 * <p>
 * On closing the high level application, turnOffRadio() should be called to
 * disconnect from the FmService. A confirmation status event should be
 * received before the high level application is terminated.
 * <p>
 * This class first acquires an interface to the FmService module.
 * This allows the FmProxy instance
 * to act as a proxy to the FmService through which all FM Proxy
 * related commands are relayed. The FmService answers the FmProxy
 * instance by issuing FmServiceEvents to the FmProxy instance. (In
 * practice using multiple synchronized callback functions.)
 * {@hide}
 */
public final class FmProxy {

    private static final String TAG = "FmProxy";

    /* FM functionality mask bit constants. */

    /** This mask sets the world region to North America. */
    public static final int FUNC_REGION_NA = 0x00; /*
                                                    * bit0/bit1/bit2: North
                                                    * America.
                                                    */
    /** This mask sets the world region to Europe. */
    public static final int FUNC_REGION_EUR = 0x01; /* bit0/bit1/bit2: Europe. */
    /** This mask sets the world region to Japan. */
    public static final int FUNC_REGION_JP = 0x02; /* bit0/bit1/bit2: Japan. */
    /** This mask sets the world region to Japan II (Upper region band). */
    public static final int FUNC_REGION_JP_II = 0x03; /* bit0/bit1/bit2: Japan. */

    /** This mask enables RDS. */
    public static final int FUNC_RDS = 1 << 4; /* bit4: RDS functionality */
    /** This mask enables RDBS. */
    public static final int FUNC_RBDS = 1 << 5; /*
                                                 * bit5: RDBS functionality,
                                                 * exclusive with RDS bit
                                                 */
    /** This mask enables the Alternate Frequency RDS feature. */
    public static final int FUNC_AF = 1 << 6; /* bit6: AF functionality */
    /** This mask enables SOFTMUTE. */
    public static final int FUNC_SOFTMUTE = 1 << 8; /* bit8: SOFTMUTE functionality */

    /* FM audio output mode. */
    /**
     * Allows the radio to automatically select between Mono and Stereo audio
     * output.
     */
    public static final int AUDIO_MODE_AUTO = 0; /* Auto blend by default. */
    /** Forces Stereo mode audio. */
    public static final int AUDIO_MODE_STEREO = 1; /* Manual stereo switch. */
    /** Forces Mono mode audio. */
    public static final int AUDIO_MODE_MONO = 2; /* Manual mono switch. */
    /** Allows Stereo mode audio with blend activation. */
    public static final int AUDIO_MODE_BLEND = 3; /* Deprecated. */
    // TODO: phase out previous line in favor of next line.
    public static final int AUDIO_MODE_SWITCH = 3; /* Switch activated. */
    /** No FM routing */
    public static final int AUDIO_PATH_NONE = 0; /* No FM routing */
    /** FM routing over DAC */
    public static final int AUDIO_PATH_ANALOG = 1; /* FM routing over DAC */
    /** FM routing over I2S */
    public static final int AUDIO_PATH_DIGITAL = 2; /* FM routing over I2S */

    /* FM audio quality. */
    /**
     * The audio quality of reception.
     */
    /** Using Stereo mode audio quality. */
    public static final int AUDIO_QUALITY_STEREO = 1; /* Manual stereo switch. */
    /** Using Mono mode audio quality. */
    public static final int AUDIO_QUALITY_MONO = 2; /* Manual mono switch. */
    /** Using Blend mode audio quality. */
    public static final int AUDIO_QUALITY_BLEND = 4; /*
                                                      * Auto stereo, and switch
                                                      * activated.
                                                      */

    /* FM scan mode. */
    /** This sets default direction scanning when seeking stations. */
    public static final int SCAN_MODE_NORMAL = 0x00;
    public static final int SCAN_MODE_FAST = 0x01;

    /** This sets scanning to go downwards when seeking stations. */
    public static final int SCAN_MODE_DOWN = 0x00;

    /** This sets scanning to go upwards when seeking stations. */
    public static final int SCAN_MODE_UP = 0x80;

    /** This sets scanning to cover the whole bandwidth and return multiple hits. */
    public static final int SCAN_MODE_FULL = 0x82;

    /* Deemphasis time */
    /** This sets deemphasis to the European default. */
    public static final int DEEMPHASIS_50U = 0; /*
                                                 * 6th bit in FM_AUDIO_CTRL0 set
                                                 * to 0, Europe default
                                                 */
    /** This sets deemphasis to the US default. */
    public static final int DEEMPHASIS_75U = 1 << 6; /*
                                                      * 6th bit in
                                                      * FM_AUDIO_CTRL0 set to 1,
                                                      * US default
                                                      */

    /* Step type for searching */
    /** This sets the frequency interval to 100 KHz when seeking stations. */
    public static final int FREQ_STEP_100KHZ = 0x00;
    /** This sets the frequency interval to 50 KHz when seeking stations. */
    public static final int FREQ_STEP_50KHZ = 0x10;

    public static final int FM_VOLUME_MAX = 255;

    /* Noise floor level */
    /** This sets the Noise Floor Level to LOW. */
    public static final int NFL_LOW = 0;
    /** This sets the Noise Floor Level to MEDIUM. */
    public static final int NFL_MED = 1;
    /** This sets the Noise Floor Level to FINE. */
    public static final int NFL_FINE = 2;

    /* RDS RDBS type */
    /** This deactivates all RDS and RDBS functionality. */
    public static final int RDS_MODE_OFF = 0;
    /** This activates RDS or RDBS as appropriate. */
    public static final int RDS_MODE_DEFAULT_ON = 1;
    /** This activates RDS. */
    public static final int RDS_MODE_RDS_ON = 2;
    /** This activates RDBS. */
    public static final int RDS_MODE_RBDS_ON = 3;

    /* RDS condition type */
    /** Selects no PTY or TP functionality. */
    public static final int RDS_COND_NONE = 0;
    /** Activates RDS PTY capability. */
    public static final int RDS_COND_PTY = 1;
    /** Activates RDS TP capability. */
    public static final int RDS_COND_TP = 2;
    /* Check this again! RDS PTY (Protram types) code, 0 ~ 31, when the PTY is specified in mPendingRdsType. */
    public static final int RDS_COND_PTY_VAL = 0;

    /* RDS feature values. */
    /** Specifies the Program Service feature. */
    public static final int RDS_FEATURE_PS = 4;
    /** Specifies the Program Type feature. */
    public static final int RDS_FEATURE_PTY = 8;
    /** Specifies the Traffic Program feature. */
    public static final int RDS_FEATURE_TP = 16;
    /** Specifies the Program Type Name feature. */
    public static final int RDS_FEATURE_PTYN = 32;
    /** Specifies the Radio Text feature. */
    public static final int RDS_FEATURE_RT = 64;

    /* AF Modes. */
    /** Disables AF capability. */
    public static final int AF_MODE_OFF = 0;
    /** Enables AF capability. */
    public static final int AF_MODE_ON = 1;

    /* The default constants applied on system startup. */
    /**
     * Specifies default minimum signal strength that will be identified as a
     * station when scanning.
     * */
    public static final int MIN_SIGNAL_STRENGTH_DEFAULT = 105;
    /** Specifies default radio functionality. */
    public static final int FUNCTIONALITY_DEFAULT = FUNC_REGION_NA;
    /** Specifies default world frequency region. */
    public static final int FUNC_REGION_DEFAULT = FUNC_REGION_NA;
    /** Specifies default frequency scanning step to use. */
    public static final int FREQ_STEP_DEFAULT = FREQ_STEP_100KHZ;
    /** Specifies if live audio quality sampling is enabled by default. */
    public static final boolean LIVE_AUDIO_QUALITY_DEFAULT = false;
    /** Specifies the default estimated Noise Floor Level. */
    public static final int NFL_DEFAULT = NFL_MED;
    /** Specifies the default signal poll interval in ms. */
    public static final int SIGNAL_POLL_INTERVAL_DEFAULT = 100;
    /** Specifies the default signal poll interval in ms. */
    public static final int DEEMPHASIS_TIME_DEFAULT = DEEMPHASIS_75U;
    /** Default Alternate Frequency mode (DISABLED). */
    public static final int AF_MODE_DEFAULT = AF_MODE_OFF;

    /** Minimum allowed SNR Threshold */
    public static final int FM_MIN_SNR_THRESHOLD = 0;
    /** Maximum allowed SNR Threshold */
    public static final int FM_MAX_SNR_THRESHOLD = 31;

    /* Return status codes. */
    /** Function executed correctly. Parameters checked OK. */
    public static final int STATUS_OK = 0;
    /** General nonspecific error occurred. */
    public static final int STATUS_FAIL = 1;
    /** Server call resulted in exception. */
    public static final int STATUS_SERVER_FAIL = 2;
    /** Function could not be executed at this time. */
    public static final int STATUS_ILLEGAL_COMMAND = 3;
    /** Function parameters are out of allowed range. */
    public static final int STATUS_ILLEGAL_PARAMETERS = 4;

    /* Internal reference to client application event handler. */
    private IFmReceiverEventHandler mEventHandler = null;

    /* Generic remote service reference. */
    private IFmReceiverService mService;

    /** Callback handler **/
    private IFmReceiverCallback mCallback;

    /**
     * Get a proxy to the this service
     * @param cb
     * @return
     */

    public static boolean getProxy(Context ctx, IFmProxyCallback cb) {
        boolean status = false;
        FmProxy p = null;

        try {
            p = new FmProxy(ctx, cb);
        } catch (Throwable t) {
            Log.e(TAG, "Unable to get FM Proxy", t);
            return false;
        }

        return true;
    }

    public FmProxy(Context ctx, IFmProxyCallback cb) {
         Log.d(TAG, "FmProxy object created obj ="+this);
         mContext = ctx;
         mProxyAvailCb = cb;

        Intent intent = new Intent();
        intent.setAction(IFmReceiverService.class.getName());  
        intent.setPackage("com.android.bluetooth");
        if (!mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
             Log.e(TAG, "Could not bind to IFmReceiverService Service");
        }
    }

    /**
     * Initialize the proxy with the service
     * @hide
     */
    protected boolean init(IBinder service) {
        try {
            mService = (IFmReceiverService) IFmReceiverService.Stub.asInterface(service);
            return true;
        } catch (Throwable t) {
            Log.e(TAG, "Unable to initialize BluetoothFM proxy with service", t);
            return false;
        }
    }

    /**
     * Register a callback event handler to receive OPP events.
     * <p/>
     * @param handler the application handler to use for FM Receiver
     *                to use for handling callback events.
     */
    public synchronized void registerEventHandler(IFmReceiverEventHandler handler) {
        Log.v(TAG, "registerEventHandler()");

        // Store the client event handler
        mEventHandler = handler;

        if (mCallback == null) {
            mCallback = new FmReceiverCallback();
            try {
                mService.registerCallback(mCallback);
            } catch (RemoteException e) {
                Log.e(TAG, "Error registering callback handler", e);
            }
        }
    }

    public synchronized void unregisterEventHandler() {
        Log.v(TAG, "unregisterEventHandler()");

        mEventHandler = null;

        try {
            mService.unregisterCallback(mCallback);
        } catch (Throwable t) {
            Log.e(TAG, "Unable to unregister callback", t);
        }
    }

    public synchronized void finish() {
        if (mEventHandler != null) {
            mEventHandler = null;
        }

        if (mCallback != null && mService != null) {
            try {
                mService.unregisterCallback(mCallback);
            } catch (Throwable t) {
                Log.e(TAG, "Unable to unregister callback", t);
            }
            mCallback = null;
        }

        if (mContext != null) {
            mContext.unbindService(mConnection);
            mContext = null;
            mService = null;
        }
    }

    /**
     * Turns on the radio and plays audio using the specified functionality
     * mask.
     * <p>
     * After executing this function, the application should wait for a
     * confirmatory status event callback before calling further API functions.
     * Furthermore, applications should call the {@link #turnOffRadio()}
     * function before shutting down.
     * @param functionalityMask
     *            is a bitmask comprised of one or more of the following fields:
     *            {@link #FUNC_REGION_NA}, {@link #FUNC_REGION_JP},
     *            {@link #FUNC_REGION_EUR}, {@link #FUNC_RDS},
     *            {@link #FUNC_RBDS} and {@link #FUNC_AF}
     *            
     * @param clientPackagename
     * 				is the the client application package name , this is required for the
     * 				fm service to clean up it state when the client process gets killed
     * 				eg scenario: when client app dies without calling turnOffRadio()
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int turnOnRadio(int functionalityMask) {
    	String clientPackagename = mContext.getPackageName();
        int returnCode = STATUS_SERVER_FAIL;

        Log.d(TAG,"Fmproxy"+FmProxy.this+"mService"+mService);
        /* Request this action from the server. */
        try {
            returnCode = mService.turnOnRadio(functionalityMask, clientPackagename.toCharArray());
        } catch (RemoteException e) {
            Log.e(TAG, "turnOnRadio() failed", e);
        }

        return returnCode;
    }

    /**
     * Turns on the radio and plays audio.
     * <p>
     * After executing this function, the application should wait for a
     * confirmatory status event callback before calling further API functions.
     * Furthermore, applications should call the {@link #turnOffRadio()}
     * function before shutting down.
     * @param clientPackagename
     * 				is the the client application package name , this is required for the
     * 				fm service to clean up it state when the client process gets killed
     * 				eg scenario: when client app dies without calling turnOffRadio()  
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public int turnOnRadio() {
        return turnOnRadio(FUNCTIONALITY_DEFAULT);
    }
    
    /**
     * Turns off the radio.
     * <p>
     * After executing this function, the application should wait for a
     * confirmatory status event callback before shutting down.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int turnOffRadio() {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.turnOffRadio();
        } catch (RemoteException e) {
            Log.e(TAG, "turnOffRadio() failed", e);
            return returnCode;
        }

        return returnCode;
    }

    /**
     * Initiates forced clean-up of FMReceiverService from the application
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     */
    public synchronized int cleanupFmService() {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            mService.cleanupFmService();
        } catch (RemoteException e) {
            Log.e(TAG, "cleanupFmService() failed", e);
        }
        Log.i(TAG, "cleanup triggered");
        return returnCode;
    }

    /**
     * Tunes radio to a specific frequency. If successful results in a status
     * event callback.
      * @param freq
     *            the frequency to tune to.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int tuneRadio(int freq) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.tuneRadio(freq);
        } catch (RemoteException e) {
            Log.e(TAG, "tuneRadio() failed", e);
        }

        return returnCode;
    }

    /**
     * Gets current radio status. This results in a status event callback.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int getStatus() {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.getStatus();
        } catch (RemoteException e) {
            Log.e(TAG, "getStatus() failed", e);
        }

        return returnCode;
    }

    /**
     * Get the On/Off status of FM radio receiver module.
     * @return true if radio is on, otherwise returns false.
     */
    public boolean getRadioIsOn() {
        boolean returnStatus = false;
        try {
            returnStatus = mService.getRadioIsOn();
        } catch (RemoteException e) {
            Log.e(TAG, "getRadioIsOn() failed", e);
        }
        return returnStatus;
    }

    /**
     * Get the Audio Mode -
     *            {@link FmProxy#AUDIO_MODE_AUTO},
     *            {@link FmProxy#AUDIO_MODE_STEREO},
     *            {@link FmProxy#AUDIO_MODE_MONO} or
     *            {@link FmProxy#AUDIO_MODE_BLEND}.
     * @param none
     * @return the mAudioMode
     */
    public int getMonoStereoMode() {
        int returnStatus = AUDIO_MODE_AUTO;
        try {
            returnStatus = mService.getMonoStereoMode();
        } catch (RemoteException e) {
            Log.e(TAG, "getMonoStereoMode() failed", e);
        }
        return returnStatus;
    }

    /**
     *  Returns the present tuned FM Frequency
     * @param none
     * @return the mFreq
     */
    public int getTunedFrequency() {
        int returnStatus = 0;
        try {
            returnStatus = mService.getTunedFrequency();
        } catch (RemoteException e) {
            Log.e(TAG, "getTunedFrequency() failed", e);
        }
        return returnStatus;
    }

    /**
     * Returns whether MUTE is turned ON or OFF
     * @param none
     * @return false if MUTE is OFF ; true otherwise
     */
    public boolean getIsMute() {
        boolean returnStatus = false;
        try {
            returnStatus = mService.getIsMute();
        } catch (RemoteException e) {
            Log.e(TAG, "getIsMute() failed", e);
        }
        return returnStatus;
    }

    /**
     * Mutes/unmutes radio audio. If muted the hardware will stop sending audio.
     * This results in a status event callback.
     * @param mute
     *            True to mute audio, False to unmute audio.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onStatusEvent().
     */
    public synchronized int muteAudio(boolean mute) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.muteAudio(mute);
        } catch (RemoteException e) {
            Log.e(TAG, "muteAudio() failed", e);
        }

        return returnCode;
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel. Will
     * result in a seek complete event callback.
     * <p>
     * 
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param minSignalStrength
     *            Minimum signal strength, default =
     *            {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * 
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekStation(int scanMode, int minSignalStrength) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.seekStation(scanMode, minSignalStrength);
        } catch (RemoteException e) {
            Log.e(TAG, "seekStation() failed", e);
        }

        return returnCode;
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel. Will
     * result in a seek complete event callback.
     * <p>
     * Scans with default signal strength setting =
     * {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public int seekStation(int scanMode) {
        return seekStation(scanMode, MIN_SIGNAL_STRENGTH_DEFAULT);
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel depending on the
     * scanDirection. Will do wrap around when reached to mMaxFreq/mMinFreq.
     * When no wrap around is needed, use the low_bound or high_bound as endFrequency.
     * Will result in a seek complete event callback.
     * <p>
     *
     * @param startFrequency
     *            Starting frequency of search operation range.
     * @param endFrequency
     *            Ending frequency of search operation
     * @param minSignalStrength
     *            Minimum signal strength, default =
     *            {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @param scanDirection
     *            the direction to search in, it can only be either
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_DOWN}.
     * @param scanMethod
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_FAST},
     * @param multi_channel
     *            Is multiple channels are required, or only find next valid channel(seek).
     * @param rdsType
     *            the type of RDS condition to scan for.
     * @param rdsTypeValue
     *            the condition value to match.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     *
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekStationCombo(int startFrequency, int endFrequency,
            int minSignalStrength, int scanDirection,
            int scanMethod, boolean multi_channel, int rdsType, int rdsTypeValue) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.seekStationCombo (startFrequency, endFrequency, minSignalStrength, scanDirection, scanMethod, multi_channel, rdsType, rdsTypeValue);
        } catch (RemoteException e) {
            Log.e(TAG, "seekStation() failed", e);
        }

        return returnCode;
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel that
     * supports the requested RDS functionality. Will result in a seek complete
     * event callback.
     * <p>
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param minSignalStrength
     *            Minimum signal strength, default =
     *            {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @param rdsCondition
     *            the type of RDS condition to scan for.
     * @param rdsValue
     *            the condition value to match.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekRdsStation(int scanMode, int minSignalStrength,
            int rdsCondition, int rdsValue) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.seekRdsStation(scanMode, minSignalStrength,
                                                 rdsCondition, rdsValue);
        } catch (RemoteException e) {
            Log.e(TAG, "seekRdsStation() failed", e);
        }

        return returnCode;
    }

    /**
     * Scans FM toward higher/lower frequency for next clear channel that
     * supports the requested RDS functionality.. Will result in a seek complete
     * event callback.
     * <p>
     * Scans with default signal strength setting =
     * {@link #MIN_SIGNAL_STRENGTH_DEFAULT}
     * @param scanMode
     *            see {@link #SCAN_MODE_NORMAL}, {@link #SCAN_MODE_DOWN},
     *            {@link #SCAN_MODE_UP} and {@link #SCAN_MODE_FULL}.
     * @param rdsCondition
     *            the type of RDS condition to scan for.
     * @param rdsValue
     *            the condition value to match.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public int seekRdsStation(int scanMode, int rdsCondition, int rdsValue) {
        return seekRdsStation(scanMode, MIN_SIGNAL_STRENGTH_DEFAULT, rdsCondition, rdsValue);
    }

    /**
     * Aborts the current station seeking operation if any. Will result in a
     * seek complete event containing the last scanned frequency.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onSeekCompleteEvent().
     */
    public synchronized int seekStationAbort() {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.seekStationAbort();
        } catch (RemoteException e) {
            Log.e(TAG, "seekStationAbort() failed", e);
        }

        return returnCode;
    }

    /**
     * Enables/disables RDS/RDBS feature and AF algorithm. Will result in a RDS
     * mode event callback.
     * <p>
     * @param rdsMode
     *            Turns on the RDS or RBDS. See {@link #RDS_MODE_OFF},
     *            {@link #RDS_MODE_DEFAULT_ON}, {@link #RDS_MODE_RDS_ON},
     *            {@link #RDS_MODE_RBDS_ON}
     * @param rdsFeatures
     *            the features to enable in RDS parsing.
     * @param afMode
     *            enables AF algorithm if True. Disables it if False
     * @param afThreshold
     *            the RSSI that the AF should jump to an alternate frequency on.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onRdsModeEvent().
     */
    public synchronized int setRdsMode(int rdsMode, int rdsFeatures,
            int afMode, int afThreshold) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setRdsMode(rdsMode, rdsFeatures, afMode, afThreshold);
        } catch (RemoteException e) {
            Log.e(TAG, "setRdsMode() failed", e);
        }

        return returnCode;
    }

    /**
     * Configures FM audio mode to be mono, stereo or blend. Will result in an
     * audio mode event callback.
     * @param audioMode
     *            the audio mode such as stereo or mono. The following values
     *            should be used {@link #AUDIO_MODE_AUTO},
     *            {@link #AUDIO_MODE_STEREO}, {@link #AUDIO_MODE_MONO} or
     *            {@link #AUDIO_MODE_BLEND}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onAudioModeEvent().
     */
    public synchronized int setAudioMode(int audioMode) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setAudioMode(audioMode);
        } catch (RemoteException e) {
            Log.e(TAG, "setAudioMode() failed", e);
        }

        return returnCode;
    }

    /**
     * Configures FM audio path to AUDIO_PATH_NONE, AUDIO_PATH_ANALOG,
     * or AUDIO_PATH_DIGITAL. Will result in an audio path event callback.

     * @param audioPath
     *            the audio path such as AUDIO_PATH_NONE, AUDIO_PATH_ANALOG,
     *            or AUDIO_PATH_DIGITAL. The following values should be used
     *            {@link #AUDIO_PATH_NONE}, {@link #AUDIO_PATH_ANALOG}
     *            or {@link #AUDIO_PATH_DIGITAL}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onAudioPathEvent().
     */
    public synchronized int setAudioPath(int audioPath) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setAudioPath(audioPath);
        } catch (RemoteException e) {
            Log.e(TAG, "setAudioPath() failed", e);
        }

        return returnCode;
    }

    /**
     * Sets the minimum frequency step size to use when scanning for stations.
     * This function does not result in a status callback and the calling
     * application should therefore keep track of this setting.
     * @param stepSize
     *            a frequency interval set to {@link #FREQ_STEP_100KHZ} or
     *            {@link #FREQ_STEP_50KHZ}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     */
    public synchronized int setStepSize(int stepSize) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setStepSize(stepSize);
        } catch (RemoteException e) {
            Log.e(TAG, "setStepSize() failed", e);
        }

        return returnCode;
    }

    /**
     * Sets the FM volume.
     * @param volume
     *            range from 0 to 255
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onVolumeEvent().
     */
    public synchronized int setFMVolume(int volume) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setFMVolume(volume);
        } catch (RemoteException e) {
            Log.e(TAG, "setFMVolume() failed", e);
        }

        return returnCode;
    }

    /**
     * Sets a the world frequency region and the deemphasis time. This results
     * in a world frequency event callback.
     * @param worldRegion
     *            the world region the FM receiver is located. Set to
     *            {@link #FUNC_REGION_NA}, {@link #FUNC_REGION_EUR},
     *            {@link #FUNC_REGION_JP}, {@link #FUNC_REGION_JP_II}.
     * @param deemphasisTime
     *            the deemphasis time that can be set to either
     *            {@link #DEEMPHASIS_50U} or {@link #DEEMPHASIS_75U}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onWorldRegionEvent().
     */
    public synchronized int setWorldRegion(int worldRegion, int deemphasisTime) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setWorldRegion(worldRegion, deemphasisTime);
        } catch (RemoteException e) {
            Log.e(TAG, "setWorldRegion() failed", e);
        }

        return returnCode;
    }

    /**
     * Estimates the noise floor level given a specific type request. This
     * function returns an RSSI level that is useful for specifying as the
     * minimum signal strength for scan operations.
     * @param nflLevel
     *            estimate noise floor for {@link #NFL_LOW}, {@link #NFL_MED} or
     *            {@link #NFL_FINE}.
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onEstimateNflEvent().
     */
    public synchronized int estimateNoiseFloorLevel(int nflLevel) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.estimateNoiseFloorLevel(nflLevel);
        } catch (RemoteException e) {
            Log.e(TAG, "estimateNoiseFloorLevel() failed", e);
        }

        return returnCode;
    }

    /**
     * Enables or disables the live polling of audio quality on the currently
     * tuned frequency using a specific poll interval.
     * NOTE : SNR value will be returned a 0 for chips not supporting this SNR feature.
     * @param liveAudioPolling
     *            enables/disables live polling of audio quality.
     * @param signalPollInterval
     *            the sample interval for live polling of audio quality.
     * 
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error
     *         code.
     * @see IFmReceiverEventHandler.onLiveAudioQualityEvent().
     */
    public synchronized int setLiveAudioPolling(boolean liveAudioPolling, int signalPollInterval) {
        int returnCode = STATUS_SERVER_FAIL;
        /* Request this action from the server. */
        try {
            returnCode = mService.setLiveAudioPolling(liveAudioPolling, signalPollInterval);
        } catch (RemoteException e) {
            Log.e(TAG, "setLiveAudioPolling() failed", e);
        }

        return returnCode;
    }

    /**
     * Sets the SNR threshold for the subsequent FM frequency tuning.
     * This value will be used by BTA stack internally.
     *
     * @param signalPollInterval
     *           SNR Threshold value (0 ~ 31 (BTA_FM_SNR_MAX) )
     *
     * @return STATUS_OK = 0 if successful. Otherwise returns a non-zero error code.
     */
    public synchronized int setSnrThreshold(int snrThreshold) {
        int returnCode = STATUS_SERVER_FAIL;

        /* Request this action from the server. */
        try {
            returnCode = mService.setSnrThreshold(snrThreshold);
        } catch (RemoteException e) {
            Log.e(TAG, "setSnrThreshold() failed", e);
        }
        return returnCode;
    }

    protected void finalize() {
        finish();
    }

    /**
     * The class containing all the FmProxy callback function handlers. These
     * functions will be called by the FmService module when callback
     * events occur. They in turn relay the callback information back to the
     * main applications callback handler.
     */
    private class FmReceiverCallback extends IFmReceiverCallback.Stub {

        public synchronized void onStatusEvent(int freq, int rssi, int snr,
                boolean radioIsOn, int rdsProgramType,
                String rdsProgramService, String rdsRadioText,
                String rdsProgramTypeName, boolean isMute)
                throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler) {
                mEventHandler.onStatusEvent(freq, rssi, snr, radioIsOn,
                    rdsProgramType, rdsProgramService, rdsRadioText,
                    rdsProgramTypeName, isMute);
            }
        }

        public synchronized void onSeekCompleteEvent(int freq, int rssi, int snr,
                boolean seeksuccess) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onSeekCompleteEvent(freq, rssi, snr, seeksuccess);
        }

        public synchronized void onRdsModeEvent(int rdsMode,
                int alternateFreqHopEnabled) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onRdsModeEvent(rdsMode, alternateFreqHopEnabled);
        }


        public synchronized void onRdsDataEvent(int rdsDataType, int rdsIndex,
                String rdsText) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onRdsDataEvent(rdsDataType, rdsIndex, rdsText);
        }


        public synchronized void onAudioModeEvent(int audioMode) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onAudioModeEvent(audioMode);
        }


        public synchronized void onAudioPathEvent(int audioPath) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onAudioPathEvent(audioPath);
        }


        public synchronized void onEstimateNflEvent(int nfl) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onEstimateNoiseFloorLevelEvent(nfl);
        }


        public synchronized void onLiveAudioQualityEvent(int rssi, int snr) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onLiveAudioQualityEvent(rssi, snr);
        }


        public synchronized void onWorldRegionEvent(int worldRegion) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onWorldRegionEvent(worldRegion);
        }

        public synchronized void onVolumeEvent(int status, int volume) throws RemoteException {
            /* Process and hand this event information to the application. */
            if (null != mEventHandler)
                mEventHandler.onVolumeEvent(status, volume);
        }
    };

    private static final boolean D = true;

    protected Context mContext;
    protected IFmProxyCallback mProxyAvailCb;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (D) {
                Log.d(TAG, "Fm proxy onServiceConnected() name = " + className + ", service = " + service);
            }
            if (service == null || !init(service) && mProxyAvailCb!=null) {
                Log.e(TAG, "Unable to create proxy");
            }
            if ( mProxyAvailCb != null ) {
                mProxyAvailCb.onProxyAvailable(FmProxy.this);
                mProxyAvailCb = null;
            }
        }
    
        public void onServiceDisconnected(ComponentName className) {
            if (D)
                Log.d(TAG, "Fm Proxy object disconnected");
            mService = null;
        }
    };

}
