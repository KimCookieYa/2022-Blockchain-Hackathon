package com.coinplug.metadiumsample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coinplug.metadiumsample.databinding.ItemVcBinding
import com.metadium.vc.VerifiableCredential
import com.nimbusds.jwt.SignedJWT
import java.util.*

class CertificationAdapter(private val dataSet: List<String>) :
    ListAdapter<List<String>, CertificationAdapter.ViewHolder>(VCDataDifferCallback()) {

    private var callback: ItemClickListener? = null

    fun setItemClickListener(callback: ItemClickListener) {
        this.callback = callback
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //val item = getItem(position)
        holder.bind(dataSet[position])

        holder.itemView.setOnClickListener {
            callback?.onClick(dataSet[position])
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class ViewHolder private constructor(
        private val binding: ItemVcBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            Log.d("item is..", "is... $item")
            val vc: SignedJWT = SignedJWT.parse(item)

            val credential = VerifiableCredential(vc)
            val subjects: Map<String, String> = credential.getCredentialSubject()

            binding.progressBar.visibility = View.GONE

            val filteredTitle = credential.types.filterNot {
                it.contains("Verifiable")
            }
            binding.tvTitle.text = filteredTitle.first()

            binding.tvCreateDate.text = "발급일: ${credential.issunaceDate}"

            if (vc.jwtClaimsSet.expirationTime != null && vc.jwtClaimsSet.expirationTime.time < Date().time) {
                println("Verifier: 유효기간 만료")
                binding.tvIsExpired.visibility = View.VISIBLE
                binding.tvIsExpired.text = "유효기간 만료"
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemVcBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class VCDataDifferCallback : DiffUtil.ItemCallback<List<String>>() {
    override fun areItemsTheSame(oldItem: List<String>, newItem: List<String>): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: List<String>, newItem: List<String>): Boolean {
        return oldItem == newItem
    }

}

interface ItemClickListener {
    fun onClick(value: String)
}