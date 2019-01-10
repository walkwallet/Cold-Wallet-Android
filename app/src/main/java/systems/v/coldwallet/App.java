package systems.v.coldwallet;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;

import systems.v.wallet.basic.wallet.Wallet;

/**
 * Application
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    private static App mInstance;

    // The list of all mActivities.
    private LinkedList<Activity> mActivities = new LinkedList<>();

    // The top activity is resumed to user.
    private WeakReference<Activity> mTopActivity;
    private int mResumed, mPaused, mStarted, mStopped;
    // Time of the last paused.
    private long mLastPausedTime;
    private Wallet mWallet;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        registerActivity();
    }

    public static synchronized App getInstance() {
        return mInstance;
    }

    /**
     * Register the lifecycle callbacks of activity
     */
    private void registerActivity() {
        registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            // I use four separate variables here. You can, of course, just use two and
            // increment/decrement them instead of using four and incrementing them all.

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.v(TAG, "onActivityCreated " + activity.getClass().getSimpleName());
                addActivity(activity);
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.v(TAG, "onActivityDestroyed " + activity.getClass().getSimpleName());
                removeActivity(activity);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.v(TAG, "onActivityResumed " + activity.getClass().getSimpleName());
                resumeActivity(activity);
                ++mResumed;
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.v(TAG, "onActivityPaused " + activity.getClass().getSimpleName());
                ++mPaused;
                mLastPausedTime = System.currentTimeMillis();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.v(TAG, "onActivitySaveInstanceState " + activity.getClass().getSimpleName());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.v(TAG, "onActivityStarted " + activity.getClass().getSimpleName());
                ++mStarted;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.v(TAG, "onActivityStopped " + activity.getClass().getSimpleName());
                ++mStopped;
            }
        });
    }


    /**
     * @return The milliseconds time of activity's last call {@link Activity#onPause()}
     */
    public long getLastPausedTime() {
        return mLastPausedTime;
    }

    /**
     * @return When the application is resumed to user, return true.
     */
    public boolean isAppVisible() {
        return mStarted > mStopped;
    }


    /**
     * Add an activity to the record list.
     */
    public void addActivity(Activity activity) {
        mActivities.add(activity);
    }


    /**
     * @return The record list.
     */
    public LinkedList<Activity> getActivities() {
        return mActivities;
    }


    /**
     * Remove an activity from the record list.
     */
    public void removeActivity(Activity activity) {
        mActivities.remove(activity);
    }


    /**
     * Record the activity use {@link #mTopActivity}.
     * Must call this method in activity's {@link Activity#onResume()}
     */
    public void resumeActivity(Activity activity) {
        mTopActivity = new WeakReference<>(activity);
    }


    /**
     * @return The top activity, maybe null.
     */
    public Activity getTopActivity() {
        return mTopActivity.get();
    }


    /**
     * @param cls The class must extends {@link Activity} or subclass of it.
     * @return If the activity is exist in {@link #mActivities}, return true.
     */
    public boolean isExist(Class<?> cls) {
        for (Activity activity : mActivities) {
            if (activity.getClass().getSimpleName().equals(cls.getSimpleName())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Finish mActivities in an appointed list.
     *
     * @param activities Activities to be finished.
     */
    public void finishActivity(Activity... activities) {
        for (Activity activity : activities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }


    /**
     * Finish all mActivities except an appointed list.
     *
     * @param except The exceptional list.
     */
    public void finishAllActivities(Class<?>... except) {
        for (Activity activity : mActivities) {
            if (activity != null) {
                for (Class<?> c : except) {
                    if (!activity.getClass().getName().equals(c.getName())) {
                        activity.finish();
                    }
                }
            }
        }
    }


    /**
     * Finish all mActivities.
     */
    public void finishAllActivities() {
        for (Activity activity : mActivities) {
            if (activity != null) {
                activity.finish();
            }
        }
    }


    /**
     * Recreate all mActivities.
     */
    public void recreateActivities() {
        for (Activity activity : mActivities) {
            if (activity != null) {
                activity.recreate();
            }
        }
    }

    /**
     * Stop a service.
     */
    public void stopService(Class<?> cls) {
        stopService(new Intent(this, cls));
    }

    /**
     * Start a service.
     */
    public void startService(Class<?> cls) {
        startService(new Intent(this, cls));
    }

    /**
     * Exit application.
     */
    public void exit() {
        finishAllActivities();
        System.exit(0);
    }

    public Wallet getWallet() {
        return mWallet;
    }

    public void setWallet(Wallet mWallet) {
        this.mWallet = mWallet;
    }
}
