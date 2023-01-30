package com.coinplug.metadiumsample.data.local.prefs

import com.metadium.did.MetadiumWallet

interface PreferenceHelper {
    fun saveWallet(wallet: String)
    fun loadWallet(): MetadiumWallet?

    fun saveVCData(jwt: String)
    fun loadVCList(): List<String>
    fun deleteVCData(position: Int)
    fun deleteVCData(jwt: String)
    fun deleteAllVCData()

    fun setDID(did: String)
    fun getDID(): String?

    fun removeAllData()
}