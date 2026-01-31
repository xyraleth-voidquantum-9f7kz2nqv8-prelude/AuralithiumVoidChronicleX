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
import com.lagradost.cloudstream3.utils.SubscriptionUtils
import com.lagradost.cloudstream3.utils.UIHelper.colorFromAttribute
import java.util.concurrent.TimeUnit

const val SUBSCRIPTION_CHANNEL_ID = "cloudstream3.subscription"
const val SUBSCRIPTION_WORK_NAME = "work_subscription"
const val SUBSCRIPTION_CHANNEL_NAME = "Subscription"
const val SUBSCRIPTION_CHANNEL_DESCRIPTION = "Notifications for background subscription checks"
const val SUBSCRIPTION_NOTIFICATION_ID = 938712899 // Random unique

class SubscriptionWorkManager(val context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    companion object {
        fun enqueuePeriodicWork(context: Context?, intervalHours: Long) {
            if (context == null) return

            if (intervalHours == 0L) {
                WorkManager.getInstance(context).cancelUniqueWork(SUBSCRIPTION_WORK_NAME)
                return
            }

            val periodicSyncDataWork =
                PeriodicWorkRequest.Builder(
                    SubscriptionWorkManager::class.java,
                    intervalHours,
                    TimeUnit.HOURS
                )
                    .addTag(SUBSCRIPTION_WORK_NAME)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SUBSCRIPTION_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicSyncDataWork
            )
        }
    }

    private val subscriptionNotificationBuilder =
        NotificationCompat.Builder(context, SUBSCRIPTION_CHANNEL_ID)
            .setColorized(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setAutoCancel(true)
            .setContentTitle(context.getString(R.string.pref_category_subscription)) // title dari string
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setColor(context.colorFromAttribute(R.attr.colorPrimary))
            .setSmallIcon(R.mipmap.ic_launcher) // ganti icon

    override suspend fun doWork(): Result {
        SubscriptionUtils.createNotificationChannel(
            context,
            SUBSCRIPTION_CHANNEL_ID,
            SUBSCRIPTION_CHANNEL_NAME,
            SUBSCRIPTION_CHANNEL_DESCRIPTION
        )

        val foregroundInfo = if (SDK_INT >= 29)
            ForegroundInfo(
                SUBSCRIPTION_NOTIFICATION_ID,
                subscriptionNotificationBuilder.build(),
                FOREGROUND_SERVICE_TYPE_DATA_SYNC
            ) else ForegroundInfo(
                SUBSCRIPTION_NOTIFICATION_ID,
                subscriptionNotificationBuilder.build()
            )

        setForeground(foregroundInfo)

        SubscriptionUtils.checkSubscription(context)

        return Result.success()
    }
    }
