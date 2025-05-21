package androidx.core.app;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FnnJobIntentService extends JobIntentService {


    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FnnJobIntentService.class, 8848, work);
    }

    public FnnJobIntentService() {
        super();
    }

    @Override
    protected void onHandleWork(Intent intent) {
        Log.e("MyJobIntentService", "FnnJobIntentService: ");
    }

    @Override
    GenericWorkItem dequeueWork() {
        try {
            return super.dequeueWork();
        } catch (Exception e) {
            return null;
        }
    }
}

