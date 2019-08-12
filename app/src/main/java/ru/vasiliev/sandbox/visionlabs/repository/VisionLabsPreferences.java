package ru.vasiliev.sandbox.visionlabs.repository;

import proxypref.annotation.DefaultBoolean;
import proxypref.annotation.DefaultString;

public interface VisionLabsPreferences {

    @DefaultBoolean(true)
    Boolean getFirstRun();

    void setFirstRun(Boolean value);

    @DefaultBoolean(true)
    Boolean getFaceAuth();

    void setFaceAuth(Boolean value);

    @DefaultBoolean(false)
    Boolean getFingerAuth();

    void setFingerAuth(Boolean value);

    @DefaultBoolean(true)
    Boolean getLivenessAuth();

    void setLivenessAuth(Boolean value);

    @DefaultBoolean(true)
    Boolean getLiveness_ftp();

    @DefaultBoolean(false)
    Boolean getZoomAuth();

    void setZoomAuth(Boolean value);

    @DefaultBoolean(true)
    Boolean getEyesAuth();

    void setEyesAuth(Boolean value);

    @DefaultBoolean(true)
    Boolean getUseFrontCamera();

    void setUseFrontCamera(Boolean value);

    @DefaultBoolean(false)
    Boolean getShowDetection();

    void setShowDetection(Boolean value);

    @DefaultBoolean(true)
    Boolean getSavePhoto();

    void setSavePhoto(Boolean value);

    @DefaultString("")
    String getPin();

    void setPin(String string);

    @DefaultString("")
    String getUsername();

    void setUsername(String string);

    String getStartTime();

    void setStartTime(String time);

    @DefaultBoolean(false)
    void setNeedPortrait(Boolean needPortrait);

    Boolean getNeedPortrait();

    @DefaultBoolean(false)
    Boolean getIgnoreEyes();

    void setIgnoreEyes(Boolean ignoreEyes);

    @DefaultBoolean(true)
    Boolean getLuna2();

    void setLuna2(Boolean val);
}
