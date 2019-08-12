package ru.vasiliev.sandbox.visionlabs.data;

import proxypref.annotation.DefaultBoolean;
import proxypref.annotation.DefaultString;

public interface VisionLabsPreferences {

    @DefaultBoolean(true)
    Boolean getFirstRun();

    void setFirstRun(Boolean value);

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

    String getStartTime();

    void setStartTime(String time);

    @DefaultBoolean(false)
    void setNeedPortrait(Boolean needPortrait);

    Boolean getNeedPortrait();

    @DefaultBoolean(false)
    Boolean getIgnoreEyes();

    void setIgnoreEyes(Boolean ignoreEyes);

    @DefaultString("")
    String getAuthDescriptor();

    void setAuthDescriptor(String descriptor);
}
