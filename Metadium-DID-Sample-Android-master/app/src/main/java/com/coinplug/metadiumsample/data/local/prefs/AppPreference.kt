package com.coinplug.metadiumsample.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.metadium.did.MetadiumWallet
import org.json.JSONArray
import org.json.JSONException


class AppPreference : PreferenceHelper {
    companion object {
        private const val KEY_MANAGER_PREF: String = "KeyManagerPrefs"
        private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC
        private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

        private const val KEY_DID = "keymanager_did"
        private const val KEY_WALLET = "key_wallet"
        private const val KEY_VC_DATA = "vc_data"

        private lateinit var mPrefs: SharedPreferences

        fun initPreference(context: Context) {
            mPrefs = EncryptedSharedPreferences.create(
                KEY_MANAGER_PREF,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }

    //지갑 저장
    override fun saveWallet(wallet: String) {
        mPrefs.edit { putString(KEY_WALLET, wallet) }
    }

    //지갑 불러오기
    override fun loadWallet(): MetadiumWallet? {
        val walletJson = mPrefs.getString(KEY_WALLET, null)
        return try {
            MetadiumWallet.fromJson(walletJson)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //VC저장
    override fun saveVCData(jwt: String) {
        val values = loadVCList().toMutableList()
        values.add(jwt)

        val array = JSONArray()
        for (i in values.indices) {
            array.put(values[i])
        }
        if (values.isNotEmpty()) {
            mPrefs.edit { putString(KEY_VC_DATA, array.toString()) }
        } else {
            mPrefs.edit { putString(KEY_VC_DATA, null) }
        }
    }

    //VC삭제
    override fun deleteVCData(position: Int) {
        val values = loadVCList().toMutableList()
        values.removeAt(position)

        val a = JSONArray()
        for (i in values.indices) {
            a.put(values[i])
        }
        if (values.isNotEmpty()) {
            mPrefs.edit { putString(KEY_VC_DATA, a.toString()) }
        } else {
            mPrefs.edit { putString(KEY_VC_DATA, null) }
        }
    }

    //VC삭제
    override fun deleteVCData(jwt: String) {
        val values = loadVCList().toMutableList()
        values.removeAt(values.indexOf(jwt))

        val array = JSONArray()
        for (i in values.indices) {
            array.put(values[i])
        }
        if (values.isNotEmpty()) {
            mPrefs.edit { putString(KEY_VC_DATA, array.toString()) }
        } else {
            mPrefs.edit { putString(KEY_VC_DATA, null) }
        }
    }

    //VC목록 불러오기
    override fun loadVCList(): List<String> {
        val vcJson: String? = mPrefs.getString(KEY_VC_DATA, null)
        val vcList = ArrayList<String>()
        if (vcJson != null) {
            try {
                val jsonArray = JSONArray(vcJson)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.optString(i)
                    vcList.add(item)
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        return vcList
    }

    //DID설정
    override fun setDID(did: String) {
        mPrefs.edit { putString(KEY_DID, did) }
    }

    //DID불러오기
    override fun getDID(): String? {
        return mPrefs.getString(KEY_DID, null)
    }

    //전체 VC삭제
    override fun deleteAllVCData() {
        mPrefs.edit { remove(KEY_VC_DATA) }
    }

    //모든 값 삭제. 초기화
    override fun removeAllData() {
        mPrefs.edit { clear() }
    }
}