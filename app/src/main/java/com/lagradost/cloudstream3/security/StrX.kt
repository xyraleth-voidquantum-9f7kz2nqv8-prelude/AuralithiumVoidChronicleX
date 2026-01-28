package com.lagradost.cloudstream3.security

object StrX {

    // KEY jangan diubah setelah release
    private const val KEY = 0x23

    /**
     * Decrypt string dari char array
     * Dipanggil dari kode hasil obfuscate / R8
     */
    @JvmStatic
    fun d(data: CharArray): String {
        for (i in data.indices) {
            data[i] = (data[i].code xor KEY).toChar()
        }
        return String(data)
    }
}