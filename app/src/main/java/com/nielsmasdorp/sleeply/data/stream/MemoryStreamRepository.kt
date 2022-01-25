package com.nielsmasdorp.sleeply.data.stream

import android.app.Application
import com.nielsmasdorp.sleeply.R
import com.nielsmasdorp.sleeply.domain.stream.Stream
import com.nielsmasdorp.sleeply.domain.stream.StreamRepository

/**
 * @author Niels Masdorp (NielsMasdorp)
 */
class MemoryStreamRepository(application: Application) : StreamRepository {

    private val streams: MutableList<Stream> = mutableListOf(
        Stream(
            "0",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.rainy_stream_title
            ),
            application.getString(R.string.rainy_stream_desc),
            R.drawable.rain_background,
            R.drawable.rain_background_small
        ),
        Stream(
            "1",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.ocean_stream_title
            ),
            application.getString(R.string.ocean_stream_desc),
            R.drawable.ocean_background,
            R.drawable.ocean_background_small
        ),
        Stream(
            "2",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.forest_stream_title
            ),
            application.getString(R.string.forest_stream_desc),
            R.drawable.nature_background,
            R.drawable.nature_background_small
        ),
        Stream(
            "3",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.meditation_stream_title
            ),
            application.getString(R.string.meditation_stream_desc),
            R.drawable.meditation_background,
            R.drawable.meditation_background_small
        ),
        Stream(
            "4",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.delta_waves_stream_title
            ),
            application.getString(R.string.delta_waves_stream_desc),
            R.drawable.delta_waves_background,
            R.drawable.delta_waves_background_small
        ),
        Stream(
            "5",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.lucid_stream_title
            ),
            application.getString(R.string.lucid_stream_desc),
            R.drawable.lucid_background,
            R.drawable.lucid_background_small
        ),
        Stream(
            "6",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.autumn_stream_title
            ),
            application.getString(R.string.autumn_stream_desc),
            R.drawable.autumn_background,
            R.drawable.autumn_background_small
        ),
        Stream(
            "7",
            "https://25343.live.streamtheworld.com/RADIO538.mp3",
            application.getString(
                R.string.void_stream_title
            ),
            application.getString(R.string.void_stream_desc),
            R.drawable.void_background,
            R.drawable.void_background_small
        )
    )

    override suspend fun getStreamById(id: String): Stream {
        return streams.firstOrNull { it.id == id } ?: streams[0]
    }

    override suspend fun getStreams(): List<Stream> = streams
}