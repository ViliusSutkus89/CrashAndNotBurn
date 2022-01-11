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

import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_CLASS_NAME;
import static androidx.work.multiprocess.RemoteListenableWorker.ARGUMENT_PACKAGE_NAME;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.viliussutkus89.crashandnotburn.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    private WorkManager m_workManager;
    private String m_packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        m_workManager = WorkManager.getInstance(this);
        m_packageName = getPackageName();

        findViewById(R.id.progress_bar).setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        findViewById(R.id.button_crash).setOnClickListener(view -> {
            WorkRequest remoteCrashWorkRequest = new OneTimeWorkRequest.Builder(RemoteCrashWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                            new Data.Builder()
                                    .putString(ARGUMENT_PACKAGE_NAME, m_packageName)
                                    .putString(ARGUMENT_CLASS_NAME, CrashWorkerService.class.getName())
                                    .build())
                    .build();
            m_workManager.enqueue(remoteCrashWorkRequest);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            m_workManager.getWorkInfoByIdLiveData(remoteCrashWorkRequest.getId()).observe(this, m_updateObserver);
        });

        findViewById(R.id.button_notcrash).setOnClickListener(view -> {
            WorkRequest remoteNonCrashWorkRequest = new OneTimeWorkRequest.Builder(RemoteNonCrashWorker.class)
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setInputData(
                            new Data.Builder()
                                    .putString(ARGUMENT_PACKAGE_NAME, m_packageName)
                                    .putString(ARGUMENT_CLASS_NAME, CrashWorkerService.class.getName())
                                    .build()
                    )
                    .build();
            m_workManager.enqueue(remoteNonCrashWorkRequest);
            findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
            m_workManager.getWorkInfoByIdLiveData(remoteNonCrashWorkRequest.getId()).observe(this, m_updateObserver);
        });
    }

    private final Observer<WorkInfo> m_updateObserver = workInfo -> {
        if (null == workInfo) {
            return;
        }

        switch (workInfo.getState()) {
            case FAILED:
                Toast.makeText(this, "Work Failed!", Toast.LENGTH_SHORT).show();
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                break;
            case SUCCEEDED:
                Toast.makeText(this, "Work OK", Toast.LENGTH_SHORT).show();
                findViewById(R.id.progress_bar).setVisibility(View.GONE);
                break;
        }
    };

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        String id = getString(R.string.worker_notification_channel_id);
        CharSequence name = getString(R.string.worker_notification_channel_name);
        String description = getString(R.string.worker_notification_channel_description);
        NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
}
