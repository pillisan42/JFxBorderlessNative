#include "io_github_pillisan42_BorderlessNative.h"
#include <iostream>
#include <string> 
#include <windows.h> 
#include <winuser.h>
#include <windowsx.h>
#include <Dwmapi.h>
#include <map>
#pragma comment(lib, "UxTheme.lib")
#pragma comment(lib, "dwmapi.lib")

using namespace std;

class WindowData {
    public:
        string title;
        HWND hwnd;
        WNDPROC prevWndProc;
        jobject thisObject;
};

map<HWND, WindowData> windowDataByWindowHandle;
map<string, WindowData> windowDataByWindowTitle;
JavaVM* jvmRef;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void* reserved)
{
    jvmRef = jvm;
    //return JNI_VERSION_10;
    return JNI_VERSION_1_8;
}
auto maximized(HWND hwnd) -> bool {
    WINDOWPLACEMENT placement;
    if (!::GetWindowPlacement(hwnd, &placement)) {
        return false;
    }

    return placement.showCmd == SW_MAXIMIZE;
}

/* Adjust client rect to not spill over monitor edges when maximized.
 * rect(in/out): in: proposed window rect, out: calculated client rect
 * Does nothing if the window is not maximized.
 */
auto adjust_maximized_client_rect(HWND window, RECT& rect) -> void {
    if (!maximized(window)) {
        return;
    }

    auto monitor = ::MonitorFromWindow(window, MONITOR_DEFAULTTONEAREST);
    if (!monitor) {
        return;
    }

    MONITORINFO monitor_info{};
    monitor_info.cbSize = sizeof(monitor_info);
    if (!::GetMonitorInfoW(monitor, &monitor_info)) {
        return;
    }

    // when maximized, make the client area fill just the monitor (without task bar) rect,
    // not the whole window rect which extends beyond the monitor.
    rect = monitor_info.rcWork;
    //rect = monitor_info.rcMonitor;
}

JNIEnv* getJniEnv() {
    //Get environment from cached jvm
    JNIEnv* env;
    int envStat = jvmRef->GetEnv((void**)&env, JNI_VERSION_1_8);

    bool attached = false;
    if (envStat == JNI_EDETACHED)
    {
        if (jvmRef->AttachCurrentThread((void**)&env, NULL) != 0)
        {
            std::cout << "Failed to attach" << std::endl;
            //return;
        }
        else if (envStat == JNI_OK)
        {
            //std::cout << "jniattach ok" << std::endl;
            attached = true;
        }
        else if (envStat == JNI_EVERSION)
        {
            // "GetEnv: version not supported"
            std::cout << "jninot supported" << std::endl;
        }
    }
    return env;
}

LRESULT hit_test(HWND handle,POINT cursor) {
    // identify borders and corners to allow resizing the window.
    // Note: On Windows 10, windows behave differently and
    // allow resizing outside the visible window frame.
    // This implementation does not replicate that behavior.
    const POINT border{
        ::GetSystemMetrics(SM_CXFRAME) + ::GetSystemMetrics(SM_CXPADDEDBORDER),
        ::GetSystemMetrics(SM_CYFRAME) + ::GetSystemMetrics(SM_CXPADDEDBORDER)
    };
    RECT window;
    if (!::GetWindowRect(handle, &window)) {
        return HTNOWHERE;
    }

    const auto drag = HTCAPTION;

    enum region_mask {
        client = 0b0000,
        left = 0b0001,
        right = 0b0010,
        top = 0b0100,
        bottom = 0b1000,
    };

    const auto result =
        left * (cursor.x < (window.left + border.x)) |
        right * (cursor.x >= (window.right - border.x)) |
        top * (cursor.y < (window.top + border.y)) |
        bottom * (cursor.y >= (window.bottom - border.y));

    bool inCaption = false;
    bool inMaximizeButton = false;
    JNIEnv* env=getJniEnv();
    WindowData windowData=windowDataByWindowHandle[handle];
    if (windowData.thisObject == NULL) {
        std::cout << "thisObject is null" << std::endl;
    } else {
        if (env != NULL) {
            jobject thisObj = windowData.thisObject;
            jclass cls = env->GetObjectClass(thisObj);
            if (cls != NULL) {
                jmethodID isMouseInCaptionVal = env->GetMethodID(cls, "isMouseInCaption", "(II)Z");
                inCaption = (bool)(env->CallBooleanMethod(thisObj, isMouseInCaptionVal, cursor.x, cursor.y) == JNI_TRUE);
                jmethodID isMouseInMaximizeButtonVal = env->GetMethodID(cls, "isMouseInMaximizeButton", "(II)Z");
                inMaximizeButton = (bool)(env->CallBooleanMethod(thisObj, isMouseInMaximizeButtonVal, cursor.x, cursor.y) == JNI_TRUE);
            }
        }
    }
    switch (result) {
    case left: return HTLEFT;
    case right: return HTRIGHT;
    case top: return HTTOP;
    case bottom: return HTBOTTOM;
    case top | left: return HTTOPLEFT;
    case top | right: return HTTOPRIGHT;
    case bottom | left: return HTBOTTOMLEFT;
    case bottom | right: return HTBOTTOMRIGHT;
    //Windows 11 Snap layout support buggy for unknown reason ???
    // https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/apply-snap-layout-menu
    case client: return inMaximizeButton ? HTMAXBUTTON :((inCaption) ? drag : HTCLIENT);
    default: return HTNOWHERE;
    }
}

LRESULT CALLBACK NewWndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    static RECT border_thickness;
    switch (message)
    {
    case WM_SYSCOMMAND:
        if (wParam == 0xF032) {
            JNIEnv* env = getJniEnv();
            WindowData windowData = windowDataByWindowHandle[hWnd];
            if (windowData.thisObject == NULL) {
                std::cout << "thisObject is null" << std::endl;
            } else {
                if (env != NULL) {
                    jobject thisObj = windowData.thisObject;
                    jclass cls = env->GetObjectClass(thisObj);
                    if (cls != NULL) {
                        jmethodID maximizeOrRestoreVal = env->GetMethodID(cls, "maximizeOrRestore", "()V");
                        env->CallBooleanMethod(thisObj, maximizeOrRestoreVal);
                    }
                }
            }
            //This ignore double click on 
            return 0;
        }
        break;
    case WM_NCCALCSIZE:
        if (lParam)
        {   
            if (maximized(hWnd)) {
                JNIEnv* env = getJniEnv();

                WindowData windowData = windowDataByWindowHandle[hWnd];
                if (windowData.thisObject == NULL) {
                    std::cout << "maximizeOrRestore thisObj is null" << std::endl;
                }
                else {
                    jobject thisObj = windowData.thisObject;
                    jclass cls = env->GetObjectClass(thisObj);
                    if (env != NULL) {
                        jmethodID maximizeOrRestoreVal = env->GetMethodID(cls, "maximizeOrRestore", "()V");
                        env->CallBooleanMethod(thisObj, maximizeOrRestoreVal);
                    }
                }
            } else {
                NCCALCSIZE_PARAMS* sz = (NCCALCSIZE_PARAMS*)lParam;
                sz->rgrc[0].left += border_thickness.left;
                sz->rgrc[0].right -= border_thickness.right;
                sz->rgrc[0].bottom -= border_thickness.bottom;
            }
            return 0;
        }
        break;
    case WM_NCHITTEST:
        //do default processing, but allow resizing from top-bord
        LRESULT result = hit_test(hWnd, POINT{
                    GET_X_LPARAM(lParam),
                    GET_Y_LPARAM(lParam)
            });
        if (result == HTNOWHERE) {
            return CallWindowProc(windowDataByWindowHandle[hWnd].prevWndProc, hWnd, message, wParam, lParam);
        }
        return result;
    }
    return CallWindowProc(windowDataByWindowHandle[hWnd].prevWndProc, hWnd, message, wParam, lParam);
}

HWND handle;

JNIEXPORT void JNICALL Java_io_github_pillisan42_BorderlessNative_makeWindowsBorderless
  (JNIEnv *env, jobject thisObject, jstring windowName) {
    const char* nameCharPointer = env->GetStringUTFChars(windowName, NULL);
    string title = nameCharPointer;
    jobject thisObj = env->NewGlobalRef(thisObject);
    handle = FindWindowA(NULL,nameCharPointer);
    SetWindowLong(handle, GWL_STYLE, WS_VISIBLE | WS_POPUP  | WS_CLIPCHILDREN
        | WS_CLIPSIBLINGS | WS_THICKFRAME  | WS_SYSMENU | WS_MAXIMIZEBOX | WS_MINIMIZEBOX );

    WNDPROC prevWndProc = (WNDPROC)SetWindowLongPtrW(handle, GWLP_WNDPROC, (LONG_PTR)NewWndProc);
    WindowData windowData;
    windowData.hwnd = handle;
    windowData.prevWndProc = prevWndProc;
    windowData.thisObject = thisObj;
    windowData.title = title;
        
    windowDataByWindowTitle[title] = windowData;
    windowDataByWindowHandle[handle] = windowData;
    COLORREF BLACK = 0x00000000;
    DwmSetWindowAttribute(handle, DWMWINDOWATTRIBUTE::DWMWA_BORDER_COLOR, &BLACK, sizeof(BLACK));
    DwmSetWindowAttribute(handle, DWMWINDOWATTRIBUTE::DWMWA_CAPTION_COLOR, &BLACK, sizeof(BLACK));
}

JNIEXPORT void JNICALL Java_io_github_pillisan42_BorderlessNative_setWindowDraggable
(JNIEnv *env, jobject thisObject, jboolean isDraggable) {
    //TODO delete this unused method
}

