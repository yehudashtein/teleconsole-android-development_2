/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class OnDtmfDigitParam {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OnDtmfDigitParam(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OnDtmfDigitParam obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_OnDtmfDigitParam(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setMethod(int value) {
    pjsua2JNI.OnDtmfDigitParam_method_set(swigCPtr, this, value);
  }

  public int getMethod() {
    return pjsua2JNI.OnDtmfDigitParam_method_get(swigCPtr, this);
  }

  public void setDigit(String value) {
    pjsua2JNI.OnDtmfDigitParam_digit_set(swigCPtr, this, value);
  }

  public String getDigit() {
    return pjsua2JNI.OnDtmfDigitParam_digit_get(swigCPtr, this);
  }

  public void setDuration(long value) {
    pjsua2JNI.OnDtmfDigitParam_duration_set(swigCPtr, this, value);
  }

  public long getDuration() {
    return pjsua2JNI.OnDtmfDigitParam_duration_get(swigCPtr, this);
  }

  public OnDtmfDigitParam() {
    this(pjsua2JNI.new_OnDtmfDigitParam(), true);
  }

}
