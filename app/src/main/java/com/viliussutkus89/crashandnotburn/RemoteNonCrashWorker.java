/*
Copyright (c) 2022 ViliusSutkus89.com
https://www.viliussutkus89.com/posts/crash-the-worker-process-but-keep-the-android-app-running-part-2/

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.viliussutkus89.crashandnotburn;

import android.app.Notification;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.WorkerParameters;
import androidx.work.multiprocess.RemoteListenableWorker;

import com.google.common.util.concurrent.ListenableFuture;

public class RemoteNonCrashWorker extends RemoteListenableWorker {
    public RemoteNonCrashWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startRemoteWork() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Log.e("crash", "Starting RemoteNonCrashWorker");
            while (true) {
                try {
                    Thread.sleep(2000);
                    break;
                } catch (InterruptedException ignored) {
                }
            }
            Log.e("crash", "Ending RemoteNonCrashWorker");
            return completer.set(Result.success());
        });
    }

    @NonNull
    @Override
    public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
        return CallbackToFutureAdapter.getFuture(completer -> {
            Context ctx = getApplicationContext();
            String channel_id = ctx.getString(R.string.worker_notification_channel_id);
            String title = ctx.getString(R.string.noncrash_worker_notification_title);

            Notification notification = new NotificationCompat.Builder(ctx, channel_id)
                    .setContentTitle(title)
                    .setTicker(title)
                    // Use whatever icon
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setOngoing(true)
                    .build();

            // Use WorkRequest ID to generate Notification ID.
            // Each Notification ID must be unique to create a new notification for each work request.
            int notification_id = getId().hashCode();
            return completer.set(new ForegroundInfo(notification_id, notification));
        });
    }
}
