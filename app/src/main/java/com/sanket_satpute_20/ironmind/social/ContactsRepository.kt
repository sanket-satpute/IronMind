package com.sanket_satpute_20.ironmind.social

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

data class LocalContactCandidate(
    val displayName: String,
    val normalizedPhone: String,
    val phoneHash: String,
    val maskedPhone: String
)

class ContactsRepository {

    fun hasContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun readNormalizedContacts(context: Context): List<LocalContactCandidate> {
        if (!hasContactsPermission(context)) return emptyList()

        val seen = LinkedHashMap<String, LocalContactCandidate>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIndex)?.trim().orEmpty()
                val rawNumber = cursor.getString(numberIndex)?.trim().orEmpty()
                val normalized = PhoneNormalizer.normalize(rawNumber)
                if (name.isBlank() || normalized.isBlank()) continue
                if (seen.containsKey(normalized)) continue

                seen[normalized] = LocalContactCandidate(
                    displayName = name,
                    normalizedPhone = normalized,
                    phoneHash = PhoneHashUtil.sha256(normalized),
                    maskedPhone = maskPhone(normalized)
                )
            }
        }

        return seen.values.toList()
    }

    private fun maskPhone(normalizedPhone: String): String {
        val digits = normalizedPhone.filter { it.isDigit() }
        if (digits.length <= 4) return normalizedPhone
        return "•••• ${digits.takeLast(4)}"
    }
}
