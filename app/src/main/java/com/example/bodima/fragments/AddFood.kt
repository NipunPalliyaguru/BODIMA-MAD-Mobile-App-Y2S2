package com.example.bodima.fragments

import com.example.bodima.models.Foods
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
//import com.example.bodima.Constants
import android.Manifest
import android.widget.ImageView
import com.example.bodima.R
import com.google.firebase.auth.FirebaseAuth


class AddFood : Fragment() {
    private  lateinit var foodDbRef: DatabaseReference
    private var _binding: View? = null

    private val binding get() = _binding!!
    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA = 101
    var sImage:String?=""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodDbRef = FirebaseDatabase.getInstance().getReference("Foods")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_add_food, container, false)


        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }


        val postbutton = view.findViewById<Button>(R.id.postbtn)
        val imagebutton = view.findViewById<Button>(R.id.imageButton)
        postbutton.setOnClickListener {
            insertFoodData()

        }
        imagebutton.setOnClickListener {
            insert_Image()

        }

        return view
    }

    private fun insert_Image() {
        val options = arrayOf<CharSequence>("Select from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Select Image")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Select from Gallery" -> {
                    val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
                    galleryIntent.type = "image/*"
                    ActivityResultLauncher.launch(galleryIntent)

                }
                options[item] == "Take a Photo" -> {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        ActivityResultLauncher.launch(cameraIntent)


                    } else {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                    }
                }
            }
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun insertFoodData(){
        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        val userEmail = currentUser?.email ?: ""

        val foodName = view?.findViewById<EditText>(R.id.addFood)?.text.toString()
        val foodPrice = view?.findViewById<EditText>(R.id.addPrice)?.text.toString()
        val foodDescription = view?.findViewById<EditText>(R.id.addDesc)?.text.toString()
        val foodAddress = view?.findViewById<EditText>(R.id.addAddress)?.text.toString()
        val foodMobile = view?.findViewById<EditText>(R.id.addMobile)?.text.toString()
        val foodStartTime = view?.findViewById<Spinner>(R.id.startTime)?.selectedItem.toString()
        val foodMeridiumStart = view?.findViewById<Spinner>(R.id.startMeridium)?.selectedItem.toString()
        val foodEndTime = view?.findViewById<Spinner>(R.id.endTime)?.selectedItem.toString()
        val foodMeridiumEnd = view?.findViewById<Spinner>(R.id.endMeridium)?.selectedItem.toString()
        val foodCategory = view?.findViewById<Spinner>(R.id.addCategory)?.selectedItem.toString()
        val foodType = view?.findViewById<Spinner>(R.id.addType)?.selectedItem.toString()

        if (foodName.isNullOrEmpty()) {
            requireView().findViewById<EditText>(R.id.addFood)?.error = "Please enter the food name"
        }

        if (foodPrice.isNullOrEmpty()) {
            requireView().findViewById<EditText>(R.id.addPrice)?.error = "Please enter the food price"
        }

        if (foodMobile.isNullOrEmpty()) {
            requireView().findViewById<EditText>(R.id.addMobile)?.error = "Please enter the mobile number"
        }

        if (foodStartTime.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select start time", Toast.LENGTH_SHORT).show()
        }
        if (foodMeridiumStart.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select start meridium", Toast.LENGTH_SHORT).show()
        }
        if (foodEndTime.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select end time", Toast.LENGTH_SHORT).show()
        }
        if (foodMeridiumEnd.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select end meridium", Toast.LENGTH_SHORT).show()
        }
        if (foodAddress.isNullOrEmpty()) {
            requireView().findViewById<EditText>(R.id.addAddress)?.error = "Please enter the address"
        }

        if (foodCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
        }

        if (foodType.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please select a type", Toast.LENGTH_SHORT).show()
        }


        val foodId = foodDbRef.push().key!!

        val food = Foods(foodId,foodName,foodPrice,foodDescription,foodMobile,foodStartTime,foodMeridiumStart,foodEndTime,foodMeridiumEnd,foodAddress,foodCategory,foodType,sImage,userEmail)
        foodDbRef.child(foodId).setValue(food)
            .addOnCompleteListener{
                Toast.makeText(requireContext(), "Data inserted", Toast.LENGTH_LONG).show()
            }.addOnFailureListener{err->
                Toast.makeText(requireContext(), "Error ${err.message}", Toast.LENGTH_LONG).show()
            }

    }


    private val ActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                if (data.data != null) {
                    val imageUri = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    val bytes = stream.toByteArray()
                    sImage = Base64.encodeToString(bytes, Base64.DEFAULT)

                    val image = view?.findViewById<ImageView>(R.id.imageFood)
                    image?.setImageBitmap(bitmap)
                    // Convert the image to Base64 and save it to your variable here

                    Toast.makeText(requireActivity(), "Image selected from gallery", Toast.LENGTH_LONG).show()
                } else {
                    val thumbnail = data.extras?.get("data") as Bitmap?
                    if (thumbnail != null) {
                        val stream = ByteArrayOutputStream()
                        thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val bytes = stream.toByteArray()
                        sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                        val image = view?.findViewById<ImageView>(R.id.imageFood)
                        image?.setImageBitmap(thumbnail)
                        Toast.makeText(requireActivity(), "Image taken from camera", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(requireActivity(), "Image selection canceled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                ActivityResultLauncher.launch(cameraIntent)
            } else {
                Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}