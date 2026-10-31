package com.example.project2.dal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.project2.entities.*

@Database(
    entities = [Account::class, Student::class, Subject::class, StudySchedule::class],
    version = 1,
    exportSchema = false
)
abstract class AppDB : RoomDatabase() {
    abstract fun dao(): DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDB? = null

        fun getInstance(context: Context): AppDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "student_schedule_db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT INTO Account (username, password, role) VALUES ('admin', 'admin', 'ADMIN');")
                            db.execSQL("INSERT INTO Account (username, password, role) VALUES ('SV01', '123', 'STUDENT');")

                            db.execSQL("INSERT INTO Student (studentCode, fullname, email, phone, accountId) VALUES ('SV01', 'Nguyễn Văn A', 'sva@gmail.com', '0901234567', (SELECT id FROM Account WHERE username = 'SV01'));")

                            db.execSQL("INSERT INTO Subject (subjectCode, subjectName, description, credits) VALUES ('INT1340', 'Lập trình Android', 'Môn học về lập trình ứng dụng di động Android', 3);")
                            db.execSQL("INSERT INTO Subject (subjectCode, subjectName, description, credits) VALUES ('INT1313', 'Cơ sở dữ liệu', 'Môn học về hệ quản trị cơ sở dữ liệu', 3);")
                            db.execSQL("INSERT INTO Subject (subjectCode, subjectName, description, credits) VALUES ('INT1332', 'Lập trình hướng đối tượng', 'Môn học về lập trình OOP với Java/Kotlin', 3);")
                            db.execSQL("INSERT INTO Subject (subjectCode, subjectName, description, credits) VALUES ('BAS1203', 'Giải tích 1', 'Môn học đại cương toán giải tích', 3);")

                            db.execSQL("INSERT INTO StudySchedule (studentId, subjectId, studyDate, startTime, endTime, location, status) VALUES ((SELECT id FROM Student WHERE studentCode = 'SV01'), (SELECT id FROM Subject WHERE subjectCode = 'INT1340'), date('now','localtime'), '19:00', '21:00', 'Phòng 301-A2', 'PENDING');")
                            db.execSQL("INSERT INTO StudySchedule (studentId, subjectId, studyDate, startTime, endTime, location, status) VALUES ((SELECT id FROM Student WHERE studentCode = 'SV01'), (SELECT id FROM Subject WHERE subjectCode = 'INT1313'), date('now','localtime','+1 day'), '08:00', '10:00', 'Phòng 102-A3', 'CONFIRMED');")
                            db.execSQL("INSERT INTO StudySchedule (studentId, subjectId, studyDate, startTime, endTime, location, status) VALUES ((SELECT id FROM Student WHERE studentCode = 'SV01'), (SELECT id FROM Subject WHERE subjectCode = 'INT1332'), date('now','localtime','+1 day'), '14:00', '16:00', 'Phòng 205-A2', 'PENDING');")
                            db.execSQL("INSERT INTO StudySchedule (studentId, subjectId, studyDate, startTime, endTime, location, status) VALUES ((SELECT id FROM Student WHERE studentCode = 'SV01'), (SELECT id FROM Subject WHERE subjectCode = 'BAS1203'), date('now','localtime','-3 day'), '09:00', '11:00', 'Phòng 401-A1', 'CANCELLED');")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
