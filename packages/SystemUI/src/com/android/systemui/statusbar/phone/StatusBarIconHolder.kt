/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.systemui.statusbar.phone

import android.annotation.IntDef
import android.content.Context
import android.graphics.drawable.Icon
import android.os.UserHandle
import androidx.annotation.Nullable
import com.android.internal.statusbar.StatusBarIcon
import com.android.systemui.statusbar.pipeline.icons.shared.model.ModernStatusBarViewCreator
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy.CallIndicatorIconState
import com.android.systemui.statusbar.phone.PhoneStatusBarPolicy.BluetoothIconState

/** Wraps [com.android.internal.statusbar.StatusBarIcon] so we can still have a uniform list */
open class StatusBarIconHolder private constructor() {

    @IntDef(TYPE_ICON, TYPE_MOBILE_NEW, TYPE_WIFI_NEW, TYPE_BINDABLE, TYPE_BLUETOOTH, TYPE_NETWORK_TRAFFIC)
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class IconType

    var icon: StatusBarIcon? = null

    @IconType
    open var type = TYPE_ICON
        internal set

    var tag = 0
        private set

    @Nullable
    var bluetoothState: BluetoothIconState? = null

    open var isVisible: Boolean
        get() = when (type) {
            TYPE_ICON -> icon?.visible ?: false
            TYPE_BINDABLE, TYPE_MOBILE_NEW, TYPE_WIFI_NEW, TYPE_NETWORK_TRAFFIC -> true
            TYPE_BLUETOOTH -> bluetoothState?.visible ?: false
            else -> true
        }
        set(visible) {
            if (isVisible == visible) return
            when (type) {
                TYPE_ICON -> icon?.visible = visible
                TYPE_BINDABLE, TYPE_MOBILE_NEW, TYPE_WIFI_NEW -> {}
            }
        }

    override fun toString(): String {
        return "StatusBarIconHolder(type=${getTypeString(type)} tag=$tag visible=$isVisible)"
    }

    companion object {
        const val TYPE_ICON = 0

        @Deprecated("This field only exists so the new status bar pipeline can interface with the view holder system.")
        const val TYPE_MOBILE_NEW = 3

        @Deprecated("This field only exists so the new status bar pipeline can interface with the view holder system.")
        const val TYPE_WIFI_NEW = 4

        const val TYPE_BINDABLE = 5
        const val TYPE_NETWORK_TRAFFIC = 6
        const val TYPE_BLUETOOTH = 7

        fun getTypeString(@IconType type: Int): String {
            return when (type) {
                TYPE_ICON -> "ICON"
                TYPE_MOBILE_NEW -> "MOBILE_NEW"
                TYPE_WIFI_NEW -> "WIFI_NEW"
                TYPE_BLUETOOTH -> "BLUETOOTH"
                TYPE_NETWORK_TRAFFIC -> "NETWORK_TRAFFIC"
                TYPE_BINDABLE -> "BINDABLE"
                else -> "UNKNOWN"
            }
        }

        @JvmStatic
        fun fromIcon(icon: StatusBarIcon?): StatusBarIconHolder {
            val wrapper = StatusBarIconHolder()
            wrapper.icon = icon
            return wrapper
        }

        @JvmStatic
        fun forNewWifiIcon(): StatusBarIconHolder {
            val holder = StatusBarIconHolder()
            holder.type = TYPE_WIFI_NEW
            return holder
        }

        @JvmStatic
        fun fromSubIdForModernMobileIcon(subId: Int): StatusBarIconHolder {
            val holder = StatusBarIconHolder()
            holder.type = TYPE_MOBILE_NEW
            holder.tag = subId
            return holder
        }

        @JvmStatic
        fun fromCallIndicatorState(context: Context, state: CallIndicatorIconState): StatusBarIconHolder {
            val holder = StatusBarIconHolder()
            val resId = if (state.isNoCalling) state.noCallingResId else state.callStrengthResId
            val contentDescription = if (state.isNoCalling) state.noCallingDescription else state.callStrengthDescription
            holder.icon = StatusBarIcon(
                UserHandle.SYSTEM,
                context.packageName,
                Icon.createWithResource(context, resId),
                0,
                0,
                contentDescription
            )
            holder.tag = state.subId
            return holder
        }

        @JvmStatic
        fun fromNetworkTraffic(): StatusBarIconHolder {
            val holder = StatusBarIconHolder()
            holder.type = TYPE_NETWORK_TRAFFIC
            return holder
        }

        @JvmStatic
        fun fromBluetoothIconState(state: BluetoothIconState): StatusBarIconHolder {
            val holder = StatusBarIconHolder()
            holder.bluetoothState = state
            holder.type = TYPE_BLUETOOTH
            return holder
        }
    }

    class BindableIconHolder(val initializer: ModernStatusBarViewCreator) : StatusBarIconHolder() {
        override var type: Int = TYPE_BINDABLE
        override var isVisible: Boolean = true

        override fun toString(): String {
            return "StatusBarIconHolder(type=BINDABLE)"
        }
    }
}

