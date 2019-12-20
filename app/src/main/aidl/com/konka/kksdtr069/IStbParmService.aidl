// IStbParmService.aidl
package com.konka.kksdtr069;

// Declare any non-default types here with import statements

interface IStbParmService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    String getStbParameter(String parmName);

    void setStbParameter(String parmName,String value);
}
