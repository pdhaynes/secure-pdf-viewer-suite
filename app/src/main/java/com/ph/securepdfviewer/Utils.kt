package com.ph.securepdfviewer

import java.io.FileDescriptor

object Utils {
    /**
     * Uses a no-no method to grab the fd of a memory file.
     * I didn't want to do this, but this is the safest way to show the user's file without exposing
     * them to the app directory unencrypted.
     *
     * @param sharedMemoryInstance used for reflection
     * @return memory file's descriptor
     */
    fun getFileDescriptorViaReflection(sharedMemoryInstance: Any): FileDescriptor? {
        try {
            val method = sharedMemoryInstance.javaClass.getDeclaredMethod("getFileDescriptor")
            method.isAccessible = true

            return method.invoke(sharedMemoryInstance) as FileDescriptor
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}