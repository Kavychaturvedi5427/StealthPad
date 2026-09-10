package com.kavya.stealthpad.data.api;

import com.kavya.stealthpad.data.DataModel.NoteRequestDTO;
import com.kavya.stealthpad.data.DataModel.NoteResponseDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface NotesApi {

    @GET("api/notes")
    Call<List<NoteResponseDTO>> getAllNotes();

    @POST("api/notes")
    Call<NoteResponseDTO> createNote(@Body NoteRequestDTO noteRequestDTO);

    @PUT("api/notes/{id}")
    Call<NoteResponseDTO> updateNote(@Path("id") Long id, @Body NoteRequestDTO noteRequestDTO);

    @DELETE("api/notes")
    Call<Void> deleteAll();

    @DELETE("api/notes/{id}")
    Call<Void> deleteById(@Path("id") Long id);



}
