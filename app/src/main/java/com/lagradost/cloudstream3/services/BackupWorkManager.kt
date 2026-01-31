package com.lagradost.cloudstream3.services

import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.os.Build.VERSION.SDK_INT
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.UIHelper.colorFromAttribute
import com.lagradost.cloudstream3.utils.AppContextUtils.createNotificationChannel
import java.util.concurrent.TimeUnit

const val BACKUP_CHANNEL_ID = "cloudstream3.backups"
const val BACKUP_WORK_NAME = "work_backup"
const val BACKUP_CHANNEL_NAME = "Backups"
const val BACKUP_CHANNEL_DESCRIPTION = "Notifications for background backups"
const val BACKUP_NOTIFICATION_ID = 938712898 // Random unique

class BackupWorkManager(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        fun enqueuePeriodicWork(context: Context?, intervalHours: Long) {
            if (context == null) return
            if (intervalHours == 0L) {
                WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
                return
            }

            // Periodic work
            val periodicSyncDataWork =
                PeriodicWorkRequest.Builder(
                    BackupWorkManager::class.java,
                    intervalHours,
                    TimeUnit.HOURS
                )
                    .addTag(BACKUP_WORK_NAME)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicSyncDataWork
            )

            // Optional: Uncomment untuk testing one-time backup
//            val oneTimeBackupWork =
//                OneTimeWorkRequest.Builder(BackupWorkManager::class.java)
//                    .addTag(BACKUP_WORK_NAME)
//                    .build()
//            WorkManager.getInstance(context).enqueue(oneTimeBackupWork)
        }
    }

    private val backupNotificationBuilder =
        NotificationCompat.Builder(context, BACKUP_CHANNEL_ID)
            .setColorized(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.pref_category_backup))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(context.colorFromAttribute(R.attr.colorPrimary))
            .setSmallIcon(R.mipmap.ic_launcher)

    override suspend fun doWork(): Result {
        // Create notification channel
        context.createNotificationChannel(
            BACKUP_CHANNEL_ID,
            BACKUP_CHANNEL_NAME,
            BACKUP_CHANNEL_DESCRIPTION
        )

        // Foreground service
        val foregroundInfo = if (SDK_INT >= 29)
            ForegroundInfo(
                BACKUP_NOTIFICATION_ID,
                backupNotificationBuilder.build(),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ) else ForegroundInfo(
                BACKUP_NOTIFICATION_ID,
                backupNotificationBuilder.build()
            )

        setForeground(foregroundInfo)

        // Run backup
        BackupUtils.backup(context)
        return Result.success()
    }
}