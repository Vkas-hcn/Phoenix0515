package com.thunderbolt.methods.bodhisattva.base

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider

abstract class BaseActivity<DB : ViewDataBinding, VM : BaseViewModel> : AppCompatActivity() {

    protected lateinit var binding: DB
    protected lateinit var viewModel: VM

    abstract val layoutId: Int
    abstract val viewModelClass: Class<VM>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, layoutId)
        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(viewModelClass)
        setupViews()
        observeViewModel()
    }

    abstract fun setupViews()

    abstract fun observeViewModel()

    protected fun navigateTo(destination: Class<out AppCompatActivity>) {
        val intent = Intent(this, destination)
        startActivity(intent)
    }

    protected fun navigateTo(destination: Class<out AppCompatActivity>, bundle: Bundle) {
        val intent = Intent(this, destination)
        intent.putExtras(bundle)
        startActivity(intent)
    }

    protected fun showCustomDialog(message: String, title: String = "Info") {
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()
    }
}
