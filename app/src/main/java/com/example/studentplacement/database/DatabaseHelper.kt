package com.example.studentplacement.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.studentplacement.model.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME    = "PlaceX.db"
        const val DB_VERSION = 1
    }


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("PRAGMA foreign_keys = ON;")


        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Student(
                student_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                name            TEXT    NOT NULL,
                email           TEXT    UNIQUE NOT NULL,
                phone           TEXT,
                dob             TEXT,
                gender          TEXT,
                address         TEXT,
                branch          TEXT    NOT NULL,
                college         TEXT,
                tenth_percent   REAL    DEFAULT 0.0,
                twelfth_percent REAL    DEFAULT 0.0,
                cgpa            REAL    NOT NULL DEFAULT 0.0,
                skills          TEXT,
                password        TEXT    NOT NULL,
                profile_complete INTEGER DEFAULT 0,
                created_at      TEXT    DEFAULT (datetime('now','localtime'))
            )
        """)


        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Company(
                company_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                company_name TEXT NOT NULL,
                industry     TEXT,
                location     TEXT,
                website      TEXT,
                description  TEXT,
                created_at   TEXT DEFAULT (datetime('now','localtime'))
            )
        """)


        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Job(
                job_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                title        TEXT NOT NULL,
                description  TEXT,
                package_lpa  REAL DEFAULT 0.0,
                min_cgpa     REAL NOT NULL DEFAULT 6.0,
                min_tenth    REAL DEFAULT 60.0,
                min_twelfth  REAL DEFAULT 60.0,
                location     TEXT,
                job_type     TEXT DEFAULT 'Full Time',
                deadline     TEXT,
                company_id   INTEGER NOT NULL,
                is_active    INTEGER DEFAULT 1,
                created_at   TEXT DEFAULT (datetime('now','localtime')),
                FOREIGN KEY(company_id) REFERENCES Company(company_id)
                    ON DELETE CASCADE ON UPDATE CASCADE
            )
        """)


        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Application(
                application_id INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id     INTEGER NOT NULL,
                job_id         INTEGER NOT NULL,
                status         TEXT    DEFAULT 'Applied',
                applied_at     TEXT    DEFAULT (datetime('now','localtime')),
                updated_at     TEXT,
                FOREIGN KEY(student_id) REFERENCES Student(student_id)
                    ON DELETE CASCADE ON UPDATE CASCADE,
                FOREIGN KEY(job_id)     REFERENCES Job(job_id)
                    ON DELETE CASCADE ON UPDATE CASCADE,
                UNIQUE(student_id, job_id)
            )
        """)


        db.execSQL("""
            CREATE TABLE IF NOT EXISTS Resume(
                resume_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                student_id     INTEGER UNIQUE NOT NULL,
                skills         TEXT,
                experience     TEXT,
                projects       TEXT,
                certifications TEXT,
                updated_at     TEXT DEFAULT (datetime('now','localtime')),
                FOREIGN KEY(student_id) REFERENCES Student(student_id)
                    ON DELETE CASCADE ON UPDATE CASCADE
            )
        """)


        db.execSQL("CREATE INDEX IF NOT EXISTS idx_student_email ON Student(email)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_job_company   ON Job(company_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_student   ON Application(student_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_app_job       ON Application(job_id)")


        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS trg_create_resume
            AFTER INSERT ON Student
            BEGIN
                INSERT INTO Resume(student_id, skills, experience, projects, certifications)
                VALUES (NEW.student_id,'','','','');
            END
        """)


        db.execSQL("""
            CREATE TRIGGER IF NOT EXISTS trg_app_updated
            AFTER UPDATE OF status ON Application
            BEGIN
                UPDATE Application
                SET updated_at = datetime('now','localtime')
                WHERE application_id = NEW.application_id;
            END
        """)

        seedData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        listOf("Application","Resume","Job","Company","Student")
            .forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        if (!db.isReadOnly) db.execSQL("PRAGMA foreign_keys = ON;")
    }


    private fun seedData(db: SQLiteDatabase) {
        // Companies
        val companies = listOf(
            listOf("Google","Technology","Mountain View, CA","google.com",
                "Organise world's information and make it universally accessible."),
            listOf("Microsoft","Technology","Redmond, WA","microsoft.com",
                "Empower every person and organisation on the planet to achieve more."),
            listOf("Amazon","E-Commerce / Cloud","Seattle, WA","amazon.com",
                "Earth's most customer-centric company."),
            listOf("Infosys","IT Services","Bengaluru, India","infosys.com",
                "Navigate your next with cutting-edge technology solutions."),
            listOf("TCS","IT Services","Mumbai, India","tcs.com",
                "Building on belief — global leader in IT services."),
            listOf("Zoho","SaaS","Chennai, India","zoho.com",
                "One operating system for your entire business."),
            listOf("Swiggy","Food Tech","Bengaluru, India","swiggy.com",
                "Delivering convenience to a billion Indians.")
        )
        companies.forEachIndexed { i, c ->
            db.execSQL("""
                INSERT INTO Company(company_name,industry,location,website,description)
                VALUES('${c[0]}','${c[1]}','${c[2]}','${c[3]}','${c[4]}')
            """)
        }


        data class J(val t:String,val d:String,val pkg:Double,val cgpa:Double,
                     val t10:Double,val t12:Double,val loc:String,val type:String,
                     val dl:String,val cid:Int)
        val jobs = listOf(
            J("Android Developer","Build next-gen Android apps for Google's ecosystem.",
                22.0,8.0,75.0,75.0,"Hyderabad","Full Time","2025-08-30",1),
            J("SDE – Backend","Design scalable backend microservices using Kotlin & gRPC.",
                20.0,7.5,70.0,70.0,"Bengaluru","Full Time","2025-09-15",2),
            J("Cloud Engineer","Deploy and maintain cloud infrastructure on AWS.",
                18.0,7.0,65.0,65.0,"Pune","Full Time","2025-09-01",3),
            J("Systems Engineer","Develop enterprise software and client solutions.",
                6.5,6.5,60.0,60.0,"Chennai","Full Time","2025-10-01",4),
            J("Assistant System Engineer","IT support and application maintenance.",
                4.5,6.0,60.0,60.0,"Mumbai","Full Time","2025-10-15",5),
            J("Software Developer","Build SaaS products loved by 75M+ users.",
                10.0,7.0,65.0,65.0,"Chennai","Full Time","2025-09-20",6),
            J("ML Engineer – Intern","Apply ML models to improve delivery predictions.",
                2.5,7.5,70.0,70.0,"Bengaluru","Internship","2025-08-15",7),
            J("Full Stack Developer","Build end-to-end web features for Google's suite.",
                24.0,8.5,80.0,80.0,"Remote","Full Time","2025-08-20",1),
            J("Data Analyst","Derive insights from petabyte-scale datasets.",
                12.0,7.0,65.0,65.0,"Noida","Full Time","2025-09-10",3),
            J("DevOps Engineer","CI/CD pipelines, Docker, Kubernetes at scale.",
                15.0,7.5,70.0,70.0,"Bengaluru","Full Time","2025-09-25",2)
        )
        jobs.forEach { j ->
            db.execSQL("""
                INSERT INTO Job(title,description,package_lpa,min_cgpa,min_tenth,min_twelfth,
                                location,job_type,deadline,company_id)
                VALUES('${j.t}','${j.d}',${j.pkg},${j.cgpa},${j.t10},${j.t12},
                       '${j.loc}','${j.type}','${j.dl}',${j.cid})
            """)
        }


        db.execSQL("""
            INSERT INTO Student(name,email,phone,dob,gender,address,branch,college,
                                tenth_percent,twelfth_percent,cgpa,skills,password,profile_complete)
            VALUES('Arjun Kumar','arjun@demo.com','9876543210','15-07-2002','Male',
                   '42, Anna Nagar, Chennai','Computer Science',
                   'SSN College of Engineering',88.0,90.0,8.5,
                   'Kotlin, Python, SQL, Firebase','demo123',1)
        """)


        db.execSQL("INSERT INTO Application(student_id,job_id,status) VALUES(1,1,'Applied')")
        db.execSQL("INSERT INTO Application(student_id,job_id,status) VALUES(1,6,'Interview Scheduled')")


        db.execSQL("""
            UPDATE Resume SET
                skills='Kotlin, Android, Python, SQL, Firebase, REST APIs, Git',
                experience='Android Intern @ StartupXYZ (June 2024 – Aug 2024)',
                projects='1. PlaceX – Student Placement App (Kotlin + SQLite)\n2. Chat App using Firebase Realtime DB',
                certifications='Google Android Developer Associate\nCisco CyberOps Associate'
            WHERE student_id=1
        """)
    }



    fun registerStudent(name:String,email:String,branch:String,cgpa:Double,pass:String): Long {
        val cv = ContentValues().apply {
            put("name",name); put("email",email); put("branch",branch)
            put("cgpa",cgpa); put("password",pass)
        }
        return writableDatabase.insert("Student",null,cv)
    }

    fun studentLogin(email:String,pass:String): Student? {
        val c = readableDatabase.rawQuery(
            "SELECT * FROM Student WHERE email=? AND password=?",
            arrayOf(email,pass))
        return if(c.moveToFirst()) cursorToStudent(c).also{c.close()} else { c.close(); null }
    }

    fun getStudentByEmail(email:String): Student? {
        val c = readableDatabase.rawQuery("SELECT * FROM Student WHERE email=?", arrayOf(email))
        return if(c.moveToFirst()) cursorToStudent(c).also{c.close()} else { c.close(); null }
    }

    fun getStudentById(id:Int): Student? {
        val c = readableDatabase.rawQuery("SELECT * FROM Student WHERE student_id=?",
            arrayOf(id.toString()))
        return if(c.moveToFirst()) cursorToStudent(c).also{c.close()} else { c.close(); null }
    }

    fun updateStudentPersonal(id:Int,name:String,phone:String,dob:String,
                              gender:String,address:String): Boolean {
        val cv = ContentValues().apply {
            put("name",name); put("phone",phone); put("dob",dob)
            put("gender",gender); put("address",address)
        }
        return writableDatabase.update("Student",cv,"student_id=?",
            arrayOf(id.toString())) > 0
    }

    fun updateStudentAcademic(id:Int,college:String,tenth:Double,twelfth:Double,cgpa:Double): Boolean {
        val cv = ContentValues().apply {
            put("college",college); put("tenth_percent",tenth)
            put("twelfth_percent",twelfth); put("cgpa",cgpa)
            put("profile_complete",1)
        }
        return writableDatabase.update("Student",cv,"student_id=?",
            arrayOf(id.toString())) > 0
    }

    fun emailExists(email:String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM Student WHERE email=?", arrayOf(email))
        return c.moveToFirst().also { c.close() }
    }

    private fun cursorToStudent(c: Cursor) = Student(
        id              = c.getInt(c.getColumnIndexOrThrow("student_id")),
        name            = c.getString(c.getColumnIndexOrThrow("name")),
        email           = c.getString(c.getColumnIndexOrThrow("email")),
        phone           = c.getString(c.getColumnIndexOrThrow("phone")) ?: "",
        dob             = c.getString(c.getColumnIndexOrThrow("dob")) ?: "",
        gender          = c.getString(c.getColumnIndexOrThrow("gender")) ?: "",
        address         = c.getString(c.getColumnIndexOrThrow("address")) ?: "",
        branch          = c.getString(c.getColumnIndexOrThrow("branch")),
        college         = c.getString(c.getColumnIndexOrThrow("college")) ?: "",
        tenthPercent    = c.getDouble(c.getColumnIndexOrThrow("tenth_percent")),
        twelfthPercent  = c.getDouble(c.getColumnIndexOrThrow("twelfth_percent")),
        cgpa            = c.getDouble(c.getColumnIndexOrThrow("cgpa")),
        skills          = c.getString(c.getColumnIndexOrThrow("skills")) ?: "",
        password        = c.getString(c.getColumnIndexOrThrow("password")),
        profileComplete = c.getInt(c.getColumnIndexOrThrow("profile_complete")) == 1,
        createdAt       = c.getString(c.getColumnIndexOrThrow("created_at")) ?: ""
    )



    /** INNER JOIN: all active jobs with company info */
    fun getAllJobsWithCompany(): List<Job> {
        val list = mutableListOf<Job>()
        val c = readableDatabase.rawQuery("""
            SELECT J.*, C.company_name, C.industry, C.location AS c_location
            FROM   Job J
            INNER JOIN Company C ON J.company_id = C.company_id
            WHERE  J.is_active = 1
            ORDER  BY J.package_lpa DESC
        """, null)
        while(c.moveToNext()) list.add(cursorToJob(c))
        c.close(); return list
    }

    /** LEFT JOIN: jobs student hasn't applied to yet and is eligible for */
    fun getEligibleJobs(studentId:Int, cgpa:Double, tenth:Double, twelfth:Double): List<Job> {
        val list = mutableListOf<Job>()
        val c = readableDatabase.rawQuery("""
            SELECT J.*, C.company_name, C.industry, C.location AS c_location
            FROM   Job J
            INNER JOIN Company C ON J.company_id = C.company_id
            LEFT  JOIN Application A
                   ON  A.job_id = J.job_id AND A.student_id = ?
            WHERE  J.is_active = 1
              AND  A.application_id IS NULL
              AND  J.min_cgpa    <= ?
              AND  J.min_tenth   <= ?
              AND  J.min_twelfth <= ?
            ORDER  BY J.package_lpa DESC
        """, arrayOf(studentId.toString(), cgpa.toString(), tenth.toString(), twelfth.toString()))
        while(c.moveToNext()) list.add(cursorToJob(c))
        c.close(); return list
    }

    fun getJobById(jobId:Int): Job? {
        val c = readableDatabase.rawQuery("""
            SELECT J.*, C.company_name, C.industry, C.location AS c_location
            FROM   Job J
            INNER JOIN Company C ON J.company_id = C.company_id
            WHERE  J.job_id = ?
        """, arrayOf(jobId.toString()))
        return if(c.moveToFirst()) cursorToJob(c).also{c.close()} else { c.close(); null }
    }

    private fun cursorToJob(c:Cursor) = Job(
        id          = c.getInt(c.getColumnIndexOrThrow("job_id")),
        title       = c.getString(c.getColumnIndexOrThrow("title")),
        description = c.getString(c.getColumnIndexOrThrow("description")) ?: "",
        packageLpa  = c.getDouble(c.getColumnIndexOrThrow("package_lpa")),
        minCgpa     = c.getDouble(c.getColumnIndexOrThrow("min_cgpa")),
        minTenth    = c.getDouble(c.getColumnIndexOrThrow("min_tenth")),
        minTwelfth  = c.getDouble(c.getColumnIndexOrThrow("min_twelfth")),
        location    = c.getString(c.getColumnIndexOrThrow("location")) ?: "",
        jobType     = c.getString(c.getColumnIndexOrThrow("job_type")) ?: "Full Time",
        deadline    = c.getString(c.getColumnIndexOrThrow("deadline")) ?: "",
        companyId   = c.getInt(c.getColumnIndexOrThrow("company_id")),
        companyName = c.getString(c.getColumnIndexOrThrow("company_name")) ?: "",
        industry    = c.getString(c.getColumnIndexOrThrow("industry")) ?: "",
        isActive    = c.getInt(c.getColumnIndexOrThrow("is_active")) == 1,
        createdAt   = c.getString(c.getColumnIndexOrThrow("created_at")) ?: ""
    )



    fun applyForJob(studentId:Int, jobId:Int): Boolean {
        return try {
            val cv = ContentValues().apply {
                put("student_id",studentId); put("job_id",jobId); put("status","Applied")
            }
            writableDatabase.insertOrThrow("Application",null,cv) != -1L
        } catch(e:Exception) { false }
    }

    fun hasApplied(studentId:Int, jobId:Int): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM Application WHERE student_id=? AND job_id=?",
            arrayOf(studentId.toString(), jobId.toString()))
        return c.moveToFirst().also { c.close() }
    }

    /** 3-table JOIN: student's applications with job + company info */
    fun getStudentApplications(studentId:Int): List<ApplicationDetail> {
        val list = mutableListOf<ApplicationDetail>()
        val c = readableDatabase.rawQuery("""
            SELECT A.application_id, A.status, A.applied_at,
                   J.title, J.package_lpa, J.location, J.job_type,
                   C.company_name, C.industry
            FROM   Application A
            INNER  JOIN Job     J ON A.job_id     = J.job_id
            INNER  JOIN Company C ON J.company_id = C.company_id
            WHERE  A.student_id = ?
            ORDER  BY A.applied_at DESC
        """, arrayOf(studentId.toString()))
        while(c.moveToNext()) {
            list.add(ApplicationDetail(
                applicationId = c.getInt(0),
                status        = c.getString(1),
                appliedAt     = c.getString(2),
                jobTitle      = c.getString(3),
                packageLpa    = c.getDouble(4),
                location      = c.getString(5),
                jobType       = c.getString(6),
                companyName   = c.getString(7),
                industry      = c.getString(8)
            ))
        }
        c.close(); return list
    }


    fun getResume(studentId:Int): Resume? {
        val c = readableDatabase.rawQuery(
            "SELECT * FROM Resume WHERE student_id=?", arrayOf(studentId.toString()))
        if(!c.moveToFirst()) { c.close(); return null }
        val r = Resume(
            id             = c.getInt(c.getColumnIndexOrThrow("resume_id")),
            studentId      = c.getInt(c.getColumnIndexOrThrow("student_id")),
            skills         = c.getString(c.getColumnIndexOrThrow("skills")) ?: "",
            experience     = c.getString(c.getColumnIndexOrThrow("experience")) ?: "",
            projects       = c.getString(c.getColumnIndexOrThrow("projects")) ?: "",
            certifications = c.getString(c.getColumnIndexOrThrow("certifications")) ?: "",
            updatedAt      = c.getString(c.getColumnIndexOrThrow("updated_at")) ?: ""
        )
        c.close(); return r
    }

    fun saveResume(studentId:Int,skills:String,exp:String,proj:String,certs:String): Boolean {
        val cv = ContentValues().apply {
            put("skills",skills); put("experience",exp)
            put("projects",proj); put("certifications",certs)
            put("updated_at","datetime('now','localtime')")
        }
        val rows = writableDatabase.update("Resume",cv,"student_id=?",
            arrayOf(studentId.toString()))
        if(rows == 0) {
            cv.put("student_id",studentId)
            writableDatabase.insert("Resume",null,cv)
        }
        // also update skills on Student table
        writableDatabase.execSQL(
            "UPDATE Student SET skills=? WHERE student_id=?",
            arrayOf(skills, studentId.toString()))
        return true
    }



    fun getTotalStudents()     = scalarInt("SELECT COUNT(*) FROM Student")
    fun getTotalJobs()         = scalarInt("SELECT COUNT(*) FROM Job WHERE is_active=1")
    fun getTotalApplications() = scalarInt("SELECT COUNT(*) FROM Application")
    fun getTotalCompanies()    = scalarInt("SELECT COUNT(*) FROM Company")

    fun getPlacedCount() = scalarInt(
        "SELECT COUNT(DISTINCT student_id) FROM Application WHERE status='Selected'")

    fun getAvgPackage() : Double {
        val c = readableDatabase.rawQuery(
            "SELECT ROUND(AVG(package_lpa),2) FROM Job WHERE is_active=1", null)
        val v = if(c.moveToFirst()) c.getDouble(0) else 0.0
        c.close(); return v
    }

    fun getMaxPackage() : Double {
        val c = readableDatabase.rawQuery(
            "SELECT MAX(package_lpa) FROM Job WHERE is_active=1", null)
        val v = if(c.moveToFirst()) c.getDouble(0) else 0.0
        c.close(); return v
    }


    fun getApplicationStatusBreakdown(): Map<String,Int> {
        val map = LinkedHashMap<String,Int>()
        val c = readableDatabase.rawQuery("""
            SELECT status, COUNT(*) as cnt
            FROM   Application
            GROUP  BY status
            ORDER  BY cnt DESC
        """, null)
        while(c.moveToNext()) map[c.getString(0)] = c.getInt(1)
        c.close(); return map
    }


    fun getCompanyJobStats(): List<Triple<String,Int,Double>> {
        val list = mutableListOf<Triple<String,Int,Double>>()
        val c = readableDatabase.rawQuery("""
            SELECT C.company_name, COUNT(J.job_id) AS job_count,
                   ROUND(AVG(J.package_lpa),2) AS avg_pkg
            FROM   Company C
            INNER  JOIN Job J ON C.company_id = J.company_id
            WHERE  J.is_active = 1
            GROUP  BY C.company_id
            ORDER  BY avg_pkg DESC
            LIMIT  7
        """, null)
        while(c.moveToNext())
            list.add(Triple(c.getString(0), c.getInt(1), c.getDouble(2)))
        c.close(); return list
    }


    fun getAboveAvgCgpaCount() = scalarInt("""
        SELECT COUNT(*) FROM Student
        WHERE  cgpa > (SELECT AVG(cgpa) FROM Student)
    """)


    fun getBranchBreakdown(): Map<String,Int> {
        val map = LinkedHashMap<String,Int>()
        val c = readableDatabase.rawQuery("""
            SELECT branch, COUNT(*) FROM Student GROUP BY branch ORDER BY 2 DESC
        """, null)
        while(c.moveToNext()) map[c.getString(0)] = c.getInt(1)
        c.close(); return map
    }

    private fun scalarInt(sql:String): Int {
        val c = readableDatabase.rawQuery(sql, null)
        val v = if(c.moveToFirst()) c.getInt(0) else 0
        c.close(); return v
    }
}
