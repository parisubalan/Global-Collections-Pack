package com.dev.pari.gcp.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dev.pari.gcp.common.Utils
import com.dev.pari.gcp.databinding.ActivityFileUploadBinding
import com.dev.pari.gcp.service_utils.DeviceFileUtils
import java.io.File

class FileUploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileUploadBinding
    private lateinit var fileUtils: DeviceFileUtils
    private lateinit var utils: Utils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialization()
    }

    private fun initialization() {
        utils = Utils(this)
        fileUtils = DeviceFileUtils(this)
        binding.uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "application/pdf"
            resultLauncher.launch(intent)
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it?.data?.data
                val file = File(uri!!.path!!)
                val originalPath = fileUtils.getPath(uri)
                val originalFile = File(originalPath!!)
                binding.uploadBtn.text = originalFile.name
                utils.shortToast(originalFile.name + "  files was uploaded successfully")
                println("--->>> Path $uri")
                println("--->>> File Path ${file.path}")
                println("--->>> File Name ${file.name}")
                println("--->>> Original File Name ${originalPath}")
            }
        }
}