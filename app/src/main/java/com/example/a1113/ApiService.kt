package com.example.a1113

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("api/notes")
    suspend fun saveNote(@Body note: Note): Response<Note>

    @GET("api/notes/{date}/{userId}")
    suspend fun getNote(
        @Path("date") date: String,
        @Path("userId") userId: String
    ): Response<Note>
}