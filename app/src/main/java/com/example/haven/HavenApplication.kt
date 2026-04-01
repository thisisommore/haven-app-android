package com.example.haven

import android.app.Application
import android.content.Context
import com.example.haven.xxdk.XXDK
import com.example.haven.xxdk.XXDKStorage

class HavenApplication : Application() {
    
    companion object {
        private var _xxdk: XXDK? = null
        private var _storage: XXDKStorage? = null
        
        fun getXXDK(context: Context): XXDK {
            if (_xxdk == null) {
                val storage = getStorage(context)
                _xxdk = XXDK(context.applicationContext).apply {
                    setAppStorage(storage)
                }
            }
            return _xxdk!!
        }
        
        fun getStorage(context: Context): XXDKStorage {
            if (_storage == null) {
                _storage = XXDKStorage.getInstance(context.applicationContext)
            }
            return _storage!!
        }
        
        fun reset() {
            _xxdk = null
            _storage = null
        }
    }
}
