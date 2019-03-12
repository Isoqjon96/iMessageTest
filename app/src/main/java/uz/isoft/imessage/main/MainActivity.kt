package uz.isoft.imessage.main

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.google.android.material.navigation.NavigationView
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import uz.isoft.imessage.*
import uz.isoft.imessage.database.message.MessageViewModel
import uz.isoft.imessage.main.fragment.MainFragment
import uz.isoft.imessage.service.ChatService

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    var mService: ChatService? = null
    private var mBound = false
    private lateinit var model:MessageViewModel

    private val runnable = Runnable {
        Log.i("doniyor test",mService?.getState().toString()?:false.toString())
        if(mBound && ChatService.isOpen){

            model.getNoSendMessage().observe(this, Observer<List<Message>> {
                it.let { u->
                    u.forEach { m->
//                        val s = Message(
//                            date = m.date,
//                            text = m.text.toString(),
//                            from = m.from,
//                            to = m.to,
//                            status = 1
//                        )

                        if(m.status==0 && getInternetState() && mService?.getState()?:false) {
                            mService?.sendMsg(Gson().toJson(m).toString())
                            m.status =1
                            model.updateMessage(m)
                            Log.i("doniyor main",m.toString())
                        }
//                        mRepository.insert(m)
                        Log.i("doniyor main",m.toString())

                    }
                }
            })
        }
    }

    val handler = Handler()

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as ChatService.MyBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mBound = false
            mService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        val toggle = ActionBarDrawerToggle(
            this, dl, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        dl.addDrawerListener(toggle)
        toggle.syncState()

        nv.setNavigationItemSelectedListener(this)
        setData()
        replaceFragment(MainFragment.getInstance(),"main")
        startService(Intent(applicationContext, ChatService::class.java))

        model = ViewModelProviders.of(this).get(MessageViewModel::class.java)
        model.getNoSendMessage().observe(this, Observer<List<Message>> {
            it.let { u->
                u.forEach { m->
                    //                        val s = Message(
//                            date = m.date,
//                            text = m.text.toString(),
//                            from = m.from,
//                            to = m.to,
//                            status = 1
//                        )

                    if(m.status==0 && getInternetState() && mService?.getState()?:false) {
                        mService?.sendMsg(Gson().toJson(m).toString())
                        m.status =1
                        model.updateMessage(m)
                        Log.i("doniyor main",m.toString())
                    }
//                        mRepository.insert(m)
                    Log.i("doniyor main",m.toString())

                }
            }
        })

    }

    override fun onStart() {
        bindService(Intent(this,ChatService::class.java),mConnection,Context.BIND_AUTO_CREATE)
//        handler.postDelayed(runnable,10000)
        super.onStart()
    }

    override fun onStop() {
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
        super.onStop()
    }

    override fun onDestroy() {
//        handler.removeCallbacks(runnable)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_done -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {

           }

        dl.closeDrawer(GravityCompat.START)
        return true
    }
    fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
    override fun onBackPressed() {
        when {
            dl.isDrawerOpen(GravityCompat.START) -> dl.closeDrawer(GravityCompat.START)
            supportFragmentManager.backStackEntryCount > 1 -> supportFragmentManager.popBackStack()
            else -> super.onBackPressed()
        }
    }

    private fun setData() {
        var s = ""
        for (x in 0..12) {
            if (x == 4 || x == 6 || x == 9 || x == 11) s += " "
            s += PManager.getPhone()[x]
        }
        nv.getHeaderView(0).findViewById<AppCompatTextView>(R.id.tvName).text = PManager.getName()
        nv.getHeaderView(0).findViewById<AppCompatTextView>(R.id.tvPhone).text = s

        if (PManager.getImage().isNotEmpty()) {
            Picasso.get()
                .load(SERVER_IMAGE_ADDRESS + PManager.getImage())
                .placeholder(R.drawable.ic_account)
                .centerCrop()
                .fit()
                .into(nv.getHeaderView(0).findViewById<CircleImageView>(R.id.ivNavigationView))
        }
    }


    private fun getInternetState(): Boolean {
        val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
