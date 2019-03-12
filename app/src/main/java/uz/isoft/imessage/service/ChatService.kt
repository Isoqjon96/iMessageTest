package uz.isoft.imessage.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import org.java_websocket.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import uz.isoft.imessage.Message
import uz.isoft.imessage.PManager
import uz.isoft.imessage.R
import uz.isoft.imessage.database.message.MessageRepository
import uz.isoft.imessage.main.fragment.MainFragment
import uz.isoft.imessage.start.SplashActivity
import java.net.URI
import java.net.URISyntaxException

class ChatService : Service() {
    companion object {
        var isOpen = false
    }

    private var webSocket: WebSocketClient? = null

    private var repository: MessageRepository? = null
    private var gson = Gson()

    override fun onCreate() {
        if (getInternetState()) {
            connectWebSocket()
        }
        repository = MessageRepository(application)
        Toast.makeText(applicationContext, "onstart", Toast.LENGTH_SHORT).show()
        super.onCreate()
    }

    private fun connectWebSocket() {
        Toast.makeText(applicationContext, "Service", Toast.LENGTH_SHORT).show()
        val uri: URI
        try {
            uri = URI("http://46.8.18.241:8080/ws")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
            return
        }
        val headers: Map<String, String> = mapOf("uid" to PManager.getUID())

        webSocket = object : WebSocketClient(uri, headers) {

            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.i("doniyor", "onOpen")
//                ChatService.isOpen = true
            }

            override fun connect() {
                ChatService.isOpen = true
                Log.i("doniyor", "Connect")
                super.connect()
            }

            override fun getConnection(): WebSocket {
//                ChatService.isOpen = true
                Log.i("doniyor", "getConnect")

                return super.getConnection()
            }

            override fun closeConnection(code: Int, message: String?) {
                ChatService.isOpen = false
                super.closeConnection(code, message)
            }


            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.i("doniyor", "onclose")
                Toast.makeText(applicationContext, "onclose", Toast.LENGTH_SHORT).show()
                ChatService.isOpen = false
            }

            override fun onMessage(message: String?) {
                Log.i("doniyor", message)
                val temp = gson.fromJson<Message>(message, Message::class.java)
                temp.status = 1
//                if(MainFragment.adapter.getData().forEach {
//                        if (it.uid != temp.to){
//
//                        }
//                    })
                repository?.insert(temp)

                val pendingIntent = PendingIntent.getActivity(
                    applicationContext, 0, Intent(applicationContext, SplashActivity::class.java)
                    , PendingIntent.FLAG_ONE_SHOT
                )

                val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

                val notificationBuilder = NotificationCompat.Builder(applicationContext)
                    .setContentTitle("${temp.from} dan xabar")
                    .setContentText(temp.text)
                    .setPriority(Notification.PRIORITY_MAX)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setSound(defaultSoundUri)
                    .setContentIntent(pendingIntent)
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, notificationBuilder.build())
            }

            override fun onError(ex: Exception?) {

                Log.i("doniyor", "onerror")
            }
        }

        webSocket?.connect()
    }

    fun getInternetState(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
    fun getState():Boolean{
        return webSocket?.connection?.isOpen ?: false
    }
    fun sendMsg(s: String) {

        if (ChatService.isOpen ?: false && webSocket != null) {


            webSocket?.send(s)
            Log.i("doniyor send", s)
        }
    }

    inner class MyBinder : Binder() {

        val service: ChatService
            get() = this@ChatService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return MyBinder()
    }


    override fun onDestroy() {
        Toast.makeText(applicationContext, "onclose service", Toast.LENGTH_SHORT).show()
        super.onDestroy()
    }

}