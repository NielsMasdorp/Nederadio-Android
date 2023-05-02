package com.nielsmasdorp.nederadio.data.stream

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.TimeUnit

const val DOWNLOAD_TIMEOUT_SECONDS = 30L

/**
 * Content provider that downloads and caches radio logo's to be able to serve them as local content
 */
class RadioLogoContentProvider : ContentProvider() {

    override fun onCreate() = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val context = this.context ?: return null
        val remoteUri = uriMap[uri] ?: throw FileNotFoundException(uri.path)

        var file = File(context.cacheDir, uri.path!!)

        if (!file.exists()) {
            val cacheFile = Glide.with(context)
                .asFile()
                .load(remoteUri)
                .submit()
                .get(DOWNLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)

            cacheFile.renameTo(file)

            file = cacheFile
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?) = 0

    override fun getType(uri: Uri): String? = null

    companion object {

        private val uriMap = mutableMapOf<Uri, Uri>()

        fun mapUri(uri: Uri): Uri {
            val path = uri.encodedPath?.substring(1)?.replace('/', ':') ?: return Uri.EMPTY
            val contentUri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority("com.nielsmasdorp.nederadio")
                .path(path)
                .build()
            uriMap[contentUri] = uri
            return contentUri
        }
    }
}