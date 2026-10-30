package com.hew.aa_firebase20251641
import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication: Application() {
    companion object {
        lateinit var auth: FirebaseAuth // 사용자 인증객체
        lateinit var db: FirebaseFirestore // firestore 데이터 베이스 객체
    }

    override fun onCreate() {
        super.onCreate()
        // FirebaseAuth 객체 생성
        auth = FirebaseAuth.getInstance()
	    auth.useAppLanguage() // 앱의 언어환경정보에 기반하여 언어 설정
        // FirebaseFirestore 객체 생성
        db = FirebaseFirestore.getInstance()
    }
}

