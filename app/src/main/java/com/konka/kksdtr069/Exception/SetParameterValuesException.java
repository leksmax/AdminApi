package com.konka.kksdtr069.Exception;

import net.sunniwell.cwmp.protocol.sdk.aidl.SetParameterValuesFault;

import java.util.HashMap;

public class SetParameterValuesException {

    private static HashMap<Integer, String> exceptionInformMap = new HashMap<Integer, String>();

    static {
        exceptionInformMap.put(9000, "Method not supported");
        exceptionInformMap.put(9001, "Request denied (no reason specified)");
        exceptionInformMap.put(9002, "Internal error");
        exceptionInformMap.put(9003, "Invalid arguments");
        exceptionInformMap.put(9004, "Resources exceeded (when used in association with " +
                "SetParameterValues, this MUST not be used to indicate parameters in error)");
        exceptionInformMap.put(9005, "Invalid parameter name (associated with " +
                "Set/GetParameterValues, GetParameterNames, Set/GetParameterAttributes)");
        exceptionInformMap.put(9006, "Invalid parameter type (associated with SetParameterValues)");
        exceptionInformMap.put(9007, "Invalid parameter value (associated with SetParameterValues)");
        exceptionInformMap.put(9008, "Attempt to set a non-writable parameter (associated with " +
                "SetParameterValues)");
        exceptionInformMap.put(9009, "Notification request rejected (associated with " +
                "SetParameterAttributes method).");
        exceptionInformMap.put(9010, "Download failure (associated with Download or " +
                "TransferComplete methods).");
        exceptionInformMap.put(9011, "Upload failure (associated with Upload or " +
                "TransferComplete methods).");
        exceptionInformMap.put(9012, "File transfer server authentication failure " +
                "(associated with Upload, Download, or TransferComplete methods).");
        exceptionInformMap.put(9013, "Unsupported protocol for file transfer " +
                "(associated with Upload and Download methods).");
        for (int i = 9800; i <= 9899; i++) {
            exceptionInformMap.put(i, "Vendor defined fault codes");
        }

    }

    public static SetParameterValuesFault checkoutFault(int result, String parameterName) {
        String faultStr = exceptionInformMap.get(result);
        if (faultStr != null) {
            SetParameterValuesFault fault = new SetParameterValuesFault();
            fault.setFaultCode(result);
            fault.setFaultString(faultStr);
            fault.setParameterName(parameterName);
            return fault;
        } else {
            return null;
        }

    }
}
