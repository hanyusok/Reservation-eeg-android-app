package com.example.reservation_eeg_android_app.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.MemorySessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import java.time.ZoneId

object SupabaseConfig {
    const val URL = "https://cznzsmqdjcneajuggcxl.supabase.co"
    const val ANON_KEY = "sb_publishable_tMSJGV6uZ6UXo-wWrPP5Hg_NJNOAg3F"
    const val KST_OFFSET = "+09:00"
    val KST_ZONE_ID: ZoneId = ZoneId.of("Asia/Seoul")
}

val supabaseClient = createSupabaseClient(
    supabaseUrl = SupabaseConfig.URL,
    supabaseKey = SupabaseConfig.ANON_KEY
) {
    install(Postgrest)
    install(Auth) {
        sessionManager = MemorySessionManager()
    }
    install(Realtime)
}
