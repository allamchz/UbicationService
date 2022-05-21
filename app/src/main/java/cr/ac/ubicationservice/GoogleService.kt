package cr.ac.ubicationservice

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.IBinder

class GoogleService : Service(), LocationListener {

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onLocationChanged(p0: Location) {
        TODO("Not yet implemented")
    }
}