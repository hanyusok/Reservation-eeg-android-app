package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable

@Serializable
data class Doctor(
    val id: String,
    val name: String,
    val specialty: String,
    val description: String,
    val fullBio: String,
    val education: List<String>,
    val experience: List<String>
)

val mockDoctors = listOf(
    Doctor(
        id = "1",
        name = "김철수 원장",
        specialty = "신경과 전문의 / 의학박사",
        description = "서울대학교 병원 신경과 외래교수 역임\n20년 경력의 뇌파 진단 전문가",
        fullBio = "김철수 원장은 20년 이상의 임상 경험을 가진 뇌파 진단 분야의 권위자입니다. 수많은 난치성 뇌질환 환자들을 진료해왔으며, 정밀한 뇌파 분석을 통한 맞춤형 치료를 지향합니다.",
        education = listOf(
            "서울대학교 의과대학 졸업",
            "서울대학교 의학박사 취득",
            "미국 메이요 클리닉 연수"
        ),
        experience = listOf(
            "서울대학교 병원 신경과 외래교수",
            "대한뇌파학회 이사",
            "전) 한국뇌연구원 선임연구원"
        )
    ),
    Doctor(
        id = "2",
        name = "이영희 과장",
        specialty = "신경과 전문의",
        description = "EEG 분석 및 판독 시스템 설계\n대한뇌파학회 정회원",
        fullBio = "이영희 과장은 최신 EEG 분석 시스템을 도입하고 판독 정확도를 높이는 데 주력하고 있습니다. 특히 소아 뇌파 및 수면 장애 진단 분야에서 탁월한 성과를 내고 있습니다.",
        education = listOf(
            "연세대학교 의과대학 졸업",
            "연세대학교 세브란스 병원 전공의 수료"
        ),
        experience = listOf(
            "대한뇌파학회 정회원",
            "수면의학 전문 과정 수료",
            "EEG 분석 알고리즘 연구"
        )
    )
)
