package com.example.rangkul.ui.search

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rangkul.R
import com.example.rangkul.data.model.UserData
import com.example.rangkul.databinding.ActivityFollowListBinding
import com.example.rangkul.databinding.ActivitySearchAccountBinding
import com.example.rangkul.ui.profile.FollowListAdapter
import com.example.rangkul.ui.profile.ProfileViewModel
import com.example.rangkul.ui.profile.VisitedProfileActivity
import com.example.rangkul.utils.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchAccountActivity : AppCompatActivity(), SearchAccountAdapter.FollowListStatusListener {
    private lateinit var binding: ActivitySearchAccountBinding
    private val profileViewModel: ProfileViewModel by viewModels()
    private var userDataList: MutableList<UserData> = arrayListOf()
    private lateinit var targetUserId: String
    private lateinit var targetUserName: String
    private lateinit var followType: String // Following or Followers
    private var currentItemPos = -1
    private val adapter by lazy {
        SearchAccountAdapter (
            onItemClicked = { pos, uid ->
                if (uid == currentUserData().userId) {
                    // navigate to profile fragment
                } else {
                    val intent = Intent(this, VisitedProfileActivity::class.java)
                    intent.putExtra("USER_ID", uid)
                    currentItemPos = pos
                    resultLauncher.launch(intent)
                }
            },
            onFollowClicked = { pos, uid ->
                addFollowData(pos, uid)
            },
            onUnfollowClicked = { pos, uid ->
                removeFollowData(pos, uid)
            },
            followListStatusListener = this
        )
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Update data in adapter if doing add/remove follow in VisitedProfileActivity
            updateDataAtPosition(currentItemPos)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        targetUserId = intent.getStringExtra("USER_ID").toString()
        followType = intent.getStringExtra("FOLLOW_TYPE").toString()
        targetUserName = intent.getStringExtra("USER_NAME").toString()

        setToolbar()

        // Tampilkan keyboard secara otomatis
        binding.etSearchAccount.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        // Configure Post RecyclerView
        binding.rvFollowList.adapter = adapter
        binding.rvFollowList.layoutManager = LinearLayoutManager(this)
        binding.rvFollowList.setHasFixedSize(true)
        binding.rvFollowList.isNestedScrollingEnabled = false


        profileViewModel.getAccountDataList(targetUserId)
        profileViewModel.getUserDataList(targetUserId, followType)
        observeGetAccountDataList()

        binding.etSearchAccount.setOnQueryTextListener(object : OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
//        binding.rvFollowList.visibility = View.GONE
    }

    private fun filterList(query: String?) {
        if (query != null && query.isNotEmpty()) {
            val filteredList = userDataList.filter { userData ->
                userData.userName.contains(query, ignoreCase = true)
            }
            adapter.updateList(filteredList.toMutableList())
//            binding.rvFollowList.visibility = View.VISIBLE
        } else {
            adapter.updateList(userDataList)
//            binding.rvFollowList.visibility = View.GONE
        }
    }

    private fun updateDataAtPosition(pos: Int) {
        adapter.dataUpdated(pos)
    }

    private fun removeFollowData(position: Int, uid: String) {
        binding.progressBar.show()
        // Avoid using observable, to prevent adapter.dataUpdated being performed multiple times
        profileViewModel.removeFollowData(currentUserData().userId, uid) { state ->
            when(state) {
                is UiState.Loading -> {}

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    updateDataAtPosition(position)
                }
            }
        }
    }

    private fun addFollowData(position: Int, uid: String) {
        binding.progressBar.show()
        // Avoid using observable, to prevent adapter.dataUpdated being performed multiple times
        profileViewModel.addFollowData(currentUserData().userId, uid) { state ->
            when(state) {
                is UiState.Loading -> {}

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    updateDataAtPosition(position)
                }
            }
        }
    }

    private fun observeGetAccountDataList() {
        profileViewModel.getAccountDataList.observe(this) {state ->
            when(state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    userDataList = state.data.toMutableList()
//                    adapter.updateCurrentUser(currentUserData().userId)
                    adapter.updateList(userDataList)
                }
            }
        }
    }


    private fun currentUserData(): UserData {
        var user = UserData()
        profileViewModel.getSessionData {
            if (it != null) {
                user = it
            }
        }
        return user
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

    override fun isUserBeingFollowed(item: String, position: Int, callback: (Boolean) -> Unit) {
        profileViewModel.isUserBeingFollowed(currentUserData().userId, item) {
            callback.invoke(it)
        }
    }
}