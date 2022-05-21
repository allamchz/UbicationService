package cr.ac.ubicationservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import cr.ac.ubicationservice.databinding.ActivityMapsBinding
import cr.ac.ubicationservice.db.LocationDatabase
import cr.ac.ubicationservice.entity.Location


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val SOLICITAR_GPS = 1 // para saber qué permisos se están solicitando
    var mLocationRequest // para configurar la frecuencia de actualización del gps
            : LocationRequest? = null
    private var mLocationCallback // para indicar qué hace la app con cada actualización del gps
            : LocationCallback? = null

    private var mFusedLocationClient //proveedor de los servicios de localización de google
            : FusedLocationProviderClient? = null
    private lateinit var locationDatabase: LocationDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getLocation();

        mLocationCallback = object : LocationCallback() {
            /**
             * Determina qué hace cada vez que se reciben un conjunto de posiciones gps
             * en nuestro caso las utilizará para mostrarlas en el mapa
             * @param locationResult array con todas las localizaciones
             */
            override fun onLocationResult(locationResult: LocationResult) {
                if (mMap != null) { // siempre y cuando el mapa esté cargado
                    if (locationResult == null) { // siempre que existan valores
                        return
                    }
                    // por cada localización la dibujamos en en mapa
                    for (location in locationResult.locations) {

                        val sydney = LatLng(location.latitude, location.longitude)
                        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

                        locationDatabase.locationDAO.insert(Location(null, location.latitude, location.longitude))

                    }
                }
            }
        }

        // inicializa el proveedor de los servicios del mapa
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this@MapsActivity)

        // el mLocationRequest determina la frecuencia y prioridad de actualización del gps
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = 1000
        mLocationRequest?.fastestInterval = 500
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY



        // se carga el request
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationDatabase = LocationDatabase.getInstance(this)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        for(location in locationDatabase.locationDAO.query()){
            val sydney = LatLng(location.latitude, location.longitude)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        }

    }


    /**
     * se verifica que el usuario haya autorizado el acceso al gps
     * en caso contrario solicita autorización
     */
    fun getLocation() {
        // ¿No tengo los permisos de gps?
        if (ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MapsActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //No tengo permisos, voy a solicitarlos al usuario
            // cuando llamo a este método se llama a @onRequestPermissionsResult
            ActivityCompat.requestPermissions(
                this@MapsActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                SOLICITAR_GPS
            )
        } else { // Ya tengo el permiso otorgado, inicia la actualización de los puntos gps
            mFusedLocationClient?.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, null
            )
        }
    }
    @SuppressLint("MissingPermission", "MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            SOLICITAR_GPS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()
                    && grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    //Ya tengo el permiso otorgado, inicia la actualización de los puntos gp
                    mFusedLocationClient?.requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        null
                    )
                } else { // el usuario no le dió la gana de dar permisos, va para afuera !!!
                    System.exit(1)
                }
                return
            }

        }
    }
}