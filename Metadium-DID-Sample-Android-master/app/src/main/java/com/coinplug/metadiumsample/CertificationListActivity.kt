package com.coinplug.metadiumsample

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.coinplug.metadiumsample.data.local.prefs.AppPreference
import com.coinplug.metadiumsample.databinding.ActivityCertificationListBinding

class CertificationListActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCertificationListBinding

    private lateinit var adapter: CertificationAdapter

    private lateinit var data: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCertificationListBinding.inflate(layoutInflater)
        val view = viewBinding.root
        setContentView(view)

        supportActionBar?.title = "발급한 인증서 목록"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        data = AppPreference().loadVCList()
        adapter = CertificationAdapter(data)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        viewBinding.recyvlerView.adapter = adapter
        viewBinding.recyvlerView.layoutManager = layoutManager
        viewBinding.recyvlerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        adapter.setItemClickListener(object : ItemClickListener {
            override fun onClick(value: String) {
                Log.e("ClickListener", "data: $value")
                openJwtBrowser(value)
            }

        })
    }

    private fun openJwtBrowser(jwt: String) {
        val url = "https://jwt.io/#debugger-io?token=$jwt"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}