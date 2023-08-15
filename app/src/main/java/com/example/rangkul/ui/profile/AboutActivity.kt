package com.example.rangkul.ui.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.rangkul.R
import com.example.rangkul.databinding.ActivityAboutBinding
import com.example.rangkul.databinding.ActivityEditProfileBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setToolbar()
    }
    private fun setToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_grey)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}