package android.os;

import android.os.Bundle;

// Hidden framework API (framework.jar / bootclasspath at runtime), copied from
// frameworks/base/core/java/android/os/IRemoteCallback.aidl. The @UnsupportedAppUsage
// annotation is dropped: the standalone AIDL compiler has no annotation set for it.
/** @hide */
oneway interface IRemoteCallback {
    void sendResult(in Bundle data);
}
