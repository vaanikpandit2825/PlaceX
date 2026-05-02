package com.example.studentplacement.model

data class Student(
    val id: Int = 0, val name: String = "", val email: String = "",
    val phone: String = "", val dob: String = "", val gender: String = "",
    val address: String = "", val branch: String = "", val college: String = "",
    val tenthPercent: Double = 0.0, val twelfthPercent: Double = 0.0,
    val cgpa: Double = 0.0, val skills: String = "", val password: String = "",
    val profileComplete: Boolean = false, val isPlaced: Boolean = false,
    val createdAt: String = ""
)

data class Company(
    val id: Int = 0, val name: String = "", val industry: String = "",
    val location: String = "", val website: String = "", val description: String = "",
    val isApproved: Int = 0, val recruiterName: String = "", val recruiterEmail: String = ""
)

data class Job(
    val id: Int = 0, val title: String = "", val description: String = "",
    val packageLpa: Double = 0.0, val minCgpa: Double = 6.0,
    val minTenth: Double = 60.0, val minTwelfth: Double = 60.0,
    val requiredSkills: String = "", val allowedBranch: String = "",
    val location: String = "", val jobType: String = "Full Time",
    val deadline: String = "", val companyId: Int = 0,
    val companyName: String = "", val industry: String = "",
    val isActive: Boolean = true, val applicantCount: Int = 0,
    val createdAt: String = ""
)

data class ApplicationDetail(
    val applicationId: Int = 0, val status: String = "",
    val appliedAt: String = "", val updatedAt: String = "",
    val jobTitle: String = "", val packageLpa: Double = 0.0,
    val location: String = "", val jobType: String = "",
    val companyName: String = "", val industry: String = "",
    val companyId: Int = 0, val jobId: Int = 0
)

data class ApplicationHistory(
    val status: String = "", val changedAt: String = ""
)

data class Resume(
    val id: Int = 0, val studentId: Int = 0, val skills: String = "",
    val experience: String = "", val projects: String = "",
    val certifications: String = "", val updatedAt: String = ""
)

data class NotificationItem(
    val id: Int = 0, val message: String = "",
    val isRead: Boolean = false, val createdAt: String = ""
)

data class Review(
    val rating: Int = 0, val comment: String = "",
    val studentName: String = "", val branch: String = "", val createdAt: String = ""
)

data class ShortlistCandidate(
    val applicationId: Int = 0, val rank: Int = 0,
    val studentId: Int = 0, val name: String = "",
    val branch: String = "", val cgpa: Double = 0.0,
    val college: String = "", val skills: String = "",
    val skillMatch: String = "", val skillMatchPct: Int = 0,
    val status: String = ""
)
